package at.ac.univie.dse2016.stream.boerse;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Random;
import at.ac.univie.dse2016.stream.common.*;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jws.*;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * 
 * Diese Klasse wird von RMI, SOAP und RESTful (not HTML) verwendet
 *
 */

@WebService(targetNamespace = "http://boerse.stream.dse2016.univie.ac.at/", endpointInterface = "at.ac.univie.dse2016.stream.common.BoersePublic", portName = "BoersePublicAdapterPort", serviceName = "BoersePublicAdapterService")
public class BoerseSOAP implements BoerseClient, ExceptionListener  {

	private BoerseClient clientRMI;
	private Session session;
	private Destination tempDest;
	private MessageProducer producer;
	
    private static int ackMode;
    private static String queueName;
    static {
    	queueName = "msgs";
        ackMode = Session.AUTO_ACKNOWLEDGE;
    }
    private boolean transacted = false;

    
	public BoerseSOAP(BoerseClient clientRMI, String messageBrokerUrl) {
		this.clientRMI = clientRMI; 
	
        //Start Messaging
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(messageBrokerUrl);
        Connection connection;
        try {
            connection = connectionFactory.createConnection();
            connection.setExceptionListener(this); 
            
            connection.start();
            this.session = connection.createSession(transacted, ackMode);
            Destination queue = session.createQueue(queueName);
 
            //Setup a message producer to send message to the queue the server is consuming from
            this.producer = session.createProducer(queue);
            this.producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
 
            //Create a temporary queue that this client will listen for responses on then create a consumer
            //that consumes message from this temporary queue
            this.tempDest = session.createTemporaryQueue();

        } catch (JMSException e) {
            //Handle the exception appropriately
        }

	}
	
	/**
	 * Status der Boerse, Open, Closed oder Error
	 */
	public BoerseStatus getStatus() throws RemoteException {
		return clientRMI.getStatus();
	}
	
	/**
	 * Get all Emitents
	 */
	public java.util.ArrayList<Emittent> getEmittentsList() throws RemoteException {
		return clientRMI.getEmittentsList();
	}
	
	/**
	 * Normaleweise sollten Clients die Adresse ihrer Brokers kennen
	 * einfachheitshalber bekommt ein Client diese Adresse mithilfe dieser Methode, z.B. localhost:12001
	 */
	public String getBrokerNetworkAddress(Integer brokerId, NetworkResource resourceKind) throws RemoteException, IllegalArgumentException  {
		return clientRMI.getBrokerNetworkAddress(brokerId, resourceKind);
	}

	
	/**
	 * Auftrag eines Brokers stellen, ohne Sicherheitspruefung
	 */
	public Integer auftragAddNew(Integer brokerId, Auftrag auftrag) throws RemoteException, IllegalArgumentException {
		
		if (auftrag.getOwnerId() != brokerId)
			throw new IllegalArgumentException("Auftrag has not equal OwnerIds");

		int ret = -1;
		try {
	        //Now create the actual message you want to send
			ObjectMessage auftragMessage = session.createObjectMessage();
			auftragMessage.setObject(auftrag);
	
	        //Set the reply to field to the temp queue you created above, this is the queue the server
	        //will respond to
			auftragMessage.setJMSReplyTo(tempDest);
	
	        //Set a correlation ID so when you get a response you know which sent message the response is for
	        //If there is never more than one outstanding message to the server then the
	        //same correlation ID can be used for all the messages...if there is more than one outstanding
	        //message to the server you would presumably want to associate the correlation ID with this
	        //message somehow...a Map works good
	        String correlationId = this.createRandomString();
	        auftragMessage.setJMSCorrelationID(correlationId);
	        
	        //create MessageConsumer to consume syncronous answers with JMSCorrelationId
	        MessageConsumer responseConsumer = session.createConsumer(tempDest, "JMSCorrelationId="+correlationId);
	        
	        System.out.println("Sending ObjectMessage JMSCorrelationId="+correlationId);
	        this.producer.send(auftragMessage);

	        //wait for answer 1000 ms
            Message message = responseConsumer.receive(1000);
            
            try {
                Auftrag answer = (Auftrag) ((ObjectMessage)message).getObject();
                correlationId = message.getJMSCorrelationID();
                System.out.println("answer for = " + correlationId);
                responseConsumer.close();

                ret = answer.getId();
                
            } catch (JMSException e) {
            	e.printStackTrace();
            }
		}
		catch (Exception e) {
		}

        return ret;

		//return clientRMI.auftragAddNew(brokerId, auftrag);
	}
	
	
    private String createRandomString() {
        Random random = new Random(System.currentTimeMillis());
        long randomLong = random.nextLong();
        return Long.toHexString(randomLong);
    }

	/**
	 * Auftrag eines Brokers zurueckrufen, ohne Sicherheitspruefung
	 */
	public void auftragCancel(Integer brokerId, Integer auftragId) throws RemoteException, IllegalArgumentException {
		try {
	        //Now create the actual message you want to send
			ObjectMessage auftragMessage = session.createObjectMessage();
			
			Auftrag auftrag = new Auftrag(auftragId, brokerId, false, "", 0, 0);
			auftrag.setStatus(AuftragStatus.Canceled);
			
			auftragMessage.setObject( auftrag );
	
	        //Set the reply to field to the temp queue you created above, this is the queue the server
	        //will respond to
			auftragMessage.setJMSReplyTo(tempDest);
	
	        //Set a correlation ID so when you get a response you know which sent message the response is for
	        //If there is never more than one outstanding message to the server then the
	        //same correlation ID can be used for all the messages...if there is more than one outstanding
	        //message to the server you would presumably want to associate the correlation ID with this
	        //message somehow...a Map works good
	        String correlationId = this.createRandomString();
	        auftragMessage.setJMSCorrelationID(correlationId);
	        
	        //create MessageConsumer to consume syncronous answers with JMSCorrelationId
	        MessageConsumer responseConsumer = session.createConsumer(tempDest, "JMSCorrelationId="+correlationId);
	        
	        System.out.println("Sending ObjectMessage JMSCorrelationId="+correlationId);
	        this.producer.send(auftragMessage);

	        //wait for answer 1000 ms
            Message message = responseConsumer.receive(1000);
            
            try {
                Auftrag answer = (Auftrag) ((ObjectMessage)message).getObject();
                correlationId = message.getJMSCorrelationID();
                System.out.println("answer for = " + correlationId);
                responseConsumer.close();       
                if (answer.getStatus() != AuftragStatus.Canceled)
                	throw new IllegalArgumentException("Exception occured");
            } catch (JMSException e) {
            	e.printStackTrace();
            }
		}
		catch (Exception e) {
        	e.printStackTrace();
		}

		//clientRMI.auftragCancel(brokerId, auftragId);
	}
	
	/**
	 * Einzahlen/auszahlen von einem Broker, ohne Sicherheitspruefung
	 * normaleweise muss es automatisch ausgefuert werden, wenn das Geld zum Tradingkonto des Brokers eingeht
	 * amount kann +/- sein (einzahlen/auszahlen)
	 * einfachheitshalber koennen die Brokers diese Methode selbst aufrufen
	 * wenn sie das machen, dann heisst es das sie das Geld zu/von ihrem Konto ueberweisen
	 */
	public void tradingAccount(Integer brokerId, float amount) throws RemoteException, IllegalArgumentException {
		clientRMI.tradingAccount(brokerId, amount);
	}

	/**
	 * Einzahlen/auszahlen eines Emittens (z.B. Aktien) von einem Broker, ohne Sicherheitspruefung
	 * normaleweise muss es automatisch ausgefuert werden, wenn die Aktien zum Tradingkonto des Brokers eingehen
	 * anzahl kann +/- sein (einzahlen/auszahlen)
	 * einfachheitshalber koennen die Brokers diese Methode selbst aufrufen
	 * wenn sie das machen, dann heisst es das sie die Aktien zu/von ihrem Konto ueberweisen 
	 */
	public void tradingAccount(Integer brokerId, Integer tickerId, Integer anzahl) throws RemoteException, IllegalArgumentException {
		clientRMI.tradingAccount(brokerId, tickerId, anzahl);
	}
	
	/**
	 * Get current State des Brokers, ohne Sicherheitspruefung
	 */
	public Broker getState(Integer brokerId) throws RemoteException, IllegalArgumentException {
		return clientRMI.getState(brokerId);
	}

	public java.util.TreeSet<Auftrag> getAuftraege(Integer brokerId) throws RemoteException, IllegalArgumentException {
		return clientRMI.getAuftraege(brokerId);
	}

	public java.util.TreeSet<Transaction> getTransaktionen(Integer brokerId) throws RemoteException, IllegalArgumentException {
		return clientRMI.getTransaktionen(brokerId);
	}
	
	public java.util.TreeSet<Transaction> getTransaktionen(Integer brokerId, Date afterDate) throws RemoteException, IllegalArgumentException {
		return clientRMI.getTransaktionen(brokerId, afterDate);
	}

	
	public Report getReport(Integer brokerId) throws RemoteException, IllegalArgumentException {
		return clientRMI.getReport(brokerId);
	}

	public void onException(JMSException e) { 
		  System.out.println("JMS Exception occurred"); 
	}
}
