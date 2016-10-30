package at.ac.univie.dse2016.stream.broker;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import at.ac.univie.dse2016.stream.boerse.BoerseAdminAdapter;
import at.ac.univie.dse2016.stream.boerse.BoerseServer;
import at.ac.univie.dse2016.stream.boerse.EmittentSection;
import at.ac.univie.dse2016.stream.common.*;

public class BrokerServer implements BrokerAdmin, BrokerClient {
	
	public BrokerServer(Integer brokerId) {
		this.brokerId = brokerId;
		emittents = new java.util.TreeMap<String, Emittent>();
		activeAuftraege = new java.util.TreeMap< Integer /* clientId */, java.util.ArrayList<Auftrag> >();
		auftraegeLog = new java.util.TreeMap< Integer /* clientId */, java.util.ArrayList<Auftrag> >();
		clients = new java.util.TreeMap<Integer, Client>();
	}

	/**
	 * Broker Id
	 */
	private Integer brokerId;
	
	/**
	 * Emittents, die auf der Boerse gekauft/verkauft werden duerfen
	 */
	private java.util.TreeMap<String, Emittent> emittents;

	/**
	 * Alle Auftraege des Brokers, die aktuell sind
	 */
	private java.util.TreeMap< Integer /* clientId */, java.util.ArrayList<Auftrag> > activeAuftraege;

	/**
	 * Alle Auftraege des Brokers
	 */
	private java.util.TreeMap< Integer /* clientId */, java.util.ArrayList<Auftrag> > auftraegeLog;

	/**
	 * Clienten, die ueber diesen Broker arbeiten duerfen
	 */
	private java.util.TreeMap<Integer, Client> clients;

	
	/**
	 * AddNew Client, Admin's Function
	 */
	public Integer clientAddNew(Client client) throws RemoteException, IllegalArgumentException {
		synchronized(clients) {
			client = new Client(clients.size() + 1, brokerId, 0f, client.getName());
			clients.put(client.getId(), client);
		}
		return client.getId();

	}

	/**
	 * Edit Client, Admin's Function
	 */
	public void clientEdit(Client client) throws RemoteException, IllegalArgumentException {
		if ( !clients.containsKey(client.getId()) )
			throw new IllegalArgumentException("Client with id=" + client.getId() + " does not exist");
		
		Client actualClient = clients.get(client.getId());
		
		actualClient.setName(client.getName());
	}
	
	/**
	 * Remove Client, Admin's Function
	 */
	public void clientLock(Client client) throws RemoteException, IllegalArgumentException {
		if ( !clients.containsKey(client.getId()) )
			throw new IllegalArgumentException("Client with id=" + client.getId() + " does not exist");
		
		synchronized(clients) {
			clients.remove( client.getId() );
		}
		
	}
	
	/**
	 * Get all Client, Admin's Function
	 */
	public java.util.ArrayList<Client> getClientsList() throws RemoteException {
		java.util.ArrayList<Client> ret = new java.util.ArrayList<Client>(clients.values());
		return ret;

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
		// BoersenMakler
		/*if (this.parentId == -1) {
			//Boerse.AuftragAddNew(this.id, auftrag);
		}
		else if (boersenMakler == null) {
			throw new IllegalArgumentException("You cann  Order only with your personal Broker");

		}*/
		return -1;
	}

	/**
	 * Auftrag eines Clients zurueckrufen, ohne Sicherheitspruefung
	 */
	public void auftragCancel(Integer clientId, Integer auftragId) throws RemoteException, IllegalArgumentException {
		
	}
	
	public Report getReport(Integer clientId) throws RemoteException, IllegalArgumentException {
		return null;
	}
	/**
	 * Get current State des Clients, ohne Sicherheitspruefung
	 */
	public Client getState(Integer clientId) throws RemoteException, IllegalArgumentException {
		return null;
	}

	/**
	 * Einzahlen/auszahlen von einem Client
	 * normaleweise muss es automatisch ausgefuert werden, wenn das Geld zum Tradingkonto des Clients eingeht
	 * amount kann +/- sein (einzahlen/auszahlen)
	 * einfachheitshalber koennen die Clienten diese Methode selbst aufrufen
	 * wenn sie das machen, dann heisst es das sie das Geld zu/von ihrem Konto ueberweisen
	 */
	public void tradingAccount(Integer clientId, float amount) throws RemoteException, IllegalArgumentException {
		
	}

	/**
	 * Einzahlen/auszahlen eines Emittens (z.B. Aktien) von einem Client
	 * normaleweise muss es automatisch ausgefuert werden, wenn die Aktien zum Tradingkonto des Clients eingehen
	 * anzahl kann +/- sein (einzahlen/auszahlen)
	 * einfachheitshalber koennen die Clienten diese Methode selbst aufrufen
	 * wenn sie das machen, dann heisst es das sie die Aktien zu/von ihrem Konto ueberweisen 
	 */
	public void tradingAccount(Integer clientId, Integer tickerId, Integer anzahl) throws RemoteException, IllegalArgumentException {
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Integer brokerId = Integer.valueOf(args[0]);
		BrokerServer brokerServer = new BrokerServer(brokerId);
		
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
        	
            Registry registryBoerse = LocateRegistry.getRegistry(10001);
            BoerseClient boerse = (BoerseClient) registryBoerse.lookup("brokerBoerse");

            //brokerServer.emittents = ;
            for (Emittent e : boerse.getEmittentsList())
            	System.out.println(e.getTicker());

            Broker brokerState = boerse.getState(brokerId);

            System.out.println(brokerState.getId());
            System.out.println(brokerState.getName());
            System.out.println(brokerState.getKontostand());
            
            
            String host = boerse.getBrokerNetworkAddress(brokerId);
            String[] ar = host.split(":");
            int port = Integer.valueOf(ar[1]);
            host = ar[0];
            
            Registry registry = LocateRegistry.createRegistry(port);
            
            BrokerAdminAdapter adminAdapter = new BrokerAdminAdapter(brokerServer);
            BrokerAdmin adminStub =
                    (BrokerAdmin) UnicastRemoteObject.exportObject(adminAdapter, 0);
                registry.rebind("adminBroker", adminStub);
            
            BrokerClient clientStub =
                    (BrokerClient) UnicastRemoteObject.exportObject(brokerServer, 0);
            registry.rebind("client", clientStub);
            
            System.out.println("Der Broker " + brokerState.getName() + " ist gestartet");
            System.out.println("Hostname=" + host + ", port=" + port);
        
        } catch (Exception e) {
            System.err.println("Client exception:");
            e.printStackTrace();
        }

	}

}
