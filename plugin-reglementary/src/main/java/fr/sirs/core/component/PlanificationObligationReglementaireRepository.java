

package fr.sirs.core.component;


import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import fr.sirs.core.model.EtapeObligationReglementaire;
import fr.sirs.core.model.PlanificationObligationReglementaire;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.ektorp.support.Views;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Outil gérant les échanges avec la bdd CouchDB pour tous les objets PlanificationObligationReglementaire.
 * 
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin     (Geomatys)
 */
@Views({
        @View(name = "all", map = "function(doc) {if(doc['@class']=='fr.sirs.core.model.PlanificationObligationReglementaire') {emit(doc._id, doc._id)}}"),
        @View(name = PlanificationObligationReglementaireRepository.PLANIFS_FOR_ETAPE, map = "function(doc) {if(doc['@class']=='fr.sirs.core.model.PlanificationObligationReglementaire') {emit(doc.etapeId, doc._id)}}")
})
@Component("fr.sirs.core.component.PlanificationObligationReglementaireRepository")
public class PlanificationObligationReglementaireRepository extends AbstractSIRSRepository<PlanificationObligationReglementaire> {
    /**
     * Nom de la vue permettant de récupérer les obligations associées à une planification.
     */
    public static final String PLANIFS_FOR_ETAPE = "planifsForEtape";

    @Autowired
    private PlanificationObligationReglementaireRepository(CouchDbConnector db) {
       super(PlanificationObligationReglementaire.class, db);
       initStandardDesignDocument();
   }
    
    @Override
    public PlanificationObligationReglementaire create() {
        return InjectorCore.getBean(SessionCore.class).getElementCreator().createElement(PlanificationObligationReglementaire.class);
    }

    /**
     * Récupère l'ensemble des planifications pour l'étape fournie en paramètre.
     *
     * @param etape Etape pour laquelle on souhaite récupérer les planifications.
     * @return La liste des planifications connectées à cette étape.
     */
    public List<PlanificationObligationReglementaire> getByEtape(final EtapeObligationReglementaire etape) {
        ArgumentChecks.ensureNonNull("Obligation parent", etape);
        return this.queryView(PLANIFS_FOR_ETAPE, etape.getId());
    }

}

