package at.ac.univie.dse2016.stream.brokeradminapp;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import at.ac.univie.dse2016.stream.common.*;

public class BrokerAdminApp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length < 1)
			throw new IllegalArgumentException("arguments: brokerId {remoteHostBoerse remotePortRMIBoerse remoteHostBroker remotePortRMIBroker}");

		Integer brokerId = Integer.valueOf(args[0]);
		java.util.ArrayList<Emittent> emittents;
		
		String remoteHostBoerse = "localhost";
		int remotePortRMIBoerse = 10001;
		if (args.length > 1)
			remoteHostBoerse = args[1];
		if (args.length > 2)
			remotePortRMIBoerse = Integer.valueOf(args[2]);
		
		String remoteHostBroker = "localhost";
		int remotePortRMIBroker = 10002;	
		if (args.length > 3)
			remoteHostBroker = args[3];
		if (args.length > 4)
			remotePortRMIBroker = Integer.valueOf(args[4]);
		
		
        if (System.getSecurityManager() == null) {
            //System.setSecurityManager(new SecurityManager());
        }
        try {
        	
            Registry registryBoerse = LocateRegistry.getRegistry(remoteHostBoerse, remotePortRMIBoerse);
            BoersePublic boerse = (BoersePublic) registryBoerse.lookup("public");

            if (args.length < 4)
            {
	            remoteHostBroker = boerse.getBrokerNetworkAddress(brokerId, NetworkResource.RMI);
	            String[] ar = remoteHostBroker.split(":");
	            remotePortRMIBroker = Integer.valueOf(ar[1]);
	            remoteHostBroker = ar[0];
            }
            Registry registryBroker = LocateRegistry.getRegistry(remoteHostBroker, remotePortRMIBroker);
            BrokerAdmin broker = (BrokerAdmin) registryBroker.lookup("adminBroker");

            broker.clientAddNew( new Client(brokerId, "Mr.Muster") );
            broker.clientAddNew( new Client(brokerId, "Mr.Muster 2") );

            for (Client b : broker.getClientsList())
            	System.out.println(b.getName() + "/n");

//            broker.auftragAddNew( clientId, new Auftrag(true, "AAPL", 10) );

/*
            //SOAP
            QName serviceName = new QName("http://boerse.com/", "BoersePublic");
            QName portName = new QName("http://boerse.com/", "WebServices/public");

            Service service = Service.create(serviceName);
            service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING,
                            "http://localhost:8080/WebServices/public"); 
            //at.ac.univie.dse2016.stream.common.BoersePublic client = service.getPort(portName,  at.ac.univie.dse2016.stream.common.BoersePublic.class);
            
            System.out.println(portName);
            at.ac.univie.dse2016.stream.common.BoersePublic clientSOAPBoerse = service.getPort(portName, at.ac.univie.dse2016.stream.common.BoersePublic.class);
            
  //          System.out.println( "client.getBrokerNetworkAddress(1, NetworkResource.RMI)=" + clientSOAPBoerse.getBrokerNetworkAddress(1, NetworkResource.RMI) );
   //         System.out.println( "client.getBrokerNetworkAddress(1, NetworkResource.SOAP)=" + clientSOAPBoerse.getBrokerNetworkAddress(1, NetworkResource.SOAP) );
   //         System.out.println( "client.getBrokerNetworkAddress(1, NetworkResource.REST)=" + clientSOAPBoerse.getBrokerNetworkAddress(1, NetworkResource.REST) );
            for (Emittent e : clientSOAPBoerse.getEmittentsList())
            	System.out.println(e.getTicker() + " - " + e.getName() );
            
            //REST
            org.apache.cxf.jaxrs.client.WebClient clientREST = org.apache.cxf.jaxrs.client.WebClient.create("http://localhost:9999/rest/public");
            clientREST.path("broker_network_address").path(3).path(NetworkResource.SOAP.getNumVal()).accept("text/plain");
            System.out.println("result = " + clientREST.get(String.class) );
*/
        
        } catch (Exception e) {
            System.err.println("Client exception:");
            e.printStackTrace();
        }


	}

}
