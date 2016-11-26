package at.ac.univie.dse2016.stream.boerse;

import java.net.*;
import java.io.*;
import java.security.*;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.ws.Endpoint;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import at.ac.univie.dse2016.stream.broker.BrokerPublicRESTful;
import at.ac.univie.dse2016.stream.common.*;

public final class BoerseServer implements BoerseAdmin, BoerseClient {
		
	static class DescendingTransactionDateComparator implements java.util.Comparator<Transaction> {
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
	
	public BoerseServer(int portUDP) {
		super();
		this.portUDP = portUDP;
		status = BoerseStatus.Closed;
		emittents = new java.util.TreeMap<String, Emittent>();
		brokers = new java.util.TreeMap<Integer, Broker>();
		marketPrices = new java.util.TreeMap<Integer, Float>();
		emittentSections = new java.util.TreeMap< Integer /* emittentId */, EmittentSection >();
		auftraegeLog = new java.util.TreeMap< Integer /* clientId */, java.util.TreeSet<Auftrag> >();
		transactionLog = new java.util.TreeMap< Integer /* clientId */, java.util.TreeSet<Transaction> >();
		auftragId = new AtomicInteger();
		auftragId.set(0);
	}

	
	/**
	 * Status der Boerse, Open, Closed oder Error
	 */
	private BoerseStatus status;
	public BoerseStatus getStatus() { return status; }

	/**
	 * Emittents, die auf der Boerse gekauft/verkauft werden duerfen
	 */
	private java.util.TreeMap<String, Emittent> emittents;

	/**
	 * Die Preise, mit denen den letzten Auftrag ueberwiesen wurden
	 */
	private java.util.TreeMap<Integer, Float> marketPrices;

	/**
	 * Brokers, die auf der Boerse arbeiten duerfenlastCommitedTransactions
	 */
	private java.util.TreeMap<Integer, Broker> brokers;

	/**
	 * Emittent Sections
	 */
	private java.util.TreeMap< Integer /* emittentId */, EmittentSection > emittentSections;

	/**
	 * Letzte ausgefuehrte Auftraege
	 */
	//private java.util.TreeMap< Integer /* emittentId */, Transaction > lastCommitedTransactions;

	
	/**
	 * Alle Auftraege der Boerse
	 */
	private java.util.TreeMap< Integer /* clientId */, java.util.TreeSet< Auftrag> > auftraegeLog;

	/**
	 * Committed Tramsactions, sorted by Date
	 */
	private java.util.TreeMap< Integer /* clientId */, java.util.TreeSet<Transaction> > transactionLog;

	/**
	 * Counter fuer Auftraege
	 */
	private AtomicInteger auftragId;
	
	/**
	 * UDP Port
	 */
	private int portUDP;

	/**
	 * AddNew Emittent, Admin's Function
	 */
	public Integer emittentAddNew(Emittent emittent) throws RemoteException, IllegalArgumentException {
		if ( emittents.containsKey(emittent.getTicker()) )
			throw new IllegalArgumentException("Ticker " + emittent.getTicker() + " exists");
		
		Emittent actualEmittent;
		
		synchronized(emittents) {
			actualEmittent = new Emittent(emittents.size() + 1, emittent.getTicker(), emittent.getName());
			emittents.put(actualEmittent.getTicker(), actualEmittent);
			marketPrices.put(actualEmittent.getId(), -1f);
			emittentSections.put(actualEmittent.getId(), new EmittentSection() );
		}
		return actualEmittent.getId();
	}

	/**
	 * Edit Emittent, Admin's Function
	 */
	public synchronized void emittentEdit(Emittent emittent) throws RemoteException, IllegalArgumentException {
		if ( !emittents.containsKey(emittent.getTicker()) )
			throw new IllegalArgumentException("Ticker " + emittent.getTicker() + " does not exist");
		
		Emittent actual_emitent = emittents.get(emittent.getTicker());
		
		if (actual_emitent.getId().intValue() != emittent.getId().intValue())
			throw new IllegalArgumentException("Ticker " + emittent.getTicker() + " has other Id");
	
		actual_emitent.setName(emittent.getName());
	}
	
	/**
	 * Lock Emittent, not Remove, Admin's Function
	 */
	public void emittentLock(Emittent emittent) throws RemoteException, IllegalArgumentException {
		if ( !emittents.containsKey(emittent.getTicker()) )
			throw new IllegalArgumentException("Ticker " + emittent.getTicker() + " does not exist");
		
		Emittent actualEmittent = emittents.get(emittent.getTicker());
		if (actualEmittent.getId().intValue() != emittent.getId().intValue())
			throw new IllegalArgumentException("Ticker " + emittent.getTicker() + " has other Id");

		synchronized(emittents) {
			marketPrices.remove( actualEmittent.getId() );
			emittents.remove( actualEmittent.getTicker() );
			emittentSections.remove( actualEmittent.getId() );
		}
	}

	/**
	 * Get all Emittents, Admin's Function
	 */
	public java.util.ArrayList<Emittent> getEmittentsList() throws RemoteException {
		java.util.ArrayList<Emittent> ret = new java.util.ArrayList<Emittent>(emittents.values());
		return ret;
	}
	
	/**
	 * AddNew Broker, Admin's Function
	 */
	public Integer brokerAddNew(Broker broker) throws RemoteException, IllegalArgumentException {
		if ( brokers.containsKey(broker.getId()) )
			throw new IllegalArgumentException("Client " + String.valueOf(broker.getId()) + " exists");
		
		Broker actualBroker;
		synchronized(brokers) {
			actualBroker = new Broker(brokers.size() + 1, broker.getKontostand(), broker.getName(), broker.getNetworkRMIAddress(), 
					broker.getNetworkSOAPAddress(), broker.getNetworkRESTAddress(), broker.getPhone(), broker.getLicense());
			brokers.put(actualBroker.getId(), actualBroker);
			transactionLog.put(actualBroker.getId(), new java.util.TreeSet<Transaction>( new DescendingTransactionDateComparator() ) );
		}
		return actualBroker.getId();
	}

	/**
	 * Edit Broker, Admin's Function
	 */
	public void brokerEdit(Broker broker) throws RemoteException, IllegalArgumentException {
		if ( !brokers.containsKey(broker.getId()) )
			throw new IllegalArgumentException("Client " + String.valueOf(broker.getId()) + " does not exist");
		
		brokers.put( broker.getId(), broker );
	}
	
	/**
	 * Lock Broker, not Remove, Admin's Function
	 */
	public void brokerLock(Broker broker) throws RemoteException, IllegalArgumentException {
		if ( !brokers.containsKey(broker.getId()) )
			throw new IllegalArgumentException("Client " + String.valueOf(broker.getId()) + " does not exist");
		
		synchronized(brokers) {
			brokers.remove(broker.getId());
		}
	}
	
	/**
	 * Get all Brokers, Admin's Function
	 */
	public java.util.ArrayList<Broker> getBrokersList() throws RemoteException {
		java.util.ArrayList<Broker> ret = new java.util.ArrayList<Broker>(brokers.values());
		return ret;
	}
	
	/**
	 * Schliesst die Boerse ab, normaleweise am Ende des Tages, Admin's Function oder ScheduleJob
	 */
	public synchronized void Close() throws RemoteException {
		if (status != BoerseStatus.Open)
			return;
			
		status = BoerseStatus.Closed;
		stopFeedUDP();
		
		threadUDP.interrupt();
	}

	/**
	 * Oeffnet die Boerse, Admin's Function oder ScheduleJob
	 */
	public synchronized void Open() throws RemoteException {
        //start UDP
        threadUDP = new Thread() {
            public void run() {
            	startFeedUDP();
            }
        };

        threadUDP.start();
        
		status = BoerseStatus.Open;
	}
	
	
	public java.util.TreeSet<Auftrag> getAuftraege(Integer brokerId) throws RemoteException, IllegalArgumentException {
		if ( !brokers.containsKey(brokerId) )
			throw new IllegalArgumentException("Client " + String.valueOf(brokerId) + " does not exist");

		return auftraegeLog.get(brokerId);
	}

	public java.util.TreeSet<Transaction> getTransaktionen(Integer brokerId) throws RemoteException, IllegalArgumentException {
		if ( !brokers.containsKey(brokerId) )
			throw new IllegalArgumentException("Client " + String.valueOf(brokerId) + " does not exist");

		return transactionLog.get(brokerId);
		
	}
	
	public java.util.TreeSet<Transaction> getTransaktionen(Integer brokerId, Date afterDate) throws RemoteException, IllegalArgumentException {
		if ( !brokers.containsKey(brokerId) )
			throw new IllegalArgumentException("Client " + String.valueOf(brokerId) + " does not exist");

		return new java.util.TreeSet<Transaction> ( transactionLog.get(brokerId).headSet( new Transaction(0, 0, 0, afterDate), true ) );
	}

	/**
	 * Get current State des Brokers
	 */
	public Broker getState(Integer brokerId) throws RemoteException, IllegalArgumentException {
		if ( !brokers.containsKey(brokerId) )
			throw new IllegalArgumentException("Client with id=" + brokerId + " does not exist");

		return brokers.get(brokerId);
	}
	/**
	 * Auftrag eines Brokers stellen
	 */
	public Integer auftragAddNew(Integer brokerId, Auftrag auftrag) throws RemoteException, IllegalArgumentException {
		if ( !brokers.containsKey(brokerId) )
			throw new IllegalArgumentException("Broker with id=" + brokerId + " does not exist");

		if ( !emittents.containsKey(auftrag.getTicker()) )
			throw new IllegalArgumentException("Ticker " + auftrag.getTicker() + " does not exist");

		Integer tickerId = emittents.get( auftrag.getTicker() ).getId();
		boolean buy = auftrag.getKaufen();
		int anzahl = auftrag.getAnzahl();
		float bedingung = auftrag.getBedingung();
		int newAuftragId = -1;
		
		Broker broker = this.brokers.get(brokerId);
		//lock Broker
		synchronized(broker) {
			java.util.TreeMap<Integer, Integer> brokerEmittents = broker.getDisponibleAccountEmittents();
			
			if (buy) {
				
				//mit Bedingung
				if (bedingung > 0) {
					if (broker.getDisponibelstand() < bedingung * anzahl)
						throw new IllegalArgumentException("Not enough money");
				}
				//ohne Bedingung - nothing to check 
				else if (bedingung == -1) {
					//if (broker.getDisponibelstand() < marketPrice * anzahl)
					//	throw new IllegalArgumentException("Not enough money");
				}
				else
					throw new IllegalArgumentException("Illegal Bedingung");
			}
			else {
				if (!brokerEmittents.containsKey(tickerId))
					throw new IllegalArgumentException("Nothing to sell");
				
				if (brokerEmittents.get(tickerId) < anzahl)
					throw new IllegalArgumentException("Not enough amount of the Emittent");
			}

			//generate auftragId
			auftrag.setId( newAuftragId = auftragId.incrementAndGet() );
			
			//find Emittent Section
			EmittentSection emittentSection = emittentSections.get(tickerId);

			//ohne Bedingung, einfach akzeptiren alle Auftraege, die einen niedriegsten/grossten Preis fuer diesen kauf/verkauf Auftrag stehen
			boolean commitTransaction = (bedingung == -1);
			boolean committed = false;
			
			//lock Section
			synchronized(emittentSection)
			{
				//mit Bedingung, suchen ob es passende Auftraege gibt 
				if (bedingung > 0) {
					
					if (buy)
						commitTransaction = ( (emittentSection.sell.size() > 0) && (emittentSection.sell.firstKey() <= bedingung) );
					else
						commitTransaction = ( (emittentSection.buy.size() > 0) && (emittentSection.buy.firstKey() >= bedingung));
					
					//es gibt keine passende Auftraege, dann muss man einfach den Auftrag in der Sektion hinzufuegen 
					if (!commitTransaction) {
						
						java.util.TreeMap<Float, java.util.TreeSet<Auftrag> > _map;
						if (buy)
							_map = emittentSection.buy;
						else
							_map = emittentSection.sell;
				
						if (!_map.containsKey(bedingung))
							_map.put(bedingung, new java.util.TreeSet<Auftrag>());
	
						_map.get(bedingung).add(auftrag);
						
						if (buy)
							broker.setDisponibelstand(-bedingung * anzahl);
						else
							broker.setDisponibelstand(tickerId, -anzahl);
						
						return newAuftragId;
					}
				}
				
				//commit Transaction
				java.util.TreeMap<Float, java.util.TreeSet<Auftrag> > _map;
				if (buy)
					_map = emittentSection.sell;
				else
					_map = emittentSection.buy;
				
		    	for(java.util.Iterator< java.util.Map.Entry< Float, java.util.TreeSet<Auftrag> >> ito = _map.entrySet().iterator(); ito.hasNext(); ) {
		    		java.util.Map.Entry< Float, java.util.TreeSet<Auftrag> > e = ito.next();	  

		    		for(java.util.Iterator< Auftrag > it = e.getValue().iterator(); it.hasNext(); ) {
		    			Auftrag a = it.next();	  
						Broker secondBroker = brokers.get( a.getOwnerId() );
						
						//test, ob es genug geld, aktien bei beiden brokers sind

						//der  Kauf
						if (buy) {
							// Marketpreis, ohne Bedingung
							if ( bedingung == -1 ) {
								//kein Geld mehr bei Kauefer
								if (broker.getDisponibelstand() < a.getBedingung()) {
									if (!committed)
										throw new IllegalArgumentException("Not enough money");

									//save to log auftrag
									auftrag.setStatus(AuftragStatus.Bearbeitet);
									setToLog(brokerId, auftrag);
									return newAuftragId;
								}
								//kein Emittent bei Verkauefer - sollte unmoeglich sein
								else if (secondBroker.getDisponibleAccountEmittents().get(tickerId) < a.getAnzahl()) {
									throw new IllegalArgumentException("BOERSE ERROR - kein Emittent bei Verkauefer - sollte unmoeglich sein!!!");
								}
								//commit ganz
								else if (anzahl <= a.getAnzahl()) {
									committed = true;
									commitAuftrage(auftrag, broker, a, secondBroker, tickerId, a.getBedingung(), anzahl, true, emittentSection.msgCounter.incrementAndGet(), (a.getAnzahl() == anzahl) ? AuftragStatus.Bearbeitet : AuftragStatus.Accepted);
									if (a.getAnzahl() == anzahl) {
										//save to log a
										a.setStatus(AuftragStatus.Bearbeitet);
										setToLog(a.getOwnerId(), a);
										it.remove();
									}
									else {
										a.setAnzahl(a.getAnzahl() - anzahl);
									}
	
									//save to log auftrag
									auftrag.setStatus(AuftragStatus.Bearbeitet);
									setToLog(brokerId, auftrag);
									
									return newAuftragId;
								}
								//commit teilweise
								else {
									committed = true;
									commitAuftrage(auftrag, broker, a, secondBroker, tickerId, a.getBedingung(), a.getAnzahl(), true, emittentSection.msgCounter.incrementAndGet(), AuftragStatus.Bearbeitet);
									anzahl -= a.getAnzahl();
									//save to log a
									a.setStatus(AuftragStatus.Bearbeitet);
									setToLog(a.getOwnerId(), a);
									it.remove();
								}
							}
							//mit bedingung
							else {
								//bedingung passt
								if (a.getBedingung() <= bedingung) {
									//kein Geld mehr bei Kauefer - sollte unmoeglich sein
									if (broker.getDisponibelstand() < a.getBedingung()) {
										throw new IllegalArgumentException("BOERSE ERROR - kein Geld mehr bei Kauefer - sollte unmoeglich sein!!!");
									}
									//kein Emittent bei Verkauefer - sollte unmoeglich sein
									else if (secondBroker.getDisponibleAccountEmittents().get(tickerId) < a.getAnzahl()) {
										throw new IllegalArgumentException("BOERSE ERROR - kein Emittent bei Verkauefer - sollte unmoeglich sein!!!");
									}
									//commit ganz
									else if (anzahl <= a.getAnzahl()) {
										committed = true;
										commitAuftrage(auftrag, broker, a, secondBroker, tickerId, a.getBedingung(), anzahl, true, emittentSection.msgCounter.incrementAndGet(), (a.getAnzahl() == anzahl) ? AuftragStatus.Bearbeitet : AuftragStatus.Accepted);
										if (a.getAnzahl() == anzahl) {
											//save to log a
											a.setStatus(AuftragStatus.Bearbeitet);
											setToLog(a.getOwnerId(), a);
											it.remove();
										}
										else {
											a.setAnzahl(a.getAnzahl() - anzahl);
										}
	
										//save to log auftrag
										auftrag.setStatus(AuftragStatus.Bearbeitet);
										setToLog(brokerId, auftrag);

										return newAuftragId;
									}
									//commit teilweise
									else {
										committed = true;
										commitAuftrage(auftrag, broker, a, secondBroker, tickerId, a.getBedingung(), a.getAnzahl(), true, emittentSection.msgCounter.incrementAndGet(), AuftragStatus.Bearbeitet);
										anzahl -= a.getAnzahl();
										
										//save to log a
										a.setStatus(AuftragStatus.Bearbeitet);
										setToLog(a.getOwnerId(), a);
										it.remove();

									}
								}
								//bedingung nicht passt - add to Section, was bleibt
								else {
									auftrag.setAnzahl(anzahl);
									
									broker.setDisponibelstand(-bedingung * anzahl);

									//send asynchrone
									sendFeedMsg(new FeedMsg(emittentSection.msgCounter.incrementAndGet(), newAuftragId, tickerId, true, anzahl, auftrag.getBedingung()/*preis*/, /*status*/AuftragStatus.Accepted));

									if (!emittentSection.buy.containsKey(bedingung))
										emittentSection.buy.put(bedingung, new java.util.TreeSet<Auftrag>());
									emittentSection.buy.get(bedingung).add(auftrag);
									
									return newAuftragId;
								}
							}
						}
						//der Verkauf
						else {
	
							// Marketpreis, ohne Bedingung
							if ( bedingung == -1 ) {
								//kein Emittent bei Kauefer - sollte unmoeglich sein
								if (broker.getDisponibleAccountEmittents().get(tickerId) < auftrag.getAnzahl()) {
									throw new IllegalArgumentException("BOERSE ERROR - kein Emittent bei Kauefer - sollte unmoeglich sein!!!");
								}
								//kein Geld bei Verkauefer - sollte unmoeglich sein
								else if (secondBroker.getDisponibelstand() < a.getBedingung() * a.getAnzahl()) {
									throw new IllegalArgumentException("BOERSE ERROR - kein Geld bei Verkauefer - sollte unmoeglich sein!!!");
								}
								//commit ganz
								else if (anzahl <= a.getAnzahl()) {
									committed = true;
									commitAuftrage(auftrag, broker, a, secondBroker, tickerId, a.getBedingung(), anzahl, false, emittentSection.msgCounter.incrementAndGet(), (a.getAnzahl() == anzahl) ? AuftragStatus.Bearbeitet : AuftragStatus.Accepted);
									if (a.getAnzahl() == anzahl) {
										//save to log a
										a.setStatus(AuftragStatus.Bearbeitet);
										setToLog(a.getOwnerId(), a);
										it.remove();
									}
									else {
										a.setAnzahl(a.getAnzahl() - anzahl);
									}
	
									//save to log auftrag
									auftrag.setStatus(AuftragStatus.Bearbeitet);
									setToLog(brokerId, auftrag);
									
									return newAuftragId;
								}
								//commit teilweise
								else {
									committed = true;
									commitAuftrage(auftrag, broker, a, secondBroker, tickerId, a.getBedingung(), a.getAnzahl(), false, emittentSection.msgCounter.incrementAndGet(), AuftragStatus.Bearbeitet);
									anzahl -= a.getAnzahl();
									//save to log a
									a.setStatus(AuftragStatus.Bearbeitet);
									setToLog(a.getOwnerId(), a);
									it.remove();
								}
							}
							//mit bedingung
							else {
								
								//bedingung passt
								if (a.getBedingung() >= bedingung) {
									//kein Emittent bei Kauefer - sollte unmoeglich sein
									if (broker.getDisponibleAccountEmittents().get(tickerId) < auftrag.getAnzahl()) {
										throw new IllegalArgumentException("BOERSE ERROR - kein Emittent bei Kauefer - sollte unmoeglich sein!!!");
									}
									//kein Geld bei Verkauefer - sollte unmoeglich sein
									else if (secondBroker.getDisponibelstand() < a.getBedingung() * a.getAnzahl()) {
										throw new IllegalArgumentException("BOERSE ERROR - kein Geld bei Verkauefer - sollte unmoeglich sein!!!");
									}
									//commit ganz
									else if (anzahl <= a.getAnzahl()) {
										committed = true;
										commitAuftrage(auftrag, broker, a, secondBroker, tickerId, a.getBedingung(), anzahl, false, emittentSection.msgCounter.incrementAndGet(), (a.getAnzahl() == anzahl) ? AuftragStatus.Bearbeitet : AuftragStatus.Accepted);
										if (a.getAnzahl() == anzahl) {
											//save to log a
											a.setStatus(AuftragStatus.Bearbeitet);
											setToLog(a.getOwnerId(), a);
											it.remove();
										}
										else {
											a.setAnzahl(a.getAnzahl() - anzahl);
										}
	
										//save to log auftrag
										auftrag.setStatus(AuftragStatus.Bearbeitet);
										setToLog(brokerId, auftrag);
										
										return newAuftragId;
									}
									//commit teilweise
									else {
										committed = true;
										commitAuftrage(auftrag, broker, a, secondBroker, tickerId, a.getBedingung(), a.getAnzahl(), false, emittentSection.msgCounter.incrementAndGet(), AuftragStatus.Bearbeitet);
										anzahl -= a.getAnzahl();
										//save to log a
										a.setStatus(AuftragStatus.Bearbeitet);
										setToLog(a.getOwnerId(), a);
										it.remove();
									}
								}
								//bedingung nicht passt - add to Section, was bleibt
								else {
									auftrag.setAnzahl(anzahl);
									
									broker.setDisponibelstand(tickerId, -anzahl);

									//send asynchrone
									sendFeedMsg(new FeedMsg(emittentSection.msgCounter.incrementAndGet(), newAuftragId, tickerId, false, anzahl, auftrag.getBedingung()/*preis*/, /*status*/AuftragStatus.Accepted));

									if (!emittentSection.sell.containsKey(bedingung))
										emittentSection.sell.put(bedingung, new java.util.TreeSet<Auftrag>());
				
									emittentSection.sell.get(bedingung).add(auftrag);
									
									return newAuftragId;
	
								}
							}
						}
					}
		    		
		    		if (e.getValue().size() == 0)
		    			ito.remove();
				}
		    	
		    	//bedingung nicht passt - add to Section, was bleibt
		    	if (auftrag.getBedingung() > 0) {
		    		if (auftrag.getKaufen()) {
						if (!emittentSection.buy.containsKey(bedingung))
							emittentSection.buy.put(bedingung, new java.util.TreeSet<Auftrag>());
	
						emittentSection.buy.get(bedingung).add(auftrag);
						
						broker.setDisponibelstand(-bedingung * anzahl);
		    		}
		    		else {
						if (!emittentSection.sell.containsKey(bedingung))
							emittentSection.sell.put(bedingung, new java.util.TreeSet<Auftrag>());
	
						emittentSection.sell.get(bedingung).add(auftrag);
						
						broker.setDisponibelstand(tickerId, -anzahl);
		    		}
		    	}
		    	else {
					if (!committed)
						throw new IllegalArgumentException("Not enough money");

					//save to log auftrag
					auftrag.setStatus(AuftragStatus.TeilweiseBearbeitet);
					setToLog(brokerId, auftrag);
					return newAuftragId;
		    	}
		    		
			}
		}
		return auftrag.getId();
	}

	private void commitAuftrage(Auftrag auftrag1, Broker broker1, Auftrag auftrag2, Broker broker2, int tickerId, float preis, int anzahl, boolean buy, int msgCounter, AuftragStatus status) {
		float sum = preis * anzahl;
		if (buy) {
			broker1.setKontostand(-sum);
			broker2.setKontostand(sum);
			
			broker1.setDisponibelstand(-sum);
			broker2.setDisponibelstand(sum);

			broker1.setKontostand(tickerId, anzahl);
			broker2.setKontostand(tickerId, -anzahl);
		}
		else  {
			broker1.setKontostand(sum);
			broker1.setDisponibelstand(sum);
			broker2.setKontostand(-sum);
			
			broker1.setKontostand(tickerId, -anzahl);
			broker1.setDisponibelstand(tickerId, -anzahl);
			broker2.setKontostand(tickerId, anzahl);
		}

		//send asynchrone
		sendFeedMsg(new FeedMsg(msgCounter, auftrag2.getId(), tickerId, buy, anzahl, preis, status, auftrag1.getId()));
		
		this.transactionLog.get(broker1.getBrokerId()).add(new Transaction(auftrag1.getId(), anzahl, preis));
		this.transactionLog.get(broker2.getBrokerId()).add(new Transaction(auftrag2.getId(), anzahl, preis));
	}
	
	/**
	 * Auftrag eines Brokers zurueckrufen
	 */
	public void auftragCancel(Integer brokerId, Integer auftragId) throws RemoteException, IllegalArgumentException {
		if ( !brokers.containsKey(brokerId) )
			throw new IllegalArgumentException("Broker with id=" + brokerId + " does not exist");

		Broker broker = this.brokers.get(brokerId);
		java.util.TreeMap<Integer, Auftrag> auftraege = broker.getAuftraegeList();	
		if ( !auftraege.containsKey(auftragId) )
			throw new IllegalArgumentException("Auftrag with id=" + auftragId + " does not exist");
		
		Auftrag auftrag = auftraege.get(auftragId);
		if (auftrag.getStatus() != AuftragStatus.Accepted)
			throw new IllegalArgumentException("Auftrag with id=" + auftragId + " can not be canceled");
		
		int tickerId = emittents.get( auftrag.getTicker() ).getId();
		EmittentSection emittentSection = emittentSections.get( tickerId );
		
		int msgCounter;
		
		synchronized(emittentSection) {
			if (auftrag.getKaufen()) {
				if ( emittentSection.buy.containsKey(auftragId) )
					emittentSection.buy.remove(auftragId);
				else
					throw new IllegalArgumentException("Auftrag with id=" + auftragId + " can not be canceled");
			}
			
			else {
				if ( emittentSection.sell.containsKey(auftragId) )
					emittentSection.sell.remove(auftragId);
				else
					throw new IllegalArgumentException("Auftrag with id=" + auftragId + " can not be canceled");
			}
			
			msgCounter = emittentSection.msgCounter.incrementAndGet();
		}

		//send asynchrone
		sendFeedMsg(new FeedMsg(msgCounter, auftrag.getId(), tickerId, auftrag.getKaufen(), auftrag.getAnzahl(), 0f/*preis*/, /*status*/AuftragStatus.Canceled));

		if (auftrag.getKaufen())
			broker.setDisponibelstand(auftrag.getBedingung() * auftrag.getAnzahl());
		else
			broker.setDisponibelstand(tickerId, auftrag.getAnzahl());
		
		auftrag.setStatus(AuftragStatus.Canceled);
		
		setToLog(brokerId, auftrag);
	}
	
	private void setToLog(int brokerId, Auftrag auftrag) {
		if ( auftraegeLog.containsKey(brokerId) )
			auftraegeLog.put(brokerId, new java.util.TreeSet<Auftrag>() );
		
		auftraegeLog.get(brokerId).add(auftrag);
	}
	
	/**
	 * Einzahlen/auszahlen von einem Broker
	 * normaleweise muss es automatisch ausgefuert werden, wenn das Geld zum Tradingkonto des Brokers eingeht
	 * amount kann +/- sein (einzahlen/auszahlen)
	 * einfachheitshalber koennen die Brokers diese Methode selbst aufrufen
	 * wenn sie das machen, dann heisst es das sie das Geld zu/von ihrem Konto ueberweisen
	 */
	public void tradingAccount(Integer brokerId, float amount) throws RemoteException, IllegalArgumentException {
		if ( !brokers.containsKey(brokerId) )
			throw new IllegalArgumentException("Broker with id=" + brokerId + " does not exist");

		Broker broker = this.brokers.get(brokerId);
		if (amount < 0 && broker.getDisponibelstand() < -amount)
			throw new IllegalArgumentException("Not enough money");
		broker.setDisponibelstand(amount);
		broker.setKontostand(amount);
	}

	/**
	 * Einzahlen/auszahlen eines Emittens (z.B. Aktien) von einem Broker
	 * normaleweise muss es automatisch ausgefuert werden, wenn die Aktien zum Tradingkonto des Brokers eingehen
	 * anzahl kann +/- sein (einzahlen/auszahlen)
	 * einfachheitshalber koennen die Brokers diese Methode selbst aufrufen
	 * wenn sie das machen, dann heisst es das sie die Aktien zu/von ihrem Konto ueberweisen 
	 */
	public void tradingAccount(Integer brokerId, Integer tickerId, Integer anzahl) throws RemoteException, IllegalArgumentException {
		if ( !brokers.containsKey(brokerId) )
			throw new IllegalArgumentException("Broker with id=" + brokerId + " does not exist");

		Broker broker = this.brokers.get(brokerId);
		synchronized(broker) {
			//check, ob aktive Auftrage existieren
			if (anzahl < 0) {
				for(Auftrag a : broker.getAuftraegeList().values()) {
					if ( this.emittents.get( a.getTicker() ).getId() == tickerId )
						throw new IllegalArgumentException("There are aktive Auftraege with this emittent");
				}
			}
			
			java.util.TreeMap<Integer, Integer> brokerEmittents = broker.getAccountEmittents();
			if (brokerEmittents.containsKey(tickerId)) {
				if (anzahl < 0 && brokerEmittents.get(tickerId) < -anzahl)
					throw new IllegalArgumentException("Not enough amount of emittent");
				
				brokerEmittents.put(tickerId, brokerEmittents.get(tickerId) + anzahl);
			}
			else {
				if (anzahl < 0)
					throw new IllegalArgumentException("Not enough amount of emittent");
				
				brokerEmittents.put(tickerId, anzahl);
			}
		}
	}
	
	

	public Report getReport(Integer brokerId) throws RemoteException, IllegalArgumentException {
		return null;
	}

	
	
	/**
	 * Normaleweise sollten Clients die Adresse ihrer Brokers kennen
	 * einfachheitshalber bekommt ein Client diese Adresse mithilfe dieser Methode, z.B. localhost:12001
	 */
	public String getBrokerNetworkAddress(Integer brokerId, NetworkResource resourceKind) throws RemoteException, IllegalArgumentException {
		if ( resourceKind == NetworkResource.UDP )
			throw new IllegalArgumentException("Broker has no UDP");

		if ( !brokers.containsKey(brokerId) )
			throw new IllegalArgumentException("Broker with id=" + brokerId + " does not exist");

		if (resourceKind == NetworkResource.REST)
			return brokers.get(brokerId).getNetworkRESTAddress();
		if (resourceKind == NetworkResource.SOAP)
			return brokers.get(brokerId).getNetworkSOAPAddress();
		//if (resourceKind == NetworkResource.RMI)
		return brokers.get(brokerId).getNetworkRMIAddress();
	}

	
	
	
	//UDP-Server stuff
	/**
	 * UDP-Thread des Servers
	 */
	private Thread threadUDP;
	
	/**
	 * UDP-Socket
	 */
	private DatagramSocket socketUDP;
	
	/**
	 * UDP Sessions
	 */
	private java.util.TreeMap< Integer, UDPSession > activeUDPSessions;

	/**
	 * Counters
	 */
	
	private AtomicInteger sessionCounter;

	/**
	 * Start UDP Server
	 * multicast waere beser, aber ist es problematisch im internet zu implementieren
	 * hier verwendet man unicast
	 */
	private void startFeedUDP() {
		activeUDPSessions = new java.util.TreeMap< Integer, UDPSession >();
		
		sessionCounter.set(0);
		
	    try {
	    	socketUDP = new DatagramSocket(portUDP);
	    	byte[] buf = new byte[1024];
	    	
	    	do {
				DatagramPacket requestPacket = new DatagramPacket(buf, buf.length);
				socketUDP.receive(requestPacket);
				
				
				//process Request async
				new Thread(new java.lang.Runnable() {
					private DatagramPacket requestPacket;
					   
				    public Runnable init(DatagramPacket requestPacket) {
				    	this.requestPacket = requestPacket;
				    	return this;
				    }
				    
				    @Override
				    public void run() {
				    	processFeedRequest(requestPacket);
				    }
				}.init(requestPacket)).start();
				   
				//processFeedRequest(request.getData(), request.getAddress(), request.getPort());
				
	    	} while (true);
	    }   
	    catch (SocketException e){
	        System.err.println("Socket: " + e.getMessage());
	    }
	    catch (IOException e){
	    	System.err.println("IO: " + e.getMessage());
	    }
	}

	/**
	 * Stop UDP Server
	 */
	private void stopFeedUDP() {
		
		threadUDP.interrupt();
		threadUDP = null;
		
		activeUDPSessions.clear();
		activeUDPSessions = null;
		
	    try {
	    	socketUDP.close();
	    	socketUDP = null;
			
	    }   
	    catch (Exception e){
	    	System.err.println("ex: " + e.getMessage());
	    }
	}

	/**
	 * Send UDP Feed Message to all Users
	 */
	private void sendFeedMsg(FeedMsg msg) {
	    try {
		    ByteArrayOutputStream bos = new ByteArrayOutputStream();
		    ObjectOutput out = new ObjectOutputStream(bos);
		    out.write((byte)1);
	    	out.writeObject(msg);
	    	
	    	for(java.util.Iterator<java.util.Map.Entry<Integer, UDPSession>> it = emittentSections.get(msg.getTickerId()).activeUDPSessions.entrySet().iterator(); it.hasNext(); ) {
	    		java.util.Map.Entry<Integer, UDPSession> se = it.next();	    	
	    	
	    		UDPSession s = se.getValue();
	    		
	    		//remove old Sessions, wenn they are 20 seconds not active
	    		java.util.Calendar calendar = java.util.Calendar.getInstance();
	    		calendar.add(java.util.Calendar.SECOND, 20);
	    		if ( s.lastConnectionTime.before(calendar.getTime()) ) {
	    			it.remove();
	    		}
	    		else {
	    		
					//Send FeedMsg async
					new Thread(new java.lang.Runnable() {
						private UDPSession session;
						private ByteArrayOutputStream bos;
						   
					    public Runnable init(UDPSession session, ByteArrayOutputStream bos) {
					    	this.session = session;
					    	this.bos = bos;
					    	return this;
					    }
					    
					    @Override
					    public void run() {
					    	sendUDPMsg(session, bos);
					    }
					}.init(s, bos)).start();
	    		}
	    	}
	    	
	    	out.close();
    	    bos.close();
	    }   
	    catch (SocketException e){
	        System.err.println("Socket: " + e.getMessage());
	    }
	    catch (IOException e){
	    	System.err.println("IO: " + e.getMessage());
	    }
	}
	
	/**
	 * Send UDP DatagramPacket for UDPSession
	 */
	private void sendUDPMsg(UDPSession session, ByteArrayOutputStream bos) {
	    try {
	    	DatagramPacket reply = new DatagramPacket(bos.toByteArray(), bos.size(),
	    			session.address, session.port);
	    	
	    	socketUDP.send(reply);
	    }   
	    catch (SocketException e){
	        System.err.println("Socket: " + e.getMessage());
	    }
	    catch (IOException e){
	    	System.err.println("IO: " + e.getMessage());
	    }
	}
		
	/**
	 * Process incoming FeedRequest
	 */
	private void processFeedRequest(DatagramPacket requestPacket) {
		try {
		    ByteArrayInputStream bis = new ByteArrayInputStream(requestPacket.getData());
		    ObjectInput in = new ObjectInputStream(bis);
		    FeedRequest feedRequest = (FeedRequest) in.readObject();
		    in.close();
		    bis.close();

		    Integer sessionId = feedRequest.getSessionId();
		    boolean newSession = !this.activeUDPSessions.containsKey(sessionId);
		    UDPSession sessionUDP;
		    
		    if (newSession) {
		    	sessionId = sessionCounter.getAndIncrement();
		    	sessionUDP = new UDPSession(feedRequest.getEmittentIds(), requestPacket.getAddress(), requestPacket.getPort());
		    	
		    	this.activeUDPSessions.put( sessionId, sessionUDP );
		    	
		    	for (Integer emittentId : feedRequest.getEmittentIds()) {
		    		this.emittentSections.get(emittentId).activeUDPSessions.put(sessionId, sessionUDP);
		    	}
		    }
		    else {
		    	sessionUDP = this.activeUDPSessions.get(sessionId);
	
		    	//if client want to get  another list of emittents, he must send new UDP Request with new sessionId
		    	
		    	//update access time
		    	sessionUDP.lastConnectionTime = java.util.Calendar.getInstance().getTime();
		    }
		    
		    //send last Market prices to client
		    if (newSession) {
			    ByteArrayOutputStream bos = new ByteArrayOutputStream();
			    ObjectOutput out = new ObjectOutputStream(bos);
			    out.write((byte)marketPrices.size());
			    for (java.util.Map.Entry<Integer, Float> ae : marketPrices.entrySet()) {
			    	FeedMsg msg = new FeedMsg(-1, -1, ae.getKey(), true, 0, ae.getValue(), AuftragStatus.Accepted);
			    	out.writeObject(msg);
			    }
	
		    	DatagramPacket reply = new DatagramPacket(bos.toByteArray(), bos.size(),
		    			sessionUDP.address, sessionUDP.port);
		    	
		    	socketUDP.send(reply);

		    	out.close();
			    bos.close();
		    }
		}
	    catch (SocketException e){
	        System.err.println("Socket: " + e.getMessage());
	    }
	    catch (IOException e){
	    	System.err.println("IO: " + e.getMessage());
	    }
	    catch (Exception e){
	    	System.err.println(": " + e.getMessage());
	    }
	}
	
	
	
	
	
	
	

    public static void main(String[] args) {
    	//default port UDP
    	int portUDP = 10000;
    	if (args.length > 0)
    		portUDP = Integer.valueOf(args[0]);

    	//default port RMI
    	int portRMI = 10001;
    	if (args.length > 1)
    		portRMI = Integer.valueOf(args[1]);
    	
    	BoerseServer boerse = new BoerseServer(portUDP);
    	
    	//initial values
    	try {
	    	boerse.emittentAddNew( new Emittent("AAPL", "Apple Inc.") );
	    	boerse.emittentAddNew( new Emittent("RDSA", "Royal Dutch Shell") );
	    	boerse.brokerAddNew( new Broker(1, 0f, "Daniil Brokers Co.", "localhost:5001", "http://localhost:20001/WebServices/public", "http://localhost:30001/rest/", "123", "Licenze: AA-001") );
	    	boerse.brokerAddNew( new Broker(2, 0f, "Zvonek Brokers Co.", "localhost:5002", "http://localhost:20002/WebServices/public", "http://localhost:30002/rest/", "456", "Licenze: AA-002") );
	    	boerse.brokerAddNew( new Broker(3, 0f, "Ayrat Brokers Co.", "localhost:5003", "http://localhost:20003/WebServices/public", "http://localhost:30003/rest/", "012", "Licenze: AA-003") );
    	}
    	catch (RemoteException e) {
    		
    	}
    	
        if (System.getSecurityManager() == null) {
            //System.setSecurityManager(new SecurityManager());
        }
        try {
            Registry registry = LocateRegistry.createRegistry(portRMI);
            
            BoersePublicAdapter publicAdapter = new BoersePublicAdapter(boerse);
            BoersePublic publicStub =
                    (BoersePublic) UnicastRemoteObject.exportObject(publicAdapter, 0);
            registry.rebind("public", publicStub);
            
            BoerseAdminAdapter adminAdapter = new BoerseAdminAdapter(boerse);
            BoerseAdmin adminStub =
                    (BoerseAdmin) UnicastRemoteObject.exportObject(adminAdapter, 0);
                registry.rebind("adminBoerse", adminStub);

            BoerseClient clientStub =
                    (BoerseClient) UnicastRemoteObject.exportObject(boerse, 0);
            registry.rebind("brokerBoerse", clientStub);

//            Registry registry = LocateRegistry.getRegistry();
//            registry.rebind(AdminObjectName, adminStub);

            Endpoint endpoint = Endpoint.publish("http://localhost:8080/WebServices/public", new BoersePublicAdapter(boerse));

            boolean status = endpoint.isPublished();
            System.out.println("Web service status = " + status);
            

            org.apache.cxf.jaxrs.JAXRSServerFactoryBean sf = new org.apache.cxf.jaxrs.JAXRSServerFactoryBean();
            sf.setResourceClasses(BoersePublicRESTful.class);
            sf.setResourceProvider(BoersePublicRESTful.class, 
                new org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider( new BoersePublicRESTful(boerse) ) );
            sf.setAddress("http://localhost:9999/rest/");
            org.apache.cxf.endpoint.Server server = sf.create();


            // destroy the server
            // uncomment when you want to close/destroy it
            // server.destroy();

//          boerse.Open();

            System.out.println("Die Boerse ist gestartet");
            
            
        }
        catch (AccessControlException e) {
            System.err.println("Boerse Start AccessControlException:");
            e.printStackTrace();
        }
        catch (RemoteException e) {
            System.err.println("Boerse Start RemoteException:");
            e.printStackTrace();
        }
        /*catch (java.rmi.AlreadyBoundException e) {
            System.err.println("Boerse Start java.rmi.AlreadyBoundException:");
            e.printStackTrace();
        }*/
    }
}

