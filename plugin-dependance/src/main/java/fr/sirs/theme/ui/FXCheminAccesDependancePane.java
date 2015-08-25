
package fr.sirs.theme.ui;

import fr.sirs.core.model.CheminAccesDependance;
import javafx.fxml.FXML;

/**
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class FXCheminAccesDependancePane extends FXCheminAccesDependancePaneStub {
    @FXML FXPositionDependancePane uiPosition;

    /**
     * Constructor. Initialize part of the UI which will not require update when element edited change.
     */
    private FXCheminAccesDependancePane() {
        super();
		/*
		 * Disabling rules.
		 */
        uiPosition.disableFieldsProperty().bind(disableFieldsProperty());
    }
    
    public FXCheminAccesDependancePane(final CheminAccesDependance cheminAccesDependance){
        this();
        this.elementProperty().set(cheminAccesDependance);
    }
}
