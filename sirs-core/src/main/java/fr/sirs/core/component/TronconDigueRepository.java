package fr.sirs.core.component;

import static fr.sirs.core.component.AbstractTronconDigueRepository.STREAM_LIGHT;
import fr.sirs.core.InjectorCore;
import java.util.List;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;

import fr.sirs.core.SirsViewIterator;
import fr.sirs.core.SessionCore;
import fr.sirs.core.SirsCore;
import fr.sirs.core.SirsCoreRuntimeException;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.util.StreamingIterable;
import java.util.ArrayList;
import java.util.logging.Level;
import org.apache.sis.util.ArgumentChecks;
import org.geotoolkit.util.collection.CloseableIterator;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@View(name = AbstractTronconDigueRepository.STREAM_LIGHT, map = "classpath:TronconDigueLight-map.js")
@Component
public class TronconDigueRepository extends AbstractTronconDigueRepository<TronconDigue> {

    @Autowired
    private TronconDigueRepository(CouchDbConnector db) {
        super(db, TronconDigue.class);
        initStandardDesignDocument();
    }

    public List<TronconDigue> getByDigue(final Digue digue) {
        ArgumentChecks.ensureNonNull("Digue parent", digue);
        return this.queryView(BY_DIGUE_ID, digue.getId());
    }

    @Override
    public void remove(TronconDigue entity) {
        ArgumentChecks.ensureNonNull("Tronçon à supprimer", entity);
        constraintDeleteBoundEntities(entity);
        super.remove(entity);
    }

    @Override
    public Class<TronconDigue> getModelClass() {
        return TronconDigue.class;
    }

    @Override
    public TronconDigue create() {
        final SessionCore session = InjectorCore.getBean(SessionCore.class);
        if(session!=null){
            return session.getElementCreator().createElement(TronconDigue.class);
        } else {
            throw new SirsCoreRuntimeException("Pas de session courante");
        }
    }

    /**
     * Note : As the objects returned here are incomplete, they're not cached.
     *
     * @return a light version of the tronçon, without sub-structures.
     */
    public List<TronconDigue> getAllLight() {
        final List<TronconDigue> lst = new ArrayList<>();
        try (final SirsViewIterator<TronconDigue> ite = SirsViewIterator.create(TronconDigue.class, db.queryForStreamingView(createQuery(STREAM_LIGHT)))) {
            while (ite.hasNext()) {
                lst.add(ite.next());
            }
        } catch (Exception e) {
            throw new SirsCoreRuntimeException("Cannot build Troncon view.", e);
        }
        return lst;
    }

    public SirsViewIterator<TronconDigue> getAllLightIterator() {
        return SirsViewIterator.create(TronconDigue.class,
                db.queryForStreamingView(createQuery(STREAM_LIGHT)));
    }

    /**
     * Cette contrainte s'assure de supprimer les SR et bornes associées au troncon
     * en cas de suppression.
     */
    private void constraintDeleteBoundEntities(TronconDigue entity) {
        //on supprime tous les SR associés
        final SystemeReperageRepository srrepo = InjectorCore.getBean(SystemeReperageRepository.class);
        final StreamingIterable<SystemeReperage> srs = srrepo.getByLinearIdStreaming(entity.getId());
        try (final CloseableIterator<SystemeReperage>  srIt = srs.iterator()) {
            while (srIt.hasNext()) {
                srrepo.remove(srIt.next(), entity);
            }
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
    protected TronconDigue onLoad(TronconDigue toLoad) {
        new DefaultSRChangeListener(toLoad);
        return toLoad;
    }
}
