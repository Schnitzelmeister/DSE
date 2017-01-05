package at.ac.univie.dse2016.stream.common;

import java.util.Calendar;
import java.util.Date;

public class Transaction extends at.ac.univie.dse2016.stream.common.dao.PersistableObject {

	private static final long serialVersionUID = 100L;

	public static class DescendingTransactionDateComparator implements java.util.Comparator<Transaction> {
		
	   	public int compare(Transaction p1, Transaction p2)
		{
			if (p1 == p2 || p1.getDateCommitted().equals(p2.getDateCommitted()))
				return 0;
			else if (p2.getDateCommitted().after(p1.getDateCommitted()))
				return 1;
			else
				return -1;
		}
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
