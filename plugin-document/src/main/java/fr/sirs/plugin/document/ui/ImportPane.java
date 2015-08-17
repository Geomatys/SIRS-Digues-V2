
package fr.sirs.plugin.document.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import java.io.File;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

/**
 *
 * @author guilhem
 */
public class ImportPane extends GridPane {
    
    @FXML
    public TextField classPlaceField;

    @FXML
    private Button chooseFileButton;

    @FXML
    public TextField fileField;

    @FXML
    public TextField inventoryNumField;
    
    public ImportPane() {
        SIRS.loadFXML(this);
        Injector.injectDependencies(this);
    }
    
    @FXML
    public void chooseFileButton(ActionEvent event) {
        final FileChooser fileChooser = new FileChooser();
        final File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            fileField.setText(file.getPath());
        }
    }
}
