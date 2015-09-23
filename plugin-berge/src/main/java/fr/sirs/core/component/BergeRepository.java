package fr.sirs.core.component;

import org.ektorp.CouchDbConnector;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import fr.sirs.core.model.Berge;

/**
 * Outil g�rant les �changes avec la bdd CouchDB pour tous les objets Berge.
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin     (Geomatys)
 */

@Component("fr.sirs.core.component.BergeRepository")
public class BergeRepository extends AbstractTronconDigueRepository<Berge> {

    @Autowired
    private BergeRepository ( CouchDbConnector db) {
       super(db, Berge.class);
   }
}

