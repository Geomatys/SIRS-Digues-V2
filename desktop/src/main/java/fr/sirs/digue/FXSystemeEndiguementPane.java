
package fr.sirs.digue;

import fr.sirs.FXEditMode;
import fr.sirs.SIRS;
import fr.sirs.core.model.SystemeEndiguement;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXSystemeEndiguementPane extends BorderPane {

    @FXML private FXEditMode uiEditMode;
    
    private final ObjectProperty<SystemeEndiguement> endiguementProp = new SimpleObjectProperty<>();
    
    public FXSystemeEndiguementPane() {
        SIRS.loadFXML(this);
    }

    public ObjectProperty<SystemeEndiguement> systemeEndiguementProp() {
        return endiguementProp;
    }

    
}
