package fr.sirs.core.component;

import fr.sirs.core.InjectorCore;
import fr.sirs.core.SessionCore;
import fr.sirs.core.SirsCore;
import fr.sirs.core.SirsCoreRuntimeExecption;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import java.util.List;
import java.util.logging.Level;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;

/**
 * Outil gérant les échanges avec la bdd CouchDB pour tous les objets tronçons.
 *
 * Note : Le cache qui permet de garder une instance unique en mémoire pour un
 * tronçon donné est extrêmement important pour les opérations de sauvegarde.
 *
 * Ex : On a un tronçon A, qui contient une crête tata et un talus de digue toto.
 *
 * On ouvre l'éditeur de toto. Le tronçon A est donc chargé en mémoire.
 * Dans le même temps on ouvre le panneau d'édition pour tata. On doit donc aussi
 * charger le tronçon A en mémoire.
 *
 * Maintenant, que se passe -'il sans cache ? On a deux copies du tronçon A en
 * mémoire pour une même révision (disons 0).
 *
 * Si on sauvegarde toto, tronçon A passe en révision 1 dans la bdd, mais pour
 * UNE SEULE des 2 copies en mémoire. En conséquence de quoi, lorsqu'on veut
 * sauvegarder tata, on a un problème : on demande à la base de faire une mise à
 * jour de la révision 1 en utilisant un objet de la révision 0.
 *
 * Résultat : ERREUR !
 *
 * Avec le cache, les deux éditeurs pointent sur la même copie en mémoire. Lorsqu'un
 * éditeur met à jour le tronçon, la révision de la copie est indentée, le deuxième
 * éditeur a donc un tronçon avec un numéro de révision correct.
 *
 * @author Samuel Andrés (Geomatys)
 * @author Alexis Manin (Geomatys)
 * 
 * @param <T> The model class managed by the repository.
 */
@View(name = AbstractTronconDigueRepository.BY_DIGUE_ID, map = "function(doc) {if(doc['@class'] && doc.digueId) {emit(doc.digueId, doc._id)}}")
public class AbstractTronconDigueRepository<T extends TronconDigue> extends AbstractSIRSRepository<T> {

    public static final String STREAM_LIGHT = "streamLight";
    public static final String BY_DIGUE_ID = "byDigueId";

    protected AbstractTronconDigueRepository(CouchDbConnector db, Class<T> typeClass) {
        super(typeClass, db);
        initStandardDesignDocument();
    }

    public List<T> getByDigue(final Digue digue) {
        ArgumentChecks.ensureNonNull("Digue parent", digue);
        return this.queryView(BY_DIGUE_ID, digue.getId());
    }

    @Override
    public void remove(T entity) {
        ArgumentChecks.ensureNonNull("Tronçon à supprimer", entity);
        constraintDeleteBoundEntities(entity);
        super.remove(entity);
    }

    @Override
    public Class<T> getModelClass() {
        return type;
    }

    @Override
    public T create() {
        final SessionCore session = InjectorCore.getBean(SessionCore.class);
        if(session!=null){
            return session.getElementCreator().createElement(type);
        } else {
            throw new SirsCoreRuntimeExecption("Pas de session courante");
        }
    }

    /**
     * Cette contrainte s'assure de supprimer les SR et bornes associées au troncon
     * en cas de suppression.
     */
    private void constraintDeleteBoundEntities(TronconDigue entity) {
        //on supprime tous les SR associés
        final SystemeReperageRepository srrepo = InjectorCore.getBean(SystemeReperageRepository.class);
        final List<SystemeReperage> srs = srrepo.getByLinearId(entity.getId());
        for(SystemeReperage sr : srs) {
            srrepo.remove(sr, entity);
        }
        List<Positionable> boundPositions = TronconUtils.getPositionableList(entity);
        for (Positionable p : boundPositions) {
            try {
                AbstractSIRSRepository repo = InjectorCore.getBean(SessionCore.class).getRepositoryForClass(p.getClass());
                repo.remove(p);
            } catch (Exception e) {
                SirsCore.LOGGER.log(Level.WARNING, "An element bound to the troncon cannot be deleted : "+ p.getId(), e);
            }
        }
        // TODO : Set an end date to bornes which are not used anymore.
    }

    @Override
    protected T onLoad(T toLoad) {
        new DefaultSRChangeListener(toLoad);
        return toLoad;
    }
}
