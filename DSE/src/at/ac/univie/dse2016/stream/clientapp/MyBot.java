package at.ac.univie.dse2016.stream.clientapp;

import at.ac.univie.dse2016.stream.common.*;
import java.util.Random;

/*
 * Es gibt ZielMarktPreise, die der Investor selbst kalkuliert (sie sind im emittentZielMarktPrices)
 * Je staerke der MarktPreis von emittentZielMarktPrices abweicht und mehr Geld der Investor hat, desto mehr die Wahrscheinlichkeit, dass der Bot die Aktien kauft/verkauft
 */
@BotDescription(Description = "My simple Bot")
public class MyBot extends Bot {
	private boolean status;
	
	private float emittentZielMarktPrice = -1f;
	private float lastMarktPrice = -1f;
	
	/*
	 * Init values
	 */
	public void InitValues() {
	}
	
	private void CancelAllOrders() {
		//cancel current Orders
		java.util.Collection<Auftrag> list = new java.util.ArrayList<Auftrag>(this.gui.auftraege.values());

		for (java.util.Iterator<Auftrag> iterator = list.iterator(); iterator.hasNext(); ) {
			Auftrag auftrag = iterator.next();
			if (auftrag.getStatus().equals(AuftragStatus.Accepted) || auftrag.getStatus().equals(AuftragStatus.TeilweiseBearbeitet))
			{
				//iterator.remove();
				this.gui.cancelOrder(auftrag.getId(), true);
			}
		}
	}
	
	
	public void Start() {
		this.status = true;

		//start Bot concurrent
		new Thread(new java.lang.Runnable() {
		    public Runnable init() {
		    	return this;
		    }
		    
		    @Override
		    public void run() {
		    	start();
		    }
		}.init()).start();

	}
	
	private void start() {
		try {
		
			while (this.status) {
				
				//10% - change zielpreis
				if (this.emittentZielMarktPrice == -1f || java.util.concurrent.ThreadLocalRandom.current().nextFloat() < 0.5f)
				{
					Thread.sleep(1000);
System.out.println( "CancelAllOrders" );
					//cancel current orders
					CancelAllOrders();
					
					//generate new zielpreis
					this.emittentZielMarktPrice = java.util.concurrent.ThreadLocalRandom.current().nextFloat() * 100f;
System.out.println( "new emittentZielMarktPrice=" + this.emittentZielMarktPrice );
				}
	
				if (this.lastMarktPrice == -1)
					this.lastMarktPrice = this.emittentZielMarktPrice;
				
				boolean buy = (this.emittentZielMarktPrice > this.lastMarktPrice);
				if (this.emittentZielMarktPrice == this.lastMarktPrice || this.lastMarktPrice < 10f)
					buy = (java.util.concurrent.ThreadLocalRandom.current().nextFloat() < 0.5f);
				
				float accountMoney = gui.kontoStand;
				int emittentCount = 0;
				if (gui.accountEmittents.containsKey(gui.udpEmittentIds[gui.udpEmittentIdActive]))
					emittentCount = gui.accountEmittents.get( gui.udpEmittentIds[gui.udpEmittentIdActive] );
				
				float emittentsMoney = emittentCount * this.lastMarktPrice;
				float totalMoney = emittentsMoney + accountMoney;

System.out.println( "accountMoney=" + accountMoney );
System.out.println( "emittentCount=" + emittentCount );
System.out.println( "emittentsMoney=" + emittentsMoney );
System.out.println( "totalMoney=" + totalMoney );

				
				//80% with bedingung
				boolean withBedingung = (java.util.concurrent.ThreadLocalRandom.current().nextFloat() < 0.8f);
				float bedingung = this.emittentZielMarktPrice + (this.lastMarktPrice - this.emittentZielMarktPrice) / this.lastMarktPrice * java.util.concurrent.ThreadLocalRandom.current().nextFloat();
				int count;
				if (buy)
					count = Math.round( accountMoney / totalMoney * (accountMoney / this.emittentZielMarktPrice) * java.util.concurrent.ThreadLocalRandom.current().nextFloat() / 100 );
				else
					count = Math.round( emittentsMoney / totalMoney * emittentCount * java.util.concurrent.ThreadLocalRandom.current().nextFloat() / 100 );
				if (count == 0)
					count = 1;
				
				if (!withBedingung)
					bedingung = -1f;

System.out.println( "lastMarktPrice=" + this.lastMarktPrice );

System.out.println( "new auftrag=" + gui.emittentTickerById(gui.udpEmittentIds[gui.udpEmittentIdActive]));
System.out.println( "new auftrag buy=" + buy );
System.out.println( "new auftrag count=" + count );
System.out.println( "new auftrag bedingung=" + bedingung );

				//add auftrag
				gui.auftragAddnew(buy, gui.emittentTickerById(gui.udpEmittentIds[gui.udpEmittentIdActive]), count, bedingung, true);
				
				//wait 1 sec
				Thread.sleep(1000);
			}
			CancelAllOrders();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void Stop() {
		this.status = false;
	}
	
	public void processFeedMsg(FeedMsg feedMsg) {
		if (gui.udpEmittentIds[gui.udpEmittentIdActive].equals( feedMsg.getTickerId() ) && ( feedMsg.getStatus().equals(AuftragStatus.Bearbeitet) || feedMsg.getStatus().equals(AuftragStatus.TeilweiseBearbeitet) ) )
			this.lastMarktPrice = feedMsg.getPrice();
	}
}
