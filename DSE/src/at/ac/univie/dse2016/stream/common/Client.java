package at.ac.univie.dse2016.stream.common;

import java.io.*;

public class Client implements Serializable {
	
	private static final long serialVersionUID = 100L;
	
	protected Integer id;
	public Integer getId() { return id; }

	public void setId(Integer id){
		this.id = id;
	}

	/**
	 * fuer ein Boersenmakler ist -1,
	 * fuer Broker ist Id von Boersenmakler, bei dem er arbeitet
	 * fuer Client ist Id von entweder einem Broker, wenn er ueber den Broker arbeitet
	 * 		oder Id von Boersenmakler, ueber den er direkt arbeitet
	 */
	protected Integer parentId;
	public Integer getBrokerId() { return parentId; }

	public void setParentId(Integer parentId){
		this.parentId = parentId;
	}



	/**
	 * das Geld auf dem Tradingskonto in EUR
	 */
	protected float kontostand;
	public synchronized float getKontostand() { return kontostand; }
	public synchronized void setKontostand(float diff) { this.kontostand += diff; }


	/**
	 * das Geld, das zu Verfuegung steht in EUR (Kontostand - die Summe der gestellten Auftraege)
	 */
	protected transient float disponibelstand;
	public synchronized float getDisponibelstand() { return disponibelstand; }
	public synchronized void setDisponibelstand(float diff) { this.disponibelstand += diff; }
		
	protected String name;
	public String getName() { return name; }

	public void setName(String name){
		this.name = name;
	}

//	protected Credit credit;
//	public Credit getKredit() { return credit; }


	/**
	 * Aktive Auftraege des Clients
	 */
	protected java.util.TreeMap<Integer, Auftrag> auftraege;
	public java.util.TreeMap<Integer, Auftrag> getAuftraegeList() {
		return auftraege;
	}

	/**
	 * Emittents, die auf dem tradingskonto des Clients sind
	 */
	private java.util.TreeMap<Integer /*emittentId*/, Integer /*anzahl*/> accountEmittents;
	public java.util.TreeMap<Integer /*emittentId*/, Integer /*anzahl*/> getAccountEmittents() {
		return accountEmittents;
	}

	public synchronized Integer getKontostand(Integer tickerId) {
		if (!accountEmittents.containsKey(tickerId))
			return 0;
		return accountEmittents.get(tickerId);
	}

	public synchronized void setKontostand(Integer tickerId, Integer diff) {
		if (!accountEmittents.containsKey(tickerId))
			accountEmittents.put(tickerId, 0);
		accountEmittents.put(tickerId, accountEmittents.get(tickerId) + diff);
	}
	
	/**
	 * Emittents, die auf dem tradingskonto des Clients sind
	 */
	private java.util.TreeMap<Integer /*emittentId*/, Integer /*anzahl*/> disponibleAccountEmittents;
	public java.util.TreeMap<Integer /*emittentId*/, Integer /*anzahl*/> getDisponibleAccountEmittents() {
		return disponibleAccountEmittents;
	}
	
	public synchronized Integer getDisponibelstand(Integer tickerId) {
		if (!disponibleAccountEmittents.containsKey(tickerId))
			return 0;
		return disponibleAccountEmittents.get(tickerId);
	}
	
	public synchronized void setDisponibelstand(Integer tickerId, Integer diff) {
		if (!disponibleAccountEmittents.containsKey(tickerId))
			disponibleAccountEmittents.put(tickerId, 0);
		disponibleAccountEmittents.put(tickerId, disponibleAccountEmittents.get(tickerId) + diff);
	}
	

	/**
	 * FeedMsg Counter
	 */
	//transient protected int iFeedMsgCounter = 0;
	
	/**
	 * Die Tickers, die der Client abfragen will
 	 */
	//transient String[] tickers;

	/*private void SendUDPRequest(Integer status) {
		try {
			DatagramSocket socket = new DatagramSocket(Boerse.FeedUDPPort);
			
			FeedRequest request;
			if (status == 0)
				request = new FeedRequest(this.id, status, tickers);
			else
				request = new FeedRequest(this.id, status, null); 
			
			byte[] sendBuf = new byte[256];
	
			InetAddress address = InetAddress.getByName(serverUDP);
			DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, 
			                                address, 4445);
			socket.send(packet);
			package at.ac.univie.dse2016.stream.common;

			public class Client {

			}

			if (status == 0) {
				
			}
			else {
				
			}
		}
	    catch (SocketException e){
	        System.err.println("Socket: " + e.getMessage());
	    }
	    catch (IOException e){
	    	System.err.println("IO: " + e.getMessage());
	    }

		
	}
	*/
	
	public Client(Integer parentId, String name) {
		this(-1, parentId, 0, name);
	}

	public Client(Integer id, Integer parentId, float kontostand, String name) {
		this.id = id;
		this.parentId = parentId;
		this.kontostand = kontostand;
		this.disponibelstand = kontostand;
		this.name = name;
		this.auftraege = new java.util.TreeMap<Integer, Auftrag>();
		this.accountEmittents = new java.util.TreeMap<Integer /*emittentId*/, Integer /*anzahl*/>();
		this.disponibleAccountEmittents = new java.util.TreeMap<Integer /*emittentId*/, Integer /*anzahl*/>();
	}

}