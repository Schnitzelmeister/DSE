package at.ac.univie.dse2016.stream;


public class Broker extends Client {
	protected String phone;
	public String getPhone() { return phone; }
	
	protected Integer ranking;
	public Integer getRanking() { return ranking; }
	
	/**
	 * Kunden des Brokers
	 */
	protected java.util.TreeMap<Integer, Client> clients;
	
	
	public Broker(Integer id, Integer parentId, float kontostand, String name, String phone, Integer ranking, BrokerFirma boersenMakler) {
		super(id, parentId, kontostand, name, boersenMakler);
		this.phone = phone;
		this.ranking = ranking;
	}
}
