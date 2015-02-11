package fr.sirs.core.model;

import java.time.LocalDateTime;
import javafx.beans.property.ObjectProperty;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public interface AvecDateMaj {
 
    ObjectProperty<LocalDateTime> dateMajProperty();
    
    LocalDateTime getDateMaj();
    
    void setDateMaj(LocalDateTime dateMaj);
}
