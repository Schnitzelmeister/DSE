package at.ac.univie.dse2016.stream.common.dao;

import at.ac.univie.dse2016.stream.common.*;

/**
 * Alle Auftraege der Boerse
 */
public class AuftragDAO extends at.ac.univie.dse2016.stream.common.dao.UniversalDAO<Auftrag> {
	private java.util.TreeMap< Integer /* clientId */, java.util.TreeSet< Auftrag> > auftraegeLog;

	public AuftragDAO(String source) throws IllegalArgumentException {
		this(source, false);
	}
	
	public AuftragDAO(String source, boolean ignoreConflicts) throws IllegalArgumentException {
		super(source, ignoreConflicts);
		
		auftraegeLog = new java.util.TreeMap< Integer /* clientId */, java.util.TreeSet< Auftrag> >();
		for (Auftrag a : super.getItems().values()) {
			if ( !auftraegeLog.containsKey(a.getOwnerId()) )
				auftraegeLog.put(a.getOwnerId(), new java.util.TreeSet< Auftrag>());
			
			auftraegeLog.get(a.getOwnerId()).add(a);
		}
	}
	
	/**
	 * Overriding of base Method
	 */
	public void speichereItem(Auftrag item) throws IllegalArgumentException {
		if (item.getOwnerId() <= 0)
			throw new IllegalArgumentException("Illegal OwnerId=" + item.getOwnerId());
		
		super.speichereItem(item);
		
		if ( !auftraegeLog.containsKey(item.getOwnerId()) )
			auftraegeLog.put(item.getOwnerId(), new java.util.TreeSet< Auftrag>());
		
		auftraegeLog.get(item.getOwnerId()).add(item);
	}
	
	/**
	 * Overriding of base Method
	 */
	public void loescheItem(Auftrag item) throws IllegalArgumentException {
		if ( !auftraegeLog.containsKey(item.getOwnerId()) )
			throw new IllegalArgumentException("Auftrag with OwnerId=" + item.getOwnerId() + " doesn't exist");

		super.loescheItem(item);
		
		auftraegeLog.get(item.getOwnerId()).remove(item);
	}

	/**
	 * Get Auftraege des Clients
	 */
	public java.util.TreeSet<Auftrag> getAuftraege(Integer ownerId) throws IllegalArgumentException {
		return auftraegeLog.get(ownerId);
	}
}
