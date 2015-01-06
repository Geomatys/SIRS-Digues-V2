
package fr.sirs.digue;

import fr.sirs.FXEditMode;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.model.SystemeEndiguement;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXSystemeEndiguementPane extends BorderPane {

    @FXML private FXEditMode uiEditMode;
    @FXML private TextField uiLibelle;
    
    private final ObjectProperty<SystemeEndiguement> endiguementProp = new SimpleObjectProperty<>();
    
    public FXSystemeEndiguementPane() {
        SIRS.loadFXML(this);
        endiguementProp.addListener(this::changed);
        uiEditMode.setSaveAction(this::save);
        
        final BooleanBinding binding = uiEditMode.editionState().not();
        uiLibelle.disableProperty().bind(binding);
    }
    
    private void changed(ObservableValue<? extends SystemeEndiguement> observable, SystemeEndiguement oldValue, SystemeEndiguement newValue) {
        uiLibelle.textProperty().unbind();
        if(newValue!=null){
            uiLibelle.textProperty().bindBidirectional(newValue.libelleProperty());
        }
    }

    public ObjectProperty<SystemeEndiguement> systemeEndiguementProp() {
        return endiguementProp;
    }

    private void save(){
        final Session session = Injector.getSession();
        session.getSystemeEndiguementRepository().update(systemeEndiguementProp().get());
    }
    
}
