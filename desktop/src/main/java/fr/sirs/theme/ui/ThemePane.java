
package fr.sirs.theme.ui;

import javafx.beans.property.ReadOnlyBooleanProperty;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public interface ThemePane {
    
    /**
     * Detects if the troncon has changed. Read-only, because external components
     * should not be able to modify it.
     * @return 
     */
    ReadOnlyBooleanProperty tronconChangedProperty();
}
