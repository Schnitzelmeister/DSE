package at.ac.univie.dse2016.stream.clientapp;

import java.util.Random;

/*
 * Es gibt ZielMarktPreise, die der Investor selbst kalkuliert (sie sind im emittentZielMarktPrices)
 * Je staerke der MarktPreis von emittentZielMarktPrices abweicht und mehr Geld der Investor hat, desto mehr die Wahrscheinlichkeit, dass der Bot die Aktien kauft/verkauft
 */
//@BotDescription(Description = "My simple Bot")
public class MyBot extends Bot {
	public String getName() {
		return "MyBot";
	}
	
	private java.util.TreeMap<Integer, Float> emittentZielMarktPrices = new java.util.TreeMap<Integer, Float>();
	private java.util.TreeMap<Integer, Float> emittentDisperseMarktPrices = new java.util.TreeMap<Integer, Float>();
	
	/*
	 * Init values
	 */
	public void InitValues() {
		emittentZielMarktPrices.put(this.gui.emittentIdByTicker("AAPL"), 100f);
		emittentZielMarktPrices.put(this.gui.emittentIdByTicker("RDSA"), 25f);
		
		emittentDisperseMarktPrices.put(this.gui.emittentIdByTicker("AAPL"), 5f);
		emittentDisperseMarktPrices.put(this.gui.emittentIdByTicker("RDSA"), 1.5f);
	}
	
	public void Start() {
		Random randomGenerator = new Random();
		
		float price = 21;
		float zielPrice = emittentZielMarktPrices.get(1);
		float diff = price - zielPrice;
		float portfelSum = 9898;
		float kontoStand = 1;
		
		if (diff > 0) {
			if ( randomGenerator.nextDouble() < diff / emittentDisperseMarktPrices.get(1) * kontoStand / portfelSum ) {
				//kaufen
			}
		}
	}
	
	public void Stop() {
		
	}
}
