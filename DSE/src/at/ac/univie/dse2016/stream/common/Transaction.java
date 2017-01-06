package at.ac.univie.dse2016.stream.common;

import java.util.Calendar;
import java.util.Date;

public class Transaction extends at.ac.univie.dse2016.stream.common.dao.PersistableObject implements Comparable<Transaction> {

	private static final long serialVersionUID = 100L;

    public int compareTo(Transaction p2) {
		if (this == p2 || this.getDateCommitted().equals(p2.getDateCommitted())) {
			if (p2.getId().intValue() > this.getId().intValue() )
				return 0;
			else if (p2.getId().intValue() > this.getId().intValue())
				return 1;
			else
				return -1;
		}
		else if (p2.getDateCommitted().after(this.getDateCommitted()))
			return 1;
		else
			return -1;
    }
    		
	protected Integer auftragId;
	public Integer getAuftragId() { return auftragId; }

	protected Integer auftragId2;
	public Integer getAuftragId2() { return auftragId2; }
	
	protected Integer anzahl;
	public Integer getAnzahl() { return anzahl; }
	
	protected float price;
	public Float getPrice() { return price; }
	
	protected Date dateCommitted;
	public Date getDateCommitted() { return dateCommitted; }
	
	public Transaction(Integer auftragId, Integer auftragId2, Integer anzahl, float price) {
		this(auftragId, auftragId2, anzahl, price, Calendar.getInstance().getTime());
	}

	public Transaction(Integer auftragId, Integer auftragId2, Integer anzahl, float price, Date dateCommitted) {
		this.auftragId = auftragId;
		this.auftragId2 = auftragId2;
		this.anzahl = anzahl;
		this.price = price;
		this.dateCommitted = dateCommitted;
	}
}
