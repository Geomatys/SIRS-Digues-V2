

package fr.sirs.core.component;


import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import static fr.sirs.core.component.TraitBergeRepository.BY_BERGE_ID;
import fr.sirs.core.model.Berge;
import fr.sirs.core.model.TraitBerge;
import java.util.List;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Outil g�rant les �changes avec la bdd CouchDB pour tous les objets TraitBerge.
 * 
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin     (Geomatys)
 */
@View(name=BY_BERGE_ID, map="function(doc) {if(doc['@class']=='fr.sirs.core.model.TraitBerge') {emit(doc.bergeId, doc._id)}}")
@Component("fr.sirs.core.component.TraitBergeRepository")
public class TraitBergeRepository extends 
AbstractSIRSRepository
<TraitBerge> {

    public static final String BY_BERGE_ID = "byBergeId";
        
    @Autowired
    private TraitBergeRepository ( CouchDbConnector db) {
       super(TraitBerge.class, db);
       initStandardDesignDocument();
   }
    
    @Override
    public TraitBerge create() {
        return InjectorCore.getBean(SessionCore.class).getElementCreator().createElement(TraitBerge.class);
    }

    public List<TraitBerge> getByPlanId(final String planId) {
        ArgumentChecks.ensureNonNull("Berge id", planId);
        return this.queryView(BY_BERGE_ID, planId);
    }

    public List<TraitBerge> getByPlan(final Berge berge) {
        ArgumentChecks.ensureNonNull("Berge", berge);
        return getByPlanId(berge.getId());
    }
}

