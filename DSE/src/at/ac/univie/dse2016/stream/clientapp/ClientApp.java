package at.ac.univie.dse2016.stream.clientapp;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import at.ac.univie.dse2016.stream.common.*;

public class ClientApp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Integer brokerId = Integer.valueOf(args[0]);
		Integer clientId = Integer.valueOf(args[1]);
		java.util.ArrayList<Emittent> emittents;
		
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
        	
            Registry registryBoerse = LocateRegistry.getRegistry(10001);
            BoersePublic boerse = (BoersePublic) registryBoerse.lookup("public");

            emittents = boerse.getEmittentsList();
            for (Emittent e : emittents)
            	System.out.println(e.getTicker() + "/n");


            Registry registryBroker = LocateRegistry.getRegistry(boerse.getBrokerNetworkAddress(brokerId));
            BrokerClient broker = (BrokerClient) registryBroker.lookup("client");

            Client clientState = broker.getState(clientId);

            System.out.println(clientState.getId() + "/n");
            System.out.println(clientState.getName() + "/n");
            System.out.println(clientState.getKontostand() + "/n");

            for (java.util.Map.Entry<Integer, Integer> e : clientState.getAccountEmittents().entrySet())
            	System.out.println(emittents.get(e.getKey()).getTicker() + " = " + e.getValue() + " Stueck/n");

            broker.auftragAddNew( clientId, new Auftrag(true, "AAPL", 10) );

        
        } catch (Exception e) {
            System.err.println("Client exception:");
            e.printStackTrace();
        }

	}
}
