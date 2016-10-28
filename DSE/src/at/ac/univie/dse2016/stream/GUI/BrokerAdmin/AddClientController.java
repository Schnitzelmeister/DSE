package at.ac.univie.dse2016.stream.GUI.BrokerAdmin;



import at.ac.univie.dse2016.stream.common.Client;
import at.ac.univie.dse2016.stream.common.BrokerAdmin;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

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
    private BrokerAdmin brokerAdmin;

    /**
     * Method for connecting to the server "Boerse"
     * if catch Exception cannot connect: write text
     */
    @FXML
    public void initialize() {
        super.connect();
    }

    /**
     * Feld fuer clients TextFiled
     */
    public void clientAddNew(){

        Integer clientId = Integer.parseInt(client_id.getText());
        Integer parentId = Integer.parseInt(parent_id.getText());
        float kontoStand = Float.parseFloat(kontostand.getText());
        String clientName = client_name.getText();
        Client client = new Client(clientId, parentId, kontoStand, clientName);
        try {
            brokerAdmin.clientAddNew(client);
        } catch (RemoteException e){
            failText.setText("Cannot connect");
        }
    }



}
