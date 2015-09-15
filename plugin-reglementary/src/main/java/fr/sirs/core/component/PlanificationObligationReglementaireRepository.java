

package fr.sirs.core.component;


import fr.sirs.Injector;
import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import fr.sirs.core.model.EtapeObligationReglementaire;
import fr.sirs.core.model.ObligationReglementaire;
import fr.sirs.core.model.PlanificationObligationReglementaire;
import fr.sirs.core.model.RefFrequenceObligationReglementaire;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.ektorp.support.Views;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
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

    @Override
    public void update(PlanificationObligationReglementaire entity) {
        super.update(entity);

        final ObligationReglementaireRepository orr = Injector.getBean(ObligationReglementaireRepository.class);
        final EtapeObligationReglementaireRepository eorr = Injector.getBean(EtapeObligationReglementaireRepository.class);
        final List<ObligationReglementaire> obligations = orr.getByPlanification(entity);
        for (final ObligationReglementaire obligation : obligations) {
            final List<EtapeObligationReglementaire> etapes = eorr.getByObligation(obligation);
            for (final EtapeObligationReglementaire etape : etapes) {
                // Suppression des étapes autogénérées
                eorr.remove(etape);
            }

            // Suppression de l'obligation autogénérée.
            orr.remove(obligation);
        }

        if (entity.getFrequenceId() != null && entity.getDateDebut() != null) {
            final RefFrequenceObligationReglementaireRepository rforr = Injector.getBean(RefFrequenceObligationReglementaireRepository.class);
            final RefFrequenceObligationReglementaire frequence = rforr.get(entity.getFrequenceId());
            final int nbMois = frequence.getNbMois();
            // Ajoute la première obligation à la date choisie
            LocalDate firstDate = LocalDate.from(entity.getDateDebut());
            generateObligationAndEtape(entity, firstDate);

            LocalDate candidDate = LocalDate.from(firstDate).plusMonths(nbMois);
            while (candidDate.getYear() - firstDate.getYear() < 10) {
                if (candidDate.compareTo(firstDate) != 0) {
                    generateObligationAndEtape(entity, candidDate);
                }
                candidDate = candidDate.plusMonths(nbMois);
            }
        }
    }

    /**
     * Génère et sauvegarde en base une obligation et une étape à partir d'un objet de planification
     *
     * @param planif
     * @param echeanceDate
     */
    private void generateObligationAndEtape(final PlanificationObligationReglementaire planif, final LocalDate echeanceDate) {
        final ObligationReglementaireRepository orr = Injector.getBean(ObligationReglementaireRepository.class);
        final ObligationReglementaire newObl = orr.create();
        newObl.setAnnee(echeanceDate.getYear());
        newObl.setCommentaire(planif.getCommentaire());
        newObl.setPlanifId(planif.getId());
        newObl.setSystemeEndiguementId(planif.getSystemeEndiguementId());
        newObl.setTypeId(planif.getTypeObligationId());
        newObl.setAuthor(planif.getAuthor());
        newObl.setValid(planif.getValid());
        orr.add(newObl);

        final EtapeObligationReglementaireRepository eorr = Injector.getBean(EtapeObligationReglementaireRepository.class);
        final EtapeObligationReglementaire newEtape = eorr.create();
        newEtape.setDateEcheance(echeanceDate);
        newEtape.setObligationReglementaireId(newObl.getId());
        newEtape.setEcheanceId(planif.getEcheanceId());
        newEtape.setAuthor(planif.getAuthor());
        newEtape.setValid(planif.getValid());
        newEtape.setTypeEtapeId(planif.getTypeEtapeId());
        eorr.add(newEtape);
    }

    @Override
    public void remove(PlanificationObligationReglementaire entity) {
        final ObligationReglementaireRepository orr = Injector.getBean(ObligationReglementaireRepository.class);
        final EtapeObligationReglementaireRepository eorr = Injector.getBean(EtapeObligationReglementaireRepository.class);
        final List<ObligationReglementaire> obligations = orr.getByPlanification(entity);
        for (final ObligationReglementaire obligation : obligations) {
            final List<EtapeObligationReglementaire> etapes = eorr.getByObligation(obligation);
            for (final EtapeObligationReglementaire etape : etapes) {
                // Suppression des étapes autogénérées
                eorr.remove(etape);
            }

            // Suppression de l'obligation autogénérée.
            orr.remove(obligation);
        }

        super.remove(entity);
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

