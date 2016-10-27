package at.ac.univie.dse2016.stream.GUI.BrokerAdmin;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by mac on 26.10.16.
 */
public class BrokerAdminGUIController {


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

        try {
            Parent root = FXMLLoader.load(getClass().getResource("getClientsList.fxml"));

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
}
