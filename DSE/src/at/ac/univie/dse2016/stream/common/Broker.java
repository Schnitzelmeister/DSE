package at.ac.univie.dse2016.stream.common;

import java.rmi.RemoteException;


public class Broker extends Client {

	private static final long serialVersionUID = 100L;
	
	protected String phone;
	public String getPhone() { return phone; }

	protected String license;
	public String getLicense() { return license; }

	protected String networkAddress;
	public String getNetworkAddress() { return networkAddress; }
	
	/**
	 * Aktuelle Krediten der Kunden
	 */
	//protected java.util.ArrayList<Credit> krediten;

	/**
	 * Kunden des Brokers
	 */
	//protected transient java.util.TreeMap<Integer, Client> clients;
	
	public Broker(Integer id, float kontostand, String name, String networkAddress, String phone, String license) {
		super(id, -1, kontostand, name);
		this.networkAddress = networkAddress;
		this.phone = phone;
		this.license = license;
	}

}