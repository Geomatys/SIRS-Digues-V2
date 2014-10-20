
package fr.sym.store;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.data.AbstractFeatureStore;
import org.geotoolkit.data.FeatureReader;
import org.geotoolkit.data.FeatureStoreFactory;
import org.geotoolkit.data.FeatureWriter;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.data.query.QueryCapabilities;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.feature.type.FeatureType;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.FeatureId;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class BeanStore extends AbstractFeatureStore{
        
    private final Supplier<Collection> beans;
    private final Map<Name,BeanFeature.Mapping> types = new HashMap<>();
    private final CoordinateReferenceSystem crs;
    
    public BeanStore(Class beanClass, Supplier<Collection> beans, final String namespace, CoordinateReferenceSystem crs) throws DataStoreException {
        super(null);
        final BeanFeature.Mapping mapping = new BeanFeature.Mapping(beanClass, getDefaultNamespace(), crs, "id");
        if(mapping.featureType.getCoordinateReferenceSystem()!=null){
            //liste uniquement des données géographique
            types.put(mapping.featureType.getName(), mapping);
        }
        this.beans = beans;
        this.crs = crs;
    }
        
    
    @Override
    public FeatureStoreFactory getFactory() {
        return null;
    }

    @Override
    public Set<Name> getNames() throws DataStoreException {
        return Collections.unmodifiableSet(types.keySet());
    }

    @Override
    public FeatureType getFeatureType(Name typeName) throws DataStoreException {
        typeCheck(typeName);
        return types.get(typeName).featureType;
    }
        
    @Override
    public FeatureReader getFeatureReader(Query query) throws DataStoreException {
        typeCheck(query.getTypeName());
        
        Collection candidates = beans.get();
        final BeanFeature.Mapping mapping = types.get(query.getTypeName());
                        
        final FeatureReader reader = new BeanFeatureReader(mapping, candidates);
        return handleRemaining(reader, query);
    }
    
    @Override
    public List<FeatureId> addFeatures(Name groupName, Collection<? extends Feature> newFeatures, Hints hints) throws DataStoreException {
        //TODO
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateFeatures(Name groupName, Filter filter, Map<? extends PropertyDescriptor, ? extends Object> values) throws DataStoreException {
        //TODO
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void removeFeatures(Name groupName, Filter filter) throws DataStoreException {
        //TODO
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public FeatureWriter getFeatureWriter(Name typeName, Filter filter, Hints hints) throws DataStoreException {
        //TODO
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void refreshMetaModel() {
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // NOT SUPPORTED ///////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    
    @Override
    public QueryCapabilities getQueryCapabilities() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void createFeatureType(Name typeName, FeatureType featureType) throws DataStoreException {
        throw new DataStoreException("Not supported.");
    }

    @Override
    public void updateFeatureType(Name typeName, FeatureType featureType) throws DataStoreException {
        throw new DataStoreException("Not supported.");
    }

    @Override
    public void deleteFeatureType(Name typeName) throws DataStoreException {
        throw new DataStoreException("Not supported.");
    }
    
}
