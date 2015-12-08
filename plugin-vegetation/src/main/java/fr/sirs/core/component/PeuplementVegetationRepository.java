

package fr.sirs.core.component;


import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import static fr.sirs.core.component.AbstractZoneVegetationRepository.BY_PARCELLE_ID;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import fr.sirs.core.model.PeuplementVegetation;

/**
 * Outil g�rant les �changes avec la bdd CouchDB pour tous les objets PeuplementVegetation.
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin     (Geomatys)
 */
@View(name=BY_PARCELLE_ID, map="function(doc) {if(doc['@class']=='fr.sirs.core.model.PeuplementVegetation') {emit(doc.parcelleId, doc._id)}}")
@Component
public class PeuplementVegetationRepository extends AbstractZoneVegetationRepository<PeuplementVegetation> {

    @Autowired
    private PeuplementVegetationRepository ( CouchDbConnector db) {
       super(PeuplementVegetation.class, db);
       initStandardDesignDocument();
   }

    @Override
    public PeuplementVegetation create() {
        return InjectorCore.getBean(SessionCore.class).getElementCreator().createElement(PeuplementVegetation.class);
    }
}

