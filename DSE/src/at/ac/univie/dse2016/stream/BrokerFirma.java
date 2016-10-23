package at.ac.univie.dse2016.stream;

public class BrokerFirma extends Client {

	protected String phone;
	public String getPhone() { return phone; }

	protected String license;
	public String getLicense() { return license; }
	
	/**
	 * Geldgeber, bei dennen die Clienten ausleihen koennen
	 */
	protected java.util.TreeMap<Integer, Geldgeber> geldgeber;
	
	/**
	 * Aktuelle Krediten der Kunden
	 */
	protected java.util.ArrayList<Credit> krediten;

	/**
	 * Broker, die in diesem BoersenMakler arbeiten
	 */
	protected java.util.TreeMap<Integer, Broker> brokers;

	/**
	 * Kunden des BoersenMaklers
	 */
	protected java.util.TreeMap<Integer, Client> clients;
	
	public BrokerFirma(Integer id, float kontostand, String name, String phone, String license) {
		super(id, -1, kontostand, name, null);
		this.phone = phone;
		this.license = license;
		this.boersenMakler = this;
	}

	/**
	 * Zuordnung eines Kunden zu einem Broker
	 * BoersenMakler's Admin's Function 
	 */
	public void SetClientToBroker(Client client, Broker broker) {

		if (brokers.size() == 0)
			throw new IllegalArgumentException("Brokers do not exist");

		if (!clients.containsKey(client.getId()))
			throw new IllegalArgumentException("Client " + String.valueOf(client.getId()) + " does not exist");

		if (!clients.containsKey(broker.getId()))
			throw new IllegalArgumentException("Broker " + String.valueOf(broker.getId()) + " does not exist");

		Client actual_client = clients.get(client.getId());
		Client actual_broker = brokers.get(broker.getId());
		if (actual_client.parentId == -1)
			throw new IllegalArgumentException("Client " + String.valueOf(actual_client.getId()) + " ist ein BoersenMakler");
		
		if (brokers.containsKey(actual_client.getId()))
			throw new IllegalArgumentException("Client " + String.valueOf(actual_client.getId()) + " ist ein Broker");

		if (actual_client.parentId != this.getId())
			throw new IllegalArgumentException("Client " + String.valueOf(actual_client.getId()) + " ist schon zu einem Broker zugeordnet");

		synchronized (this/*actual_broker and actual_client in arrays */) {
			//actual_client.setParentId(actual_broker.id);
			//actual_broker.ClientAddNew(actual_client);
			//clients.put(actual_client.getId(), actual_client);
			//brokers.put(actual_broker.getId(), actual_broker);
		}
	}

	/**
	 * einfachheitshalber erstellen wir einen Kredit mit dem ersten Geldgeber, 5% proJahr, x10
	 * BoersenMakler's Admin's Function
	 */
	public void SetCredit(Client client) {

		if (geldgeber.size() == 0)
			throw new IllegalArgumentException("Geldgeber do not exist");

		if (!clients.containsKey(client.getId()))
			throw new IllegalArgumentException("Client " + String.valueOf(client.getId()) + " does not exist");

		Client actual_client = clients.get(client);
		if (actual_client.credit != null)
			throw new IllegalArgumentException("Client " + String.valueOf(actual_client.getId()) + " hat schon einen Kredit");

		//Nehemen wir den ersten Geldgeber und erstellen einen Vertrag
		Credit kredit = new Credit(actual_client.getId(), this.getId(), 1, 0.05f, 10f, 0f);
		krediten.add(kredit);
		clients.put(actual_client.getId(), actual_client);
	}
	
	/**
	 * wenn die Boerse abschliesst
	 * BoersenMakler's Admin's Function oder ScheduleJob
	 */
	public void Close() {
		
		// single thread
		for(Credit kredit : krediten)
		{
			if (kredit.getUsed() != 0) {
				Client client = this.clients.get(kredit.getClientId());
				
				// Zinsen
				float zinsen = kredit.getUsed() / 360 * kredit.getRate();
				kredit.setUsed(kredit.getUsed() + zinsen);
				
				// Wenn das Geld auf dem Tradingkonto zur Verfuegung steht 
				if (client.kontostand != 0) {
					if (client.kontostand > kredit.getUsed()) {
						//ueberweisen den ganzen Credit zum Geldgeber
						client.Auszahlen(kredit.getUsed());
						kredit.setUsed(0);
					}
					else {
						//ueberweisen den Teil des Kredits zum Geldgeber
						client.Auszahlen(client.kontostand);
						kredit.setUsed(kredit.getUsed() - client.kontostand);
					}
				}
			}
		}
		
	}
	
	public void AuftragAddNew(Auftrag auftrag) {
		// BoersenMakler
		if (this.parentId == -1) {
			//Boerse.AuftragAddNew(this.id, auftrag);
		}
		else if (boersenMakler == null) {
			throw new IllegalArgumentException("You cann  Order only with your personal Broker");

		}
	}

	public void AuftragCancel(Auftrag auftrag) {
		
	}

}
