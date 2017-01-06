package at.ac.univie.dse2016.stream.boerseadminapp;

import at.ac.univie.dse2016.stream.common.BoersePublic;
import at.ac.univie.dse2016.stream.common.BrokerAdmin;
import at.ac.univie.dse2016.stream.common.NetworkResource;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class Main extends Application {

    public static Integer brokerId;
	public static BrokerAdmin brokerAdmin;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("BrokerAdminGUI.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

    }


    public static void main(String[] args) {

		if (args.length < 1)
			throw new IllegalArgumentException("arguments: brokerId {remoteHostBoerse remotePortRMIBoerse remoteHostBroker remotePortRMIBroker}");

		Integer brokerId = Integer.valueOf(args[0]);
		
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
            Main.brokerAdmin = (BrokerAdmin) registryBroker.lookup("adminBroker");

          //  Main.brokerAdmin.clientAddNew( new Client(brokerId, "Mr.Muster") );
          //  Main.brokerAdmin.clientAddNew( new Client(brokerId, "Mr.Muster 2") );
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
        launch(args);
    }
}
