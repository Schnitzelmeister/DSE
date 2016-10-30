package at.ac.univie.dse2016.stream.boerse;

import java.net.*;
import java.io.*;
import java.security.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import at.ac.univie.dse2016.stream.common.*;

public final class BoerseServer implements BoerseAdmin, BoerseClient {
		
	public BoerseServer() {
		super();
		status = BoerseStatus.Closed;
		auftragId = 0;
		emittents = new java.util.TreeMap<String, Emittent>();
		brokers = new java.util.TreeMap<Integer, Broker>();
		marketPrices = new java.util.TreeMap<Integer, Float>();
		emittentSections = new java.util.TreeMap< Integer /* emittentId */, EmittentSection >();
		activeAuftraege = new java.util.TreeMap< Integer /* clientId */, java.util.TreeMap< Integer /* auftragId */, java.util.ArrayList<Auftrag> > >();
		auftraegeLog = new java.util.TreeMap< Integer /* clientId */, java.util.TreeMap< Integer /* auftragId */, java.util.ArrayList<Auftrag> > >();
		lastCommitedAuftraege = new java.util.TreeMap< Integer /* emittentId */, Auftrag >();
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
	 * Die Preise, mit denen den letzten Auftrag ueberwiesen wurden
	 */
	private java.util.TreeMap<Integer, Float> marketPrices;

	/**
	 * Brokers, die auf der Boerse arbeiten duerfen
	 */
	private java.util.TreeMap<Integer, Broker> brokers;

	/**
	 * Emittent Sections
	 */
	private java.util.TreeMap< Integer /* emittentId */, EmittentSection > emittentSections;

	/**
	 * Letzte ausgefuehrte Auftraege
	 */
	private java.util.TreeMap< Integer /* emittentId */, Auftrag > lastCommitedAuftraege;

	
	/**
	 * Alle Auftraege der Boerse, die aktuell sind
	 */
	private java.util.TreeMap< Integer /* clientId */, java.util.TreeMap< Integer /* auftragId */, java.util.ArrayList<Auftrag> > > activeAuftraege;

	/**
	 * Alle Auftraege der Boerse
	 */
	private java.util.TreeMap< Integer /* clientId */, java.util.TreeMap< Integer /* auftragId */, java.util.ArrayList<Auftrag> > > auftraegeLog;

	/**
	 * Counter fuer Auftraege
	 */
	private Integer auftragId;
	
	

	/**
	 * AddNew Emittent, Admin's Function
	 */
	public Integer emittentAddNew(Emittent emittent) throws RemoteException, IllegalArgumentException {
		if ( emittents.containsKey(emittent.getTicker()) )
			throw new IllegalArgumentException("Ticker " + emittent.getTicker() + " exists");
		
		Emittent actualEmittent;
		
		synchronized(emittents) {
			actualEmittent = new Emittent(emittents.size() + 1, emittent.getTicker(), emittent.getName());
			emittents.put(actualEmittent.getTicker(), actualEmittent);
			marketPrices.put(actualEmittent.getId(), -1f);
			emittentSections.put(actualEmittent.getId(), new EmittentSection() );
		}
		return actualEmittent.getId();
	}

	/**
	 * Edit Emittent, Admin's Function
	 */
	public synchronized void emittentEdit(Emittent emittent) throws RemoteException, IllegalArgumentException {
		if ( !emittents.containsKey(emittent.getTicker()) )
			throw new IllegalArgumentException("Ticker " + emittent.getTicker() + " does not exist");
		
		Emittent actual_emitent = emittents.get(emittent.getTicker());
		
		if (actual_emitent.getId() != emittent.getId())
			throw new IllegalArgumentException("Ticker " + emittent.getTicker() + " has other Id");
	
		actual_emitent.setName(emittent.getName());
	}
	
	/**
	 * Lock Emittent, not Remove, Admin's Function
	 */
	public void emittentLock(Emittent emittent) throws RemoteException, IllegalArgumentException {
		if ( !emittents.containsKey(emittent.getTicker()) )
			throw new IllegalArgumentException("Ticker " + emittent.getTicker() + " does not exist");
		
		Emittent actualEmittent = emittents.get(emittent.getTicker());
		if (actualEmittent.getId() != emittent.getId())
			throw new IllegalArgumentException("Ticker " + emittent.getTicker() + " has other Id");

		synchronized(emittents) {
			marketPrices.remove( actualEmittent.getId() );
			activeAuftraege.remove( actualEmittent.getId() );
			emittents.remove( actualEmittent.getTicker() );
			emittentSections.remove( actualEmittent.getId() );
		}
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
		
		Broker actualBroker;
		synchronized(brokers) {
			actualBroker = new Broker(brokers.size() + 1, broker.getKontostand(), broker.getName(), broker.getPhone(), broker.getLicense(), broker.getNetworkAddress());
			brokers.put(actualBroker.getId(), actualBroker);
		}
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
	 * Lock Broker, not Remove, Admin's Function
	 */
	public void brokerLock(Broker broker) throws RemoteException, IllegalArgumentException {
		if ( !brokers.containsKey(broker.getId()) )
			throw new IllegalArgumentException("Client " + String.valueOf(broker.getId()) + " does not exist");
		
		synchronized(brokers) {
			brokers.remove(broker.getId());
		}
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
	public synchronized void Close() throws RemoteException {
		status = BoerseStatus.Closed;
		stopFeedUDP();
	}

	/**
	 * Oeffnet die Boerse, Admin's Function oder ScheduleJob
	 */
	public synchronized void Open() throws RemoteException {
        //start UDP
        threadUDP = new Thread() {
            public void run() {
            	startFeedUDP();
            }
        };

		status = BoerseStatus.Open;
	}
	
	
	
	
	
	
	
	/**
	 * Get current State des Brokers
	 */
	public Broker getState(Integer brokerId) throws RemoteException, IllegalArgumentException {
		if ( !brokers.containsKey(brokerId) )
			throw new IllegalArgumentException("Broker with id=" + brokerId + " does not exist");

		return brokers.get(brokerId);
	}
	
	/**
	 * Auftrag eines Brokers stellen
	 */
	public Integer auftragAddNew(Integer brokerId, Auftrag auftrag) throws RemoteException, IllegalArgumentException {
		if ( !brokers.containsKey(brokerId) )
			throw new IllegalArgumentException("Broker with id=" + brokerId + " does not exist");

		if ( !emittents.containsKey(auftrag.getTicker()) )
			throw new IllegalArgumentException("Ticker " + auftrag.getTicker() + " does not exist");

		Integer tickerId = emittents.get( auftrag.getTicker() ).getId();
		float marketPrice = marketPrices.get(tickerId);
		boolean buy = auftrag.getKaufen();
		
		Broker broker = this.brokers.get(brokerId);
		synchronized(broker) {
			java.util.TreeMap<Integer, Integer> brokerEmittents = broker.getAccountEmittents();
			
			if (buy) {
				if (broker.getKontostand() < marketPrice * auftrag.getAnzahl())
					throw new IllegalArgumentException("Not enough money");
			}
			else {
				if (!brokerEmittents.containsKey(tickerId))
					throw new IllegalArgumentException("Nothing to sell");
				
				if (brokerEmittents.get(tickerId) < auftrag.getAnzahl())
					throw new IllegalArgumentException("Not enough amount of the Emittent");
			}

			synchronized(auftragId) {
				auftrag.setId( auftragId++ );
			}
			
			//find Emittent Section
			EmittentSection emittentSection = emittentSections.get(tickerId);

			int anzahl = auftrag.getAnzahl();
			float bedingung = auftrag.getBedingung();
			//ohne Bedingung, einfach akzeptiren alle Auftraege, die einen niedriegsten/grossten Preis fuer diesen kauf/verkauf Auftrag stehen
			boolean commitTransaction = (bedingung == -1);
			
			//mit Bedingung, suchen ob es passende Auftraege gibt 
			if (bedingung > 0) {
				
				if (buy)
					commitTransaction = (emittentSection.sell.firstKey() <= bedingung);
				else
					commitTransaction = (emittentSection.buy.firstKey() >= bedingung);
				
				//es gibt keine passende Auftraege, dann muss man einfach den Auftrag in der Sektion hinzufuegen 
				if (!commitTransaction) {
					
					java.util.TreeMap<Float, java.util.ArrayList<Auftrag> > _map;
					if (buy)
						_map = emittentSection.buy;
					else
						_map = emittentSection.sell;
			
					if (!_map.containsKey(bedingung))
						_map.put(bedingung, new java.util.ArrayList<Auftrag>());

					_map.get(bedingung).add(auftrag);
				}
			}
			
			//commit Transaction
			if (commitTransaction) {
				java.util.TreeMap<Float, java.util.ArrayList<Auftrag> > _map;
				if (buy)
					_map = emittentSection.sell;
				else
					_map = emittentSection.buy;
				
				for(java.util.Map.Entry<Float, java.util.ArrayList<Auftrag> > e : _map.entrySet()) {
					for(Auftrag a : e.getValue()) {
						
						//genug geld, aktien bei beiden ???
						
						if (bedingung == -1 || a.getBedingung() < auftrag.getBedingung()) {
							//commit a ganz
							if (a.getAnzahl() <= anzahl) {
								anzahl -= a.getAnzahl();
							}
							//commit a teilweise
							else {
								anzahl = 0;
							}
							
							//emittentSection.setCommitedAuftrage(auftrag.getId(), a.getId());
						}
						
						//alles ist erledigt
						if (anzahl == 0);
							return auftrag.getId();
					}					
				}
				
				sendFeedMsg(new FeedMsg(msgCounter.getAndDecrement(), 0/*id*/, tickerId, true, anzahl, 0f/*preis*/, /*status*/AuftragStatus.Accepted));
				
			}

			
		}
		//auftraegeLog;
		//;
		
		return auftrag.getId();
	}

	/**
	 * Auftrag eines Brokers zurueckrufen
	 */
	public void auftragCancel(Integer brokerId, Integer auftragId) throws RemoteException, IllegalArgumentException {
		
	}
	
	/**
	 * Einzahlen/auszahlen von einem Broker
	 * normaleweise muss es automatisch ausgefuert werden, wenn das Geld zum Tradingkonto des Brokers eingeht
	 * amount kann +/- sein (einzahlen/auszahlen)
	 * einfachheitshalber koennen die Brokers diese Methode selbst aufrufen
	 * wenn sie das machen, dann heisst es das sie das Geld zu/von ihrem Konto ueberweisen
	 */
	public void tradingAccount(Integer brokerId, float amount) throws RemoteException, IllegalArgumentException {
		if ( !brokers.containsKey(brokerId) )
			throw new IllegalArgumentException("Broker with id=" + brokerId + " does not exist");

		Broker broker = this.brokers.get(brokerId);
		synchronized(broker) {
			if (amount < 0 && broker.getKontostand() < -amount)
				throw new IllegalArgumentException("Not enough money");
			broker.setKontostand(amount);
		}

	}

	/**
	 * Einzahlen/auszahlen eines Emittens (z.B. Aktien) von einem Broker
	 * normaleweise muss es automatisch ausgefuert werden, wenn die Aktien zum Tradingkonto des Brokers eingehen
	 * anzahl kann +/- sein (einzahlen/auszahlen)
	 * einfachheitshalber koennen die Brokers diese Methode selbst aufrufen
	 * wenn sie das machen, dann heisst es das sie die Aktien zu/von ihrem Konto ueberweisen 
	 */
	public void tradingAccount(Integer brokerId, Integer tickerId, Integer anzahl) throws RemoteException, IllegalArgumentException {
		if ( !brokers.containsKey(brokerId) )
			throw new IllegalArgumentException("Broker with id=" + brokerId + " does not exist");

		Broker broker = this.brokers.get(brokerId);
		synchronized(broker) {
			java.util.TreeMap<Integer, Integer> brokerEmittents = broker.getAccountEmittents();
			if (brokerEmittents.containsKey(tickerId)) {
				if (anzahl < 0 && brokerEmittents.get(tickerId) < -anzahl)
					throw new IllegalArgumentException("Not enough amount of emittent");
				
				brokerEmittents.put(tickerId, brokerEmittents.get(tickerId) + anzahl);
			}
			else {
				if (anzahl < 0)
					throw new IllegalArgumentException("Not enough amount of emittent");
				
				brokerEmittents.put(tickerId, anzahl);
			}
		}
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
	/**
	 * UDP-Thread des Servers
	 */
	private Thread threadUDP;
	
	/**
	 * UDP-Socket
	 */
	private DatagramSocket socketUDP;
	
	/**
	 * UDP Sessions
	 */
	private java.util.TreeMap< Integer, UDPSession > activeUDPSessions;

	/**
	 * Counters
	 */
	private AtomicInteger msgCounter;
	private AtomicInteger sessionCounter;

	/**
	 * Start UDP Server
	 * multicast waere beser, aber ist es problematisch im internet zu implementieren
	 * hier verwendet man unicast
	 */
	private void startFeedUDP() {
		activeUDPSessions = new java.util.TreeMap< Integer, UDPSession >();
		msgCounter.set(0);
		sessionCounter.set(0);
		
	    try {
	    	socketUDP = new DatagramSocket(10000);
	    	byte[] buf = new byte[1024];
	    	
	    	do {
				DatagramPacket requestPacket = new DatagramPacket(buf, buf.length);
				socketUDP.receive(requestPacket);
				
				
				//process Request async
				new Thread(new java.lang.Runnable() {
					private DatagramPacket requestPacket;
					   
				    public Runnable init(DatagramPacket requestPacket) {
				    	this.requestPacket = requestPacket;
				    	return this;
				    }
				    
				    @Override
				    public void run() {
				    	processFeedRequest(requestPacket);
				    }
				}.init(requestPacket)).start();
				   
				//processFeedRequest(request.getData(), request.getAddress(), request.getPort());
				
	    	} while (true);
	    }   
	    catch (SocketException e){
	        System.err.println("Socket: " + e.getMessage());
	    }
	    catch (IOException e){
	    	System.err.println("IO: " + e.getMessage());
	    }
	}

	/**
	 * Stop UDP Server
	 */
	private void stopFeedUDP() {
		
		threadUDP.interrupt();
		threadUDP = null;
		
		activeUDPSessions.clear();
		activeUDPSessions = null;
		
	    try {
	    	socketUDP.close();
	    	socketUDP = null;
			
	    }   
	    catch (Exception e){
	    	System.err.println("ex: " + e.getMessage());
	    }
	}

	/**
	 * Send UDP Feed Message to all Users
	 */
	private void sendFeedMsg(FeedMsg msg) {
	    try {
		    ByteArrayOutputStream bos = new ByteArrayOutputStream();
		    ObjectOutput out = new ObjectOutputStream(bos);
		    out.write((byte)1);
	    	out.writeObject(msg);
	    	
	    	//vielleicht not thread safe, testing later 
	    	for ( java.util.Map.Entry<Integer, UDPSession> se : emittentSections.get(msg.getTickerId()).activeUDPSessions.entrySet() ) {
	    		UDPSession s = se.getValue();
	    		
	    		//remove old Sessions, wenn they are 20 seconds not active
	    		java.util.Calendar calendar = java.util.Calendar.getInstance();
	    		calendar.add(java.util.Calendar.SECOND, 20);
	    		if ( s.lastConnectionTime.before(calendar.getTime()) ) {
	    			emittentSections.get(msg.getTickerId()).activeUDPSessions.remove(se.getKey());
	    		}
	    		else {
	    		
					//Send FeedMsg async
					new Thread(new java.lang.Runnable() {
						private UDPSession session;
						private ByteArrayOutputStream bos;
						   
					    public Runnable init(UDPSession session, ByteArrayOutputStream bos) {
					    	this.session = session;
					    	this.bos = bos;
					    	return this;
					    }
					    
					    @Override
					    public void run() {
					    	sendUDPMsg(session, bos);
					    }
					}.init(s, bos)).start();
	    		}
	    	}
	    	
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
	 * Send UDP DatagramPacket for UDPSession
	 */
	private void sendUDPMsg(UDPSession session, ByteArrayOutputStream bos) {
	    try {
	    	DatagramPacket reply = new DatagramPacket(bos.toByteArray(), bos.size(),
	    			session.address, session.port);
	    	
	    	socketUDP.send(reply);
	    }   
	    catch (SocketException e){
	        System.err.println("Socket: " + e.getMessage());
	    }
	    catch (IOException e){
	    	System.err.println("IO: " + e.getMessage());
	    }
	}
		
	/**
	 * Process incoming FeedRequest
	 */
	private void processFeedRequest(DatagramPacket requestPacket) {
		try {
		    ByteArrayInputStream bis = new ByteArrayInputStream(requestPacket.getData());
		    ObjectInput in = new ObjectInputStream(bis);
		    FeedRequest feedRequest = (FeedRequest) in.readObject();
		    in.close();
		    bis.close();

		    Integer sessionId = feedRequest.getSessionId();
		    boolean newSession = !this.activeUDPSessions.containsKey(sessionId);
		    UDPSession sessionUDP;
		    
		    if (newSession) {
		    	sessionId = sessionCounter.getAndIncrement();
		    	sessionUDP = new UDPSession(feedRequest.getEmittentIds(), requestPacket.getAddress(), requestPacket.getPort());
		    	
		    	this.activeUDPSessions.put( sessionId, sessionUDP );
		    	
		    	for (Integer emittentId : feedRequest.getEmittentIds()) {
		    		this.emittentSections.get(emittentId).activeUDPSessions.put(sessionId, sessionUDP);
		    	}
		    }
		    else {
		    	sessionUDP = this.activeUDPSessions.get(sessionId);
	
		    	
		    	//correct existend Request, wenn another comes !!!
		    	
		    	
		    	//update access time
		    	sessionUDP.lastConnectionTime = java.util.Calendar.getInstance().getTime();
		    }
		    
		    //send last Market prices to client
		    if (newSession) {
			    ByteArrayOutputStream bos = new ByteArrayOutputStream();
			    ObjectOutput out = new ObjectOutputStream(bos);
			    out.write((byte)lastCommitedAuftraege.size());
			    for (java.util.Map.Entry<Integer, Auftrag> ae : lastCommitedAuftraege.entrySet()) {
			    	Auftrag a = ae.getValue();
			    	FeedMsg msg = new FeedMsg(msgCounter.getAndDecrement(), a.getId(), ae.getKey(), a.getKaufen(), a.getAnzahl(), this.marketPrices.get(ae.getKey()), a.getStatus());
			    	out.writeObject(msg);
			    }
	
		    	DatagramPacket reply = new DatagramPacket(bos.toByteArray(), bos.size(),
		    			sessionUDP.address, sessionUDP.port);
		    	
		    	socketUDP.send(reply);

		    	out.close();
			    bos.close();
		    }
		}
	    catch (SocketException e){
	        System.err.println("Socket: " + e.getMessage());
	    }
	    catch (IOException e){
	    	System.err.println("IO: " + e.getMessage());
	    }
	    catch (Exception e){
	    	System.err.println(": " + e.getMessage());
	    }
	}
	
	
	
	
	
	
	

    public static void main(String[] args) {
    	BoerseServer boerse = new BoerseServer();
    	
    	//initial values
    	boerse.emittents.put("AAPL", new Emittent("AAPL", "Apple Inc."));
    	boerse.emittents.put("RDSA", new Emittent("RDSA", "Royal Dutch Shell"));
    	boerse.brokers.put(1, new Broker(1, 0f, "Daniil Brokers Co.", "localhost:5001", "123", "Licenze: AA-001"));
    	boerse.brokers.put(2, new Broker(2, 0f, "Zvonek Brokers Co.", "localhost:5002", "456", "Licenze: AA-002"));
    	boerse.brokers.put(3, new Broker(3, 0f, "Ayrat Brokers Co.", "localhost:5003", "012", "Licenze: AA-003"));

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

