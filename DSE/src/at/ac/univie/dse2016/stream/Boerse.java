package at.ac.univie.dse2016.stream;

import java.net.*;
import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public final class Boerse implements BoerseAdmin {
	
	public Boerse() {
		super();
	}

	/**
	 * UDP Port des Feeddata Servers
	 */
	public static final Integer FeedUDPPort = 10001;

	/**
	 * TCP Port des Admins Servers und seiner Name vom RemoteObject (fuer Registry.Bind)
	 */
	public static final Integer AdminTCPPort = 10002;
	public static final String AdminObjectName = "admin";

	/**
	 * TCP Port des Clients Servers und seiner Name vom RemoteObject (fuer Registry.Bind)
	 */
	public static final Integer ClientTCPPort = 10003;
	public static final String ClientObjectName = "client";

	
	/**
	 * Status der Boerse, Open, Closed oder Error
	 */
	private BoerseStatus status = BoerseStatus.Closed;

	public BoerseStatus Status() { return status; }

	/**
	 * Emittents, die auf der Boerse gekauft/verkauft werden duerfen
	 */
	private java.util.TreeMap<String, Emittent> emittents;

	/**
	 * Boersenmakler, die auf der Boerse arbeiten duerfen
	 */
	private java.util.TreeMap<Integer, BrokerFirma> clients;

	/**
	 * Alle Auftraege der Boerse, die aktuell sind
	 */
	private java.util.TreeMap< Integer /* emittentId */, java.util.TreeMap< Float /* preis */, java.util.ArrayList<Auftrag> > > activeAuftraege;

	/**
	 * Alle Auftraege der Boerse
	 */
	private java.util.TreeMap< Integer /* clientId */, java.util.ArrayList<Auftrag> > auftraegeLog;
	
	/**
	 * AddNew Emitent, Admin's Function
	 */
	public Integer emitentAddNew(Emittent emitent) {
		if ( emittents.containsKey(emitent.getTicker()) )
			throw new IllegalArgumentException("Ticker " + emitent.getTicker() + " exists");
		
		Emittent actual_emitent = new Emittent(emittents.size() + 1, emitent.getTicker(), emitent.getName());
		emittents.put(actual_emitent.getTicker(), actual_emitent);
		return actual_emitent.getId();
	}

	/**
	 * Edit Emitent, Admin's Function
	 */
	public void emitentEdit(Emittent emitent) {
		if ( !emittents.containsKey(emitent.getTicker()) )
			throw new IllegalArgumentException("Ticker " + emitent.getTicker() + " does not exist");
		
		Emittent actual_emitent = emittents.get(emitent.getTicker());
		if (actual_emitent.getId() != emitent.getId())
			throw new IllegalArgumentException("Ticker " + emitent.getTicker() + " has other Id");
		
		actual_emitent.setName(emitent.getName());
	}
	
	/**
	 * Remove Emitent, Admin's Function
	 */
	public void emitentLock(Emittent emitent) {
		if ( !emittents.containsKey(emitent.getTicker()) )
			throw new IllegalArgumentException("Ticker " + emitent.getTicker() + " does not exist");
		
		Emittent actual_emitent = emittents.get(emitent.getTicker());
		if (actual_emitent.getId() != emitent.getId())
			throw new IllegalArgumentException("Ticker " + emitent.getTicker() + " has other Id");
		
		emittents.remove(emitent.getTicker());
	}

	/**
	 * Get all Emitents, Admin's Function
	 */
	public java.util.Collection<Emittent> getEmitentsList() {
		return emittents.values();
	}
	
	/**
	 * AddNew BoersenMakler, Admin's Function
	 */
	public Integer BoersenMaklerAddNew(BrokerFirma makler) {
		if ( clients.containsKey(makler.getId()) )
			throw new IllegalArgumentException("Client " + String.valueOf(makler.getId()) + " exists");
		
		BrokerFirma actual_makler = new BrokerFirma(clients.size() + 1, makler.getKontostand(), makler.getName(), makler.getPhone(), makler.getLicense());
		clients.put(actual_makler.getId(), actual_makler);
		return actual_makler.getId();
	}

	/**
	 * Edit BoersenMakler, Admin's Function
	 */
	public void BoersenMaklerEdit(BrokerFirma makler) {
		if ( !clients.containsKey(makler.getId()) )
			throw new IllegalArgumentException("Client " + String.valueOf(makler.getId()) + " does not exist");
		
		clients.put( makler.getId(), makler );
	}
	
	/**
	 * Remove BoersenMakler, Admin's Function
	 */
	public void BoersenMaklerLock(BrokerFirma makler) {
		if ( !clients.containsKey(makler.getId()) )
			throw new IllegalArgumentException("Client " + String.valueOf(makler.getId()) + " does not exist");
		
		clients.remove(makler.getId());
	}
	
	/**
	 * Get all BoersenMakler, Admin's Function
	 */
	public java.util.Collection<BrokerFirma> getClientsList() {
		return clients.values();
	}
	
	/**
	 * Schliesst die Boerse ab, normaleweise am Ende des Tages, Admin's Function oder ScheduleJob
	 */
	public void Close() {
		status = BoerseStatus.Closed;
	}

	/**
	 * Oeffnet die Boerse, Admin's Function oder ScheduleJob
	 */
	public void Open() {
		status = BoerseStatus.Open;
	}
	
	/**
	 * Auftrag eines BoersenMakleres stellen, ohne Sicherheitspruefung
	 */
	public Integer AuftragAddNew(Integer clientId, Auftrag auftrag) {
		//auftraegeLog;
		//activeAuftraege;
		return -1;
	}

	/**
	 * Auftrag eines BoersenMakleres zurueckrufen, ohne Sicherheitspruefung
	 */
	public void AuftragCancel(Integer clientId, Auftrag auftrag) {
		
	}

	public Report GetReport(Integer clientId) {
		return null;
	}

	
	
	
	
	//UDP-Server stuff
	/**
	 * Auftrag eines BoersenMakleres zurueckrufen, ohne Sicherheitspruefung
	 */
	private java.util.TreeMap< Integer /* emittentId */, java.util.TreeMap< Float /* preis */, java.util.ArrayList<Auftrag> > > activeUDPSssions;
	
	/**
	 * Auftrag eines BoersenMakleres zurueckrufen, ohne Sicherheitspruefung
	 */
	public void GetFeed(Integer clientId, String[] tickers) {
		
	}

	public void StartFeedUDP() throws IOException {
		DatagramSocket socket = new DatagramSocket(FeedUDPPort);
		byte[] buf = new byte[256];

	    try {
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
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
        	Boerse boerse = new Boerse();
            BoerseAdmin boerseAdmin = boerse;
            BoerseAdmin adminStub =
                (BoerseAdmin) UnicastRemoteObject.exportObject(boerseAdmin, AdminTCPPort);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(AdminObjectName, adminStub);

            //nicht implementiert
/*            
            BoerseClient boerseClient = boerse;
            BoerseClient clientStub =
                (BoerseClient) UnicastRemoteObject.exportObject(boerseClient, ClientTCPPort);
            registry.rebind(ClientObjectName, clientStub);
*/
            
            System.out.println("Die Boerse ist gestartet");
        } catch (Exception e) {
            System.err.println("Boerse Start exception:");
            e.printStackTrace();
        }
    }
}
