package at.ac.univie.dse2016.stream.common;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class Transaction implements Serializable {
	private static final long serialVersionUID = 100L;
	
	protected Integer auftragId;
	public Integer getAuftragId() { return auftragId; }

	protected Integer anzahl;
	public Integer getAnzahl() { return anzahl; }
	
	protected float price;
	public Float getPrice() { return price; }
	
	protected Date dateCommitted;
	public Date getDateCommitted() { return dateCommitted; }
	
	public Transaction(Integer auftragId, Integer anzahl, float price) {
		this(auftragId, anzahl, price, Calendar.getInstance().getTime());
	}

	public Transaction(Integer auftragId, Integer anzahl, float price, Date dateCommitted) {
		this.auftragId = auftragId;
		this.anzahl = anzahl;
		this.price = price;
		this.dateCommitted = dateCommitted;
	}
}
