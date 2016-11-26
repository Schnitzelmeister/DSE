package at.ac.univie.dse2016.stream.brokeradminapp;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import at.ac.univie.dse2016.stream.common.BoersePublic;
import at.ac.univie.dse2016.stream.common.BrokerAdmin;
import at.ac.univie.dse2016.stream.common.Client;
import at.ac.univie.dse2016.stream.common.NetworkResource;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


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

		brokerId = Integer.valueOf(args[0]);
		
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            Registry registryBoerse = LocateRegistry.getRegistry(10001);
            BoersePublic boerse = (BoersePublic) registryBoerse.lookup("public");

            String host = boerse.getBrokerNetworkAddress(brokerId, NetworkResource.REST);
            String[] ar = host.split(":");
            int port = Integer.valueOf(ar[1]);
            host = ar[0];

            
            Registry registry = LocateRegistry.getRegistry(host, port);
            brokerAdmin = (BrokerAdmin) registry.lookup("adminBroker");

            
            Main.brokerAdmin.clientAddNew( new Client(brokerId, "Mr.Muster") );
            Main.brokerAdmin.clientAddNew( new Client(brokerId, "Mr.Muster 2") );

            for (Client b : Main.brokerAdmin.getClientsList())
            	System.out.println(b.getName());
            

        } catch (Exception e) {
            System.err.println("Client exception:");
            e.printStackTrace();
        }

        launch(args);
    }
}
