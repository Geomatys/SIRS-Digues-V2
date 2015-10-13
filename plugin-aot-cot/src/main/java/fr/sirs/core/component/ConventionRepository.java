

package fr.sirs.core.component;


import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import fr.sirs.core.model.AotCotAssociable;
import fr.sirs.core.model.Convention;
import java.util.List;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Outil g�rant les �changes avec la bdd CouchDB pour tous les objets Convention.
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin     (Geomatys)
 */

@View(name=ConventionRepository.BY_OBJET_ID, map="classpath:conventionsByAssociableId.js")
@Component("fr.sirs.core.component.ConventionRepository")
public class ConventionRepository extends
AbstractSIRSRepository
<Convention> {

    public static final String BY_OBJET_ID = "byObjetId";
    @Autowired
    private ConventionRepository ( CouchDbConnector db) {
       super(Convention.class, db);
       initStandardDesignDocument();
   }

    @Override
    public Convention create() {
        return InjectorCore.getBean(SessionCore.class).getElementCreator().createElement(Convention.class);
    }

    public List<Convention> getByObjet(final AotCotAssociable objet) {
        ArgumentChecks.ensureNonNull("objet", objet);
        return this.queryView(BY_OBJET_ID, objet.getId());
    }
}

