
package fr.sirs.plugin.vegetation;

import fr.sirs.core.model.PlanVegetation;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Session de vegetation.
 * Contient le plan de gestion en cours.
 *
 * @author Johann Sorel (Geomatys)
 */
public final class VegetationSession {

    public static final VegetationSession INSTANCE = new VegetationSession();

    private final ObjectProperty<PlanVegetation> planProperty = new SimpleObjectProperty<>();

    private VegetationSession(){}

    /**
     * Plan de gestion actif.
     * 
     * @return
     */
    public ObjectProperty<PlanVegetation> planProperty() {
        return planProperty;
    }

}
