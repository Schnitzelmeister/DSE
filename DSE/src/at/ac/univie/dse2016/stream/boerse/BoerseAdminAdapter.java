package at.ac.univie.dse2016.stream.boerse;

import java.rmi.RemoteException;

import at.ac.univie.dse2016.stream.common.*;

public class BoerseAdminAdapter implements BoerseAdmin {

	private BoerseServer server;
	public BoerseAdminAdapter(BoerseServer server) { this.server = server; }
	
	/**
	 * AddNew Emittent, Admin's Function
	 */
	public Integer emittentAddNew(Emittent emittent) throws RemoteException, IllegalArgumentException {
		return server.emittentAddNew(emittent);
	}

	/**
	 * Edit Emittent, Admin's Function
	 */
	public void emittentEdit(Emittent emittent) throws RemoteException, IllegalArgumentException {
		server.emittentEdit(emittent);
	}
	
	/**
	 * Remove Emittent, Admin's Function
	 */
	public void emittentLock(Emittent emittent) throws RemoteException, IllegalArgumentException {
		server.emittentLock(emittent);
	}
	
	/**
	 * AddNew Broker, Admin's Function
	 */
	public Integer brokerAddNew(Broker broker) throws RemoteException, IllegalArgumentException {
		return server.brokerAddNew(broker);
	}


	/**
	 * Edit Broker, Admin's Function
	 */
	public void brokerEdit(Broker broker) throws RemoteException, IllegalArgumentException {
		server.brokerEdit(broker);
	}
	
	/**
	 * Remove Broker, Admin's Function
	 */
	public void brokerLock(Broker broker) throws RemoteException, IllegalArgumentException {
		server.brokerLock(broker);
	}
	
	/**
	 * Get all Brokers, Admin's Function
	 */
	public java.util.ArrayList<Broker> getBrokersList() throws RemoteException {
		return server.getBrokersList();
	}
	
	/**
	 * Schliesst die Boerse ab, normaleweise am Ende des Tages, Admin's Function oder ScheduleJob
	 */
	public void Close() throws RemoteException {
		server.Close();
	}

	/**
	 * Oeffnet die Boerse, Admin's Function oder ScheduleJob
	 */
	public void Open() throws RemoteException {
		server.Open();
	}

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
