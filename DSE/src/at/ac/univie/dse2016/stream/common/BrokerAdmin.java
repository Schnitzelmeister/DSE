package at.ac.univie.dse2016.stream.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService(name = "BrokerAdmin", targetNamespace = "http://common.stream.dse2016.univie.ac.at/")
public interface BrokerAdmin extends Remote {
	

	/**
	 * AddNew Client, Admin's Function
	 */
	@WebMethod(operationName = "clientAddNew", action = "urn:ClientAddNew")
	public Integer clientAddNew(@WebParam(name = "arg0") Client client) throws RemoteException, IllegalArgumentException;

	/**
	 * Edit Client, Admin's Function
	 */
	@WebMethod(operationName = "clientEdit", action = "urn:ClientEdit")
	public void clientEdit(@WebParam(name = "arg0") Client client) throws RemoteException, IllegalArgumentException;
	
	/**
	 * Remove Client, Admin's Function
	 */
	@WebMethod(operationName = "clientLock", action = "urn:ClientLock")
	public void clientLock(@WebParam(name = "arg0") Client client) throws RemoteException, IllegalArgumentException;
	
	/**
	 * Get all Client, Admin's Function
	 */
	@WebMethod(operationName = "getClientsList", action = "urn:GetClientsList")
	public java.util.ArrayList<Client> getClientsList() throws RemoteException;
	
}