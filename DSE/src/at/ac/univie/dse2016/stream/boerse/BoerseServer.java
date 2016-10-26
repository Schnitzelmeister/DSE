package at.ac.univie.dse2016.stream.boerse;

import java.net.*;
import java.io.*;
import java.security.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import at.ac.univie.dse2016.stream.common.*;

public final class BoerseServer implements BoerseAdmin, BoerseClient {
		
	public BoerseServer() {
		super();
		status = BoerseStatus.Closed;
		emittents = new java.util.TreeMap<String, Emittent>();
		brokers = new java.util.TreeMap<Integer, Broker>();
	}

	
	/**
	 * Status der Boerse, Open, Closed oder Error
	 */
	private BoerseStatus status;
	public BoerseStatus getStatus() { return status; }

	/**
	 * Emittents, die auf der Boerse gekauft/verkauft werden duerfen
	 */
	private java.util.TreeMap<String, Emittent> emittents;

	/**
	 * Brokers, die auf der Boerse arbeiten duerfen
	 */
	private java.util.TreeMap<Integer, Broker> brokers;

	/**
	 * Alle Auftraege der Boerse, die aktuell sind
	 */
	private java.util.TreeMap< Integer /* emittentId */, java.util.TreeMap< Float /* preis */, java.util.ArrayList<Auftrag> > > activeAuftraege;

	/**
	 * Alle Auftraege der Boerse
	 */
	private java.util.TreeMap< Integer /* clientId */, java.util.ArrayList<Auftrag> > auftraegeLog;
	

	/**
	 * AddNew Emittent, Admin's Function
	 */
	public Integer emittentAddNew(Emittent emittent) throws RemoteException, IllegalArgumentException {
		if ( emittents.containsKey(emittent.getTicker()) )
			throw new IllegalArgumentException("Ticker " + emittent.getTicker() + " exists");
		
		Emittent actualEmittent = new Emittent(emittents.size() + 1, emittent.getTicker(), emittent.getName());
		emittents.put(actualEmittent.getTicker(), actualEmittent);
		return actualEmittent.getId();
	}

	/**
	 * Edit Emittent, Admin's Function
	 */
	public void emittentEdit(Emittent emittent) throws RemoteException, IllegalArgumentException {
		if ( !emittents.containsKey(emittent.getTicker()) )
			throw new IllegalArgumentException("Ticker " + emittent.getTicker() + " does not exist");
		
		Emittent actual_emitent = emittents.get(emittent.getTicker());
		if (actual_emitent.getId() != emittent.getId())
			throw new IllegalArgumentException("Ticker " + emittent.getTicker() + " has other Id");
		
		actual_emitent.setName(emittent.getName());
	}
	
	/**
	 * Remove Emittent, Admin's Function
	 */
	public void emittentLock(Emittent emittent) throws RemoteException, IllegalArgumentException {
		if ( !emittents.containsKey(emittent.getTicker()) )
			throw new IllegalArgumentException("Ticker " + emittent.getTicker() + " does not exist");
		
		Emittent actualEmittent = emittents.get(emittent.getTicker());
		if (actualEmittent.getId() != emittent.getId())
			throw new IllegalArgumentException("Ticker " + emittent.getTicker() + " has other Id");
		
		emittents.remove(emittent.getTicker());
	}

	/**
	 * Get all Emittents, Admin's Function
	 */
	public java.util.ArrayList<Emittent> getEmittentsList() throws RemoteException {
		java.util.ArrayList<Emittent> ret = new java.util.ArrayList<Emittent>(emittents.values());
		return ret;
	}
	
	/**
	 * AddNew Broker, Admin's Function
	 */
	public Integer brokerAddNew(Broker broker) throws RemoteException, IllegalArgumentException {
		if ( brokers.containsKey(broker.getId()) )
			throw new IllegalArgumentException("Client " + String.valueOf(broker.getId()) + " exists");
		
		Broker actualBroker = new Broker(brokers.size() + 1, broker.getKontostand(), broker.getName(), broker.getPhone(), broker.getLicense(), broker.getNetworkAddress());
		brokers.put(actualBroker.getId(), actualBroker);
		return actualBroker.getId();
	}

	/**
	 * Edit Broker, Admin's Function
	 */
	public void brokerEdit(Broker broker) throws RemoteException, IllegalArgumentException {
		if ( !brokers.containsKey(broker.getId()) )
			throw new IllegalArgumentException("Client " + String.valueOf(broker.getId()) + " does not exist");
		
		brokers.put( broker.getId(), broker );
	}
	
	/**
	 * Remove Broker, Admin's Function
	 */
	public void brokerLock(Broker broker) throws RemoteException, IllegalArgumentException {
		if ( !brokers.containsKey(broker.getId()) )
			throw new IllegalArgumentException("Client " + String.valueOf(broker.getId()) + " does not exist");
		
		brokers.remove(broker.getId());
	}
	
	/**
	 * Get all Brokers, Admin's Function
	 */
	public java.util.ArrayList<Broker> getBrokersList() throws RemoteException {
		java.util.ArrayList<Broker> ret = new java.util.ArrayList<Broker>(brokers.values());
		return ret;
	}
	
	/**
	 * Schliesst die Boerse ab, normaleweise am Ende des Tages, Admin's Function oder ScheduleJob
	 */
	public void Close() throws RemoteException {
		status = BoerseStatus.Closed;
	}

	/**
	 * Oeffnet die Boerse, Admin's Function oder ScheduleJob
	 */
	public void Open() throws RemoteException {
		status = BoerseStatus.Open;
	}
	
	
	
	
	
	
	
	/**
	 * Get current State des Brokers, ohne Sicherheitspruefung
	 */
	public Broker getState(Integer brokerId) throws RemoteException, IllegalArgumentException {
		if ( !brokers.containsKey(brokerId) )
			throw new IllegalArgumentException("Broker with id=" + brokerId + " does not exist");

		return brokers.get(brokerId);
	}
	
	/**
	 * Auftrag eines Brokers stellen, ohne Sicherheitspruefung
	 */
	public Integer auftragAddNew(Integer brokerId, Auftrag auftrag) throws RemoteException, IllegalArgumentException {
		//auftraegeLog;
		//activeAuftraege;
		return -1;
	}

	/**
	 * Auftrag eines Brokers zurueckrufen, ohne Sicherheitspruefung
	 */
	public void auftragCancel(Integer brokerId, Integer auftragId) throws RemoteException, IllegalArgumentException {
		
	}
	
	/**
	 * Einzahlen/auszahlen von einem Broker, ohne Sicherheitspruefung
	 * normaleweise muss es automatisch ausgefuert werden, wenn das Geld zum Tradingkonto des Brokers eingeht
	 * amount kann +/- sein (einzahlen/auszahlen)
	 * einfachheitshalber koennen die Brokers diese Methode selbst aufrufen
	 * wenn sie das machen, dann heisst es das sie das Geld zu/von ihrem Konto ueberweisen
	 */
	public void tradingAccount(Integer brokerId, float amount) throws RemoteException, IllegalArgumentException {
		
	}

	/**
	 * Einzahlen/auszahlen eines Emittens (z.B. Aktien) von einem Broker, ohne Sicherheitspruefung
	 * normaleweise muss es automatisch ausgefuert werden, wenn die Aktien zum Tradingkonto des Brokers eingehen
	 * anzahl kann +/- sein (einzahlen/auszahlen)
	 * einfachheitshalber koennen die Brokers diese Methode selbst aufrufen
	 * wenn sie das machen, dann heisst es das sie die Aktien zu/von ihrem Konto ueberweisen 
	 */
	public void tradingAccount(Integer brokerId, Integer tickerId, Integer anzahl) throws RemoteException, IllegalArgumentException {
		
	}
	
	

	public Report getReport(Integer brokerId) throws RemoteException, IllegalArgumentException {
		return null;
	}

	
	
	/**
	 * Normaleweise sollten Clients die Adresse ihrer Brokers kennen
	 * einfachheitshalber bekommt ein Client diese Adresse mithilfe dieser Methode, z.B. localhost:12001
	 */
	public String getBrokerNetworkAddress(Integer brokerId) throws RemoteException, IllegalArgumentException {
		if ( !brokers.containsKey(brokerId) )
			throw new IllegalArgumentException("Broker with id=" + brokerId + " does not exist");

		return brokers.get(brokerId).getNetworkAddress();
	}

	
	
	
	//UDP-Server stuff
	// noch nicht implementiert
	/**
	 * Thread des Servers
	 */
	private Thread threadUDP;
	
	
	/**
	 * UDP Clients
	 */
	private java.util.TreeMap< Integer /* emittentId */, java.util.TreeMap< Float /* preis */, java.util.ArrayList<Auftrag> > > activeUDPSssions;
	
	/**
	 * Start UDP Server
	 * multicast ware beser, aber ist es problematisch im interner zu implementieren
	 * hier verwendet man unicast
	 */
	private static void startFeedUDP() {
	    try {
	    	DatagramSocket socket = new DatagramSocket(10000);
	    	byte[] buf = new byte[256];

			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);
			
	    }   
	    catch (SocketException e){
	        System.err.println("Socket: " + e.getMessage());
	    }
	    catch (IOException e){
	    	System.err.println("IO: " + e.getMessage());
	    }
	}  

    public static void main(String[] args) {
    	BoerseServer boerse = new BoerseServer();
    	
    	//initial values
    	boerse.emittents.put("AAPL", new Emittent("AAPL", "Apple Inc."));
    	boerse.emittents.put("RDSA", new Emittent("RDSA", "Royal Dutch Shell"));
    	boerse.brokers.put(1, new Broker(1, 0f, "localhost:5001", "Daniil Brokers Co.", "123", "Licenze: AA-001"));
    	boerse.brokers.put(2, new Broker(2, 0f, "localhost:5002", "Zwonek Brokers Co.", "456", "Licenze: AA-002"));
    	boerse.brokers.put(3, new Broker(3, 0f, "localhost:5003", "Khatuna Brokers Co.", "789", "Licenze: AA-003"));
    	boerse.brokers.put(4, new Broker(4, 0f, "localhost:5004", "Ayrat Brokers Co.", "012", "Licenze: AA-004"));

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            Registry registry = LocateRegistry.createRegistry(10001);
            
            BoersePublicAdapter publicAdapter = new BoersePublicAdapter(boerse);
            BoersePublic publicStub =
                    (BoersePublic) UnicastRemoteObject.exportObject(publicAdapter, 0);
            registry.rebind("public", publicStub);
            
            BoerseAdminAdapter adminAdapter = new BoerseAdminAdapter(boerse);
            BoerseAdmin adminStub =
                    (BoerseAdmin) UnicastRemoteObject.exportObject(adminAdapter, 0);
                registry.rebind("adminBoerse", adminStub);

            BoerseClient clientStub =
                    (BoerseClient) UnicastRemoteObject.exportObject(boerse, 0);
            registry.rebind("brokerBoerse", clientStub);

                
                
//            Registry registry = LocateRegistry.getRegistry();
//            registry.rebind(AdminObjectName, adminStub);

      /*
            //start UDP
            boerse.threadUDP = new Thread() {
                public void run() {
                	Boerse.startFeedUDP();
                }
            };

        */
            System.out.println("Die Boerse ist gestartet");
        }
        catch (AccessControlException e) {
            System.err.println("Boerse Start AccessControlException:");
            e.printStackTrace();
        }
        catch (RemoteException e) {
            System.err.println("Boerse Start RemoteException:");
            e.printStackTrace();
        }
        /*catch (java.rmi.AlreadyBoundException e) {
            System.err.println("Boerse Start java.rmi.AlreadyBoundException:");
            e.printStackTrace();
        }*/
    }
}

