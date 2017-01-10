package at.ac.univie.dse2016.stream.boerseadminapp;

import at.ac.univie.dse2016.stream.common.Broker;
import at.ac.univie.dse2016.stream.common.Emittent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.rmi.RemoteException;

/**
 * Created by mac on 28.10.16.
 */


public class EditBrokerController {
     @FXML
     private TextField brokerName;
    @FXML
    private TextField brokerPhone;
    @FXML
    private TextField brokerLicence;


    private Broker broker;

    @FXML
    public void initialize(){
        broker = BrokerListController.selectedBroker;

        this.brokerName.setText(broker.getName());

        this.brokerPhone.setText(broker.getPhone());

        this.brokerLicence.setText(broker.getLicense());
    }




    public void onClickSave(ActionEvent actionEvent) {
       // System.out.println("vasa");
//        Broker broker = new Broker(this.broker.getId(), this.broker.getKontostand(),
//                this.brokerName.getText(), this.broker.getNetworkRMIAddress(),
//                this.broker.getNetworkSOAPAddress(), this.broker.getNetworkRESTAddress(),
//                this.brokerPhone.getText(), this.brokerLicence.getText());
//
        broker.setLicense(brokerLicence.getText());
        broker.setPhone(brokerPhone.getText());
        broker.setName(brokerName.getText());

        try {
            Main.boerseAdmin.brokerEdit(broker);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
