package at.ac.univie.dse2016.stream.boerseadminapp;

/**
 * Base Class for all controllers and for connecting to the Server
 */
public abstract class AbstractController {

    //Storage for Data from controllers
    //private static Map<String, Object> storage = new HashMap<>();


    public void setToStorage(String key, Object object){
        //storage.put(key, object);
    }

    public Object getObject(String key){
        return null;//storage.get(key);
    }


}
