package at.ac.univie.dse2016.stream.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BrokerAdmin extends Remote {
	

	/**
	 * AddNew Client, Admin's Function
	 */
	public Integer clientAddNew(Client client) throws RemoteException, IllegalArgumentException;

	/**
	 * Edit Client, Admin's Function
	 */
	public void clientEdit(Client client) throws RemoteException, IllegalArgumentException;
	
	/**
	 * Remove Client, Admin's Function
	 */
	public void clientLock(Client client) throws RemoteException, IllegalArgumentException;
	
	/**
	 * Get all Client, Admin's Function
	 */
	public java.util.ArrayList<Client> getClientsList() throws RemoteException;
	
}