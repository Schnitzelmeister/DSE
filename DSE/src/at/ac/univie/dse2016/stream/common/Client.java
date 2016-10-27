package at.ac.univie.dse2016.stream.common;

import java.net.*;
import java.rmi.RemoteException;
import java.io.*;

public class Client implements Serializable {
	
	private static final long serialVersionUID = 100L;
	
	protected Integer id;
	public Integer getId() { return id; }

	/**
	 * fuer ein Boersenmakler ist -1,
	 * fuer Broker ist Id von Boersenmakler, bei dem er arbeitet
	 * fuer Client ist Id von entweder einem Broker, wenn er ueber den Broker arbeitet
	 * 		oder Id von Boersenmakler, ueber den er direkt arbeitet
	 */
	protected Integer parentId;
	public Integer getBrokerId() { return parentId; }

	/**
	 * das Geld auf dem Tradingskonto in EUR
	 */
	protected float kontostand;
	public float getKontostand() { return kontostand; }
	
	protected String name;
	public String getName() { return name; }
	
//	protected Credit credit;
//	public Credit getKredit() { return credit; }


	/**
	 * Alle Auftraege des Clients
	 */
	protected java.util.ArrayList<Auftrag> auftraege;
	public java.util.ArrayList<Auftrag> getAuftraegeList() {
		return auftraege;
	}

	/**
	 * Emittents, die auf dem tradingskonto des Clients sind
	 */
	private java.util.TreeMap<Integer /*emittentId*/, Integer /*anzahl*/> accountEmittents;
	public java.util.TreeMap<Integer /*emittentId*/, Integer /*anzahl*/> getAccountEmittents() {
		return accountEmittents;
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
		this.name = name;
		this.auftraege = new java.util.ArrayList<Auftrag>();
		this.accountEmittents = new java.util.TreeMap<Integer /*emittentId*/, Integer /*anzahl*/>();
	}

}