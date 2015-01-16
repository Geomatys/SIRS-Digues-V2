package fr.sirs.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public interface FXTextAbregeable {
    
    BooleanProperty abregeableProperty();
    boolean isAbregeable();
    void setAbregeable(final boolean abregeable);
    
    IntegerProperty nbAffichableProperty();
    int getNbAffichable();
    void setNbAffichable(final int nbAffichable);
}
