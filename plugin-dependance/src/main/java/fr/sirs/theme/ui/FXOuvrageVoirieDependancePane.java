
package fr.sirs.theme.ui;

import fr.sirs.core.model.OuvrageVoirieDependance;
import javafx.fxml.FXML;

/**
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class FXOuvrageVoirieDependancePane extends FXOuvrageVoirieDependancePaneStub {

    @FXML FXPositionDependancePane uiPosition;

    /**
     * Constructor. Initialize part of the UI which will not require update when element edited change.
     */
    private FXOuvrageVoirieDependancePane() {
        super();
		/*
		 * Disabling rules.
		 */
        uiPosition.disableFieldsProperty().bind(disableFieldsProperty());
    }
    
    public FXOuvrageVoirieDependancePane(final OuvrageVoirieDependance ouvrageVoirieDependance){
        this();
        this.elementProperty().set(ouvrageVoirieDependance);
        
    }
}
