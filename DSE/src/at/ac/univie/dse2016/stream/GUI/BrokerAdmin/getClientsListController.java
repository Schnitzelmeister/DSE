package at.ac.univie.dse2016.stream.GUI.BrokerAdmin;


import at.ac.univie.dse2016.stream.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;



import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by mac on 26.10.16.
 */





public class getClientsListController {

    @FXML
    public void initialize() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 10003);
           // this.brokerAdmin = (BrokerAdmin)registry.lookup("client");
        } catch (RemoteException e) {
            e.printStackTrace();
            //failText.setText("Cannot connect");
        }
    }

    public void editClient(ActionEvent actionEvent) {
            
    }

    public void lockClient(ActionEvent actionEvent) {

    }

}
