package at.ac.univie.dse2016.stream.brokeradminapp;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import at.ac.univie.dse2016.stream.common.*;

public class BrokerAdminApp {

	/**
	 * @param args
	 */
	public static void _main(String[] args) {
		
		Integer brokerId = Integer.valueOf(args[0]);
		
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            Registry registry = LocateRegistry.getRegistry(10001);
            BrokerAdmin broker = (BrokerAdmin) registry.lookup("adminBroker");

            broker.clientAddNew( new Client(brokerId, "Mr.Muster") );
            broker.clientAddNew( new Client(brokerId, "Mr.Muster 2") );

            for (Client b : broker.getClientsList())
            	System.out.println(b.getName() + "/n");
            

        
        } catch (Exception e) {
            System.err.println("Client exception:");
            e.printStackTrace();
        }

	}

}
