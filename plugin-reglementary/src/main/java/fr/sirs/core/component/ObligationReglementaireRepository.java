package fr.sirs.core.component;


import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;

import fr.sirs.core.model.RappelObligationReglementaire;
import fr.sirs.plugin.reglementaire.PluginReglementary;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
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
@View(name="all", map="function(doc) {if(doc['@class']=='fr.sirs.core.model.ObligationReglementaire') {emit(doc._id, doc._id)}}")
@Component("fr.sirs.core.component.ObligationReglementaireRepository")
public class ObligationReglementaireRepository extends
        AbstractSIRSRepository
                <ObligationReglementaire> {

    @Autowired
    public ObligationReglementaireRepository (CouchDbConnector db) {
        super(ObligationReglementaire.class, db);
        initStandardDesignDocument();
    }

    @Override
    public ObligationReglementaire create() {
        return InjectorCore.getBean(SessionCore.class).getElementCreator().createElement(ObligationReglementaire.class);
    }

    /**
     * Mets à jour l'obligation réglementaire et répercute le changement sur l'affichage des alertes.
     *
     * @param entity L'obligation réglementaire à mettre à jour.
     */
    @Override
    public void update(ObligationReglementaire entity) {
        super.update(entity);

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

        final RappelObligationReglementaireRepository rorr = InjectorCore.getBean(RappelObligationReglementaireRepository.class);
        final List<RappelObligationReglementaire> rappels = rorr.getByObligation(entity);

        if (!rappels.isEmpty()) {
            for (RappelObligationReglementaire rappel : rappels) {
                rorr.remove(rappel);
            }
        }

        PluginReglementary.showAlerts();
    }
}
