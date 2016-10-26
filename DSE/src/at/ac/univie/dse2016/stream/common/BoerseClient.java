package at.ac.univie.dse2016.stream.common;

import java.rmi.RemoteException;


public interface BoerseClient extends BoersePublic {
	
	/**
	 * Auftrag eines Brokers stellen, ohne Sicherheitspruefung
	 */
	public Integer auftragAddNew(Integer brokerId, Auftrag auftrag) throws RemoteException, IllegalArgumentException;

	/**
	 * Auftrag eines Brokers zurueckrufen, ohne Sicherheitspruefung
	 */
	public void auftragCancel(Integer brokerId, Integer auftragId) throws RemoteException, IllegalArgumentException;
	
	/**
	 * Einzahlen/auszahlen von einem Broker, ohne Sicherheitspruefung
	 * normaleweise muss es automatisch ausgefuert werden, wenn das Geld zum Tradingkonto des Brokers eingeht
	 * amount kann +/- sein (einzahlen/auszahlen)
	 * einfachheitshalber koennen die Brokers diese Methode selbst aufrufen
	 * wenn sie das machen, dann heisst es das sie das Geld zu/von ihrem Konto ueberweisen
	 */
	public void tradingAccount(Integer brokerId, float amount) throws RemoteException, IllegalArgumentException;

	/**
	 * Einzahlen/auszahlen eines Emittens (z.B. Aktien) von einem Broker, ohne Sicherheitspruefung
	 * normaleweise muss es automatisch ausgefuert werden, wenn die Aktien zum Tradingkonto des Brokers eingehen
	 * anzahl kann +/- sein (einzahlen/auszahlen)
	 * einfachheitshalber koennen die Brokers diese Methode selbst aufrufen
	 * wenn sie das machen, dann heisst es das sie die Aktien zu/von ihrem Konto ueberweisen 
	 */
	public void tradingAccount(Integer brokerId, Integer tickerId, Integer anzahl) throws RemoteException, IllegalArgumentException;
	
	/**
	 * Get current State des Brokers, ohne Sicherheitspruefung
	 */
	public Broker getState(Integer brokerId) throws RemoteException, IllegalArgumentException;
	
	public Report getReport(Integer brokerId) throws RemoteException, IllegalArgumentException;
}