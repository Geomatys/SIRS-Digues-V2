

package fr.sirs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class SplashController {

    @FXML
    public Label uiProgressLabel;

    @FXML
    public ProgressBar uiProgressBar;

    @FXML
    public Button uiCancel;
    
    @FXML
    void closeApp(ActionEvent event) {
        System.exit(0);
    }
    
}
