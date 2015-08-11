package fr.sirs.core.component;

import fr.sirs.core.model.ZoneVegetation;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T>
 */
public abstract class AbstractZoneVegetationRepository<T extends ZoneVegetation> extends AbstractSIRSRepository<T> {
    
    public static final String BY_PARCELLE_ID = "byParcelleId";
    
    public AbstractZoneVegetationRepository(Class<T> type, CouchDbConnector db) {
        super(type, db);
    }
}
