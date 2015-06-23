package fr.sirs.core.model;

import java.time.LocalDate;
import javafx.beans.property.ObjectProperty;

/**
 * Spécifie un interval de validité temporelle, borné par une date de début et une 
 * date de fin.
 * 
 * @author Alexis Manin (Geomatys)
 */
public interface AvecBornesTemporelles {
             
    public ObjectProperty<LocalDate> date_debutProperty();

    public LocalDate getDate_debut();

    public void setDate_debut(LocalDate date_debut);

    public ObjectProperty<LocalDate> date_finProperty();

    public LocalDate getDate_fin();

    public void setDate_fin(LocalDate date_fin);
}
