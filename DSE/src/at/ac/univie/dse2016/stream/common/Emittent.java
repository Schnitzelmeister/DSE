package at.ac.univie.dse2016.stream.common;

public final class Emittent extends at.ac.univie.dse2016.stream.common.dao.PersistableObject {
	
	private static final long serialVersionUID = 100L;
	
	protected String ticker;
	public String getTicker() { return ticker; }
	public void setTicker(String ticker) { this.ticker = ticker; }
	
	protected String name;
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public Emittent() {}
	
	public Emittent(String ticker, String name) {
		this.id = -1;
		this.ticker = ticker;
		this.name = name;
	}

	public Emittent(Integer id, String ticker, String name) {
		this.id = id;
		this.ticker = ticker;
		this.name = name;
	}

	public String toString() { 
	    return this.ticker;// + " - " + this.name;
	} 
}