package at.ac.univie.dse2016.stream.boerseadminapp;


import at.ac.univie.dse2016.stream.common.Broker;
import at.ac.univie.dse2016.stream.common.Emittent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.rmi.RemoteException;

public class AddBrokerController {
    @FXML
    private TextField broker_name;
    @FXML
    private TextField broker_phone;
    @FXML
    private TextField broker_licence;




    /**
     * Feld fuer clients TextFiled
     */
    public void BrokerAddNew() throws RemoteException {
        String name = broker_name.getText();
        String phone = broker_phone.getText();
        String licence = broker_licence.getText();
        int size = Main.boerseAdmin.getBrokersList().size()+1;
        String RMI = "localhost:500" + size;
        String SOAP = "http://localhost:2000"+ size + "/WebServices/public";
        String REST = "http://localhost:3000" + size + "/rest/";

        Broker broker = new Broker(name, RMI, SOAP, REST, phone, licence);
        try {
            Main.boerseAdmin.brokerAddNew(broker);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }



}
