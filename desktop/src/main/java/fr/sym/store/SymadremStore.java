
package fr.sym.store;

import fr.sym.Session;
import fr.symadrem.sirs.core.model.TronconDigue;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.data.AbstractFeatureStore;
import org.geotoolkit.data.FeatureReader;
import org.geotoolkit.data.FeatureStoreFactory;
import org.geotoolkit.data.FeatureStoreFinder;
import org.geotoolkit.data.FeatureWriter;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.data.query.QueryCapabilities;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.feature.type.FeatureType;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.feature.type.PropertyDescriptor;
import org.geotoolkit.parameter.Parameters;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.FeatureId;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class SymadremStore extends AbstractFeatureStore{

    private static final Class[] BEAN_CLASSES = {TronconDigue.class};
    
    private final Session session;
    
    private Map<Name,BeanFeature.Mapping> types;
    private final CoordinateReferenceSystem crs;
    
    public SymadremStore(final Session session, final String namespace, CoordinateReferenceSystem crs) throws DataStoreException {
        this(toParameter(session, namespace, crs));
    }
    
    private static ParameterValueGroup toParameter(final Session session, final String namespace, CoordinateReferenceSystem crs){
        final ParameterValueGroup params = SymadremStoreFactory.PARAMETERS_DESCRIPTOR.createValue();
        Parameters.getOrCreate(SymadremStoreFactory.SESSION, params).setValue(session);
        Parameters.getOrCreate(SymadremStoreFactory.NAMESPACE, params).setValue(namespace);
        Parameters.getOrCreate(SymadremStoreFactory.CRS, params).setValue(crs);
        return params;
    }
    
    public SymadremStore(ParameterValueGroup params) {
        super(params);
        this.session = (Session) params.parameter("session").getValue();
        this.crs = (CoordinateReferenceSystem) params.parameter("crs").getValue();
        
    }
    
    @Override
    public FeatureStoreFactory getFactory() {
        return FeatureStoreFinder.getFactoryById(SymadremStoreFactory.NAME);
    }

    @Override
    public Set<Name> getNames() throws DataStoreException {
        loadTypes();
        return Collections.unmodifiableSet(types.keySet());
    }

    @Override
    public FeatureType getFeatureType(Name typeName) throws DataStoreException {
        typeCheck(typeName);
        return types.get(typeName).featureType;
    }
    
    private synchronized void loadTypes(){
        if(types!=null) return;
        
        types = new HashMap<>();
        for(Class beanClass : BEAN_CLASSES){
            final BeanFeature.Mapping mapping = new BeanFeature.Mapping(beanClass, getDefaultNamespace(), crs, "id");
            types.put(mapping.featureType.getName(), mapping);
        } 
    }
    
    @Override
    public FeatureReader getFeatureReader(Query query) throws DataStoreException {
        typeCheck(query.getTypeName());
        
        final List candidates;
        final BeanFeature.Mapping mapping = types.get(query.getTypeName());
        if(query.getTypeName().getLocalPart().equals(TronconDigue.class.getSimpleName())){
            candidates = session.getTroncons();
        }else{
            throw new DataStoreException("Unexpected type : "+query.getTypeName());
        }
                
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
