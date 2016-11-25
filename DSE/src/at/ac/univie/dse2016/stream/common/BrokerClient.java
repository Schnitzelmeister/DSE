package at.ac.univie.dse2016.stream.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;


public interface BrokerClient extends Remote {
	
	/**
	 * Auftrag eines Clients stellen, ohne Sicherheitspruefung
	 */
	public Integer auftragAddNew(Integer clientId, Auftrag auftrag) throws RemoteException, IllegalArgumentException;

	/**
	 * Auftrag eines Clients zurueckrufen, ohne Sicherheitspruefung
	 */
	public void auftragCancel(Integer clientId, Integer auftragId) throws RemoteException, IllegalArgumentException;

	/**
	 * Get current State des Clients, ohne Sicherheitspruefung
	 */
	public Client getState(Integer clientId) throws RemoteException, IllegalArgumentException;

	public java.util.TreeSet<Auftrag> getAuftraege(Integer clientId) throws RemoteException, IllegalArgumentException;

	public java.util.TreeSet<Transaction> getTransaktionen(Integer clientId) throws RemoteException, IllegalArgumentException;
	public java.util.TreeSet<Transaction> getTransaktionen(Integer clientId, Date afterDate) throws RemoteException, IllegalArgumentException;

	/**
	 * Einzahlen/auszahlen von einem Client
	 * normaleweise muss es automatisch ausgefuert werden, wenn das Geld zum Tradingkonto des Clients eingeht
	 * amount kann +/- sein (einzahlen/auszahlen)
	 * einfachheitshalber koennen die Clienten diese Methode selbst aufrufen
	 * wenn sie das machen, dann heisst es das sie das Geld zu/von ihrem Konto ueberweisen
	 */
	public void tradingAccount(Integer clientId, float amount) throws RemoteException, IllegalArgumentException;

	/**
	 * Einzahlen/auszahlen eines Emittens (z.B. Aktien) von einem Client
	 * normaleweise muss es automatisch ausgefuert werden, wenn die Aktien zum Tradingkonto des Clients eingehen
	 * anzahl kann +/- sein (einzahlen/auszahlen)
	 * einfachheitshalber koennen die Clienten diese Methode selbst aufrufen
	 * wenn sie das machen, dann heisst es das sie die Aktien zu/von ihrem Konto ueberweisen 
	 */
	public void tradingAccount(Integer clientId, Integer tickerId, Integer anzahl) throws RemoteException, IllegalArgumentException;
	
	
	public Report getReport(Integer clientId) throws RemoteException, IllegalArgumentException;
}