

package fr.sirs.core.component;


import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import fr.sirs.core.model.EtapeObligationReglementaire;
import fr.sirs.core.model.ObligationReglementaire;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Outil g�rant les �changes avec la bdd CouchDB pour tous les objets EtapeObligationReglementaire.
 * 
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin     (Geomatys)
 */
@View(name = "all", map = "function(doc) {if(doc['@class']=='fr.sirs.core.model.EtapeObligationReglementaire') {emit(doc._id, doc._id)}}")
@Component("fr.sirs.core.component.EtapeObligationReglementaireRepository")
public class EtapeObligationReglementaireRepository extends AbstractSIRSRepository<EtapeObligationReglementaire> {
    /**
     * Nom de la vue permettant de récupérer les obligations associées à une planification.
     */
    public static final String ETAPE_FOR_PLANIF = "etapeForPlanif";

    @Autowired
    private EtapeObligationReglementaireRepository(CouchDbConnector db) {
       super(EtapeObligationReglementaire.class, db);
       initStandardDesignDocument();
   }
    
    @Override
    public EtapeObligationReglementaire create() {
        return InjectorCore.getBean(SessionCore.class).getElementCreator().createElement(EtapeObligationReglementaire.class);
    }

}

