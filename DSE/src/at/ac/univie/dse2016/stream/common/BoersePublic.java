package at.ac.univie.dse2016.stream.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService(name = "BoersePublic", targetNamespace = "http://common.stream.dse2016.univie.ac.at/")
public interface BoersePublic extends Remote {
	
	/**
	 * Status der Boerse, Open, Closed oder Error
	 */
	@WebMethod(operationName = "getStatus", action = "urn:GetStatus")
	public BoerseStatus getStatus() throws RemoteException;
	
	/**, 
	 * Get all Emitents
	 */
	@WebMethod(operationName = "getEmittentsList", action = "urn:GetEmittentsList")
	public java.util.ArrayList<Emittent> getEmittentsList() throws RemoteException;
	
	/**
	 * Normaleweise sollten Clients die Adresse ihrer Brokers kennen
	 * einfachheitshalber bekommt ein Client diese Adresse mithilfe dieser Methode, z.B. localhost:12001
	 */
	@WebMethod(operationName = "getBrokerNetworkAddress", action = "urn:GetBrokerNetworkAddress")
	public String getBrokerNetworkAddress(@WebParam(name = "arg0") Integer brokerId, @WebParam(name = "arg1") NetworkResource resourceKind) throws RemoteException, IllegalArgumentException;

}