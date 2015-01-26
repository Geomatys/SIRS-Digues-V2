package fr.sirs.core.model;

import javafx.beans.property.StringProperty;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public interface AvecLibelle {
    
    StringProperty libelleProperty();
    
    String getLibelle();
    
    void setLibelle(String libelle);
}
