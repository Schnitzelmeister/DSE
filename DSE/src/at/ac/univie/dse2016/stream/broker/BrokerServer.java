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

import javax.xml.ws.Endpoint;

public class BrokerServer implements BrokerAdmin, BrokerClient {
	
	public BrokerServer(int brokerId, String remoteHostBoerse, int remotePortUDPBoerse, int remotePortRMIBoerse, int localPortRMIBroker
			, String localSOAPHostBroker, String localRESTHostBroker, String path) {
		this.brokerId = brokerId;
		this.remoteHostBoerse = remoteHostBoerse;
		this.remotePortUDPBoerse = remotePortUDPBoerse;
		this.remotePortRMIBoerse = remotePortRMIBoerse;
		this.localPortRMIBroker = localPortRMIBroker;
		this.localSOAPHostBroker = localSOAPHostBroker;
		this.localRESTHostBroker = localRESTHostBroker;
		
		//init DAO
		this.poolDAO = new PoolDAO( path );
		System.out.println("Path to DATA Folder = "+ path);
		System.out.println("Die Daten werden in diesem Folder gespeichert");

		emittents = new java.util.TreeMap<String, Emittent>();
		auftraege = new java.util.TreeMap< Integer /* auftragId */, Auftrag >();
		this.execUDP = null;
		
        try {
        	
        	//get Boerse object
            Registry registryBoerse = LocateRegistry.getRegistry(this.remoteHostBoerse, this.remotePortRMIBoerse);
            boerse = (BoerseClient) registryBoerse.lookup("brokerBoerse");

            //get aktual Emittents
            for (Emittent emittent : boerse.getEmittentsList())
            	emittents.put(emittent.getTicker(), emittent);
            
            //get port from Boerse Settings
    		if (localPortRMIBroker == -1)
    		{
                String host = boerse.getBrokerNetworkAddress(brokerId, NetworkResource.RMI);
                String[] ar = host.split(":");
                this.localPortRMIBroker = Integer.valueOf(ar[1]);
    		}

            //get SOAPHostBroker from Boerse Settings
    		if (localSOAPHostBroker == null)
    			this.localSOAPHostBroker = boerse.getBrokerNetworkAddress(brokerId, NetworkResource.SOAP);

            //get RESTHostBroker from Boerse Settings
    		if (localRESTHostBroker == null)
    			this.localRESTHostBroker = boerse.getBrokerNetworkAddress(brokerId, NetworkResource.REST);

    		
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
	private BoerseClient boerse;
	
	
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
			/*
			for(java.util.Map.Entry<Integer,Integer> entry : clientEmittents.entrySet()) {
				Integer key = entry.getKey();
				  Integer value = entry.getValue();

				  System.out.println(key + " => " + value);
				}
			*/
			
			
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
			int ret = boerse.auftragAddNew(this.brokerId, sent);
			if (buy)
				client.setDisponibelstand(-bedingung * anzahl);
			else
				client.getDisponibleAccountEmittents().put(tickerId, client.getDisponibleAccountEmittents().get(tickerId) - anzahl);
			
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
			    	processFeedMsg((FeedMsg) in.readObject());
			    }
			    in.close();
			    bis.close();
			    
	    	} while (true);
	    }   
	    catch (SocketException e){
	        System.err.println("Socket: " + e.getMessage());
	    }
	    catch (IOException e){
	    	System.err.println("IO: " + e.getMessage());
	    }
	    catch (ClassNotFoundException e){
	    	System.err.println("IO: " + e.getMessage());
	    }
	    /*	    catch (Exception e){
	    	System.err.println("Exception: " + e.getMessage());
	    }*/
	}
	
	private void processFeedMsg(FeedMsg feedMsg) {
		
		System.out.println( "get feed = " + feedMsg.getId() + " " + feedMsg.getAnzahl() );
		
		//Fehlererkennung
		if (this.udpCounters.get(feedMsg.getTickerId()) != -1 && feedMsg.getCounter() == -1) {

			System.out.println( "Fehlererkennung!!!" );

			//Fehler - mach etwas, vielleicht kan man einfach boerse.getState aufrufen
			if (feedMsg.getCounter() != this.udpCounters.get(feedMsg.getTickerId()) + 1) {
				//updateStatus();
			}
		}
		
		processFeedMsgAuftrag(feedMsg, true);
		processFeedMsgAuftrag(feedMsg, false);
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
		
		//search if this Auftrag Data is our Client Auftrag 
		if (this.auftraege.containsKey(id)) {
			Auftrag clientAuftrag = this.auftraege.get( id );
			Client client = this.poolDAO.getClientDAO().getItemById(clientAuftrag.getOwnerId());
			
			synchronized(client) {
				if (status == AuftragStatus.Canceled) {
					if (clientAuftrag.getKaufen())
						client.setDisponibelstand(clientAuftrag.getBedingung() * clientAuftrag.getAnzahl());
					else
						client.setDisponibelstand( this.emittents.get( clientAuftrag.getTicker() ).getId(), clientAuftrag.getAnzahl());
				}
				else if (status == AuftragStatus.Bearbeitet || status == AuftragStatus.TeilweiseBearbeitet) {
					if (clientAuftrag.getKaufen()) {
						client.setKontostand(-feedMsg.getPrice() * feedMsg.getAnzahl());
						
						if (client.getAccountEmittents().containsKey(feedMsg.getTickerId()))
							client.getAccountEmittents().put(feedMsg.getTickerId(), client.getAccountEmittents().get(feedMsg.getTickerId()) + feedMsg.getAnzahl());
						else
							client.getAccountEmittents().put(feedMsg.getTickerId(), feedMsg.getAnzahl());
					}
					else {
						client.setKontostand(feedMsg.getPrice() * feedMsg.getAnzahl());
						client.getAccountEmittents().put(feedMsg.getTickerId(), client.getAccountEmittents().get(feedMsg.getTickerId()) - feedMsg.getAnzahl());
					}				
				}
				else if (status != AuftragStatus.Accepted)
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
	    	out.writeObject(new FeedRequest(this.udpEmittentIds));
	    	
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
			throw new IllegalArgumentException("arguments: brokerId {remoteHostBoerse remotePortUDPBoerse remotePortRMIBoerse localPortRMIBroker}");
		
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

		String localSOAPHostBroker = null;
		if (args.length > 5)
			localSOAPHostBroker = args[5];
	
		String localRESTHostBroker = null;
		if (args.length > 6)
			localRESTHostBroker = args[6];
		
		
		try {
			BrokerServer brokerServer = new BrokerServer(brokerId, remoteHostBoerse, remotePortUDPBoerse, remotePortRMIBoerse, localPortRMIBroker, localSOAPHostBroker, localRESTHostBroker, System.getProperty("user.dir"));
		
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
			brokerServer.auftragAddNew(1, new Auftrag(1, true, "AAPL", 1000, 50) );

			Thread.sleep(3000);
			brokerServer.auftragAddNew(2, new Auftrag(2, false, "AAPL", 100) );

		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
