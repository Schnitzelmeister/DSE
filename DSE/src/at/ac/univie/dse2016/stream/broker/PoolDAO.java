package at.ac.univie.dse2016.stream.broker;

import at.ac.univie.dse2016.stream.common.*;
import at.ac.univie.dse2016.stream.common.dao.*;


/*
 * Dieser Klasse enthaelt alle DAO Objekte des Brokers
 */
public class PoolDAO {

	/**
	 * Alle Transactions des Brokers
	 */
	private TransactionDAO transactionDAO;
	public TransactionDAO getTransactionDAO() {
		return transactionDAO;
	}
	

	/**
	 * Alle Auftraege des Brokers
	 */
	private AuftragDAO auftragDAO;
	public AuftragDAO getAuftragDAO() {
		return auftragDAO;
	}
	
	/**
	 * Brokers, die auf dem Broker arbeiten duerfenlastCommitedTransactions
	 */
	private UniversalDAO<Client> clientDAO;
	public UniversalDAO<Client> getClientDAO() {
		return clientDAO;
	}
	
	/*
	 * Als Parameter muss man den Pfad vom Verzeichnis mit Daten uebergeben
	 */
	public PoolDAO(String dataDir) throws IllegalArgumentException {

	    if ( !java.nio.file.Files.exists( java.nio.file.Paths.get(dataDir) ) )
	    	throw new IllegalArgumentException("Illegal Data Directory Path " + dataDir);
		
	    clientDAO = new UniversalDAO<Client>(dataDir + "/client.dao");
	    auftragDAO = new AuftragDAO(dataDir + "/auftrag.dao");
	    transactionDAO = new TransactionDAO(dataDir + "/transaction.dao");
	}
}
