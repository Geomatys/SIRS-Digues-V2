package fr.sirs.core.component;


import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;

import fr.sirs.core.model.PlanificationObligationReglementaire;
import fr.sirs.plugin.reglementaire.PluginReglementary;
import fr.sirs.ui.AlertManager;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.ektorp.support.Views;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import fr.sirs.core.model.ObligationReglementaire;

import java.util.List;


/**
 * Outil gérant les échanges avec la bdd CouchDB pour tous les objets ObligationReglementaire.
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin     (Geomatys)
 * @author Cédric Briançon  (Geomatys)
 */
//@Views({
//        @View(name="all", map="function(doc) {if(doc['@class']=='fr.sirs.core.model.ObligationReglementaire') {emit(doc._id, doc._id)}}"),
//        @View(name = ObligationReglementaireRepository2.OBLIGATIONS_FOR_PLANIF, map = "function(doc) {if(doc['@class']=='fr.sirs.core.model.ObligationReglementaire') {emit(doc.planifId, doc._id)}}")
//})
//@Component("fr.sirs.core.component.ObligationReglementaireRepository2")
public class ObligationReglementaireRepository2 extends
        AbstractSIRSRepository
                <ObligationReglementaire> {

    /**
     * Nom de la vue permettant de récupérer les obligations associées à une planification.
     */
    public static final String OBLIGATIONS_FOR_PLANIF = "obligationsForPlanif";

    @Autowired
    public ObligationReglementaireRepository2(CouchDbConnector db) {
        super(ObligationReglementaire.class, db);
        initStandardDesignDocument();
    }

    @Override
    public ObligationReglementaire create() {
        return InjectorCore.getBean(SessionCore.class).getElementCreator().createElement(ObligationReglementaire.class);
    }

    /**
     * Ajoute l'obligation réglementaire et répercute le changement sur l'affichage des alertes.
     *
     * @param entity L'obligation réglementaire à ajouter.
     */
    @Override
    public void add(ObligationReglementaire entity) {
        super.add(entity);

        PluginReglementary.showAlerts();
    }

    /**
     * Mets à jour l'obligation réglementaire et répercute le changement sur l'affichage des alertes.
     *
     * @param entity L'obligation réglementaire à mettre à jour.
     */
    @Override
    public void update(ObligationReglementaire entity) {
        super.update(entity);

        AlertManager.getInstance().removeAlertsForParent(entity);
        PluginReglementary.showAlerts();
    }

    /**
     * A la suppression d'une obligation réglementaire, supprimes les rappels sur cette obligation également
     * et répercute le changement sur l'affichage des alertes
     *
     * @param entity L'obligation réglementaire à supprimer.
     */
    @Override
    public void remove(ObligationReglementaire entity) {
        super.remove(entity);

        AlertManager.getInstance().removeAlertsForParent(entity);
    }


    /**
     * Récupère l'ensemble des obligations pour la planification fournie en paramètre.
     *
     * @param planif Planification pour laquelle on souhaite récupérer les obligations associées.
     * @return La liste des obligations connectées à cette planification.
     */
    public List<ObligationReglementaire> getByPlanification(final PlanificationObligationReglementaire planif) {
        ArgumentChecks.ensureNonNull("Obligation parent", planif);
        return this.queryView(OBLIGATIONS_FOR_PLANIF, planif.getId());
    }
}
