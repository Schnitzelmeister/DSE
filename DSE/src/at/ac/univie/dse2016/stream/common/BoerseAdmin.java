package at.ac.univie.dse2016.stream.common;

import java.rmi.RemoteException;


public interface BoerseAdmin extends BoersePublic {
	
	/**
	 * AddNew Emittent, Admin's Function
	 */
	public Integer emittentAddNew(Emittent emittent) throws RemoteException, IllegalArgumentException;

	/**
	 * Edit Emittent, Admin's Function
	 */
	public void emittentEdit(Emittent emittent) throws RemoteException, IllegalArgumentException;
	
	/**
	 * Remove Emittent, Admin's Function
	 */
	public void emittentLock(Emittent emittent) throws RemoteException, IllegalArgumentException;

	/**
	 * Show all Emittents
	 * @return
	 * @throws RemoteException
	 */
	public java.util.ArrayList<Emittent> getEmittentsList() throws RemoteException;
	
	/**
	 * AddNew Broker, Admin's Function
	 */
	public Integer brokerAddNew(Broker broker) throws RemoteException, IllegalArgumentException;

	/**
	 * Edit Broker, Admin's Function
	 */
	public void brokerEdit(Broker broker) throws RemoteException, IllegalArgumentException;
	
	/**
	 * Remove Broker, Admin's Function
	 */
	public void brokerLock(Broker broker) throws RemoteException, IllegalArgumentException;
	
	/**
	 * Get all Brokers, Admin's Function
	 */
	public java.util.ArrayList<Broker> getBrokersList() throws RemoteException;
	
	/**
	 * Schliesst die Boerse ab, normaleweise am Ende des Tages, Admin's Function oder ScheduleJob
	 */
	public void Close() throws RemoteException;

	/**
	 * Oeffnet die Boerse, Admin's Function oder ScheduleJob
	 */
	public void Open() throws RemoteException;

}