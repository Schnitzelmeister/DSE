package at.ac.univie.dse2016.stream.boerse;

import java.rmi.RemoteException;

import at.ac.univie.dse2016.stream.common.*;

public class BoersePublicAdapter implements BoersePublic {
	

	private BoerseServer server;
	public BoersePublicAdapter(BoerseServer server) { this.server = server; }
	
	/**
	 * Status der Boerse, Open, Closed oder Error
	 */
	public BoerseStatus getStatus() throws RemoteException {
		return server.getStatus();
	}
	
	/**
	 * Get all Emitents
	 */
	public java.util.ArrayList<Emittent> getEmittentsList() throws RemoteException {
		return server.getEmittentsList();
	}
	
	/**
	 * Normaleweise sollten Clients die Adresse ihrer Brokers kennen
	 * einfachheitshalber bekommt ein Client diese Adresse mithilfe dieser Methode, z.B. localhost:12001
	 */
	public String getBrokerNetworkAddress(Integer brokerId) throws RemoteException, IllegalArgumentException  {
		return server.getBrokerNetworkAddress(brokerId);
	}

}
