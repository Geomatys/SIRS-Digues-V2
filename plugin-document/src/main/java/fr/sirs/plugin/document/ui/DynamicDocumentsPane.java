package fr.sirs.plugin.document.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Cédric Briançon (Geomatys)
 */
public class DynamicDocumentsPane extends BorderPane implements Initializable {
    public DynamicDocumentsPane() {
        SIRS.loadFXML(this, DynamicDocumentsPane.class);
        Injector.injectDependencies(this);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
