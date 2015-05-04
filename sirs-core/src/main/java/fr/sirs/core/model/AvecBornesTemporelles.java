package fr.sirs.core.model;

import java.time.LocalDateTime;
import javafx.beans.property.ObjectProperty;

/**
 * Spécifie un interval de validité temporelle, borné par une date de début et une 
 * date de fin.
 * 
 * @author Alexis Manin (Geomatys)
 */
public interface AvecBornesTemporelles {
             
    public ObjectProperty<LocalDateTime> date_debutProperty();

    public LocalDateTime getDate_debut();

    public void setDate_debut(LocalDateTime date_debut);

    public ObjectProperty<LocalDateTime> date_finProperty();

    public LocalDateTime getDate_fin();

    public void setDate_fin(LocalDateTime date_fin);
}
