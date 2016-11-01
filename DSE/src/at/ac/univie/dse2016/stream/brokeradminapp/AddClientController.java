package at.ac.univie.dse2016.stream.brokeradminapp;



import at.ac.univie.dse2016.stream.common.*;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class AddClientController extends AbstractController {
    @FXML
    private TextField client_id;
    @FXML
    private TextField parent_id;
    @FXML
    private TextField kontostand;
    @FXML
    private TextField client_name;
    @FXML
    private Text failText;

    /**
     * Variable, mite der wir Methoden im BrokerAdmin aufrufen koennen
     */
//    private BrokerAdmin brokerAdmin;

    /**
     * Method for connecting to the server "Boerse"
     * if catch Exception cannot connect: write text
     */
    @FXML
    public void initialize() {

    	client_id.setText("generating on Server");
    	parent_id.setText(String.valueOf(Main.brokerId));
    	kontostand.setText("0.00");
    }

    /**
     * Feld fuer clients TextFiled
     */
    public void clientAddNew(){
        Integer parentId = Main.brokerId;
        String clientName = client_name.getText();
        Client client = new Client(parentId, clientName);
        try {
            Main.brokerAdmin.clientAddNew(client);
        } catch (RemoteException e){
            failText.setText("Cannot connect");
        }
    }



}
