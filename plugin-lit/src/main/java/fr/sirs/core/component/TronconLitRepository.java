

package fr.sirs.core.component;


import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
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
@Component("fr.sirs.core.component.TronconLitRepository")
public class TronconLitRepository extends AbstractSIRSRepository<TronconLit> {
        
    public static final String BY_LIT_ID = "byLitId";
    
    @Autowired
    private TronconLitRepository ( CouchDbConnector db) {
       super(TronconLit.class, db);
       initStandardDesignDocument();
   }
    
    @Override
    public TronconLit create() {
        return InjectorCore.getBean(SessionCore.class).getElementCreator().createElement(TronconLit.class);
    }
    
    public List<TronconLit> getByLit(final Lit lit) {
        ArgumentChecks.ensureNonNull("Lit parent", lit);
        return this.queryView(BY_LIT_ID, lit.getId());
    }
}

