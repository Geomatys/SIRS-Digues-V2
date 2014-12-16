package fr.sirs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXLoginscreen {

    @FXML public Label uiProgressLabel;

    @FXML public TextField uiLogin;

    @FXML public PasswordField uiPassword;
    
    @FXML public Button uiConnexion;
    @FXML public Button uiCancel;
    
    @FXML
    void closeApp(ActionEvent event) {
        System.exit(0);
    }
}
