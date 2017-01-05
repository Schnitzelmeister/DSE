package at.ac.univie.dse2016.stream.boerse;

import java.net.*;
import java.io.*;
import java.security.*;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.*;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;

import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;

import at.ac.univie.dse2016.stream.common.*;


public final class BoerseServer implements BoerseAdmin, BoerseClient, MessageListener, ExceptionListener  {
	
	public BoerseServer(int portUDP, String messageBrokerUrl, String dataPath) {
		this.portUDP = portUDP;
		this.messageBrokerUrl = messageBrokerUrl;
		status = BoerseStatus.Closed;
		marketPrices = new java.util.TreeMap<Integer, Float>();
		emittentSections = new java.util.TreeMap< Integer /* emittentId */, EmittentSection >();

		//init DAO
		poolDAO = new PoolDAO( dataPath );
		System.out.println("Path to DATA Folder = "+ dataPath);
		System.out.println("Die Daten werden in diesem Folder gespeichert");
	}

	/**
	 * DAO Objects
	 */
	private PoolDAO poolDAO;
	
	/**
	 * Status der Boerse, Open, Closed oder Error
	 */
	private BoerseStatus status;
	public BoerseStatus getStatus() { return status; }

	/**
	 * Die Preise, mit denen den letzten Auftrag ueberwiesen wurden
	 */
	private java.util.TreeMap<Integer, Float> marketPrices;

	/**
	 * Emittent Sections
	 */
	private java.util.TreeMap< Integer /* emittentId */, EmittentSection > emittentSections;
	

	/**
	 * UDP Port
	 */
	private int portUDP;

	/**
	 * AddNew Emittent, Admin's Function
	 */
	public Integer emittentAddNew(Emittent emittent) throws RemoteException, IllegalArgumentException {
		synchronized(this.poolDAO.getEmittentDAO()) {
			this.poolDAO.getEmittentDAO().speichereItem(emittent);

			marketPrices.put(emittent.getId(), -1f);
			emittentSections.put(emittent.getId(), new EmittentSection() );
		}
		return emittent.getId();
	}

	/**
	 * Edit Emittent, Admin's Function
	 */
	public synchronized void emittentEdit(Emittent emittent) throws RemoteException, IllegalArgumentException {
		synchronized(this.poolDAO.getEmittentDAO()) {
			Emittent _dest = this.poolDAO.getEmittentDAO().getItemById(emittent.getId());
			_dest.setName(emittent.getName());
			this.poolDAO.getEmittentDAO().speichereItem(_dest);
		}
	}
	
	/**
	 * Lock Emittent, not Remove, Admin's Function
	 */
	public void emittentLock(Emittent emittent) throws RemoteException, IllegalArgumentException {
		synchronized(this.poolDAO.getEmittentDAO()) {
			this.poolDAO.getEmittentDAO().loescheItem(emittent);
			marketPrices.remove( emittent.getId() );
			emittentSections.remove( emittent.getId() );
		}
	}

	/**
	 * Get all Emittents, Admin's Function
	 */
	public java.util.ArrayList<Emittent> getEmittentsList() throws RemoteException {
		return new java.util.ArrayList<Emittent>( this.poolDAO.getEmittentDAO().getItems().values() );
	}
	
	/**
	 * AddNew Broker, Admin's Function
	 */
	public Integer brokerAddNew(Broker broker) throws RemoteException, IllegalArgumentException {
		synchronized(this.poolDAO.getBrokerDAO()) {
			this.poolDAO.getBrokerDAO().speichereItem(broker);
		}
		return broker.getId();
	}

	/**
	 * Edit Broker, Admin's Function
	 */
	public void brokerEdit(Broker broker) throws RemoteException, IllegalArgumentException {
		synchronized(this.poolDAO.getBrokerDAO()) {
			this.poolDAO.getBrokerDAO().speichereItem(broker);
		}
	}
	
	/**
	 * Lock Broker, not Remove, Admin's Function
	 */
	public void brokerLock(Broker broker) throws RemoteException, IllegalArgumentException {
		synchronized(this.poolDAO.getBrokerDAO()) {
			this.poolDAO.getBrokerDAO().loescheItem(broker);
		}
	}
	
	/**
	 * Get all Brokers, Admin's Function
	 */
	public java.util.ArrayList<Broker> getBrokersList() throws RemoteException {
		return new java.util.ArrayList<Broker>(this.poolDAO.getBrokerDAO().getItems().values());
	}
	
	/**
	 * Schliesst die Boerse ab, normaleweise am Ende des Tages, Admin's Function oder ScheduleJob
	 */
	public synchronized void Close() throws RemoteException {
		if (status != BoerseStatus.Open)
			return;

		status = BoerseStatus.Closed;

        stopMessaging();
		stopFeedUDP();
		
		threadUDP.interrupt();
	}

	/**
	 * Oeffnet die Boerse, Admin's Function oder ScheduleJob
	 */
	public synchronized void Open() throws RemoteException {
		sessionCounter = new AtomicInteger();

		//start UDP
        threadUDP = new Thread() {
            public void run() {
            	startFeedUDP();
            }
        };

        threadUDP.start();
        
        //start Messaging
        startMessaging();
        
		status = BoerseStatus.Open;
	}
	
	
	public java.util.TreeSet<Auftrag> getAuftraege(Integer brokerId) throws RemoteException, IllegalArgumentException {
		if ( !this.poolDAO.getBrokerDAO().containsKey(brokerId) )
			throw new IllegalArgumentException("Client " + String.valueOf(brokerId) + " does not exist");

		return this.poolDAO.getAuftragDAO().getAuftraege(brokerId);
	}

	public java.util.TreeSet<Transaction> getTransaktionen(Integer brokerId) throws RemoteException, IllegalArgumentException {
		if ( !this.poolDAO.getBrokerDAO().containsKey(brokerId) )
			throw new IllegalArgumentException("Client " + String.valueOf(brokerId) + " does not exist");

		java.util.TreeSet<Transaction> ret = new java.util.TreeSet<Transaction>();
		
		for (Auftrag a : this.poolDAO.getAuftragDAO().getAuftraege(brokerId)) {
			for (Transaction t : this.poolDAO.getTransactionDAO().getTransactions(a.getId())) {
				ret.add(t);
			}
		}
		
		return ret;
	}
	
	public java.util.TreeSet<Transaction> getTransaktionen(Integer brokerId, Date afterDate) throws RemoteException, IllegalArgumentException {
		if ( !this.poolDAO.getBrokerDAO().containsKey(brokerId) )
			throw new IllegalArgumentException("Client " + String.valueOf(brokerId) + " does not exist");

		java.util.TreeSet<Transaction> ret = new java.util.TreeSet<Transaction>();
		
		for (Auftrag a : this.poolDAO.getAuftragDAO().getAuftraege(brokerId)) {
			for (Transaction t : this.poolDAO.getTransactionDAO().getTransactions(a.getId())) {
				if (t.getDateCommitted().after(afterDate))
					ret.add(t);
			}
		}
		
		return ret;

		//return new java.util.TreeSet<Transaction> ( transactionLog.get(brokerId).headSet( new Transaction(0, 0, 0, afterDate), true ) );
	}

	/**
	 * Get current State des Brokers
	 */
	public Broker getState(Integer brokerId) throws RemoteException, IllegalArgumentException {
		if ( !this.poolDAO.getBrokerDAO().containsKey(brokerId) )
			throw new IllegalArgumentException("Client with id=" + brokerId + " does not exist");

		return this.poolDAO.getBrokerDAO().getItemById(brokerId);
	}
	/**
	 * Auftrag eines Brokers stellen
	 */
	public Integer auftragAddNew(Integer brokerId, Auftrag auftrag) throws RemoteException, IllegalArgumentException {
		if ( status != BoerseStatus.Open )
			throw new IllegalArgumentException("Die Boerse ist geschlossen");
			
		if ( !this.poolDAO.getBrokerDAO().containsKey(brokerId) )
			throw new IllegalArgumentException("Broker with id=" + brokerId + " does not exist");

		if ( !this.poolDAO.getEmittentDAO().containsTicker(auftrag.getTicker()) )
			throw new IllegalArgumentException("Ticker " + auftrag.getTicker() + " does not exist");

		if (auftrag.getOwnerId() != brokerId)
			throw new IllegalArgumentException("Auftrag has not equal OwnerIds");

		Integer tickerId = this.poolDAO.getEmittentDAO().getEmittentByTicker( auftrag.getTicker() ).getId();
		boolean buy = auftrag.getKaufen();
		int anzahl = auftrag.getAnzahl();
		float bedingung = auftrag.getBedingung();
		Integer bedingungInteger = Math.round(bedingung * 10000);	//es ist zulaessig nur 4 Ziffern nach der Komma zu stellen
		int newAuftragId = -1;
		
		Broker broker = this.poolDAO.getBrokerDAO().getItemById(brokerId);
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
			setToLog(brokerId, auftrag);
			newAuftragId = auftrag.getId();
			
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
						
						java.util.TreeMap<Integer, java.util.TreeSet<Auftrag> > _map;
						if (buy)
							_map = emittentSection.buy;
						else
							_map = emittentSection.sell;
				
						if (!_map.containsKey(bedingungInteger))
							_map.put(bedingungInteger, new java.util.TreeSet<Auftrag>());					
						
						_map.get(bedingungInteger).add(auftrag);
						
						if (buy)
							broker.setDisponibelstand(-bedingung * anzahl);
						else
							broker.setDisponibelstand(tickerId, -anzahl);
						
						//send asynchrone
						sendFeedMsg(new FeedMsg(emittentSection.msgCounter.incrementAndGet(), newAuftragId, tickerId, buy, anzahl, auftrag.getBedingung()/*preis*/, /*status*/AuftragStatus.Accepted));

						return newAuftragId;
					}
				}
				
				//commit Transaction
				java.util.TreeMap<Integer, java.util.TreeSet<Auftrag> > _map;
				if (buy)
					_map = emittentSection.sell;
				else
					_map = emittentSection.buy;
				
		    	for(java.util.Iterator< java.util.Map.Entry< Integer, java.util.TreeSet<Auftrag> >> ito = _map.entrySet().iterator(); ito.hasNext(); ) {
		    		java.util.Map.Entry< Integer, java.util.TreeSet<Auftrag> > e = ito.next();	  

		    		for(java.util.Iterator< Auftrag > it = e.getValue().iterator(); it.hasNext(); ) {
		    			Auftrag a = it.next();	  
						Broker secondBroker = this.poolDAO.getBrokerDAO().getItemById( a.getOwnerId() );
						
						//test, ob es genug geld, aktien bei beiden brokers sind

						//der  Kauf
						if (buy) {
							// Marketpreis, ohne Bedingung
							if ( bedingung == -1 ) {
								//kein Geld mehr bei Kauefer
								if (broker.getDisponibelstand() < a.getBedingung()) {
									if (!committed)
										throw new IllegalArgumentException("Not enough money");

									auftrag.setStatus(AuftragStatus.Bearbeitet);
									
									//send asynchrone
									sendFeedMsg(new FeedMsg(emittentSection.msgCounter.incrementAndGet(), newAuftragId, tickerId, buy, anzahl, auftrag.getBedingung()/*preis*/, /*status*/AuftragStatus.Bearbeitet));
									
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

										a.setStatus(AuftragStatus.Bearbeitet);
										it.remove();
									}
									else {
										a.setAnzahl(a.getAnzahl() - anzahl);
									}
	
									auftrag.setStatus(AuftragStatus.Bearbeitet);
									
									//send asynchrone
									sendFeedMsg(new FeedMsg(emittentSection.msgCounter.incrementAndGet(), newAuftragId, tickerId, buy, anzahl, auftrag.getBedingung()/*preis*/, /*status*/AuftragStatus.Bearbeitet));

									return newAuftragId;
								}
								//commit teilweise
								else {
									committed = true;
									commitAuftrage(auftrag, broker, a, secondBroker, tickerId, a.getBedingung(), a.getAnzahl(), true, emittentSection.msgCounter.incrementAndGet(), AuftragStatus.Bearbeitet);
									anzahl -= a.getAnzahl();

									a.setStatus(AuftragStatus.Bearbeitet);
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
											a.setStatus(AuftragStatus.Bearbeitet);
											it.remove();
										}
										else {
											a.setAnzahl(a.getAnzahl() - anzahl);
										}
	
										auftrag.setStatus(AuftragStatus.Bearbeitet);

										//send asynchrone
										sendFeedMsg(new FeedMsg(emittentSection.msgCounter.incrementAndGet(), newAuftragId, tickerId, buy, anzahl, auftrag.getBedingung()/*preis*/, /*status*/AuftragStatus.Bearbeitet));

										return newAuftragId;
									}
									//commit teilweise
									else {
										committed = true;
										commitAuftrage(auftrag, broker, a, secondBroker, tickerId, a.getBedingung(), a.getAnzahl(), true, emittentSection.msgCounter.incrementAndGet(), AuftragStatus.Bearbeitet);
										anzahl -= a.getAnzahl();
										
										a.setStatus(AuftragStatus.Bearbeitet);
										it.remove();

									}
								}
								//bedingung nicht passt - add to Section, was bleibt
								else {
									auftrag.setAnzahl(anzahl);
									
									broker.setDisponibelstand(-bedingung * anzahl);

									if (!emittentSection.buy.containsKey(bedingungInteger))
										emittentSection.buy.put(bedingungInteger, new java.util.TreeSet<Auftrag>());
									emittentSection.buy.get(bedingungInteger).add(auftrag);
									
									//send asynchrone
									sendFeedMsg(new FeedMsg(emittentSection.msgCounter.incrementAndGet(), newAuftragId, tickerId, true, anzahl, auftrag.getBedingung()/*preis*/, /*status*/AuftragStatus.Accepted));

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
										a.setStatus(AuftragStatus.Bearbeitet);
										it.remove();
									}
									else {
										a.setAnzahl(a.getAnzahl() - anzahl);
									}
	
									auftrag.setStatus(AuftragStatus.Bearbeitet);
									
									//send asynchrone
									sendFeedMsg(new FeedMsg(emittentSection.msgCounter.incrementAndGet(), newAuftragId, tickerId, buy, anzahl, auftrag.getBedingung()/*preis*/, /*status*/AuftragStatus.Bearbeitet));

									return newAuftragId;
								}
								//commit teilweise
								else {
									committed = true;
									commitAuftrage(auftrag, broker, a, secondBroker, tickerId, a.getBedingung(), a.getAnzahl(), false, emittentSection.msgCounter.incrementAndGet(), AuftragStatus.Bearbeitet);
									anzahl -= a.getAnzahl();
									a.setStatus(AuftragStatus.Bearbeitet);
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
											a.setStatus(AuftragStatus.Bearbeitet);
											it.remove();
										}
										else {
											a.setAnzahl(a.getAnzahl() - anzahl);
										}
	
										auftrag.setStatus(AuftragStatus.Bearbeitet);
										
										//send asynchrone
										sendFeedMsg(new FeedMsg(emittentSection.msgCounter.incrementAndGet(), newAuftragId, tickerId, buy, anzahl, auftrag.getBedingung()/*preis*/, /*status*/AuftragStatus.Bearbeitet));
										
										return newAuftragId;
									}
									//commit teilweise
									else {
										committed = true;
										commitAuftrage(auftrag, broker, a, secondBroker, tickerId, a.getBedingung(), a.getAnzahl(), false, emittentSection.msgCounter.incrementAndGet(), AuftragStatus.Bearbeitet);
										anzahl -= a.getAnzahl();
										a.setStatus(AuftragStatus.Bearbeitet);
										it.remove();
									}
								}
								//bedingung nicht passt - add to Section, was bleibt
								else {
									auftrag.setAnzahl(anzahl);
									
									broker.setDisponibelstand(tickerId, -anzahl);

									if (!emittentSection.sell.containsKey(bedingungInteger))
										emittentSection.sell.put(bedingungInteger, new java.util.TreeSet<Auftrag>());
				
									emittentSection.sell.get(bedingungInteger).add(auftrag);
									
									//send asynchrone
									sendFeedMsg(new FeedMsg(emittentSection.msgCounter.incrementAndGet(), newAuftragId, tickerId, false, anzahl, auftrag.getBedingung()/*preis*/, /*status*/AuftragStatus.Accepted));

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
						if (!emittentSection.buy.containsKey(bedingungInteger))
							emittentSection.buy.put(bedingungInteger, new java.util.TreeSet<Auftrag>());
	
						emittentSection.buy.get(bedingungInteger).add(auftrag);
						
						broker.setDisponibelstand(-bedingung * anzahl);
		    		}
		    		else {
						if (!emittentSection.sell.containsKey(bedingungInteger))
							emittentSection.sell.put(bedingungInteger, new java.util.TreeSet<Auftrag>());
	
						emittentSection.sell.get(bedingungInteger).add(auftrag);
						
						broker.setDisponibelstand(tickerId, -anzahl);
		    		}
		    		
					//send asynchrone
					sendFeedMsg(new FeedMsg(emittentSection.msgCounter.incrementAndGet(), newAuftragId, tickerId, auftrag.getKaufen(), anzahl, auftrag.getBedingung()/*preis*/, /*status*/AuftragStatus.Accepted));
		    	}
		    	else {
					if (_map.size() == 0)
						throw new IllegalArgumentException("No Orders");
		    		if (!committed)
						throw new IllegalArgumentException("Not enough money");

					auftrag.setStatus(AuftragStatus.TeilweiseBearbeitet);
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
		
		this.poolDAO.getTransactionDAO().speichereItem(new Transaction(auftrag1.getId(), auftrag2.getId(), anzahl, preis));
	}
	
	/**
	 * Auftrag eines Brokers zurueckrufen
	 */
	public void auftragCancel(Integer brokerId, Integer auftragId) throws RemoteException, IllegalArgumentException {
		if ( status != BoerseStatus.Open )
			throw new IllegalArgumentException("Die Boerse ist geschlossen");
		
		if ( !this.poolDAO.getBrokerDAO().containsKey(brokerId) )
			throw new IllegalArgumentException("Broker with id=" + brokerId + " does not exist");

		Broker broker = this.poolDAO.getBrokerDAO().getItemById(brokerId);
		java.util.TreeMap<Integer, Auftrag> auftraege = broker.getAuftraegeList();	
		if ( !auftraege.containsKey(auftragId) )
			throw new IllegalArgumentException("Auftrag with id=" + auftragId + " can not be canceled");
		
		Auftrag auftrag = auftraege.get(auftragId);
		if (auftrag.getOwnerId() != brokerId)
			throw new IllegalArgumentException("Auftrag with id=" + auftragId + " can not be canceled");
		
		int tickerId = this.poolDAO.getEmittentDAO().getEmittentByTicker( auftrag.getTicker() ).getId();
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
	}
	
	private void setToLog(int brokerId, Auftrag auftrag) {
		this.poolDAO.getAuftragDAO().speichereItem(auftrag);
	}
	
	/**
	 * Einzahlen/auszahlen von einem Broker
	 * normaleweise muss es automatisch ausgefuert werden, wenn das Geld zum Tradingkonto des Brokers eingeht
	 * amount kann +/- sein (einzahlen/auszahlen)
	 * einfachheitshalber koennen die Brokers diese Methode selbst aufrufen
	 * wenn sie das machen, dann heisst es das sie das Geld zu/von ihrem Konto ueberweisen
	 */
	public void tradingAccount(Integer brokerId, float amount) throws RemoteException, IllegalArgumentException {
		if ( !this.poolDAO.getBrokerDAO().containsKey(brokerId) )
			throw new IllegalArgumentException("Broker with id=" + brokerId + " does not exist");

		Broker broker = this.poolDAO.getBrokerDAO().getItemById(brokerId);
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
		if ( !this.poolDAO.getBrokerDAO().containsKey(brokerId) )
			throw new IllegalArgumentException("Broker with id=" + brokerId + " does not exist");

		Broker broker = this.poolDAO.getBrokerDAO().getItemById(brokerId);
		synchronized(broker) {
			//check, ob aktive Auftrage existieren
			if (anzahl < 0) {
				for(Auftrag a : broker.getAuftraegeList().values()) {
					if ( this.poolDAO.getEmittentDAO().getEmittentByTicker( a.getTicker() ).getId() == tickerId )
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
//		getEmittentsList().get(0).setName( resourceKind.toString() + "==" + NetworkResource.UDP.toString() );

		if ( resourceKind == NetworkResource.UDP )
			throw new IllegalArgumentException("Broker has no UDP");
		
		if ( !this.poolDAO.getBrokerDAO().containsKey(brokerId) )
			throw new IllegalArgumentException("Broker with id=" + brokerId + " does not exist");

		if (resourceKind == NetworkResource.REST)
			return this.poolDAO.getBrokerDAO().getItemById(brokerId).getNetworkRESTAddress();
		if (resourceKind == NetworkResource.SOAP)
			return this.poolDAO.getBrokerDAO().getItemById(brokerId).getNetworkSOAPAddress();
		//if (resourceKind.getNumVal() == NetworkResource.RMI.getNumVal())
		return this.poolDAO.getBrokerDAO().getItemById(brokerId).getNetworkRMIAddress();
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
    	System.out.println( "send msg = " + msg.getId() );

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
	    		if ( s.lastConnectionTime.after(calendar.getTime()) ) {
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
    	System.out.println( "sendUDPMsg" );
	    try {
	    	DatagramPacket reply = new DatagramPacket(bos.toByteArray(), bos.size(),
	    			session.address, session.port);
	    	
	    	socketUDP.send(reply);
	    	System.out.println( "reply Addr = " + reply.getAddress().toString() );
	    	System.out.println( "reply Port = " + reply.getPort() );
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
            //System.out.println( "requestPacket Addr = " + requestPacket.getAddress().toString() );
            //System.out.println( "requestPacket Port = " + requestPacket.getPort() );

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
	
	
    private static int exec(@SuppressWarnings("rawtypes") Class klass) throws IOException, InterruptedException {
		String javaHome = System.getProperty("java.home");
		String javaBin = javaHome +
		File.separator + "bin" +
		File.separator + "java";
		String classpath = System.getProperty("java.class.path");
		String className = klass.getCanonicalName();
		
		ProcessBuilder builder = new ProcessBuilder(
		javaBin, "-cp", classpath, className);
		
		Process process = builder.start();
		//process.waitFor();
		return process.exitValue();
	}
	
	//Messaging Stuff
    private static int ackMode;
    private static String queueName;
    static {
    	queueName = "msgs";
        ackMode = Session.AUTO_ACKNOWLEDGE;
    }
    
    private String messageBrokerUrl;
    
    private Session session;
    private boolean transacted = false;
    private MessageProducer replyProducer;
    private MessageConsumer consumer;
    private Connection connection;
    
    private void startMessaging() {
        try {
        	BrokerService broker = new BrokerService();
            broker.setPersistent(false);
            broker.setUseJmx(false);
            broker.addConnector(messageBrokerUrl);
            broker.start();

            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(messageBrokerUrl);
            
            try {
                connection = connectionFactory.createConnection();
                connection.setExceptionListener(this); 
                connection.start();
                
                this.session = connection.createSession(this.transacted, ackMode);
                Destination queue = this.session.createQueue(queueName);
     
                //Setup a message producer to respond to messages from clients, we will get the destination
                //to send to from the JMSReplyTo header field from a Message
                this.replyProducer = this.session.createProducer(null);
                this.replyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
     
                //Set up a consumer to consume messages off of the admin queue
                consumer = this.session.createConsumer(queue);
                consumer.setMessageListener(this);
                
            } catch (JMSException e) {
            	e.printStackTrace();
            }
            
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }

    private void stopMessaging() {
    	try {
    		this.consumer.close();
    		this.replyProducer.close();
    		this.session.close();
    		this.connection.close();
    	}
    	catch (Exception e) {
            e.printStackTrace();
    	}
    }
    
    public void onMessage(Message message) {
        if (!(message instanceof ObjectMessage))
        	return;
        
    	System.out.println("incoming ObjectMessage");

    	try {
            Auftrag incomingAuftrag = (Auftrag) ((ObjectMessage)message).getObject();
            int id = incomingAuftrag.getId();
            
            if (incomingAuftrag.getStatus() == AuftragStatus.Init) {
            	try {
            		id = this.auftragAddNew(incomingAuftrag.getOwnerId(), incomingAuftrag);
            	}
            	catch(Exception e) {
            	}
            }
            else if (incomingAuftrag.getStatus() == AuftragStatus.Canceled) {
            	try {
            		this.auftragCancel(incomingAuftrag.getOwnerId(), id);
            	}
            	catch(Exception e) {
            	}
            }

            incomingAuftrag = this.poolDAO.getAuftragDAO().getItemById(id);
            ObjectMessage response = this.session.createObjectMessage();
            Auftrag ret = new Auftrag(incomingAuftrag.getId(), incomingAuftrag.getOwnerId(), incomingAuftrag.getKaufen(), incomingAuftrag.getTicker(), incomingAuftrag.getAnzahl(), incomingAuftrag.getBedingung() );
            ret.setStatus(incomingAuftrag.getStatus());
            response.setObject( ret );
 
            //Set the correlation ID from the received message to be the correlation id of the response message
            //this lets the client identify which message this is a response to if it has more than
            //one outstanding message to the server
            response.setJMSCorrelationID(message.getJMSCorrelationID());

        	System.out.println("send answer");

            //Send the response to the Destination specified by the JMSReplyTo field of the received message,
            //this is presumably a temporary queue created by the client
            this.replyProducer.send(message.getJMSReplyTo(), response);
        } catch (JMSException e) {
        	e.printStackTrace();
        }
    }
	
	public void onException(JMSException e) { 
		  System.out.println("JMS Exception occurred"); 
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

    	//default port RMI
    	String messageBrokerUrl = "tcp://localhost:61616";
    	if (args.length > 2)
    		messageBrokerUrl = args[2];
    	
    	BoerseServer boerse = new BoerseServer(portUDP, messageBrokerUrl, System.getProperty("user.dir") );
    	   	
    	//initial values
    	try {
    		if (boerse.poolDAO.getEmittentDAO().getItems().size() == 0) {
		    	boerse.emittentAddNew( new Emittent("AAPL", "Apple Inc.") );
		    	boerse.emittentAddNew( new Emittent("RDSA", "Royal Dutch Shell") );
		    	boerse.brokerAddNew( new Broker("Daniil Brokers Co.", "localhost:5001", "http://localhost:20001/WebServices/public", "http://localhost:30001/rest/", "123", "Licenze: AA-001") );
		    	boerse.brokerAddNew( new Broker("Zvonek Brokers Co.", "localhost:5002", "http://localhost:20002/WebServices/public", "http://localhost:30002/rest/", "456", "Licenze: AA-002") );
		    	boerse.brokerAddNew( new Broker("Ayrat Brokers Co.", "localhost:5003", "http://localhost:20003/WebServices/public", "http://localhost:30003/rest/", "012", "Licenze: AA-003") );
    		}
    	}
    	catch (RemoteException e) {
    		
    	}
    	
        if (System.getSecurityManager() == null) {
            //System.setSecurityManager(new SecurityManager());
        }
        try {
        	
        	//RMI
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

            /*
            try {
            	Runtime.getRuntime().exec("notepad.exe");
            }
            catch(Exception e) {
            }
            */
            /*
            try {
            	exec(SOAPStart.class);
            }
            catch(Exception e) {
            }*/
            
//            Registry registry = LocateRegistry.getRegistry();
//            registry.rebind(AdminObjectName, adminStub);

            /*
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
*/
            
            boerse.Open();

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

