

package fr.sirs.core.component;


import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.SystemeReperageBorne;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.util.StreamingIterable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import org.geotoolkit.util.collection.CloseableIterator;
import org.ektorp.CouchDbConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Outil gérant les échanges avec la bdd CouchDB pour tous les objets BorneDigue.
 *
 * Important :
 * - La suppression d'une borne demande une gestion compliquée des relations.
 * Si vous surchargez la méthode {@link #remove(fr.sirs.core.model.BorneDigue) } ou
 * {@link #remove(fr.sirs.core.model.BorneDigue...) }, vous devriez appeler la méthode
 * super.remove pour éviter de corrompre l'intégrité de vos données.
 *
 * - La suppression d'une borne peut déclencher une réaction en cascade ayant pour
 * effet la mise à jour de beaucoup d'objets de la base. Ceci s'explique car les
 * tronçons ont une liste de bornes associées. Pour chaque tronçon, les systèmes
 * de repérages peuvent utiliser une de ces bornes. Supprimer une borne revient
 * donc à supprimer son association dans potentiellement 1 à n tronçon et / ou
 * système de repérage. C'est le premier niveau de la réaction en chaine. Si un
 * SR est affecté, la position de tous les objets affectés par ce SR doit être
 * modifiée.
 *
 * - Si vous devez supprimer plusieurs bornes, privilégiez la méthode {@link #remove(fr.sirs.core.model.BorneDigue...) },
 * elle sera beaucoup plus efficace pour gérer les associations qu'une boucle sur
 * la méthode {@link #remove(fr.sirs.core.model.BorneDigue) }.
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin     (Geomatys)
 */
@Component("fr.sirs.core.component.BorneDigueRepository")
public class BorneDigueRepository extends AbstractSIRSRepository<BorneDigue> {

    @Autowired SessionCore session;

    @Autowired SystemeReperageRepository srRepository;

    @Autowired
    private BorneDigueRepository ( CouchDbConnector db) {
       super(BorneDigue.class, db);
       initStandardDesignDocument();
    }

    @Override
    public BorneDigue create() {
        return InjectorCore.getBean(SessionCore.class).getElementCreator().createElement(BorneDigue.class);
    }

    /**
     * Supprime une borne de la base.
     *
     * IMPORTANT : - La suppression d'une borne demande une gestion compliquée
     * des relations. Si vous surchargez cette méthode, vous devriez appeler la
     * méthode super.remove pour éviter de corrompre l'intégrité de vos données.
     *
     * - La suppression d'une borne peut déclencher une réaction en cascade
     * ayant pour effet la mise à jour de beaucoup d'objets de la base. Ceci
     * s'explique car les tronçons ont une liste de bornes associées. Pour
     * chaque tronçon, les systèmes de repérages peuvent utiliser une de ces
     * bornes. Supprimer une borne revient donc à supprimer son association dans
     * potentiellement 1 à n tronçon et / ou système de repérage. C'est le
     * premier niveau de la réaction en chaine. Si un SR est affecté, la
     * position de tous les objets affectés par ce SR doit être modifiée.
     *
     * - Si vous devez supprimer plusieurs bornes, privilégiez la méthode
     * {@link #remove(fr.sirs.core.model.BorneDigue...)} , elle sera beaucoup
     * plus efficace pour gérer les associations qu'une boucle sur cette
     * méthode.
     *
     * @param entity la borne à supprimer.
     */
    @Override
    public void remove(BorneDigue entity) {
        remove(new BorneDigue[]{entity});
    }

    /**
     * Supprime toutes les bornes données de la base.
     *
     * IMPORTANT : - La suppression d'une borne demande une gestion compliquée
     * des relations. Si vous surchargez cette méthode, vous devriez appeler la
     * méthode super.remove pour éviter de corrompre l'intégrité de vos données.
     *
     * - La suppression d'une borne peut déclencher une réaction en cascade
     * ayant pour effet la mise à jour de beaucoup d'objets de la base. Ceci
     * s'explique car les tronçons ont une liste de bornes associées. Pour
     * chaque tronçon, les systèmes de repérages peuvent utiliser une de ces
     * bornes. Supprimer une borne revient donc à supprimer son association dans
     * potentiellement 1 à n tronçon et / ou système de repérage. C'est le
     * premier niveau de la réaction en chaine. Si un SR est affecté, la
     * position de tous les objets affectés par ce SR doit être modifiée.
     *
     * @param entity la liste des bornes à supprimer.
     */
    public void remove(BorneDigue... entity) {

        final HashSet<String> toDeleteIds = new HashSet<>();
        for (BorneDigue bd : entity) {
            toDeleteIds.add(bd.getId());
        }

        /**
         * We iterate over db troncons to find ones which reference input bornes.
         * For all matching troncon, we will analyse its SRs to update the ones
         * which work with bornes marked for deletion.
         */
        for (final AbstractSIRSRepository tdRepository : session.getRepositoriesForClass(TronconDigue.class)) {
            try (CloseableIterator<TronconDigue> tdIterator = tdRepository.getAllStreaming().iterator()) {
                while (tdIterator.hasNext()) {
                    final TronconDigue t = tdIterator.next();
                    // Use list in case doublons where present in troncon borne list.
                    final ArrayList<String> joinToDeleteAndTroncon = new ArrayList<>();
                    for (final String bdId : t.getBorneIds()) {
                        if (toDeleteIds.contains(bdId)) {
                            joinToDeleteAndTroncon.add(bdId);
                        }
                    }

                    // found matching bornes in the current troncon. We have to analyze
                    // its SRs.
                    if (joinToDeleteAndTroncon.size() > 0) {
                        /*
                         * We'll wait that all SR are updated before removing bornes from
                         * tronçon, because if we ever update troncon first, if an error
                         * happens while updating SRs, database would contain SRs with bornes
                         * which are no longer in its parent troncon.
                         */
                        final StreamingIterable<SystemeReperage> srList = srRepository.getByLinearStreaming(t);
                        try (final CloseableIterator<SystemeReperage> it = srList.iterator()) {
                            while (it.hasNext()) {
                                final SystemeReperage sr = it.next();
                                final Iterator<SystemeReperageBorne> srBornes = sr.systemeReperageBornes.iterator();
                                boolean mustUpdateSR = false;
                                while (srBornes.hasNext()) {
                                    final SystemeReperageBorne srBorne = srBornes.next();
                                    /* HashSet implies a constant response time whatever its size. So we
                                     * can use full set of Ids to perform comparison. It also could
                                     * repair sr in case it references a borne which is not in its troncon.
                                     */
                                    if (toDeleteIds.contains(srBorne.getBorneId())) {
                                        mustUpdateSR = true;
                                        srBornes.remove();
                                    }
                                }
                                if (mustUpdateSR) {
                                    srRepository.update(sr, t);
                                }
                            }
                        }
                        t.getBorneIds().removeAll(joinToDeleteAndTroncon);
                        tdRepository.update(t);
                    }
                }
            }
        }

        for (final BorneDigue bd : entity) {
            super.remove(bd);
        }
    }
}

