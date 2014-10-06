
package fr.sym.store;

import java.util.Collection;
import java.util.Iterator;
import org.geotoolkit.data.FeatureReader;
import org.geotoolkit.data.FeatureStoreRuntimeException;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.feature.type.FeatureType;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class BeanFeatureReader implements FeatureReader<FeatureType, Feature>{

    private final BeanFeature.Mapping mapping;
    private final Iterator<Object> candidates;

    public BeanFeatureReader(BeanFeature.Mapping mapping, Collection<Object> candidates) {
        this.mapping = mapping;
        this.candidates = candidates.iterator();
    }

    @Override
    public FeatureType getFeatureType() {
        return mapping.featureType;
    }

    @Override
    public Feature next() throws FeatureStoreRuntimeException {
        return new BeanFeature(candidates.next(), mapping);
    }

    @Override
    public boolean hasNext() throws FeatureStoreRuntimeException {
        return candidates.hasNext();
    }

    @Override
    public void close() {
    }
    
}
