package fr.sirs.core.component;

import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.ZoneVegetation;
import java.util.Collection;
import java.util.List;
import org.apache.sis.util.ArgumentChecks;
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
    
    public List<T> getByParcelleId(final String parcelleId) {
        ArgumentChecks.ensureNonNull("Parcelle", parcelleId);
        return this.queryView(BY_PARCELLE_ID, parcelleId);
    }

    public List<T> getByParcelleIds(final String... parcelleIds) {
        ArgumentChecks.ensureNonNull("Parcelles", parcelleIds);
        return this.queryView(BY_PARCELLE_ID, (Object[]) parcelleIds);
    }

    public List<T> getByParcelleIds(final Collection<String> parcelleIds) {
        ArgumentChecks.ensureNonNull("Parcelles", parcelleIds);
        return this.queryView(BY_PARCELLE_ID, parcelleIds);
    }
    
    public List<T> getByParcelle(final ParcelleVegetation parcelle){
        ArgumentChecks.ensureNonNull("Parcelle", parcelle);
        return this.getByParcelleId(parcelle.getId());
    }
}
