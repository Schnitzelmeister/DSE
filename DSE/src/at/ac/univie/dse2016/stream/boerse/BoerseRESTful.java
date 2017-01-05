package at.ac.univie.dse2016.stream.boerse;

import at.ac.univie.dse2016.stream.common.*;

import java.rmi.RemoteException;
import java.util.Random;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;

import org.apache.activemq.ActiveMQConnectionFactory;

@Path("/public")
public class BoerseRESTful implements ExceptionListener {
	
	private BoerseClient clientRMI;
	private BoerseAdmin adminRMI;
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
    
	public BoerseRESTful(BoerseClient clientRMI, BoerseAdmin adminRMI, String messageBrokerUrl) {
		this.clientRMI = clientRMI; 
		this.adminRMI = adminRMI; 
		
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
	@GET
	@Path("/status")
	@Produces(MediaType.TEXT_HTML)
	public String getStatusHTML() throws RemoteException {
		return "<html><title>BoerseStatus</title><body><h1>BoerseStatus = " + clientRMI.getStatus() + "</h1></body></html>";
	}
	
	

	@GET
	@Path("/status")
	@Produces(MediaType.TEXT_PLAIN)
	public String getStatus() throws RemoteException {
		return clientRMI.getStatus().toString();
	}

	/**
	 * Get all Emitents
	 */
	@GET
	@Path("/emittents")
	@Produces(MediaType.TEXT_HTML)
	public String getEmittentsListHTML() throws RemoteException {
		StringBuilder sb = new StringBuilder("<html><title>BoerseStatus</title><body><ul>");
		for(Emittent e : clientRMI.getEmittentsList()) {
			sb.append("<li><div>");
			sb.append(e.getTicker());
			sb.append(" - ");
			sb.append(e.getName());
			sb.append("<form action=\"emittent\\"+ e.getId() + "\" method=\"post\"><input type=\"hidden\" name=\"ticker\" value=\""+ e.getTicker() +"\">Name:<input type=\"text\" name=\"name\" value=\""+ e.getName() +"\"><input type=\"submit\" value=\"Update\"></form>"
					+ "<form action=\"emittent\\"+ e.getId() + "\\lock\" method=\"post\"><input type=\"hidden\" name=\"ticker\" value=\""+ e.getTicker() +"\"><input type=\"submit\" value=\"Remove\"></form>"
					+ "</div></li>");
		}
		sb.append("</ul><br><form action=\"add_new_emittent\" method=\"post\">Ticker: <input type=\"text\" name=\"ticker\"><br>Name: <input type=\"text\" name=\"name\"><br><input type=\"submit\" value=\"Add New\">"
		+ "</form></body></html>");
		return sb.toString();
	}
	
	@GET
	@Path("/emittents")
	@Produces(MediaType.TEXT_PLAIN)
	public String getEmittentsList() throws RemoteException {
		StringBuilder sb = new StringBuilder();
		for(Emittent e : clientRMI.getEmittentsList()) {
			sb.append(e.getTicker());
			sb.append("; ");
		}
		return sb.substring(0, sb.length() - 2);
	}
	
	/**
	 * Normaleweise sollten Clients die Adresse ihrer Brokers kennen
	 * einfachheitshalber bekommt ein Client diese Adresse mithilfe dieser Methode, z.B. localhost:12001
	 */
	private NetworkResource getNetworkResourceFromId(Integer id) {
		if (NetworkResource.UDP.getNumVal() == id.intValue())
			return NetworkResource.UDP;
		if (NetworkResource.SOAP.getNumVal() == id.intValue())
			return NetworkResource.SOAP;
		if (NetworkResource.REST.getNumVal() == id.intValue())
			return NetworkResource.REST;

		return NetworkResource.RMI;
	}
	
	@GET
	@Path("/broker_network_address/{broker_id}/{resourceKind}")
	@Produces(MediaType.TEXT_HTML)
	public String getBrokerNetworkAddressHTML(@PathParam("broker_id") Integer brokerId, @PathParam("resourceKind") Integer resourceKind) throws RemoteException {
		return "<html><title>broker_network_address</title><body><h1>broker_network_address (" + getNetworkResourceFromId(resourceKind).toString() + ") = " + clientRMI.getBrokerNetworkAddress(brokerId, getNetworkResourceFromId(resourceKind)) + "</h1></body></html>";
	}

	@GET
	@Path("/broker_network_address/{broker_id}/{resourceKind}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getBrokerNetworkAddress(@PathParam("broker_id") Integer brokerId, @PathParam("resourceKind") Integer resourceKind) throws RemoteException {
		return clientRMI.getBrokerNetworkAddress(brokerId,  getNetworkResourceFromId(resourceKind));
	}
	
	
	
	/**
	 * AddNew Emittent, Admin's Function
	 */
	@POST
    @Path("/add_new_emittent")
	@Produces(MediaType.TEXT_HTML)
	public String emittentAddNewHTML(@FormParam("ticker") String ticker, @FormParam("name") String name) throws RemoteException, IllegalArgumentException {
		try {
			adminRMI.emittentAddNew( new Emittent(ticker, name) );
			return getEmittentsListHTML();
		}
		catch (Exception e) {
			return e.getMessage();
		}
	}

	@POST
    @Path("/add_new_emittent")
	@Produces(MediaType.TEXT_PLAIN)
	public String emittentAddNew(@FormParam("ticker") String ticker, @FormParam("name") String name) throws RemoteException, IllegalArgumentException {
		StringBuilder sb = new StringBuilder();
		try {
			sb.append( "ok - " + adminRMI.emittentAddNew( new Emittent(ticker, name) ).toString() );
		}
		catch (Exception e) {
			sb.append( e.getMessage() );
		}
		return sb.toString(); 
	}

	/**
	 * Edit Emittent, Admin's Function
	 */
	//@PUT
	@POST
    @Path("/emittent/{id}")
	@Produces(MediaType.TEXT_HTML)
	public String emittentEditHTML(@PathParam("id") Integer id, @FormParam("ticker") String ticker, @FormParam("name") String name) throws RemoteException, IllegalArgumentException {
		try {
			adminRMI.emittentEdit( new Emittent(id, ticker, name) );
			return getEmittentsListHTML();
		}
		catch (Exception e) {
			return e.getMessage();
		}
	}
	
	@PUT
    @Path("/emittent/{id}")
	@Produces(MediaType.TEXT_PLAIN)
	public String emittentEdit(@PathParam("id") Integer id, @FormParam("ticker") String ticker, @FormParam("name") String name) throws RemoteException, IllegalArgumentException {
		try {
			adminRMI.emittentEdit( new Emittent(id, ticker, name) );
			return "ok";
		}
		catch (Exception e) {
			return e.getMessage();
		}
	}
	
	
	/**
	 * Remove Emittent, Admin's Function
	 */
	//@DELETE
	@POST
    @Path("/emittent/{id}/lock")
	@Produces(MediaType.TEXT_HTML)
	public String emittentLockHTML(@PathParam("id") Integer id, @FormParam("ticker") String ticker) throws RemoteException, IllegalArgumentException {
		try {
			adminRMI.emittentLock( new Emittent(id, ticker, "") );
			return getEmittentsListHTML();
		}
		catch (Exception e) {
			return e.getMessage();
		}
	}

	@DELETE
    @Path("/emittent/{id}/lock")
	@Produces(MediaType.TEXT_PLAIN)
	public String emittentLock(@PathParam("id") Integer id, @FormParam("ticker") String ticker) throws RemoteException, IllegalArgumentException {
		try {
			adminRMI.emittentLock( new Emittent(id, ticker, "") );
			return "ok";
		}
		catch (Exception e) {
			return e.getMessage();
		}
	}
	
	
	@POST
    @Path("/add_new_auftrag")
	@Produces(MediaType.TEXT_HTML)
	public String auftragAddNewHTML(@FormParam("owner") Integer brokerId, @FormParam("auftrag") Auftrag auftrag) throws RemoteException, IllegalArgumentException {
		try {
			return "<html><title>ddNew Auftrag</title><body><h1>OK - AuftragId=" + _auftragAddNew(brokerId, auftrag) + "</h1></body></html>";
		}
		catch (Exception e) {
			return e.getMessage();
		}
	}

	@POST
    @Path("/add_new_auftrag")
	@Produces(MediaType.TEXT_PLAIN)
	public String emittentAddNew(@FormParam("owner") Integer brokerId, @FormParam("auftrag") Auftrag auftrag) throws RemoteException, IllegalArgumentException {
		StringBuilder sb = new StringBuilder();
		try {
			sb.append( "ok - auftragId=" + _auftragAddNew(brokerId, auftrag).toString() );
		}
		catch (Exception e) {
			sb.append( e.getMessage() );
		}
		return sb.toString(); 
	}

	
	/**
	 * Auftrag eines Brokers stellen, ohne Sicherheitspruefung
	 */
	private Integer _auftragAddNew(Integer brokerId, Auftrag auftrag) throws RemoteException, IllegalArgumentException {
		
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

    
	//@DELETE
	@POST
    @Path("/auftrag/{id}/cancel")
	@Produces(MediaType.TEXT_HTML)
	public String auftragCancelHTML(@FormParam("owner") Integer brokerId, @PathParam("id") Integer auftragId) throws RemoteException, IllegalArgumentException {
		try {
			_auftragCancel(brokerId, auftragId);
			return "<html><title>ddNew Auftrag</title><body><h1>OK</h1></body></html>";
		}
		catch (Exception e) {
			return e.getMessage();
		}
	}

	@DELETE
    @Path("/auftrag/{id}/cancel")
	@Produces(MediaType.TEXT_PLAIN)
	public String auftragCancel(@FormParam("owner") Integer brokerId, @PathParam("id") Integer auftragId) throws RemoteException, IllegalArgumentException {
		try {
			_auftragCancel(brokerId, auftragId);
			return "ok";
		}
		catch (Exception e) {
			return e.getMessage();
		}
	}
	
        
	/**
	 * Auftrag eines Brokers zurueckrufen, ohne Sicherheitspruefung
	 */
	public void _auftragCancel(Integer brokerId, Integer auftragId) throws RemoteException, IllegalArgumentException {
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

	
	
	public void onException(JMSException e) { 
		  System.out.println("JMS Exception occurred"); 
	}
}
