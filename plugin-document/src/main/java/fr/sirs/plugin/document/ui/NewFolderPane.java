
package fr.sirs.plugin.document.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 *
 * @author guilhem
 */
public class NewFolderPane extends GridPane {
    
    public static final String IN_CURRENT_FOLDER = " Dans le dossier sélectionné";
    public static final String IN_ALL_FOLDER     = " Dans tous les dossiers de SE, digues et tronçons";
    public static final String IN_SE_FOLDER      = " Uniquement dans les systèmes d'endiguements";
    public static final String IN_DG_FOLDER      = " Uniquement dans les digues";
    public static final String IN_TR_FOLDER      = " Uniquement dans les tronçons";
    
    @FXML
    public TextField folderNameField;

    @FXML
    public ComboBox<String> locCombo;
    
    public NewFolderPane() {
        SIRS.loadFXML(this);
        Injector.injectDependencies(this);
        
        final ObservableList prop = FXCollections.observableArrayList();
        prop.add(IN_CURRENT_FOLDER);
        prop.add(IN_ALL_FOLDER);
        prop.add(IN_SE_FOLDER);
        prop.add(IN_DG_FOLDER);
        prop.add(IN_TR_FOLDER);
        locCombo.setItems(prop);
        locCombo.getSelectionModel().selectFirst();
    }
}
