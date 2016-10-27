package at.ac.univie.dse2016.stream.broker;

import java.rmi.RemoteException;
import at.ac.univie.dse2016.stream.common.*;

public class BrokerAdminAdapter implements BrokerAdmin {
	
	private BrokerServer server;
	public BrokerAdminAdapter(BrokerServer server) { this.server = server; }
	
	/**
	 * AddNew Client, Admin's Function
	 */
	public Integer clientAddNew(Client client) throws RemoteException, IllegalArgumentException {
		return server.clientAddNew(client);
	}

	/**
	 * Edit Client, Admin's Function
	 */
	public void clientEdit(Client client) throws RemoteException, IllegalArgumentException {
		server.clientEdit(client);
	}
	
	/**
	 * Remove Client, Admin's Function
	 */
	public void clientLock(Client client) throws RemoteException, IllegalArgumentException {
		server.clientLock(client);
	}
	
	/**
	 * Get all Client, Admin's Function
	 */
	public java.util.ArrayList<Client> getClientsList() throws RemoteException {
		return server.getClientsList();
	}
	

}
