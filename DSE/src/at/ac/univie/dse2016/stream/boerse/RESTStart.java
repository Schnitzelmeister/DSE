package at.ac.univie.dse2016.stream.boerse;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import at.ac.univie.dse2016.stream.common.*;


/*
 * Diese Klasse kommuniziert mit BoerseServer teilweise mit Messaging, teilweise mit RMI 
 */
public class RESTStart {

	private String localRESTHostBroker;
	private String remoteHostBoerse;
	private Integer remotePortRMIBoerse;
	private String messageBrokerUrl;
	private BoerseClient boerseClient;
	private BoerseAdmin adminClient;
	
	public RESTStart(String remoteHostBoerse, int remotePortRMIBoerse, String localRESTHostBroker, String messageBrokerUrl) {

		this.remoteHostBoerse = remoteHostBoerse;
		this.remotePortRMIBoerse = remotePortRMIBoerse;
		this.localRESTHostBroker = localRESTHostBroker;
		this.messageBrokerUrl = messageBrokerUrl;
		
        try {
        	
        	//get Boerse RMI object
            Registry registryBoerse = LocateRegistry.getRegistry(this.remoteHostBoerse, this.remotePortRMIBoerse);
            boerseClient = (BoerseClient) registryBoerse.lookup("brokerBoerse");
            adminClient = (BoerseAdmin) registryBoerse.lookup("adminBoerse");

            //publish REST
            System.out.println("Try to publish REST " +  this.localRESTHostBroker);

            org.apache.cxf.jaxrs.JAXRSServerFactoryBean sf = new org.apache.cxf.jaxrs.JAXRSServerFactoryBean();
            sf.setResourceClasses(BoerseRESTful.class);
            sf.setResourceProvider(BoerseRESTful.class, 
                new org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider( new BoerseRESTful(this.boerseClient, this.adminClient, this.messageBrokerUrl) ) );
            sf.setAddress(this.localRESTHostBroker);
            org.apache.cxf.endpoint.Server server = sf.create();


            // destroy the server
            // uncomment when you want to close/destroy it
            // server.destroy();   
            
	        System.out.println("Boerse REST with messageBrokerUrl=" + this.messageBrokerUrl + " ist gestartet");
            
        } catch (Exception e) {
            System.err.println("RESTStart exception:");
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
			String localRESTHostBroker = "http://localhost:9999/rest/";
			if (args.length > 2)
				localRESTHostBroker = args[2];
			String messageBrokerUrl = "tcp://localhost:61616";
			if (args.length > 3)
				messageBrokerUrl = args[3];
			
			//start REST
			new RESTStart(remoteHostBoerse, remotePortRMIBoerse, localRESTHostBroker, messageBrokerUrl);
		}
		catch (Exception e) {
			
		}
	}

}
