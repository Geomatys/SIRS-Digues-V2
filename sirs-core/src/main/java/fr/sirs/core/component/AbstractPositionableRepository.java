package fr.sirs.core.component;

import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.TronconDigue;
import java.util.List;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T>
 */
public abstract class AbstractPositionableRepository<T extends Positionable> extends AbstractSIRSRepository<T> {
    
    public AbstractPositionableRepository(Class<T> type, CouchDbConnector db) {
        super(type, db);
    }

    public List<T> getByLinear(final TronconDigue linear) {
        ArgumentChecks.ensureNonNull("Linear", linear);
        return this.getByLinearId(linear.getId());
    }

    public List<T> getByLinearId(final String linearId) {
        ArgumentChecks.ensureNonNull("Linear", linearId);
        return cacheList(globalRepo.getByLinearId(type, linearId));
    }
}
