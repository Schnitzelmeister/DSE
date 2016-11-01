package at.ac.univie.dse2016.stream.brokeradminapp;


import at.ac.univie.dse2016.stream.common.BrokerAdmin;
import at.ac.univie.dse2016.stream.common.Client;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by mac on 26.10.16.
 */


public class ClientsListController extends AbstractController {

	
    //private BrokerAdmin brokerAdmin;

    @FXML
    TableView<Client> clientList;

    @FXML
    public void initialize() {

    	ObservableList<Client> data =FXCollections.observableArrayList();
    	try {
            data.addAll(Main.brokerAdmin.getClientsList());
    	} catch (Exception e) {
            System.err.println("Client exception:");
            e.printStackTrace();
        }

    	clientList.setEditable(true);
        
        TableColumn idColumn = new TableColumn("Client ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<Client,Integer>("id"));

        TableColumn parentIdColumn = new TableColumn("Parent Id");
        parentIdColumn.setCellValueFactory(
                new PropertyValueFactory<Client,Integer>("brokerId")
        );

        TableColumn kontostandColumn = new TableColumn("Kontostand");
        kontostandColumn.setMinWidth(200);
        kontostandColumn.setCellValueFactory(
                new PropertyValueFactory<Client, Float>("kontostand")
        );
        TableColumn nameColumn = new TableColumn("Name");
        nameColumn.setCellValueFactory(
                new PropertyValueFactory<Client,String>("name")
        );
        clientList.getColumns().addAll(idColumn, parentIdColumn, kontostandColumn, nameColumn);
        clientList.setItems(data);




    }

    public void editClient(ActionEvent actionEvent) {
        //speichern Clienten den man aendert moechte
        //super.setToStorage("editClient", clientList.getSelectionModel().getSelectedItem());

        try {
            Parent root = FXMLLoader.load(getClass().getResource("EditClient.fxml"));

            Stage stage = new Stage();
            // stage.setTitle("My New Stage Title");
            stage.setScene(new Scene(root));
            stage.show();

            //hide this current window (if this is whant you want
            //((Node)(actionEvent.getSource())).getScene().getWindow().hide();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void lockClient(ActionEvent actionEvent) {
        Client client = clientList.getSelectionModel().getSelectedItem();
        try {
            Main.brokerAdmin.clientLock(client);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
