package at.ac.univie.dse2016.stream.boerse;

import at.ac.univie.dse2016.stream.common.*;
import at.ac.univie.dse2016.stream.common.dao.*;

/*
 * Dieser Klasse enthaelt alle DAO Objekte der Boerse
 */
public class PoolDAO {

	/**
	 * Alle Transactions der Boerse
	 */
	private TransactionDAO transactionDAO;
	public TransactionDAO getTransactionDAO() {
		return transactionDAO;
	}
	

	/**
	 * Alle Auftraege der Boerse
	 */
	private AuftragDAO auftragDAO;
	public AuftragDAO getAuftragDAO() {
		return auftragDAO;
	}
	
	/**
	 * Brokers, die auf der Boerse arbeiten duerfenlastCommitedTransactions
	 */
	private UniversalDAO<Broker> brokerDAO;
	public UniversalDAO<Broker> getBrokerDAO() {
		return brokerDAO;
	}
	
	/**
	 * Emittents, die auf der Boerse gekauft/verkauft werden duerfen
	 */
	private EmittentDAO emittentDAO;
	public EmittentDAO getEmittentDAO() {
		return emittentDAO;
	}
	
	
	/*
	 * Als Parameter muss man den Pfad vom Verzeichnis mit Daten uebergeben
	 */
	public PoolDAO(String dataDir) throws IllegalArgumentException {

	    if ( !java.nio.file.Files.exists( java.nio.file.Paths.get(dataDir) ) )
	    	throw new IllegalArgumentException("Illegal Data Directory Path " + dataDir);
		
	    emittentDAO = new EmittentDAO(dataDir + "/emittent.dao");
	    brokerDAO = new UniversalDAO<Broker>(dataDir + "/broker.dao");
	    auftragDAO = new AuftragDAO(dataDir + "/bauftrag.dao");
	    transactionDAO = new TransactionDAO(dataDir + "/btransaction.dao");
	}
}
