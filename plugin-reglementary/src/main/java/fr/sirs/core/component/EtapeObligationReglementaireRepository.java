

package fr.sirs.core.component;


import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import fr.sirs.core.model.EtapeObligationReglementaire;
import fr.sirs.core.model.ObligationReglementaire;
import fr.sirs.core.model.PlanificationObligationReglementaire;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.ektorp.support.Views;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Outil gérant les échanges avec la bdd CouchDB pour tous les objets EtapeObligationReglementaire.
 * 
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin     (Geomatys)
 */
@Views({
        @View(name = "all", map = "function(doc) {if(doc['@class']=='fr.sirs.core.model.EtapeObligationReglementaire') {emit(doc._id, doc._id)}}"),
        @View(name = EtapeObligationReglementaireRepository.ETAPE_FOR_OBLIGATION, map = "function(doc) {if(doc['@class']=='fr.sirs.core.model.EtapeObligationReglementaire') {emit(doc.obligationReglementaireId, doc._id)}}")
})
@Component("fr.sirs.core.component.EtapeObligationReglementaireRepository")
public class EtapeObligationReglementaireRepository extends AbstractSIRSRepository<EtapeObligationReglementaire> {
    /**
     * Nom de la vue permettant de récupérer les obligations associées à une planification.
     */
    public static final String ETAPE_FOR_OBLIGATION = "etapeForObligation";

    @Autowired
    private EtapeObligationReglementaireRepository(CouchDbConnector db) {
       super(EtapeObligationReglementaire.class, db);
       initStandardDesignDocument();
   }
    
    @Override
    public EtapeObligationReglementaire create() {
        return InjectorCore.getBean(SessionCore.class).getElementCreator().createElement(EtapeObligationReglementaire.class);
    }

    /**
     * Récupère l'ensemble des obligations pour l'obligation fournie en paramètre.
     *
     * @param obligation Obligation pour laquelle on souhaite récupérer les étapes.
     * @return La liste des planifications connectées à cette étape.
     */
    public List<EtapeObligationReglementaire> getByObligation(final ObligationReglementaire obligation) {
        ArgumentChecks.ensureNonNull("Obligation parent", obligation);
        return this.queryView(ETAPE_FOR_OBLIGATION, obligation.getId());
    }
}

