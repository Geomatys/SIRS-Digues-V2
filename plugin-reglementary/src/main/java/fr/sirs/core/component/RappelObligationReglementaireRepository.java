package fr.sirs.core.component;


import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import fr.sirs.core.SirsCoreRuntimeExecption;
import fr.sirs.core.model.ElementCreator;

import fr.sirs.core.model.ObligationReglementaire;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.ektorp.support.Views;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import fr.sirs.core.model.RappelObligationReglementaire;

import java.util.List;

/**
 * Outil gérant les échanges avec la bdd CouchDB pour tous les objets RappelObligationReglementaire.
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin     (Geomatys)
 * @author Cédric Briançon  (Geomatys)
 */
@Views({
    @View(name = "all", map = "function(doc) {if(doc['@class']=='fr.sirs.core.model.RappelObligationReglementaire') {emit(doc._id, doc._id)}}"),
    @View(name = RappelObligationReglementaireRepository.RAPPELS_FOR_OBLIGATION, map = "function(doc) {if(doc['@class']=='fr.sirs.core.model.RappelObligationReglementaire') {emit(doc.obligationId, doc._id)}}")
})
@Component("fr.sirs.core.component.RappelObligationReglementaireRepository")
public class RappelObligationReglementaireRepository extends AbstractSIRSRepository<RappelObligationReglementaire> {
    /**
     * Nom de la vue permettant de récupérer les rappels associés à une obligation réglementaire.
     */
    public static final String RAPPELS_FOR_OBLIGATION = "rappelsForObl";

    @Autowired
    public RappelObligationReglementaireRepository(CouchDbConnector db) {
        super(RappelObligationReglementaire.class, db);
        initStandardDesignDocument();
    }

    @Override
    public RappelObligationReglementaire create() {
        return InjectorCore.getBean(SessionCore.class).getElementCreator().createElement(RappelObligationReglementaire.class);
    }

    /**
     * Récupère l'ensemble des rappels d'obligations pour l'obligation fournie en paramètre.
     *
     * @param obligation Obligation réglementaire pour laquelle on souhaite récupérer les rappels associés.
     * @return La liste des rappels connectés à cette obligation.
     */
    public List<RappelObligationReglementaire> getByObligation(final ObligationReglementaire obligation) {
        ArgumentChecks.ensureNonNull("Obligation parent", obligation);
        return this.queryView(RAPPELS_FOR_OBLIGATION, obligation.getId());
    }
}
