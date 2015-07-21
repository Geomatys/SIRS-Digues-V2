

package fr.sirs.core.component;


import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;

import org.ektorp.CouchDbConnector;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import fr.sirs.core.model.Convention;
import fr.sirs.core.model.ObjetReseau;
import java.util.List;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.support.View;
import org.ektorp.support.Views;

/**
 * Outil g�rant les �changes avec la bdd CouchDB pour tous les objets Convention.
 * 
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin     (Geomatys)
 */
@Views({
    @View(name="all", map="function(doc) {if(doc['@class']=='fr.sirs.core.model.Convention') {emit(doc._id, doc._id)}}"),
    @View(name=ConventionRepository.BY_RESEAU_ID, map="classpath:conventionsByReseauId.js")
})
@Component("fr.sirs.core.component.ConventionRepository")
public class ConventionRepository extends 
AbstractSIRSRepository
<Convention> {
    
    public static final String BY_RESEAU_ID = "byReseauId";
        
    @Autowired
    private ConventionRepository ( CouchDbConnector db) {
       super(Convention.class, db);
       initStandardDesignDocument();
   }

    public List<Convention> getByReseau(final ObjetReseau reseau) {
        ArgumentChecks.ensureNonNull("reseau", reseau);
        return this.queryView(BY_RESEAU_ID, reseau.getId());
    }
    
    @Override
    public Convention create() {
        return InjectorCore.getBean(SessionCore.class).getElementCreator().createElement(Convention.class);
    }
}

