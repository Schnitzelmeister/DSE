package at.ac.univie.dse2016.stream.broker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import at.ac.univie.dse2016.stream.common.*;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import javax.xml.ws.Endpoint;

public class BrokerServer implements BrokerAdmin, BrokerClient {
	
	int tmpSentMode = 0;	//0 - RANDOM, 1 - SOAP, 2 - REST, 3 - RMI

	
	public BrokerServer(int brokerId, String remoteHostBoerse, int remotePortUDPBoerse, int remotePortRMIBoerse, int localPortRMIBroker,
				String remoteSOAPHost, String remoteRESTHost, String localSOAPHostBroker, String localRESTHostBroker, String path) {
		this.brokerId = brokerId;
		this.remoteHostBoerse = remoteHostBoerse;
		this.remotePortUDPBoerse = remotePortUDPBoerse;
		this.remotePortRMIBoerse = remotePortRMIBoerse;
		this.localPortRMIBroker = localPortRMIBroker;
		this.localSOAPHostBroker = localSOAPHostBroker;
		this.localRESTHostBroker = localRESTHostBroker;
		this.remoteSOAPHost = remoteSOAPHost;
		this.remoteRESTHost = remoteRESTHost;
		
		//init DAO
		this.poolDAO = new PoolDAO( path );
		System.out.println("Path to DATA Folder = "+ path);
		System.out.println("Die Daten werden in diesem Folder gespeichert");

		emittents = new java.util.TreeMap<String, Emittent>();
		auftraege = new java.util.TreeMap< Integer /* auftragId */, Auftrag >();
		this.execUDP = null;
		
        try {
        	
        	//get RMI-Boerse object
            Registry registryBoerse = LocateRegistry.getRegistry(this.remoteHostBoerse, this.remotePortRMIBoerse);
            this.boerse = (BoerseClient) registryBoerse.lookup("brokerBoerse");

            //get aktual Emittents
            for (Emittent emittent : this.boerse.getEmittentsList())
            	emittents.put(emittent.getTicker(), emittent);
            
            //get port from Boerse Settings
    		if (localPortRMIBroker == -1)
    		{
                String host = this.boerse.getBrokerNetworkAddress(brokerId, NetworkResource.RMI);
                String[] ar = host.split(":");
                this.localPortRMIBroker = Integer.valueOf(ar[1]);
    		}

            //get SOAPHostBroker from Boerse Settings
    		if (localSOAPHostBroker == null)
    			this.localSOAPHostBroker = this.boerse.getBrokerNetworkAddress(brokerId, NetworkResource.SOAP);

            //get RESTHostBroker from Boerse Settings
    		if (localRESTHostBroker == null)
    			this.localRESTHostBroker = this.boerse.getBrokerNetworkAddress(brokerId, NetworkResource.REST);

    		
            //Init SOAP
            QName serviceName = new QName("http://boerse.com/", "BoersePublic");
            QName portName = new QName("http://boerse.com/", "WebServices/public");

            Service service = Service.create(serviceName);
            service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, this.remoteSOAPHost); 
            this.clientSOAPBoerse = service.getPort(portName, BoerseClient.class);
            
            //Init REST
            this.clientRESTBoerse = org.apache.cxf.jaxrs.client.WebClient.create(this.remoteRESTHost,  java.util.Collections.singletonList(new com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider() ));
            
            
            //publish Broker objects RMI
            Registry registry = LocateRegistry.createRegistry(this.localPortRMIBroker);
            BrokerAdminAdapter adminAdapter = new BrokerAdminAdapter(this);
            BrokerAdmin adminStub =
                    (BrokerAdmin) UnicastRemoteObject.exportObject(adminAdapter, 0);
                registry.rebind("adminBroker", adminStub);
            
            BrokerClient clientStub =
                    (BrokerClient) UnicastRemoteObject.exportObject(this, 0);
            registry.rebind("client", clientStub);
        
            //publish SOAP
            System.out.println("Try to publish SOAP " +  this.localSOAPHostBroker);
            Endpoint endpoint = Endpoint.publish(this.localSOAPHostBroker, new BrokerAdminAdapter(this));

            boolean status = endpoint.isPublished();
            System.out.println("Web service status = " + status);
            
            //publish REST
			org.apache.cxf.jaxrs.JAXRSServerFactoryBean sf = new org.apache.cxf.jaxrs.JAXRSServerFactoryBean();
			sf.setResourceClasses(BrokerPublicRESTful.class);
			sf.setResourceProvider(BrokerPublicRESTful.class,
					new org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider( new BrokerPublicRESTful(this) ) );
			sf.setAddress(this.localRESTHostBroker);
			org.apache.cxf.endpoint.Server server = sf.create();
            
			
			
            // destroy the server
            // uncomment when you want to close/destroy it
            // server.destroy();
			
	        System.out.println("Der Broker id=" + brokerId + " ist gestartet, RMI port = "+ this.localPortRMIBroker);
            
        } catch (Exception e) {
            System.err.println("BrokerServer exception:");
            e.printStackTrace();
        }
	}

	/**
	 * Broker Id
	 */
	private Integer brokerId;
	private Integer sessionId = -1;
	
	
	/**
	 * DAO Objects
	 */
	private PoolDAO poolDAO;

	/**
	 * Connection Data
	 */
	private String localSOAPHostBroker;
	private String localRESTHostBroker;
	private String remoteHostBoerse;
	private Integer remotePortUDPBoerse;
	private Integer remotePortRMIBoerse;
	private Integer localPortRMIBroker;
	private String remoteSOAPHost;
	private String remoteRESTHost;
	private BoerseClient boerse;
	private BoerseClient clientSOAPBoerse;
	private org.apache.cxf.jaxrs.client.WebClient clientRESTBoerse;
	
	
	/**
	 * Emittents, die auf der Boerse gekauft/verkauft werden duerfen
	 */
	private java.util.TreeMap<String, Emittent> emittents;

	/**
	 * Aktuelle Auftraege des Brokers
	 */
	private java.util.TreeMap< Integer /* auftragId */, Auftrag > auftraege;

	
	/**
	 * AddNew Client, Admin's Function
	 */
	public Integer clientAddNew(Client client) throws RemoteException, IllegalArgumentException {
		synchronized(this.poolDAO.getClientDAO()) {
			this.poolDAO.getClientDAO().speichereItem(client);
		}
		return client.getId();
	}

	/**
	 * Edit Client, Admin's Function
	 */
	public void clientEdit(Client client) throws RemoteException, IllegalArgumentException {
		synchronized(this.poolDAO.getClientDAO()) {
			Client _dest = this.poolDAO.getClientDAO().getItemById(client.getId());
			_dest.setName(client.getName());
			this.poolDAO.getClientDAO().speichereItem(_dest);
		}
	}
	
	/**
	 * Remove Client, Admin's Function
	 */
	public void clientLock(Client client) throws RemoteException, IllegalArgumentException {
		synchronized(this.poolDAO.getClientDAO()) {
			this.poolDAO.getClientDAO().loescheItem(client);
		}
	}
	
	/**
	 * Get all Client, Admin's Function
	 */
	public java.util.ArrayList<Client> getClientsList() throws RemoteException {
		return new java.util.ArrayList<Client>(this.poolDAO.getClientDAO().getItems().values());
	}

	
	/**
	 * einfachheitshalber erstellen wir einen Kredit mit dem ersten Geldgeber, 5% proJahr, x10
	 * BoersenMakler's Admin's Function
	 */
	/*
	public void SetCredit(Client client) {

		if (geldgeber.size() == 0)
			throw new IllegalArgumentException("Geldgeber do not exist");

		if (!clients.containsKey(client.getId()))
			throw new IllegalArgumentException("Client " + String.valueOf(client.getId()) + " does not exist");

		Client actual_client = clients.get(client);
		if (actual_client.credit != null)
			throw new IllegalArgumentException("Client " + String.valueOf(actual_client.getId()) + " hat schon einen Kredit");

		//Nehemen wir den ersten Geldgeber und erstellen einen Vertrag
		Credit kredit = new Credit(actual_client.getId(), this.getId(), 1, 0.05f, 10f, 0f);
		krediten.add(kredit);
		clients.put(actual_client.getId(), actual_client);
	}*/
	
	/**
	 * wenn die Boerse abschliesst
	 * BoersenMakler's Admin's Function oder ScheduleJob
	 */
	/*public void Close() {
		
		// single thread
		for(Credit kredit : krediten)
		{
			if (kredit.getUsed() != 0) {
				Client client = this.clients.get(kredit.getClientId());
				
				// Zinsen
				float zinsen = kredit.getUsed() / 360 * kredit.getRate();
				kredit.setUsed(kredit.getUsed() + zinsen);
				
				// Wenn das Geld auf dem Tradingkonto zur Verfuegung steht 
				if (client.kontostand != 0) {
					if (client.kontostand > kredit.getUsed()) {
						//ueberweisen den ganzen Credit zum Geldgeber
						client.Auszahlen(kredit.getUsed());
						kredit.setUsed(0);
					}
					else {
						//ueberweisen den Teil des Kredits zum Geldgeber
						client.Auszahlen(client.kontostand);
						kredit.setUsed(kredit.getUsed() - client.kontostand);
					}
				}
			}
		}
		
	}*/
	
	/**
	 * Auftrag eines Clients stellen, ohne Sicherheitspruefung
	 */
	public Integer auftragAddNew(Integer clientId, Auftrag auftrag) throws RemoteException, IllegalArgumentException {
		if ( !this.poolDAO.getClientDAO().containsKey(clientId) )
			throw new IllegalArgumentException("Client with id=" + clientId + " does not exist");

		if ( !emittents.containsKey(auftrag.getTicker()) )
			throw new IllegalArgumentException("Ticker " + auftrag.getTicker() + " does not exist");

		int tickerId = emittents.get( auftrag.getTicker() ).getId();
		boolean buy = auftrag.getKaufen();
		int anzahl = auftrag.getAnzahl();
		float bedingung = auftrag.getBedingung();
		
		Client client = this.poolDAO.getClientDAO().getItemById(clientId);
		//lock Client
		synchronized(client) {
			java.util.TreeMap<Integer, Integer> clientEmittents = client.getDisponibleAccountEmittents();

			if (buy) {
				
				//mit Bedingung
				if (bedingung > 0) {
					if (client.getDisponibelstand() < bedingung * anzahl)
						throw new IllegalArgumentException("Not enough money");
				}
				//ohne Bedingung - nothing to check 
				else if (bedingung == -1) {
					//if (broker.getDisponibelstand() < marketPrice * anzahl)
					//	throw new IllegalArgumentException("Not enough money");
				}
				else
					throw new IllegalArgumentException("Illegal Bedingung");
			}
			else {
				if (!clientEmittents.containsKey(tickerId))
					throw new IllegalArgumentException("Nothing to sell");
				
				if (clientEmittents.get(tickerId) < anzahl)
					throw new IllegalArgumentException("Not enough amount of the Emittent");
			}
		}
		
		try {
			Auftrag sent = new Auftrag(-1, this.brokerId, auftrag.getKaufen(), auftrag.getTicker(), auftrag.getAnzahl(), auftrag.getBedingung());
			
			int ret = -1;
			float rndVal = java.util.concurrent.ThreadLocalRandom.current().nextFloat();
			if (tmpSentMode != 0)
				rndVal = tmpSentMode / 3.0f;
			
			//SOAP call
			if (rndVal <= 1.0f/3.0f) {
				ret = this.clientSOAPBoerse.auftragAddNew(this.brokerId, sent);
				System.out.println( "SOAP added new auftrag with id = " + ret );
			}
			//REST call
			else if (rndVal <= 2.0f/3.0f) {
				String body = "";
				
				
				this.clientRESTBoerse.path("add_new_auftrag").accept("text/plain");
				String returnText = this.clientRESTBoerse.post(body, String.class);
				if (returnText.startsWith("ok - auftragId=")) {
					ret = Integer.valueOf(returnText.substring(15, returnText.length() - 1));
				}
				else {
					throw new IllegalArgumentException(returnText);
				}
				
				System.out.println( "REST added new auftrag with id = " + ret );
			}
			//RMI call
			else {
				ret = this.boerse.auftragAddNew(this.brokerId, sent);
				System.out.println( "RMI added new auftrag with id = " + ret );
			}

			if (bedingung > 0) {
				if (buy)
					client.setDisponibelstand(-bedingung * anzahl);
				else
					client.getDisponibleAccountEmittents().put(tickerId, client.getDisponibleAccountEmittents().get(tickerId) - anzahl);
			}
			
			auftrag.setId(ret);
			auftrag.setStatus(AuftragStatus.Accepted);
			auftraege.put(ret, auftrag);

			this.poolDAO.getAuftragDAO().speichereItem( auftrag );
			return ret;
		}
		catch (IllegalArgumentException e) {
			throw e;
		}
	}

	/**
	 * Auftrag eines Clients zurueckrufen, ohne Sicherheitspruefung
	 */
	public void auftragCancel(Integer clientId, Integer auftragId) throws RemoteException, IllegalArgumentException {
		if ( !this.poolDAO.getClientDAO().containsKey(clientId) )
			throw new IllegalArgumentException("Client with id=" + clientId + " does not exist");

		Client client = this.poolDAO.getClientDAO().getItemById(clientId);
		java.util.TreeMap<Integer, Auftrag> auftraege = client.getAuftraegeList();	
		if ( !auftraege.containsKey(auftragId) )
			throw new IllegalArgumentException("Auftrag with id=" + auftragId + " does not exist");
		
		Auftrag auftrag = auftraege.get(auftragId);
		if (auftrag.getStatus() != AuftragStatus.Accepted)
			throw new IllegalArgumentException("Auftrag with id=" + auftragId + " can not be canceled");

		int tickerId = this.emittents.get( auftrag.getTicker() ).getId();
		try {
			
			float rndVal = java.util.concurrent.ThreadLocalRandom.current().nextFloat();
			if (tmpSentMode != 0)
				rndVal = tmpSentMode / 3.0f;
			
			//SOAP call
			if (rndVal <= 1.0f/3.0f) {
				this.clientSOAPBoerse.auftragCancel(this.brokerId, auftragId);
				System.out.println( "SOAP cancel auftrag with id = " + auftragId );
			}
			//REST call
			else if (rndVal <= 2.0f/3.0f) {
				String body = "";
				
				this.clientRESTBoerse.path("auftrag").path(auftragId).path("cancel").accept("text/plain");
				String returnText = this.clientRESTBoerse.post(body, String.class);
				if (!returnText.startsWith("ok")) {
					throw new IllegalArgumentException(returnText);
				}
				
				System.out.println( "REST cancel auftrag with id = " + auftragId  );
			}
			//RMI call
			else {
				boerse.auftragCancel(this.brokerId, auftragId);
				System.out.println( "RMI cancel auftrag with id = " + auftragId );
			}

			
			
			boerse.auftragCancel(this.brokerId, auftragId);
			
			if (auftrag.getKaufen())
				client.setDisponibelstand(auftrag.getBedingung() * auftrag.getAnzahl());
			else
				client.getDisponibleAccountEmittents().put(tickerId, client.getDisponibleAccountEmittents().get(tickerId) + auftrag.getAnzahl());

		}
		catch (IllegalArgumentException e) {
			throw e;
		}
		
		auftrag.setStatus(AuftragStatus.Canceled);
		setToLog(auftrag);
	}
	
	private void setToLog(Auftrag auftrag) {
		this.poolDAO.getAuftragDAO().speichereItem(auftrag);
	}
	
	public Report getReport(Integer clientId) throws RemoteException, IllegalArgumentException {
		return null;
	}
	/**
	 * Get current State des Clients, ohne Sicherheitspruefung
	 */
	public Client getState(Integer clientId) throws RemoteException, IllegalArgumentException {
		if ( !this.poolDAO.getClientDAO().containsKey(clientId) )
			throw new IllegalArgumentException("Client with id=" + brokerId + " does not exist");

		return this.poolDAO.getClientDAO().getItemById(clientId);
	}

	public java.util.TreeSet<Auftrag> getAuftraege(Integer clientId) throws RemoteException, IllegalArgumentException {
		if ( !this.poolDAO.getClientDAO().containsKey(clientId) )
			throw new IllegalArgumentException("Client " + String.valueOf(clientId) + " does not exist");

		return this.poolDAO.getAuftragDAO().getAuftraege(brokerId);
	}

	public java.util.TreeSet<Transaction> getTransaktionen(Integer clientId) throws RemoteException, IllegalArgumentException {
		if ( !this.poolDAO.getClientDAO().containsKey(clientId) )
			throw new IllegalArgumentException("Client " + String.valueOf(clientId) + " does not exist");

		java.util.TreeSet<Transaction> ret = new java.util.TreeSet<Transaction>();
		
		for (Auftrag a : this.poolDAO.getAuftragDAO().getAuftraege(brokerId)) {
			for (Transaction t : this.poolDAO.getTransactionDAO().getTransactions(a.getId())) {
				ret.add(t);
			}
		}
		
		return ret;
	}
	
	public java.util.TreeSet<Transaction> getTransaktionen(Integer clientId, Date afterDate) throws RemoteException, IllegalArgumentException {
		if ( !this.poolDAO.getClientDAO().containsKey(clientId) )
			throw new IllegalArgumentException("Client " + String.valueOf(clientId) + " does not exist");

		java.util.TreeSet<Transaction> ret = new java.util.TreeSet<Transaction>();
		
		for (Auftrag a : this.poolDAO.getAuftragDAO().getAuftraege(brokerId)) {
			for (Transaction t : this.poolDAO.getTransactionDAO().getTransactions(a.getId())) {
				if (t.getDateCommitted().after(afterDate))
					ret.add(t);
			}
		}
		
		return ret;

		//return new java.util.TreeSet<Transaction> ( transactionLog.get(clientId).headSet( new Transaction(0, 0, 0, 0, afterDate), true ) );
	}

	/**
	 * Einzahlen/auszahlen von einem Client
	 * normaleweise muss es automatisch ausgefuert werden, wenn das Geld zum Tradingkonto des Clients eingeht
	 * amount kann +/- sein (einzahlen/auszahlen)
	 * einfachheitshalber koennen die Clienten diese Methode selbst aufrufen
	 * wenn sie das machen, dann heisst es das sie das Geld zu/von ihrem Konto ueberweisen
	 */
	public void tradingAccount(Integer clientId, float amount) throws RemoteException, IllegalArgumentException {
		if ( !this.poolDAO.getClientDAO().containsKey(clientId) )
			throw new IllegalArgumentException("Client with id=" + clientId + " does not exist");

		Client client = this.poolDAO.getClientDAO().getItemById(clientId);
		synchronized(client) {
			if (amount < 0 && client.getDisponibelstand() < -amount)
				throw new IllegalArgumentException("Not enough money");
			client.setDisponibelstand(amount);
			client.setKontostand(amount);
			boerse.tradingAccount(this.brokerId, amount);
			this.poolDAO.getClientDAO().speichereItem(client);
		}
	}

	/**
	 * Einzahlen/auszahlen eines Emittens (z.B. Aktien) von einem Client
	 * normaleweise muss es automatisch ausgefuert werden, wenn die Aktien zum Tradingkonto des Clients eingehen
	 * anzahl kann +/- sein (einzahlen/auszahlen)
	 * einfachheitshalber koennen die Clienten diese Methode selbst aufrufen
	 * wenn sie das machen, dann heisst es das sie die Aktien zu/von ihrem Konto ueberweisen 
	 */
	public void tradingAccount(Integer clientId, Integer tickerId, Integer anzahl) throws RemoteException, IllegalArgumentException {
		if ( !this.poolDAO.getClientDAO().containsKey(clientId) )
			throw new IllegalArgumentException("Client with id=" + clientId + " does not exist");

		Client client = this.poolDAO.getClientDAO().getItemById(clientId);
		synchronized(client) {
			//check, ob aktive Auftrage existieren
			if (anzahl < 0) {
				for(Auftrag a : client.getAuftraegeList().values()) {
					if ( this.emittents.get( a.getTicker() ).getId() == tickerId )
						throw new IllegalArgumentException("There are aktive Auftraege with this emittent");
				}
			}
			
			java.util.TreeMap<Integer, Integer> clientEmittents = client.getAccountEmittents();
			if (clientEmittents.containsKey(tickerId)) {
				if (anzahl < 0 && clientEmittents.get(tickerId) < -anzahl)
					throw new IllegalArgumentException("Not enough amount of emittent");
				
				client.setKontostand(tickerId, anzahl);
				client.setDisponibelstand(tickerId, anzahl);
			}
			else {
				if (anzahl < 0)
					throw new IllegalArgumentException("Not enough amount of emittent");
				
				client.setKontostand(tickerId, anzahl);
				client.setDisponibelstand(tickerId, anzahl);
			}
		}
		
		boerse.tradingAccount(this.brokerId, tickerId, anzahl);
		this.poolDAO.getClientDAO().speichereItem(client);

	}

	
	//UDP-Server stuff
	/**
	 * UDP-Thread des Servers
	 */
	private ScheduledExecutorService execUDP;
	private DatagramSocket socketUDP;
	
	//Emittents, die via UDP abgehoert sind
	private Integer[] udpEmittentIds;
	//UDP Counters - Fehlererkennung
	private java.util.TreeMap< Integer /* emittentId */, Integer > udpCounters;

	/**
	 * Start UDP Listener
	 */
	public void getFeedUDP() throws IllegalArgumentException {
		Integer[] emittentIds = new Integer[this.emittents.size()];
		int i = 0;
		for (Emittent e : this.emittents.values())
			emittentIds[i++] = e.getId();
		
		getFeedUDP(emittentIds);
	}
	
	/**
	 * Start UDP Listener
	 */
	public void getFeedUDP(Integer[] emittentIds) throws IllegalArgumentException {
        
		if (emittentIds == null || emittentIds.length == 0)
			throw new IllegalArgumentException("arg is empty");

		//stop current UDP Listener
		if (execUDP != null)
			execUDP.shutdown();

		this.udpEmittentIds = emittentIds;
		
		//init counters
		this.udpCounters = new java.util.TreeMap< Integer /* emittentId */, Integer >();
		for (byte i = 0; i < this.udpEmittentIds.length; ++i)
			this.udpCounters.put(this.udpEmittentIds[i], -1);
		
		
		try {
			socketUDP = new DatagramSocket();
			execUDP = Executors.newScheduledThreadPool(2);
	
			//start UDP Listener
			execUDP.schedule(new Runnable() {
				  @Override
				  public void run() {
					  listenUDPResponse();
				  }
				}, 0, TimeUnit.SECONDS);
			
			//start UDP Request every 5 seconds
			execUDP.scheduleAtFixedRate(new Runnable() {
			  @Override
			  public void run() {
				  sendUDPrequest();
			  }
			}, 0, 5, TimeUnit.SECONDS);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void listenUDPResponse() {
	    try {
	    	byte[] buf = new byte[1024];
	    	
	    	do {
				DatagramPacket requestPacket = new DatagramPacket(buf, buf.length);
				socketUDP.receive(requestPacket);
	
			    ByteArrayInputStream bis = new ByteArrayInputStream(requestPacket.getData());
			    ObjectInput in = new ObjectInputStream(bis);
			    byte len = in.readByte();
			    for (byte i = 0; i < len; ++i) {
					FeedMsg feed = (FeedMsg) in.readObject();
			    	processFeedMsg(feed);
			    }
			    in.close();
			    bis.close();
			    
	    	} while (true);
	    }   
	    catch (SocketException e){
	        System.err.println("Socket: " + e.getMessage());
	        e.printStackTrace();
	    }
	    catch (IOException e){
	    	System.err.println("IO: " + e.getMessage());
	    	e.printStackTrace();
	    }
	    catch (ClassNotFoundException e){
	    	System.err.println("IO: " + e.getMessage());
	    	e.printStackTrace();
	    }
	    catch (Exception e){
	    	System.err.println("Exception: " + e.getMessage());
	    	e.printStackTrace();
	    }
	}
	
	private void processFeedMsg(FeedMsg feedMsg) {
		System.out.println( "get feed = " + feedMsg.getCounter() + " " + feedMsg.getAnzahl() + " " + feedMsg.getId()  + " " + feedMsg.getId2() );

		if (feedMsg.getCounter() == -1) {
			if (this.sessionId < feedMsg.getId())
				this.sessionId = feedMsg.getId();
			
			//start preise kommen
			return;
		}
		
		//Fehlererkennung
		if (this.udpCounters.get(feedMsg.getTickerId()) != -1) {

			//same packet
			if ( this.udpCounters.get(feedMsg.getTickerId()) >= feedMsg.getCounter() )
				return;
							
			//Fehler - mach etwas, vielleicht kan man einfach boerse.getState aufrufen
			if (feedMsg.getCounter() != this.udpCounters.get(feedMsg.getTickerId()) + 1) {
				System.out.println( "Fehlererkennung!!!" );
				//updateStatus();
			}
		}
		
		this.udpCounters.put(feedMsg.getTickerId(), feedMsg.getCounter());
		
		if (feedMsg.getId() != -1)
			processFeedMsgAuftrag(feedMsg, false);
		if (feedMsg.getId2() != -1)
			processFeedMsgAuftrag(feedMsg, true);
	}
	
	private void processFeedMsgAuftrag(FeedMsg feedMsg, boolean id2) {

		int id;
		AuftragStatus status;
		if (id2) {
			id = feedMsg.getId2();
			status = feedMsg.getStatus2();
		}
		else {
			id = feedMsg.getId();
			status = feedMsg.getStatus();
		}
		
		//System.out.println( "processFeedMsg Auftrag , auftragId= " + id + ", status= " + status + " " + feedMsg.getId2() );

		
		//search if this Auftrag Data is our Client Auftrag 
		if (this.auftraege.containsKey(id)) {
			Auftrag clientAuftrag = this.auftraege.get( id );
			Client client = this.poolDAO.getClientDAO().getItemById(clientAuftrag.getOwnerId());
			//System.out.println( "processFeedMsg Auftrag , clientAuftrag.getOwnerId() = " + clientAuftrag.getOwnerId() + ", client= " + client.getName() );

			synchronized(client) {
				if (status == AuftragStatus.Canceled) {
					if (clientAuftrag.getBedingung() > 0) {
						if (clientAuftrag.getKaufen())
							client.setDisponibelstand(clientAuftrag.getBedingung() * clientAuftrag.getAnzahl());
						else
							client.setDisponibelstand( feedMsg.getTickerId(), clientAuftrag.getAnzahl());
					}
				}
				else if (status == AuftragStatus.Bearbeitet || status == AuftragStatus.TeilweiseBearbeitet) {
					if (clientAuftrag.getKaufen()) {
						client.setKontostand(-feedMsg.getPrice() * feedMsg.getAnzahl());
						client.setKontostand(feedMsg.getTickerId(), feedMsg.getAnzahl());
						
						if (!id2 && clientAuftrag.getBedingung() <= 0) {
							client.setDisponibelstand(-feedMsg.getPrice() * feedMsg.getAnzahl());
						}
						client.setDisponibelstand(feedMsg.getTickerId(), feedMsg.getAnzahl());
					}
					else {
						client.setKontostand(feedMsg.getPrice() * feedMsg.getAnzahl());
						client.setKontostand(feedMsg.getTickerId(), -feedMsg.getAnzahl());
						
						System.out.println( "processFeedMsg Auftrag , clientAuftrag.getBedingung() = " + clientAuftrag.getBedingung() + ", id2= " + id2 );

						client.setDisponibelstand(feedMsg.getPrice() * feedMsg.getAnzahl());
						if (!id2 && clientAuftrag.getBedingung() <= 0) {
							client.setDisponibelstand(feedMsg.getTickerId(), -feedMsg.getAnzahl());
						}
					}
				}
				else if (status == AuftragStatus.Accepted) {
					clientAuftrag.setAnzahl( feedMsg.getAnzahl() );
				}
				else
					throw new IllegalArgumentException("BROKER ERROR - impossible!!!");
				
				clientAuftrag.setStatus(status);
				setToLog(clientAuftrag);
			}
		}
	}

	
	
	private void sendUDPrequest() {
		
	    try {
		    ByteArrayOutputStream bos = new ByteArrayOutputStream();
		    ObjectOutput out = new ObjectOutputStream(bos);
	    	out.writeObject(new FeedRequest(this.sessionId, this.udpEmittentIds));
	    	
			DatagramPacket requestPacket = new DatagramPacket(bos.toByteArray(), bos.size(), InetAddress.getByName(this.remoteHostBoerse), this.remotePortUDPBoerse);
			socketUDP.send(requestPacket);
			//System.out.println( "send Addr = " + requestPacket.getAddress().toString() );
			//System.out.println( "send Port = " + requestPacket.getPort() );

            
	    	out.close();
    	    bos.close();
    	}   
	    catch (SocketException e){
	        System.err.println("Socket: " + e.getMessage());
	    }
	    catch (IOException e){
	    	System.err.println("IO: " + e.getMessage());
	    }
	}

	
	/**
	 * Stop UDP Listener
	 */
	public void stopFeedUDP() throws IllegalArgumentException {
		socketUDP.close();
		socketUDP = null;
		
		if (execUDP != null)
			execUDP.shutdown();
		execUDP = null;
	}
	
	

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

        if (System.getSecurityManager() == null) {
            //System.setSecurityManager(new SecurityManager());
        }

		if (args.length == 0)
			throw new IllegalArgumentException("arguments: brokerId {remoteHostBoerse remotePortUDPBoerse remotePortRMIBoerse localPortRMIBroker remoteSOAPHost remoteRESTHost localSOAPHostBroker localRESTHostBroker}");
		
		Integer brokerId = Integer.valueOf(args[0]);
		String remoteHostBoerse = "localhost";
		int remotePortUDPBoerse = 10000;
		int remotePortRMIBoerse = 10001;
		if (args.length > 1)
			remoteHostBoerse = args[1];
		if (args.length > 2)
			remotePortUDPBoerse = Integer.valueOf(args[2]);
		if (args.length > 3)
			remotePortRMIBoerse = Integer.valueOf(args[3]);

        int localPortRMIBroker = -1;
		if (args.length > 4)
			localPortRMIBroker = Integer.valueOf(args[4]);

		String remoteSOAPHost = "http://localhost:8080/WebServices/public";
		if (args.length > 5)
			remoteSOAPHost = args[5];
	
		String remoteRESTHost = "http://localhost:9999/rest/public";
		if (args.length > 6)
			remoteRESTHost = args[6];

		String localSOAPHostBroker = null;
		if (args.length > 7)
			localSOAPHostBroker = args[7];
	
		String localRESTHostBroker = null;
		if (args.length > 8)
			localRESTHostBroker = args[8];

		
		try {
			BrokerServer brokerServer = new BrokerServer(brokerId, remoteHostBoerse, remotePortUDPBoerse, remotePortRMIBoerse, localPortRMIBroker, remoteSOAPHost, remoteRESTHost, localSOAPHostBroker, localRESTHostBroker, System.getProperty("user.dir"));
		
			//initial Data, Clients und ihre TradingsKontoStaende
			switch (brokerId) {
			case 1: 
				if (brokerServer.poolDAO.getClientDAO().getItems().size() == 0) {
					int clId = brokerServer.clientAddNew( new Client(brokerId, "Daniil 1") );
					brokerServer.tradingAccount(clId, 1000000);
					brokerServer.tradingAccount(clId, brokerServer.emittents.get("AAPL").getId(), 100000);
					brokerServer.tradingAccount(clId, brokerServer.emittents.get("RDSA").getId(), 100000);
					clId = brokerServer.clientAddNew( new Client(brokerId, "Daniil 2") );
					brokerServer.tradingAccount(clId, 100000);
					brokerServer.tradingAccount(clId, brokerServer.emittents.get("AAPL").getId(), 10000);
					brokerServer.tradingAccount(clId, brokerServer.emittents.get("RDSA").getId(), 10000);
				}
				break;
			case 2: 
				if (brokerServer.poolDAO.getClientDAO().getItems().size() == 0) {
					int clId = brokerServer.clientAddNew( new Client(brokerId, "Jakub 1") );
					brokerServer.tradingAccount(clId, 1000000);
					brokerServer.tradingAccount(clId, brokerServer.emittents.get("AAPL").getId(), 100000);
					brokerServer.tradingAccount(clId, brokerServer.emittents.get("RDSA").getId(), 100000);
					clId = brokerServer.clientAddNew( new Client(brokerId, "Jakub 2") );
					brokerServer.tradingAccount(clId, 100000);
					brokerServer.tradingAccount(clId, brokerServer.emittents.get("AAPL").getId(), 10000);
					brokerServer.tradingAccount(clId, brokerServer.emittents.get("RDSA").getId(), 10000);
				}
				break;
			case 3: 
				if (brokerServer.poolDAO.getClientDAO().getItems().size() == 0) {
					int clId = brokerServer.clientAddNew( new Client(brokerId, "Ayrat 1") );
					brokerServer.tradingAccount(clId, 1000000);
					brokerServer.tradingAccount(clId, brokerServer.emittents.get("AAPL").getId(), 100000);
					brokerServer.tradingAccount(clId, brokerServer.emittents.get("RDSA").getId(), 100000);
					clId = brokerServer.clientAddNew( new Client(brokerId, "Ayrat 2") );
					brokerServer.tradingAccount(clId, 100000);
					brokerServer.tradingAccount(clId, brokerServer.emittents.get("AAPL").getId(), 10000);
					brokerServer.tradingAccount(clId, brokerServer.emittents.get("RDSA").getId(), 10000);
				}
				break;
			}
			
			brokerServer.getFeedUDP();
			
			Thread.sleep(3000);
			//RMI
			brokerServer.tmpSentMode = 3;
			brokerServer.auftragAddNew(1, new Auftrag(1, true, "AAPL", 1000, 50) );
/*
			Thread.sleep(3000);
            //SOAP
			brokerServer.tmpSentMode = 1;            
			brokerServer.auftragAddNew(2, new Auftrag(2, false, "AAPL", 100) );
*/
			Thread.sleep(3000);
            //REST
			brokerServer.tmpSentMode = 2;            
			brokerServer.auftragCancel(1, 1);
//			brokerServer.auftragAddNew(2, new Auftrag(2, false, "AAPL", 100) );

            
  //          System.out.println( "client.getBrokerNetworkAddress(1, NetworkResource.RMI)=" + clientSOAPBoerse.getBrokerNetworkAddress(1, NetworkResource.RMI) );
   //         System.out.println( "client.getBrokerNetworkAddress(1, NetworkResource.SOAP)=" + clientSOAPBoerse.getBrokerNetworkAddress(1, NetworkResource.SOAP) );
   //         System.out.println( "client.getBrokerNetworkAddress(1, NetworkResource.REST)=" + clientSOAPBoerse.getBrokerNetworkAddress(1, NetworkResource.REST) );
//            for (Emittent e : clientSOAPBoerse.getEmittentsList())
//            	System.out.println(e.getTicker() + " - " + e.getName() );
            
            /*
            //REST
            org.apache.cxf.jaxrs.client.WebClient clientREST = org.apache.cxf.jaxrs.client.WebClient.create("http://localhost:9999/rest/public");
            clientREST.path("broker_network_address").path(3).path(NetworkResource.SOAP.getNumVal()).accept("text/plain");
            System.out.println("result = " + clientREST.get(String.class) );

            */
            
            
//			Thread.sleep(3000);
//			brokerServer.auftragAddNew(2, new Auftrag(2, false, "AAPL", 100) );

//			Thread.sleep(3000);
//			brokerServer.auftragAddNew(2, new Auftrag(2, false, "AAPL", 1000, 45) );

			Thread.sleep(3000);
			
			//System.out.println( "send Addr = " + requestPacket.getAddress().toString() );

			
			for(Client cl : brokerServer.getClientsList()) {
				  System.out.println(cl + " => " + cl.getDisponibelstand() + ", " + cl.getKontostand());
				  
					for(java.util.Map.Entry<Integer,Integer> entry : cl.getAccountEmittents().entrySet()) {
						Integer key = entry.getKey();
						  Integer value = entry.getValue();

						  System.out.println(key + " => " + value);
						}

					for(java.util.Map.Entry<Integer,Integer> entry : cl.getDisponibleAccountEmittents().entrySet()) {
						Integer key = entry.getKey();
						  Integer value = entry.getValue();

						  System.out.println(key + " => " + value);
						}


				}
			

		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
