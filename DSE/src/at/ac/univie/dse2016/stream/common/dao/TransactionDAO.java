package at.ac.univie.dse2016.stream.common.dao;

import at.ac.univie.dse2016.stream.common.*;

/**
 * Tramsactions, sorted by Date
 */
public class TransactionDAO extends at.ac.univie.dse2016.stream.common.dao.UniversalDAO<Transaction> {
	private java.util.TreeMap< Integer /* auftragId */, java.util.TreeSet< Transaction> > transactionsLog;
	
	public TransactionDAO(String source) throws IllegalArgumentException {
		super(source);
		
		transactionsLog = new java.util.TreeMap< Integer /* auftragId */, java.util.TreeSet< Transaction> >();
		for (Transaction t : super.getItems().values()) {
			if ( !transactionsLog.containsKey(t.getAuftragId() ) )
				transactionsLog.put(t.getAuftragId(), new java.util.TreeSet< Transaction>());
			
			transactionsLog.get(t.getAuftragId()).add(t);

			if ( !transactionsLog.containsKey(t.getAuftragId2() ) )
				transactionsLog.put(t.getAuftragId2(), new java.util.TreeSet< Transaction>());
			
			transactionsLog.get(t.getAuftragId2()).add(t);
		}
	}
	
	/**
	 * Overriding of base Method
	 */
	public void speichereItem(Transaction item) throws IllegalArgumentException {
		if (item.getAuftragId() <= 0)
			throw new IllegalArgumentException("Illegal AuftragId=" + item.getAuftragId());

		if (item.getAuftragId2() <= 0)
			throw new IllegalArgumentException("Illegal AuftragId2=" + item.getAuftragId2());

		super.speichereItem(item);
		
		if ( !transactionsLog.containsKey(item.getAuftragId() ) )
			transactionsLog.put(item.getAuftragId(), new java.util.TreeSet< Transaction>());
		
		transactionsLog.get(item.getAuftragId()).add(item);

		if ( !transactionsLog.containsKey(item.getAuftragId2() ) )
			transactionsLog.put(item.getAuftragId2(), new java.util.TreeSet< Transaction>());
		
		transactionsLog.get(item.getAuftragId2()).add(item);
	}
	
	/**
	 * Overriding of base Method
	 */
	public void loescheItem(Transaction item) throws IllegalArgumentException {
		if (item.getAuftragId() <= 0)
			throw new IllegalArgumentException("Illegal AuftragId=" + item.getAuftragId());

		if (item.getAuftragId2() <= 0)
			throw new IllegalArgumentException("Illegal AuftragId2=" + item.getAuftragId2());

		super.loescheItem(item);
		
		transactionsLog.get(item.getAuftragId()).remove(item);
		transactionsLog.get(item.getAuftragId2()).remove(item);
	}
	
	/**
	 * Get Transaction des Auftrags
	 */
	public java.util.TreeSet<Transaction> getTransactions(Integer auftragId) throws IllegalArgumentException {
		return transactionsLog.get(auftragId);
	}

}

