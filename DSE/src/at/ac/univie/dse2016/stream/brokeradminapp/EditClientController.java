package at.ac.univie.dse2016.stream.brokeradminapp;

import at.ac.univie.dse2016.stream.common.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.rmi.RemoteException;

/**
 * Created by mac on 28.10.16.
 */


public class EditClientController extends AbstractController {
     @FXML
     private TextField parentId;
    @FXML
    private TextField clientName;
    @FXML
    private TextField kontoStand;

    private Client client;

    @FXML
    public void initialize(){

        client = (Client) super.getObject("editClient");
        this.parentId.setText(client.getBrokerId().toString());
        this.clientName.setText(client.getName());
        this.kontoStand.setText(""+client.getKontostand());
    }

    public void onClickSave(ActionEvent actionEvent) {
        Client client = new Client(this.client.getId(), Integer.parseInt(parentId.getText()), Float.parseFloat(kontoStand.getText()), clientName.getText());
        try {
            Main.brokerAdmin.clientEdit(client);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
