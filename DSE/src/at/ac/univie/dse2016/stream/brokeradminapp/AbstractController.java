package at.ac.univie.dse2016.stream.brokeradminapp;

import at.ac.univie.dse2016.stream.common.BoersePublic;
import at.ac.univie.dse2016.stream.common.Broker;
import at.ac.univie.dse2016.stream.common.BrokerAdmin;
import at.ac.univie.dse2016.stream.common.Client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Base Class for all controllers and for connecting to the Server
 */
public abstract class AbstractController {

    //Storage for Data from controllers
    private static Map<String, Object> storage = new HashMap<>();


    public void setToStorage(String key, Object object){
        storage.put(key, object);
    }

    public Object getObject(String key){
        return storage.get(key);
    }


}
