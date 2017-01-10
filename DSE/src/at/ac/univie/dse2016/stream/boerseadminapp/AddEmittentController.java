package at.ac.univie.dse2016.stream.boerseadminapp;


import at.ac.univie.dse2016.stream.boerseadminapp.Main;
import at.ac.univie.dse2016.stream.common.Client;
import at.ac.univie.dse2016.stream.common.Emittent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.rmi.RemoteException;

public class AddEmittentController {
    @FXML
    private TextField emittent_ticker;
    @FXML
    private TextField emittent_name;





    /**
     * Feld fuer clients TextFiled
     */
    public void EmittentAddNew() {
        Emittent emittent = new Emittent(emittent_ticker.getText(), emittent_name.getText());
        try {
            Main.boerseAdmin.emittentAddNew(emittent);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }



}
