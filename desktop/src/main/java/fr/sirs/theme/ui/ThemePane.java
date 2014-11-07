
package fr.sirs.theme.ui;

import javafx.beans.property.BooleanProperty;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public interface ThemePane {
    
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
