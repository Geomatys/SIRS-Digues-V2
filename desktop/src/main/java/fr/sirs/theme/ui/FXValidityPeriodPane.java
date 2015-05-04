package fr.sirs.theme.ui;

import fr.sirs.SIRS;
import fr.sirs.core.model.AvecBornesTemporelles;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import org.geotoolkit.gui.javafx.util.FXDateField;

/**
 * A simple editor for edition of temporal bornes of an object.
 * @author Alexis Manin (Geomatys)
 */
public class FXValidityPeriodPane extends BorderPane {
    
    @FXML private FXDateField uiDateDebut;
    @FXML private FXDateField uiDateFin;
    
    private final SimpleObjectProperty<AvecBornesTemporelles> target = new SimpleObjectProperty<>();
    
    private final SimpleBooleanProperty disableFieldsProperty = new SimpleBooleanProperty(false);
    
    public FXValidityPeriodPane() {
        super();
        SIRS.loadFXML(this);
        target.addListener(this::targetChanged);
        
        uiDateDebut.disableProperty().bind(disableFieldsProperty);
        uiDateFin.disableProperty().bind(disableFieldsProperty);
    }
    
    private void targetChanged(ObservableValue<? extends AvecBornesTemporelles> observable, AvecBornesTemporelles oldTarget, AvecBornesTemporelles newTarget) {
        if (oldTarget != null) {
            uiDateDebut.valueProperty().unbindBidirectional(oldTarget.date_debutProperty());
            uiDateFin.valueProperty().unbindBidirectional(oldTarget.date_finProperty());
        }
        
        if (newTarget != null) {
            uiDateDebut.valueProperty().bindBidirectional(newTarget.date_debutProperty());
            uiDateFin.valueProperty().bindBidirectional(newTarget.date_finProperty());
        }
    }
        
    public BooleanProperty disableFieldsProperty(){
        return disableFieldsProperty;
    }

    public SimpleObjectProperty<AvecBornesTemporelles> targetProperty() {
        return target;
    }
    
    
}
