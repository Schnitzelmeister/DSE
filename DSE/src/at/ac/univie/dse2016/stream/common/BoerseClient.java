package at.ac.univie.dse2016.stream.common;

import java.rmi.RemoteException;
import java.util.Date;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService(name = "BoerseClient", targetNamespace = "http://common.stream.dse2016.univie.ac.at/")
public interface BoerseClient extends BoersePublic {
	
	/**
	 * Auftrag eines Brokers stellen, ohne Sicherheitspruefung
	 */
	@WebMethod(operationName = "auftragAddNew", action = "urn:AuftragAddNew")
	public Integer auftragAddNew(@WebParam(name = "arg0")Integer brokerId, @WebParam(name = "arg1")Auftrag auftrag) throws RemoteException, IllegalArgumentException;

	/**
	 * Auftrag eines Brokers zurueckrufen, ohne Sicherheitspruefung
	 */

	@WebMethod(operationName = "auftragCancel", action = "urn:AuftragCancel")
	public void auftragCancel(@WebParam(name = "arg0")Integer brokerId, @WebParam(name = "arg1") Integer auftragId) throws RemoteException, IllegalArgumentException;
	
	/**
	 * Einzahlen/auszahlen von einem Broker, ohne Sicherheitspruefung
	 * normaleweise muss es automatisch ausgefuert werden, wenn das Geld zum Tradingkonto des Brokers eingeht
	 * amount kann +/- sein (einzahlen/auszahlen)
	 * einfachheitshalber koennen die Brokers diese Methode selbst aufrufen
	 * wenn sie das machen, dann heisst es das sie das Geld zu/von ihrem Konto ueberweisen
	 */
	@WebMethod(operationName = "tradingAccount", action = "urn:TradingAccount")
	public void tradingAccount(@WebParam(name = "arg0")Integer brokerId, @WebParam(name = "arg1")float amount) throws RemoteException, IllegalArgumentException;

	/**
	 * Einzahlen/auszahlen eines Emittens (z.B. Aktien) von einem Broker, ohne Sicherheitspruefung
	 * normaleweise muss es automatisch ausgefuert werden, wenn die Aktien zum Tradingkonto des Brokers eingehen
	 * anzahl kann +/- sein (einzahlen/auszahlen)
	 * einfachheitshalber koennen die Brokers diese Methode selbst aufrufen
	 * wenn sie das machen, dann heisst es das sie die Aktien zu/von ihrem Konto ueberweisen 
	 */
	@WebMethod(operationName = "tradingAccount2", action = "urn:TradingAccount2")
	public void tradingAccount(@WebParam(name = "arg0")Integer brokerId, @WebParam(name = "arg1")Integer tickerId, @WebParam(name = "arg2")Integer anzahl) throws RemoteException, IllegalArgumentException;
	
	/**
	 * Get current State des Brokers, ohne Sicherheitspruefung
	 */
	@WebMethod(operationName = "getState", action = "urn:GetState")
	public Broker getState(@WebParam(name = "arg0")Integer brokerId) throws RemoteException, IllegalArgumentException;

	@WebMethod(operationName = "getAuftraege", action = "urn:GetAuftraege")
	public java.util.TreeSet<Auftrag> getAuftraege(@WebParam(name = "arg0")Integer brokerId) throws RemoteException, IllegalArgumentException;

	@WebMethod(operationName = "getTransaktionen", action = "urn:GetTransaktionen")
	public java.util.TreeSet<Transaction> getTransaktionen(@WebParam(name = "arg0")Integer brokerId) throws RemoteException, IllegalArgumentException;
	
	@WebMethod(operationName = "getTransaktionen2", action = "urn:GetTransaktionen2")
	public java.util.TreeSet<Transaction> getTransaktionen(@WebParam(name = "arg0")Integer brokerId, @WebParam(name = "arg1")Date afterDate) throws RemoteException, IllegalArgumentException;

	@WebMethod(operationName = "getReport", action = "urn:GetReport")
	public Report getReport(@WebParam(name = "arg0")Integer brokerId) throws RemoteException, IllegalArgumentException;
}