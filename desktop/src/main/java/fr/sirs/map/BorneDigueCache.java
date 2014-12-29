
package fr.sirs.map;

import fr.sirs.CorePlugin;
import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.SirsCore;
import fr.sirs.core.component.BorneDigueRepository;
import fr.sirs.core.component.DocumentListener;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Element;
import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.geotoolkit.data.bean.BeanFeatureSupplier;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.filter.identity.DefaultFeatureId;
import org.opengis.filter.Id;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class BorneDigueCache implements DocumentListener{
    
    private static BorneDigueCache INSTANCE;
    
    private final BorneDigueRepository bornesRepo;
    private final Map<String,BorneDigue> cache = new ConcurrentHashMap<>();
    private final Supplier supplier;

    public static synchronized BorneDigueCache getInstance() {
        if(INSTANCE ==null){
            INSTANCE = new BorneDigueCache();
        }
        return INSTANCE;
    }

    private BorneDigueCache() {
        final Session session = Injector.getSession();
        bornesRepo = session.getBorneDigueRepository();
        Injector.getDocumentChangeEmiter().addListener(this);
        
        for(BorneDigue bd : bornesRepo.getAll()){
            cache.put(bd.getDocumentId(), bd);
        }
        
        supplier = new Supplier();
    }

    public Supplier getSupplier() {
        return supplier;
    }
    
    public Collection<BorneDigue> getAll(){
        return cache.values();
    }

    @Override
    public void documentCreated(Element candidate) {
        if(candidate instanceof BorneDigue){
            final String docId = candidate.getDocumentId();
            cache.put(docId, (BorneDigue) candidate);
            final Id id = GO2Utilities.FILTER_FACTORY.id(Collections.singleton(new DefaultFeatureId(docId)));
            supplier.fireFeaturesAdded(id);
        }
    }

    @Override
    public void documentChanged(Element candidate) {
        if(candidate instanceof BorneDigue){
            final String docId = candidate.getDocumentId();
            cache.put(docId, (BorneDigue) candidate);
            final Id id = GO2Utilities.FILTER_FACTORY.id(Collections.singleton(new DefaultFeatureId(docId)));
            supplier.fireFeaturesUpdated(id);
        }
    }

    @Override
    public void documentDeleted(Element candidate) {
        if(candidate instanceof BorneDigue){
            final String docId = candidate.getDocumentId();
            cache.remove(docId);
            final Id id = GO2Utilities.FILTER_FACTORY.id(Collections.singleton(new DefaultFeatureId(docId)));
            supplier.fireFeaturesDeleted(id);
        }
    }
    
    public final class Supplier extends BeanFeatureSupplier{

        public Supplier() {
            super(BorneDigue.class, "id", "geometry", 
                (PropertyDescriptor t) -> CorePlugin.MAPPROPERTY_PREDICATE.test(t), 
                null, SirsCore.getEpsgCode(), BorneDigueCache.this::getAll);
        }

        @Override
        protected void fireFeaturesAdded(Id ids) {
            super.fireFeaturesAdded(ids);
        }

        @Override
        protected void fireFeaturesDeleted(Id ids) {
            super.fireFeaturesDeleted(ids);
        }

        @Override
        protected void fireFeaturesUpdated(Id ids) {
            super.fireFeaturesUpdated(ids);
        }
        
    }
    
}
