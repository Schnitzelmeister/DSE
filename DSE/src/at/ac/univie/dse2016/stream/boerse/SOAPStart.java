package at.ac.univie.dse2016.stream.boerse;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import at.ac.univie.dse2016.stream.common.*;
import javax.xml.ws.Endpoint;


/*
 * Diese Klasse kommuniziert mit BoerseServer teilweise mit Messaging, teilweise mit RMI 
 */
public class SOAPStart {

	private String localSOAPHostBroker;
	private String remoteHostBoerse;
	private Integer remotePortRMIBoerse;
	private String messageBrokerUrl;
	private BoerseClient boerseClient;
	
	public SOAPStart(String remoteHostBoerse, int remotePortRMIBoerse, String localSOAPHostBroker, String messageBrokerUrl) {

		this.remoteHostBoerse = remoteHostBoerse;
		this.remotePortRMIBoerse = remotePortRMIBoerse;
		this.localSOAPHostBroker = localSOAPHostBroker;
		this.messageBrokerUrl = messageBrokerUrl;
		
        try {
        	
        	//get Boerse RMI object
            Registry registryBoerse = LocateRegistry.getRegistry(this.remoteHostBoerse, this.remotePortRMIBoerse);
            boerseClient = (BoerseClient) registryBoerse.lookup("brokerBoerse");

            //publish SOAP
            System.out.println("Try to publish SOAP " +  this.localSOAPHostBroker);
            Endpoint endpoint = Endpoint.publish(this.localSOAPHostBroker, new BoerseSOAP(this.boerseClient, this.messageBrokerUrl));

            boolean status = endpoint.isPublished();
            System.out.println("Web service status = " + status);
            
	        System.out.println("Boerse SOAP with messageBrokerUrl=" + this.messageBrokerUrl + " ist gestartet");
            
        } catch (Exception e) {
            System.err.println("SOAPStart exception:");
            e.printStackTrace();
        }
	}
	
	
	public static void main(String[] args) {
		try {
			
	        if (System.getSecurityManager() == null) {
	            //System.setSecurityManager(new SecurityManager());
	        }
			
			String remoteHostBoerse = "localhost";
			if (args.length > 0)
				remoteHostBoerse = args[0];
			int remotePortRMIBoerse = 10001;
			if (args.length > 1)
				remotePortRMIBoerse = Integer.valueOf(args[1]);
			String localSOAPHostBroker = "http://localhost:8080/WebServices/public";
			if (args.length > 2)
				localSOAPHostBroker = args[2];
			String messageBrokerUrl = "tcp://localhost:61616";
			if (args.length > 3)
				messageBrokerUrl = args[3];
			
			//start SOAP
			new SOAPStart(remoteHostBoerse, remotePortRMIBoerse, localSOAPHostBroker, messageBrokerUrl);
		}
		catch (Exception e) {
			
		}
	}

}
