package at.ac.univie.dse2016.stream.boerseadminapp;



import at.ac.univie.dse2016.stream.boerseadminapp.Main;
import at.ac.univie.dse2016.stream.common.Broker;
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


public class BrokerListController {


    //private BrokerAdmin brokerAdmin;

    @FXML
    TableView<Broker> brokerList;

    public static Broker selectedBroker;

    @FXML
    public void initialize() {
        try {
            System.out.println(Main.boerseAdmin.getBrokersList());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        ObservableList<Broker> data =FXCollections.observableArrayList();
        try {
            data.addAll(Main.boerseAdmin.getBrokersList());
        } catch (Exception e) {
            System.err.println("Broker exception:");
            e.printStackTrace();
        }

        brokerList.setEditable(true);

        TableColumn idColumn = new TableColumn("Broker ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<Emittent,Integer>("id"));

        TableColumn parentIdColumn = new TableColumn("Broker Name");
        parentIdColumn.setCellValueFactory(
                new PropertyValueFactory<Broker,String>("name")
        );

        TableColumn kontostandColumn = new TableColumn("Kontostand");
        kontostandColumn.setMinWidth(200);
        kontostandColumn.setCellValueFactory(
                new PropertyValueFactory<Broker, Float>("kontostand")
        );

        TableColumn phoneColumn = new TableColumn("Phone");
        kontostandColumn.setMinWidth(200);
        kontostandColumn.setCellValueFactory(
                new PropertyValueFactory<Broker, String>("phone")
        );

        TableColumn licenseColumn = new TableColumn("Licence");
        kontostandColumn.setMinWidth(200);
        kontostandColumn.setCellValueFactory(
                new PropertyValueFactory<Broker, String>("licence")
        );

        brokerList.getColumns().addAll(idColumn, parentIdColumn, kontostandColumn, phoneColumn, licenseColumn);
        brokerList.setItems(data);




    }

    public void editBroker(ActionEvent actionEvent) {
        selectedBroker = brokerList.getSelectionModel().getSelectedItem();
        //speichern Clienten den man aendert moechte
        //super.setToStorage("editClient", clientList.getSelectionModel().getSelectedItem());

        try {
            Parent root = FXMLLoader.load(getClass().getResource("EditBroker.fxml"));

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

    public void lockBroker(ActionEvent actionEvent) {
        Broker broker = brokerList.getSelectionModel().getSelectedItem();
        try {
            Main.boerseAdmin.brokerLock(broker);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
