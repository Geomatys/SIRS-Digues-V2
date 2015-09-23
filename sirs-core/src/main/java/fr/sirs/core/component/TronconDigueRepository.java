package fr.sirs.core.component;

import java.util.List;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.springframework.beans.factory.annotation.Autowired;

import fr.sirs.core.SirsViewIterator;
import fr.sirs.core.SirsCoreRuntimeException;
import fr.sirs.core.model.TronconDigue;
import java.util.ArrayList;
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

}
