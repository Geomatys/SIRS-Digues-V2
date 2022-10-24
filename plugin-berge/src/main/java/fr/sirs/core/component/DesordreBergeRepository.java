

package fr.sirs.core.component;


import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.DesordreBerge;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.ektorp.support.Views;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Outil gérant les échanges avec la bdd CouchDB pour tous les objets DesordreBerge.
 *
 * @author Estelle Idée (Geomatys)
 */

@Views({
        @View(name = AbstractDesordreRepository.ALL_OPEN_BY_LINEAR_ID, map = "classpath:DesordreLit-by-linearId.js")
})
@Component("fr.sirs.core.component.DesordreBergeRepository")
public class DesordreBergeRepository extends AbstractDesordreRepository<DesordreBerge> {
    @Autowired
    private DesordreBergeRepository(CouchDbConnector db) {
        super(DesordreBerge.class, db);
        initStandardDesignDocument();
    }

    @Override
    public DesordreBerge create() {
        return InjectorCore.getBean(SessionCore.class).getElementCreator().createElement(DesordreBerge.class);
    }

    /**
     * get all the desordres for a Berge that are not archived (end date null or end date in the future)
     * @param linearId id of the berge
     * @return the list of the open desordres present on the berge
     */
    @Override
    public List<DesordreBerge> getDesordreOpenByLinearId(final String linearId) {
        ArgumentChecks.ensureNonNull("Desordre Berge", linearId);
        return this.queryView(ALL_OPEN_BY_LINEAR_ID, linearId);
    }
}