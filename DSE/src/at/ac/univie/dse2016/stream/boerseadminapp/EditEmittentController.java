package at.ac.univie.dse2016.stream.boerseadminapp;

import at.ac.univie.dse2016.stream.common.Emittent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.rmi.RemoteException;

/**
 * Created by mac on 28.10.16.
 */


public class EditEmittentController {
     @FXML
     private TextField emittentName;
    @FXML
    private TextField emittentTicker;

    private Emittent emittent;

    @FXML
    public void initialize(){
        emittent = EmittentListController.selectedEmittent;

        this.emittentName.setText(emittent.getName());

        this.emittentTicker.setText(emittent.getTicker());
    }




    public void onClickSave(ActionEvent actionEvent) {
        Emittent emittent = new Emittent(this.emittent.getId(), this.emittentName.getText(), this.emittentTicker.getText());
        try {
            Main.boerseAdmin.emittentEdit(emittent);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
