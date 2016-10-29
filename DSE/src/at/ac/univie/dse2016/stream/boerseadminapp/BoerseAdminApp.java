package at.ac.univie.dse2016.stream.boerseadminapp;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import at.ac.univie.dse2016.stream.common.*;


public final class BoerseAdminApp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            Registry registry = LocateRegistry.getRegistry(10001);
            BoerseAdmin boerse = (BoerseAdmin) registry.lookup("adminBoerse");
//            System.out.println(boerse.hi());
//            System.out.println(boerse.getEmittent().getName());
            
            for (Emittent e : boerse.getEmittentsList())
            	System.out.println(e.getName() + "/n");

            //add new Emittent
            //boerse.emittentAddNew( new Emittent("GAZP", "Gazprom") );
            
            for (Broker b : boerse.getBrokersList())
            	System.out.println(b.getName() + "/n");

        
        } catch (Exception e) {
            System.err.println("Client exception:");
            e.printStackTrace();
        }

	}

}
