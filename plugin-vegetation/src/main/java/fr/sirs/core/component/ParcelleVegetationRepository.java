

package fr.sirs.core.component;


import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import static fr.sirs.core.component.ParcelleVegetationRepository.BY_PLAN_ID;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.ektorp.support.Views;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.PlanVegetation;
import java.util.List;
import org.apache.sis.util.ArgumentChecks;

/**
 * Outil g�rant les �changes avec la bdd CouchDB pour tous les objets ParcelleVegetation.
 * 
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin     (Geomatys)
 */
@Views ({
@View(name=AbstractPositionableRepository.BY_LINEAR_ID, map="function(doc) {if(doc['@class']=='fr.sirs.core.model.ParcelleVegetation') {emit(doc.linearId, doc._id)}}"),
@View(name=BY_PLAN_ID, map="function(doc) {if(doc['@class']=='fr.sirs.core.model.ParcelleVegetation') {emit(doc.planId, doc._id)}}"),
@View(name="all", map="function(doc) {if(doc['@class']=='fr.sirs.core.model.ParcelleVegetation') {emit(doc._id, doc._id)}}")
})
@Component("fr.sirs.core.component.ParcelleVegetationRepository")
public class ParcelleVegetationRepository extends AbstractPositionableRepository<ParcelleVegetation> {
    
    public static final String BY_PLAN_ID = "byPlanId";
        
    @Autowired
    private ParcelleVegetationRepository ( CouchDbConnector db) {
       super(ParcelleVegetation.class, db);
       initStandardDesignDocument();
   }
    
    @Override
    public ParcelleVegetation create() {
        return InjectorCore.getBean(SessionCore.class).getElementCreator().createElement(ParcelleVegetation.class);
    }
    
    public List<ParcelleVegetation> getByPlanId(final String planId) {
        ArgumentChecks.ensureNonNull("Plan", planId);
        return this.queryView(BY_PLAN_ID, planId);
    }
    
    public List<ParcelleVegetation> getByPlan(final PlanVegetation plan) {
        ArgumentChecks.ensureNonNull("Plan", plan);
        return getByPlanId(plan.getId());
    }
}

