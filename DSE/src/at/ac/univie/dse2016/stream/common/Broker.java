package at.ac.univie.dse2016.stream.common;

public class Broker extends Client {

	private static final long serialVersionUID = 100L;
	
	protected String phone;
	public String getPhone() { return phone; }

	protected String license;
	public String getLicense() { return license; }

	protected String networkRMIAddress;
	protected String networkSOAPAddress;
	protected String networkRESTAddress;
	public String getNetworkRMIAddress() { return networkRMIAddress; }
	public String getNetworkSOAPAddress() { return networkSOAPAddress; }
	public String getNetworkRESTAddress() { return networkRESTAddress; }
	
	/**
	 * Aktuelle Krediten der Kunden
	 */
	//protected java.util.ArrayList<Credit> krediten;

	/**
	 * Kunden des Brokers
	 */
	//protected transient java.util.TreeMap<Integer, Client> clients;

	public Broker(String name, String networkRMIAddress, String networkSOAPAddress, String networkRESTAddress, String phone, String license) {
		super(-1, -1, 0, name);
		this.networkRMIAddress = networkRMIAddress;
		this.networkSOAPAddress = networkSOAPAddress;
		this.networkRESTAddress = networkRESTAddress;
		this.phone = phone;
		this.license = license;
	}

	public Broker(float kontostand, String name, String networkRMIAddress, String networkSOAPAddress, String networkRESTAddress, String phone, String license) {
		super(-1, -1, kontostand, name);
		this.networkRMIAddress = networkRMIAddress;
		this.networkSOAPAddress = networkSOAPAddress;
		this.networkRESTAddress = networkRESTAddress;
		this.phone = phone;
		this.license = license;
	}

	public Broker(Integer id, float kontostand, String name, String networkRMIAddress, String networkSOAPAddress, String networkRESTAddress, String phone, String license) {
		super(id, -1, kontostand, name);
		this.networkRMIAddress = networkRMIAddress;
		this.networkSOAPAddress = networkSOAPAddress;
		this.networkRESTAddress = networkRESTAddress;
		this.phone = phone;
		this.license = license;
	}
	
	public String toString() { 
	    return this.name + " - " + this.networkRMIAddress;
	} 
}