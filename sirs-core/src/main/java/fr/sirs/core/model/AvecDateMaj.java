package fr.sirs.core.model;

import java.time.LocalDate;
import javafx.beans.property.ObjectProperty;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public interface AvecDateMaj {
 
    ObjectProperty<LocalDate> dateMajProperty();
    
    LocalDate getDateMaj();
    
    void setDateMaj(LocalDate dateMaj);
}
