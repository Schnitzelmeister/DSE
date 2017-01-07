package at.ac.univie.dse2016.stream.boerseadminapp;

import at.ac.univie.dse2016.stream.common.BoerseAdmin;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {

    //public static Integer brokerId;
	public static BoerseAdmin boerseAdmin;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("BoerseAdminGUI.fxml"));
        primaryStage.setTitle("Boerse Admin");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

    }


    public static void main(String[] args) {
/**

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

        
        } catch (Exception e) {
            System.err.println("Client exception:");
            e.printStackTrace();
        }
        launch(args);

 */
		launch(args);
    }

}
