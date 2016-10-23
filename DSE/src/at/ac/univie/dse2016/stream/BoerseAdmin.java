package at.ac.univie.dse2016.stream;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BoerseAdmin extends Remote {
	
	/**
	 * Status der Boerse, Open, Closed oder Error
	 */
	public BoerseStatus Status() throws RemoteException;

	/**
	 * AddNew Emitent, Admin's Function
	 */
	public Integer emitentAddNew(Emittent emitent) throws RemoteException, IllegalArgumentException;

	/**
	 * Edit Emitent, Admin's Function
	 */
	public void emitentEdit(Emittent emitent) throws RemoteException, IllegalArgumentException;
	
	/**
	 * Remove Emitent, Admin's Function
	 */
	public void emitentLock(Emittent emitent) throws RemoteException, IllegalArgumentException;

	/**
	 * Get all Emitents, Admin's Function
	 */
	public java.util.Collection<Emittent> getEmitentsList() throws RemoteException;
	
	/**
	 * AddNew BoersenMakler, Admin's Function
	 */
	public Integer BoersenMaklerAddNew(BrokerFirma makler) throws RemoteException, IllegalArgumentException;

	/**
	 * Edit BoersenMakler, Admin's Function
	 */
	public void BoersenMaklerEdit(BrokerFirma makler) throws RemoteException, IllegalArgumentException;
	
	/**
	 * Remove BoersenMakler, Admin's Function
	 */
	public void BoersenMaklerLock(BrokerFirma makler) throws RemoteException, IllegalArgumentException;
	
	/**
	 * Get all BoersenMakler, Admin's Function
	 */
	public java.util.Collection<BrokerFirma> getClientsList() throws RemoteException;
	
	/**
	 * Schliesst die Boerse ab, normaleweise am Ende des Tages, Admin's Function oder ScheduleJob
	 */
	public void Close() throws RemoteException;

	/**
	 * Oeffnet die Boerse, Admin's Function oder ScheduleJob
	 */
	public void Open() throws RemoteException;

}
