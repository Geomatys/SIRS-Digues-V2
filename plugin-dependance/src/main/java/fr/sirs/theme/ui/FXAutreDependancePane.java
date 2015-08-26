
package fr.sirs.theme.ui;

import fr.sirs.core.model.AutreDependance;
import javafx.fxml.FXML;

/**
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class FXAutreDependancePane extends FXAutreDependancePaneStub {

    @FXML FXPositionDependancePane uiPosition;

    /**
     * Constructor. Initialize part of the UI which will not require update when element edited change.
     */
    private FXAutreDependancePane() {
        super();
		/*
		 * Disabling rules.
		 */
        uiPosition.disableFieldsProperty().bind(disableFieldsProperty());

        uiPosition.dependanceProperty().bind(elementProperty);
    }
    
    public FXAutreDependancePane(final AutreDependance autreDependance){
        this();
        this.elementProperty().set(autreDependance);
        
    }     
}
