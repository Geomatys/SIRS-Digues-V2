

package fr.sirs.core.component;


import static fr.sirs.core.component.TronconLitRepository.BY_LIT_ID;
import fr.sirs.core.model.Lit;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import fr.sirs.core.model.TronconLit;
import java.util.List;
import org.apache.sis.util.ArgumentChecks;

/**
 * Outil g�rant les �changes avec la bdd CouchDB pour tous les objets TronconLit.
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin     (Geomatys)
 */

@View(name = BY_LIT_ID, map = "function(doc) {if(doc['@class'] && doc.litId) {emit(doc.litId, doc._id)}}")
@Component
public class TronconLitRepository extends AbstractTronconDigueRepository<TronconLit> {

    public static final String BY_LIT_ID = "byLitId";

    @Autowired
    private TronconLitRepository ( CouchDbConnector db) {
       super(db, TronconLit.class);
   }

    public List<TronconLit> getByLit(final Lit lit) {
        ArgumentChecks.ensureNonNull("Lit parent", lit);
        return this.queryView(BY_LIT_ID, lit.getId());
    }
}

