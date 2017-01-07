package at.ac.univie.dse2016.stream.boerseadminapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by mac on 26.10.16.
 */
public class BoerseAdminGUIController extends AbstractController {
    @FXML
    public void initialize() {
        //super.connect();
    }

    public void emittentAdd(ActionEvent actionEvent) {


        try {
            Parent root = FXMLLoader.load(getClass().getResource(""));

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

    public void getEmittentsList(ActionEvent actionEvent) {
//erzeugen ein Window
        try {
            Parent root = FXMLLoader.load(getClass().getResource(""));

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
    public void brokerAdd(ActionEvent actionEvent) {


        try {
            Parent root = FXMLLoader.load(getClass().getResource(""));

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

    public void getBrokersList(ActionEvent actionEvent) {
//erzeugen ein Window
        try {
            Parent root = FXMLLoader.load(getClass().getResource(""));

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
