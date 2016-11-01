package at.ac.univie.dse2016.stream.common;

import java.io.*;

public final class Emittent implements Serializable {
	
	private static final long serialVersionUID = 100L;
	
	protected Integer id;
	public Integer getId() { return id; }
	
	protected String ticker;
	public String getTicker() { return ticker; }
	public void setTicker(String ticker) { this.ticker = ticker; }
	
	protected String name;
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	protected float lastPrice;
	public float getLastPrice() { return lastPrice; }
	public void setLastPrise(float lastPrice) { this.lastPrice = lastPrice; }

	public Emittent(String ticker, String name) {
		this.id = -1;
		this.ticker = ticker;
		this.name = name;
		this.lastPrice = -1;
	}

	public Emittent(Integer id, String ticker, String name) {
		this.id = id;
		this.ticker = ticker;
		this.name = name;
		this.lastPrice = -1;
	}

	public String toString() { 
	    return this.ticker + " - " + this.name;
	} 
}