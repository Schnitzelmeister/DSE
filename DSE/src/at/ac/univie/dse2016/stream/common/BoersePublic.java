package at.ac.univie.dse2016.stream.common;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface BoersePublic extends Remote {
	
	/**
	 * Status der Boerse, Open, Closed oder Error
	 */
	public BoerseStatus getStatus() throws RemoteException;
	
	/**
	 * Get all Emitents
	 */
	public java.util.ArrayList<Emittent> getEmittentsList() throws RemoteException;
	
	/**
	 * Normaleweise sollten Clients die Adresse ihrer Brokers kennen
	 * einfachheitshalber bekommt ein Client diese Adresse mithilfe dieser Methode, z.B. localhost:12001
	 */
	public String getBrokerNetworkAddress(Integer brokerId) throws RemoteException, IllegalArgumentException;

}