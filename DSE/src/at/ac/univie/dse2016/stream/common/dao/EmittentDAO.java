package at.ac.univie.dse2016.stream.common.dao;

import at.ac.univie.dse2016.stream.common.*;

public class EmittentDAO extends at.ac.univie.dse2016.stream.common.dao.UniversalDAO<Emittent> {
	private java.util.TreeMap<String, Emittent> emittentByTicker;
	
	public EmittentDAO(String source) throws IllegalArgumentException {
		super(source);
		
		emittentByTicker = new java.util.TreeMap<String, Emittent>();
		for (Emittent e : super.getItems().values())
			emittentByTicker.put(e.getTicker(), e);
	}
	
	public Emittent getEmittentByTicker(String ticker) throws IllegalArgumentException {
		if ( !emittentByTicker.containsKey(ticker) )
			throw new IllegalArgumentException("Emittent with ticker=" + ticker + " doesn't exist");
		
		return emittentByTicker.get(ticker);
	}
	
	/**
	 * Overriding of base Method
	 */
	public void speichereItem(Emittent item) throws IllegalArgumentException {
		boolean new_item = (item.getId() == -1);
		
		if ( new_item && emittentByTicker.containsKey(item.getTicker()) )
			throw new IllegalArgumentException("Emittent with ticker=" + item.getTicker() + " exist");
		
		super.speichereItem(item);
		
		if (new_item)
			emittentByTicker.put(item.getTicker(), item);
	}
	
	/**
	 * Overriding of base Method
	 */
	public void loescheItem(Emittent item) throws IllegalArgumentException {
		if ( !emittentByTicker.containsKey(item.getTicker()) )
			throw new IllegalArgumentException("Emittent with ticker=" + item.getTicker() + " doesn't exist");

		super.loescheItem(item);
		emittentByTicker.remove(item);
	}
	
	/**
	 * Get Auftraege des Clients
	 */
	public boolean containsTicker(String ticker) throws IllegalArgumentException {
		return emittentByTicker.containsKey(ticker);
	}
	
}
