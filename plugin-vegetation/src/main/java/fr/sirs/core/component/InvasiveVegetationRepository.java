

package fr.sirs.core.component;


import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import static fr.sirs.core.component.InvasiveVegetationRepository.BY_PARCELLE_ID;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import fr.sirs.core.model.InvasiveVegetation;

/**
 * Outil g�rant les �changes avec la bdd CouchDB pour tous les objets InvasiveVegetation.
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin     (Geomatys)
 */
@View(name=BY_PARCELLE_ID, map="function(doc) {if(doc['@class']=='fr.sirs.core.model.InvasiveVegetation') {emit(doc.parcelleId, doc._id)}}")
@Component
public class InvasiveVegetationRepository extends AbstractZoneVegetationRepository<InvasiveVegetation> {

    @Autowired
    private InvasiveVegetationRepository ( CouchDbConnector db) {
       super(InvasiveVegetation.class, db);
       initStandardDesignDocument();
   }

    @Override
    public InvasiveVegetation create() {
        return InjectorCore.getBean(SessionCore.class).getElementCreator().createElement(InvasiveVegetation.class);
    }
}

