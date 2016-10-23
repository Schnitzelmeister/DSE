package at.ac.univie.dse2016.stream;

import java.net.*;
import java.io.*;

public class Client implements Serializable {
	protected Integer id;
	public Integer getId() { return id; }
	
	/**
	 * BoersenMakler des Clients
	 */
	protected BrokerFirma boersenMakler;
	
	/**
	 * fuer ein Boersenmakler ist -1,
	 * fuer Broker ist Id von Boersenmakler, bei dem er arbeitet
	 * fuer Client ist Id von entweder einem Broker, wenn er ueber den Broker arbeitet
	 * 		oder Id von Boersenmakler, ueber den er direkt arbeitet
	 */
	protected Integer parentId;
	public Integer getParentId() { return parentId; }

	/**
	 * das Geld auf dem Tradingskonto in EUR
	 */
	protected float kontostand;
	public float getKontostand() { return kontostand; }
	
	protected String name;
	public String getName() { return name; }
	
	protected Credit credit;
	public Credit getKredit() { return credit; }

	/**
	 * Emittents, die auf der Boerse gekauft/verkauft werden duerfen
	 */
	protected static final java.util.Collection<Emittent> emittents = null;// = Boerse.getEmitentsList();;

	/**
	 * Alle Auftraege des Clients
	 */
	protected java.util.ArrayList<Auftrag> auftraege;


	/**
	 * FeedMsg Counter
	 */
	transient protected int iFeedMsgCounter = 0;
	
	/**
	 * Die Methode muss aufgerufen werden, wenn das Geld auf das Tradingskonto eingeht
	 * z.B. der Client ueberweist das Geld von seinem Konto zum Tradingskonto
	 * oder der Geldgeber gibt einen Kredit dem Client
	 */
	public void Einzahlen(float amount) {
		synchronized(this) {
			kontostand += amount;
		}
	}
	
	/**
	 * Die Methode muss aufgerufen werden, wenn das Geld vom Tradingskonto ausgeht
	 * z.B. der Client ueberweist das Geld vom Tradingskonto zu seinem Konto
	 * oder der Client bezahlt die Zinsen oder Kredit dem Geldgeber
 	 */
	public void Auszahlen(float amount) {
		synchronized(this) {
			kontostand -= amount;
		}
	}

	public void AuftragAddNew(Auftrag auftrag) {
		if (boersenMakler == null)
			throw new IllegalArgumentException("You can ordering only with your personal Broker");
		
		//boersenMakler.AuftragAddNew(this.getId(), auftrag);
	}

	public void AuftragCancel(Auftrag auftrag) {
		if (boersenMakler == null)
			throw new IllegalArgumentException("You can ordering only with your personal Broker");
		
		//boersenMakler.AuftragCancel(this.getId(), auftrag);
	}

	
	public void GetFeed() {
		
	}
	
	/**
	 * Die Tickers, die der Client abfragen will
 	 */
	String[] tickers;
	
	/**
	 * Der Name des UDP-Servers
 	 */
	String serverUDP;

	private void SendUDPRequest(Integer status) {
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

	public Client(Integer id, Integer parentId, float kontostand, String name, BrokerFirma boersenMakler) {
		this.id = id;
		this.parentId = parentId;
		this.kontostand = kontostand;
		this.name = name;
		this.boersenMakler = boersenMakler;
	}

}
