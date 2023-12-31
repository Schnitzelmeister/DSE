package at.ac.univie.dse2016.stream.brokeradminapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.io.IOException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * Created by mac on 26.10.16.
 */
public class BrokerAdminGUIController extends AbstractController {
    @FXML
    public void initialize() {
        //super.connect();
    }

    public void clientAdd(ActionEvent actionEvent) {


        try {
            Parent root = FXMLLoader.load(getClass().getResource("AddClient.fxml"));

            Stage stage = new Stage();
           // stage.setTitle("My New Stage Title");
            stage.setScene(new Scene(root));
            stage.show();

            //hide this current window (if this is whant you want
            ((Node)(actionEvent.getSource())).getScene().getWindow().hide();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    public void clientEdit(ActionEvent actionEvent) {
    }

    public void clientLock(ActionEvent actionEvent) {
    }
    */

    public void getClientsList(ActionEvent actionEvent) {
//erzeugen ein Window
        try {
            Parent root = FXMLLoader.load(getClass().getResource("getClientsList.fxml"));

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
}
