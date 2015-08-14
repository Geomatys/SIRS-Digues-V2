

package fr.sirs.core.component;


import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import static fr.sirs.core.component.AbstractZoneVegetationRepository.BY_PARCELLE_ID;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.ektorp.support.Views;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import fr.sirs.core.model.HerbaceeVegetation;

/**
 * Outil g�rant les �changes avec la bdd CouchDB pour tous les objets HerbaceeVegetation.
 * 
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin     (Geomatys)
 */
@Views({
@View(name=BY_PARCELLE_ID, map="function(doc) {if(doc['@class']=='fr.sirs.core.model.HerbaceeVegetation') {emit(doc.parcelleId, doc._id)}}"),
@View(name="all", map="function(doc) {if(doc['@class']=='fr.sirs.core.model.HerbaceeVegetation') {emit(doc._id, doc._id)}}")
})
@Component("fr.sirs.core.component.HerbaceeVegetationRepository")
public class HerbaceeVegetationRepository extends AbstractZoneVegetationRepository<HerbaceeVegetation> {
        
    @Autowired
    private HerbaceeVegetationRepository ( CouchDbConnector db) {
       super(HerbaceeVegetation.class, db);
       initStandardDesignDocument();
   }
    
    @Override
    public HerbaceeVegetation create() {
        return InjectorCore.getBean(SessionCore.class).getElementCreator().createElement(HerbaceeVegetation.class);
    }
}
