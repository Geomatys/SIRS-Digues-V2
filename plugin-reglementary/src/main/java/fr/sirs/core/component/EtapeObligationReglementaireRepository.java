

package fr.sirs.core.component;


import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import fr.sirs.core.model.EtapeObligationReglementaire;
import fr.sirs.core.model.ObligationReglementaire;
import fr.sirs.plugin.reglementaire.PluginReglementary;
import fr.sirs.ui.AlertManager;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Outil gérant les échanges avec la bdd CouchDB pour tous les objets EtapeObligationReglementaire.
 * 
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin     (Geomatys)
 */
@View(name = EtapeObligationReglementaireRepository.ETAPE_FOR_OBLIGATION, map = "function(doc) {if(doc['@class']=='fr.sirs.core.model.EtapeObligationReglementaire') {emit(doc.obligationReglementaireId, doc._id)}}")
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
     * Ajoute l'étape d'obligation réglementaire et répercute le changement sur l'affichage des alertes.
     *
     * @param entity L'étape d'obligation réglementaire à ajouter.
     */
    @Override
    public void add(EtapeObligationReglementaire entity) {
        super.add(entity);

        PluginReglementary.showAlerts();
    }

    /**
     * Mets à jour l'étape d'obligation réglementaire et répercute le changement sur l'affichage des alertes.
     *
     * @param entity L'étape d'obligation réglementaire à mettre à jour.
     */
    @Override
    public void update(EtapeObligationReglementaire entity) {
        super.update(entity);

        AlertManager.getInstance().removeAlertsForParent(entity);
        PluginReglementary.showAlerts();
    }

    /**
     * A la suppression d'une étape d'obligation réglementaire, supprimes les rappels sur cette étape également
     * et répercute le changement sur l'affichage des alertes
     *
     * @param entity L'obligation réglementaire à supprimer.
     */
    @Override
    public void remove(EtapeObligationReglementaire entity) {
        super.remove(entity);

        AlertManager.getInstance().removeAlertsForParent(entity);
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

