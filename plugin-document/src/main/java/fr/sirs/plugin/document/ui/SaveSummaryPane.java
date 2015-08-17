
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
public class SaveSummaryPane extends GridPane {

    @FXML
    public TextField newFileFIeld;

    @FXML
    private Button saveButton;

    public SaveSummaryPane() {
        SIRS.loadFXML(this);
        Injector.injectDependencies(this);
    }
    
    @FXML
    public void chooseRootFile(ActionEvent event) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Open Office doc(*.odt)", "*.odt"));
        fileChooser.setInitialFileName("*.odt");
        
        final File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            if (file.getName().endsWith(".odt")) {
                newFileFIeld.setText(file.getPath());
            } else {
                newFileFIeld.setText(file.getPath() + ".odt");
            }
        }
    }
}
