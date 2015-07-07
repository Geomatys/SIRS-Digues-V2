package fr.sirs.util;

import fr.sirs.FXEditMode;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class SimpleFXEditMode extends FXEditMode {

    public SimpleFXEditMode() {
        uiValidationBox.setVisible(false);
        uiValidationBox.setManaged(false);
        uiSave.setVisible(false);
        uiSave.setManaged(false);
    }
}
