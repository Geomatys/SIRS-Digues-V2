
package fr.sirs.theme.detail;

import javafx.beans.property.BooleanProperty;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public interface DetailThemePane {
    
    /**
     * Binds enabling/disabling state of Pane edition elements.
     * @return 
     */
    BooleanProperty disableFieldsProperty();
    
    /**
     * Detects if the troncon has changed.
     * @return 
     */
    BooleanProperty tronconChangedProperty();
    
    /**
     * Record unbinded fields changes before saving.
     */
    void preSave();
}
