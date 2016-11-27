package at.ac.univie.dse2016.stream.clientapp;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import at.ac.univie.dse2016.stream.common.*;

public class ClientApp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 2)
			throw new IllegalArgumentException("arguments: brokerId clientId {remoteHostBoerse remotePortUDPBoerse remotePortRMIBoerse remoteHostBroker remotePortRMIBroker}");

		Integer brokerId = Integer.valueOf(args[0]);
		Integer clientId = Integer.valueOf(args[1]);
		java.util.ArrayList<Emittent> emittents;
		
		String remoteHostBoerse = "localhost";
		int remotePortUDPBoerse = 10000;
		int remotePortRMIBoerse = 10001;
		if (args.length > 2)
			remoteHostBoerse = args[2];
		if (args.length > 3)
			remotePortUDPBoerse = Integer.valueOf(args[3]);
		if (args.length > 4)
			remotePortRMIBoerse = Integer.valueOf(args[4]);
		
		String remoteHostBroker = "localhost";
		int remotePortRMIBroker = 10002;	
		if (args.length > 5)
			remoteHostBroker = args[5];
		if (args.length > 6)
			remotePortRMIBroker = Integer.valueOf(args[6]);
		
		
        if (System.getSecurityManager() == null) {
            //System.setSecurityManager(new SecurityManager());
        }
        try {
        	
            Registry registryBoerse = LocateRegistry.getRegistry(remoteHostBoerse, remotePortRMIBoerse);
            BoersePublic boerse = (BoersePublic) registryBoerse.lookup("public");

            emittents = boerse.getEmittentsList();
            for (Emittent e : emittents)
            	System.out.println(e.getTicker());

            if (args.length < 6)
            {
	            remoteHostBroker = boerse.getBrokerNetworkAddress(brokerId, NetworkResource.RMI);
	            String[] ar = remoteHostBroker.split(":");
	            remotePortRMIBroker = Integer.valueOf(ar[1]);
	            remoteHostBroker = ar[0];
            }
            Registry registryBroker = LocateRegistry.getRegistry(remoteHostBroker, remotePortRMIBroker);
            BrokerClient broker = (BrokerClient) registryBroker.lookup("client");

            Client clientState = broker.getState(clientId);

            System.out.println(clientState.getId());
            System.out.println(clientState.getName());
            System.out.println(clientState.getKontostand());

            for (java.util.Map.Entry<Integer, Integer> e : clientState.getAccountEmittents().entrySet())
            	System.out.println(emittents.get(e.getKey()).getTicker() + " = " + e.getValue() + " Stueck");

//            broker.auftragAddNew( clientId, new Auftrag(true, "AAPL", 10) );

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

        
        } catch (Exception e) {
            System.err.println("Client exception:");
            e.printStackTrace();
        }

	}
}
