
package fr.sirs.theme.ui;

import fr.sirs.core.model.AireStockageDependance;
import javafx.fxml.FXML;

/**
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class FXAireStockageDependancePane extends FXAireStockageDependancePaneStub {

    @FXML FXPositionDependancePane uiPosition;

    /**
     * Constructor. Initialize part of the UI which will not require update when element edited change.
     */
    private FXAireStockageDependancePane() {
        super();
		/*
		 * Disabling rules.
		 */
        uiPosition.disableFieldsProperty().bind(disableFieldsProperty());

        uiPosition.dependanceProperty().bind(elementProperty);
    }
    
    public FXAireStockageDependancePane(final AireStockageDependance aireStockageDependance){
        this();
        this.elementProperty().set(aireStockageDependance);
    }
}
