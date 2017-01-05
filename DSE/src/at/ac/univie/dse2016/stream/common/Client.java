package at.ac.univie.dse2016.stream.common;

public class Client extends at.ac.univie.dse2016.stream.common.dao.PersistableObject {
	
	private static final long serialVersionUID = 100L;
	/**
	 * fuer ein Boersenmakler ist -1,
	 * fuer Broker ist Id von Boersenmakler, bei dem er arbeitet
	 * fuer Client ist Id von entweder einem Broker, wenn er ueber den Broker arbeitet
	 * 		oder Id von Boersenmakler, ueber den er direkt arbeitet
	 */
	protected Integer parentId;
	public Integer getBrokerId() { return parentId; }

	public void setParentId(Integer parentId){
		this.parentId = parentId;
	}

	@Override
	public String toString() {
		return "Client{" +
				"id=" + id +
				", parentId=" + parentId +
				", kontostand=" + kontostand +
				", name='" + name + '\'' +
				'}';
	}

	/**
	 * das Geld auf dem Tradingskonto in EUR
	 */
	protected float kontostand;
	public synchronized float getKontostand() { return kontostand; }
	public synchronized void setKontostand(float diff) { this.kontostand += diff; }


	/**
	 * das Geld, das zu Verfuegung steht in EUR (Kontostand - die Summe der gestellten Auftraege)
	 */
	protected transient float disponibelstand;
	public synchronized float getDisponibelstand() { return disponibelstand; }
	public synchronized void setDisponibelstand(float diff) { this.disponibelstand += diff; }
		
	protected String name;
	public String getName() { return name; }

	public void setName(String name){
		this.name = name;
	}

//	protected Credit credit;
//	public Credit getKredit() { return credit; }


	/**
	 * Aktive Auftraege des Clients
	 */
	protected java.util.TreeMap<Integer, Auftrag> auftraege;
	public java.util.TreeMap<Integer, Auftrag> getAuftraegeList() {
		return auftraege;
	}

	/**
	 * Emittents, die auf dem tradingskonto des Clients sind
	 */
	private java.util.TreeMap<Integer /*emittentId*/, Integer /*anzahl*/> accountEmittents;
	public java.util.TreeMap<Integer /*emittentId*/, Integer /*anzahl*/> getAccountEmittents() {
		return accountEmittents;
	}

	public synchronized Integer getKontostand(Integer tickerId) {
		if (!accountEmittents.containsKey(tickerId))
			return 0;
		return accountEmittents.get(tickerId);
	}

	public synchronized void setKontostand(Integer tickerId, Integer diff) {
		if (!accountEmittents.containsKey(tickerId))
			accountEmittents.put(tickerId, 0);
		accountEmittents.put(tickerId, accountEmittents.get(tickerId) + diff);
	}
	
	/**
	 * Emittents, die auf dem tradingskonto des Clients sind
	 */
	private java.util.TreeMap<Integer /*emittentId*/, Integer /*anzahl*/> disponibleAccountEmittents;
	public java.util.TreeMap<Integer /*emittentId*/, Integer /*anzahl*/> getDisponibleAccountEmittents() {
		return disponibleAccountEmittents;
	}
	
	public synchronized Integer getDisponibelstand(Integer tickerId) {
		if (!disponibleAccountEmittents.containsKey(tickerId))
			return 0;
		return disponibleAccountEmittents.get(tickerId);
	}
	
	public synchronized void setDisponibelstand(Integer tickerId, Integer diff) {
		if (!disponibleAccountEmittents.containsKey(tickerId))
			disponibleAccountEmittents.put(tickerId, 0);
		disponibleAccountEmittents.put(tickerId, disponibleAccountEmittents.get(tickerId) + diff);
	}
		
	public Client(Integer parentId, String name) {
		this(-1, parentId, 0, name);
	}

	public Client(Integer id, Integer parentId, float kontostand, String name) {
		super(id);
		this.parentId = parentId;
		this.kontostand = kontostand;
		this.disponibelstand = kontostand;
		this.name = name;
		this.auftraege = new java.util.TreeMap<Integer, Auftrag>();
		this.accountEmittents = new java.util.TreeMap<Integer /*emittentId*/, Integer /*anzahl*/>();
		this.disponibleAccountEmittents = new java.util.TreeMap<Integer /*emittentId*/, Integer /*anzahl*/>();
	}

}