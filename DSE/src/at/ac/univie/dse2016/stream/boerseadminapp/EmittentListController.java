package at.ac.univie.dse2016.stream.boerseadminapp;



import at.ac.univie.dse2016.stream.boerseadminapp.Main;
import at.ac.univie.dse2016.stream.common.Client;
import at.ac.univie.dse2016.stream.common.Emittent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.rmi.RemoteException;

/**
 * Created by mac on 26.10.16.
 */


public class EmittentListController{

	
    //private BrokerAdmin brokerAdmin;

    @FXML
    TableView<Emittent> emittentList;

    public static Emittent selectedEmittent;

    @FXML
    public void initialize() {

    	ObservableList<Emittent> data =FXCollections.observableArrayList();
    	try {
            data.addAll(Main.boerseAdmin.getEmittentsList());
    	} catch (Exception e) {
            System.err.println("Emittent exception:");
            e.printStackTrace();
        }

    	emittentList.setEditable(true);

        TableColumn idColumn = new TableColumn("Emittent ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<Emittent,Integer>("id"));

        TableColumn parentIdColumn = new TableColumn("Emittent Ticker");
        parentIdColumn.setCellValueFactory(
                new PropertyValueFactory<Emittent,Integer>("ticker")
        );

        TableColumn kontostandColumn = new TableColumn("Name");
        kontostandColumn.setMinWidth(200);
        kontostandColumn.setCellValueFactory(
                new PropertyValueFactory<Client, Float>("name")
        );

        emittentList.getColumns().addAll(idColumn, parentIdColumn, kontostandColumn);
        emittentList.setItems(data);




    }

    public void editEmittent(ActionEvent actionEvent) {
       selectedEmittent = emittentList.getSelectionModel().getSelectedItem();
        //speichern Clienten den man aendert moechte
        //super.setToStorage("editClient", clientList.getSelectionModel().getSelectedItem());

        try {
            Parent root = FXMLLoader.load(getClass().getResource("EditEmittent.fxml"));

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

    public void lockEmittent(ActionEvent actionEvent) {
        Emittent emittent = emittentList.getSelectionModel().getSelectedItem();
        try {
            Main.boerseAdmin.emittentLock(emittent);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
