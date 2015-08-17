
package fr.sirs.plugin.document.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import java.io.File;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;

/**
 *
 * @author guilhem
 */
public class MainFolderPane extends GridPane {
    
    @FXML
    public TextField rootFolderField;
    
    @FXML
    private Button chooRootButton;

    @FXML
    public void chooseRootFile(ActionEvent event) {
        final DirectoryChooser fileChooser = new DirectoryChooser();
        final File file = fileChooser.showDialog(null);
        if (file != null) {
            rootFolderField.setText(file.getPath());
        }
    }
    
    public MainFolderPane() {
        SIRS.loadFXML(this);
        Injector.injectDependencies(this);
    }
    
}
