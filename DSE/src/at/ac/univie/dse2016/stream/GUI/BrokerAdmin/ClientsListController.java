package at.ac.univie.dse2016.stream.GUI.BrokerAdmin;


import at.ac.univie.dse2016.stream.common.BrokerAdmin;
import at.ac.univie.dse2016.stream.common.Client;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;


import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by mac on 26.10.16.
 */





public class ClientsListController {

    private BrokerAdmin brokerAdmin;

    @FXML
    TableView<Client> clientList;

    @FXML
    public void initialize() {
        ObservableList<Client> data =FXCollections.observableArrayList(
                new Client(123, 312, 100000, "John"),
        new Client(1234, 3123, 100000, "John"),
        new Client(1235, 3124, 100000, "John"),
         new Client(1236, 3125, 100000, "John")
        );
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
        Client client = clientList.getSelectionModel().getSelectedItem();
        try {
            brokerAdmin.clientLock(client);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
