

package fr.sirs.core.component;


import fr.sirs.Injector;
import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import fr.sirs.core.model.ObligationReglementaire;
import fr.sirs.core.model.PlanificationObligationReglementaire;
import fr.sirs.core.model.RefFrequenceObligationReglementaire;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Outil gérant les échanges avec la bdd CouchDB pour tous les objets PlanificationObligationReglementaire.
 * 
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin     (Geomatys)
 * @author Cédric Briançon  (Geomatys)
 */
//@View(name="all", map="function(doc) {if(doc['@class']=='fr.sirs.core.model.PlanificationObligationReglementaire') {emit(doc._id, doc._id)}}")
//@Component("fr.sirs.core.component.PlanificationObligationReglementaireRepository2")
public class PlanificationObligationReglementaireRepository2 extends
AbstractSIRSRepository
<PlanificationObligationReglementaire> {
        
    @Autowired
    public PlanificationObligationReglementaireRepository2(CouchDbConnector db) {
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

//        final ObligationReglementaireRepository2 orr = Injector.getBean(ObligationReglementaireRepository2.class);
//        final List<ObligationReglementaire> obligations = orr.getByPlanification(entity);
//        for (final ObligationReglementaire obligation : obligations) {
//            orr.remove(obligation);
//        }
//
//        if (entity.getFrequenceId() != null && entity.getDateEcheance() != null) {
//            final RefFrequenceObligationReglementaireRepository rforr = Injector.getBean(RefFrequenceObligationReglementaireRepository.class);
//            final RefFrequenceObligationReglementaire frequence = rforr.get(entity.getFrequenceId());
//            final int nbMois = frequence.getNbMois();
//            // Ajoute la première obligation à la date choisie
//            LocalDate firstDate = LocalDate.from(entity.getDateEcheance());
//            generateObligation(entity, firstDate);
//
//            LocalDate candidDate = LocalDate.from(firstDate).plusMonths(nbMois);
//            while (candidDate.getYear() - firstDate.getYear() < 10) {
//                if (candidDate.compareTo(firstDate) != 0) {
//                    generateObligation(entity, candidDate);
//                }
//                candidDate = candidDate.plusMonths(nbMois);
//            }
//        }
    }

    @Override
    public void remove(PlanificationObligationReglementaire entity) {
        final ObligationReglementaireRepository2 orr = Injector.getBean(ObligationReglementaireRepository2.class);
        final List<ObligationReglementaire> obligations = orr.getByPlanification(entity);
        for (final ObligationReglementaire obligation : obligations) {
            orr.remove(obligation);
        }

        super.remove(entity);
    }

    /**
     * Génère et sauvegarde en base une obligation à partir d'un objet de planification
     *
     * @param planif
     * @param echeanceDate
     */
    private void generateObligation(final PlanificationObligationReglementaire planif, final LocalDate echeanceDate) {
        final ObligationReglementaireRepository2 orr = Injector.getBean(ObligationReglementaireRepository2.class);
        final ObligationReglementaire newObl = orr.create();
//        newObl.setDateEcheance(echeanceDate);
//        newObl.setEcheanceId(planif.getEcheanceId());
//        newObl.setAnnee(echeanceDate.getYear());
//        newObl.setCommentaire(planif.getCommentaire());
//        newObl.setEtapeId(planif.getEtapeId());
//        newObl.setPlanifId(planif.getId());
//        newObl.setSystemeEndiguementId(planif.getSystemeEndiguementId());
//        newObl.setTypeId(planif.getTypeId());
        newObl.setAuthor(planif.getAuthor());
        newObl.setValid(planif.getValid());
        orr.add(newObl);
    }
}

