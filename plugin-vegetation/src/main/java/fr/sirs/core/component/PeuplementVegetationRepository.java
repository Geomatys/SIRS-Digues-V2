

package fr.sirs.core.component;


import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import static fr.sirs.core.component.PeuplementVegetationRepository.BY_PARCELLE_ID;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.ektorp.support.Views;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import fr.sirs.core.model.PeuplementVegetation;
import java.util.List;
import org.apache.sis.util.ArgumentChecks;

/**
 * Outil g�rant les �changes avec la bdd CouchDB pour tous les objets PeuplementVegetation.
 * 
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin     (Geomatys)
 */
@Views({
@View(name=BY_PARCELLE_ID, map="function(doc) {if(doc['@class']=='fr.sirs.core.model.PeuplementVegetation') {emit(doc.parcelleId, doc._id)}}"),
@View(name="all", map="function(doc) {if(doc['@class']=='fr.sirs.core.model.PeuplementVegetation') {emit(doc._id, doc._id)}}")
})
@Component("fr.sirs.core.component.PeuplementVegetationRepository")
public class PeuplementVegetationRepository extends 
AbstractSIRSRepository
<PeuplementVegetation> {
        
    public static final String BY_PARCELLE_ID = "byParcelleId";
    
    @Autowired
    private PeuplementVegetationRepository ( CouchDbConnector db) {
       super(PeuplementVegetation.class, db);
       initStandardDesignDocument();
   }
    
    @Override
    public PeuplementVegetation create() {
        return InjectorCore.getBean(SessionCore.class).getElementCreator().createElement(PeuplementVegetation.class);
    }
    
    public List<PeuplementVegetation> getByParcelleId(final String parcelleId) {
        ArgumentChecks.ensureNonNull("Parcelle", parcelleId);
        return this.queryView(BY_PARCELLE_ID, parcelleId);
    }
}

