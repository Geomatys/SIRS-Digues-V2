

package fr.sirs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXSplashscreen {

    @FXML public GridPane uiLoadingPane;
    @FXML public Label uiProgressLabel;
    @FXML public ProgressBar uiProgressBar;
    @FXML public Button uiCancel;
    
    @FXML public GridPane uiLoginPane;
    @FXML public TextField uiLogin;
    @FXML public PasswordField uiPassword;
    @FXML public Button uiConnexion;
    @FXML public Label uiLogInfo;
    
    @FXML
    void closeApp(ActionEvent event) {
        System.exit(0);
    }
        
}
