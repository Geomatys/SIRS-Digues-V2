package fr.sirs.owc;

import fr.sirs.CorePlugin;
import fr.sirs.Injector;
import org.geotoolkit.owc.xml.OwcExtension;
import fr.sirs.Plugin;
import fr.sirs.Session;
import fr.sirs.core.h2.H2Helper;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.spi.ServiceRegistry;
import javax.xml.bind.JAXBElement;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.bean.BeanFeatureSupplier;
import org.geotoolkit.data.bean.BeanStore;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.data.query.Selector;
import org.geotoolkit.data.query.Source;
import org.geotoolkit.data.query.TextStatement;
import org.geotoolkit.db.JDBCFeatureStore;
import org.geotoolkit.feature.type.DefaultName;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.owc.gtkext.ParameterType;
import static org.geotoolkit.owc.xml.OwcMarshallerPool.*;
import org.geotoolkit.owc.xml.v10.OfferingType;

/**
 * Extension OWC pour SIRS.
 * 
 * @author Samuel Andrés (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
public class OwcExtentionSirs extends OwcExtension {
    
    public static final String CODE = "http://www.france-digues.fr/owc";
    public static final String KEY_BEANCLASS = "beanClass";
    public static final String KEY_SQLQUERY = "sqlQuery";

    private static final List<MapItem> mapItems = new ArrayList();
    
    static {
        final Iterator<Plugin> ite = ServiceRegistry.lookupProviders(Plugin.class);
        while(ite.hasNext()){
            mapItems.addAll(ite.next().getMapItems());
        }
    }

    public OwcExtentionSirs() {
        super(CODE,10);
    }
    
    @Override
    public boolean canHandle(MapLayer layer) {
        if(layer instanceof FeatureMapLayer){
            final FeatureMapLayer fml = (FeatureMapLayer) layer;
            final FeatureCollection collection = fml.getCollection();
            final FeatureStore store = collection.getSession().getFeatureStore();
            return (store instanceof BeanStore && getTypeName(layer) != null) || getSQLQuery(layer)!=null;
        }
        return false;
    }
    
    @Override
    public MapLayer createLayer(OfferingType offering) throws DataStoreException {
        final List<Object> fields = offering.getOperationOrContentOrStyleSet();
        
        //rebuild parameters map
        String beanClassName = null;
        String sqlQuery = null;
        for(Object o : fields){
            if(o instanceof JAXBElement){
                o = ((JAXBElement)o).getValue();
            }
            if(o instanceof ParameterType){
                final ParameterType param = (ParameterType) o;
                if(KEY_BEANCLASS.equals(param.getKey())){
                    beanClassName = param.getValue();
                }else if(KEY_SQLQUERY.equals(param.getKey())){
                    sqlQuery = param.getValue();
                }
            }
        }
        
        if(beanClassName!=null){
            final Class clazz;
            try {
                clazz = Class.forName(beanClassName);
            } catch (ClassNotFoundException ex) {
                throw new DataStoreException(ex.getMessage(),ex);
            }
            return CorePlugin.createLayer(clazz);
        }else if(sqlQuery!=null){
            final Session session = Injector.getSession();
            try {
                final FeatureStore h2Store = H2Helper.getStore(session.getConnector());
                final Query fsquery = org.geotoolkit.data.query.QueryBuilder.language(
                    JDBCFeatureStore.CUSTOM_SQL, sqlQuery, new DefaultName("requete"));
                final FeatureCollection col = h2Store.createSession(false).getFeatureCollection(fsquery);
                return MapBuilder.createFeatureLayer(col);
            } catch (SQLException ex) {
                throw new DataStoreException(ex.getMessage(),ex);
            }
        }else{
            throw new DataStoreException("Invalid configuration, missing bean class name or sql query");
        }
    }
    
    @Override
    public OfferingType createOffering(MapLayer mapLayer) {
        final OfferingType offering = OWC_FACTORY.createOfferingType();
        offering.setCode(CODE);
        
        final FeatureMapLayer fml = (FeatureMapLayer) mapLayer;
        final FeatureCollection collection = fml.getCollection();
        final FeatureStore store = collection.getSession().getFeatureStore();
        final Name typeName = getTypeName(fml);
        final String sqlQuery = getSQLQuery(mapLayer);
        
        if(sqlQuery!=null){
            //write the blean class name
            final List<Object> fieldList = offering.getOperationOrContentOrStyleSet();
            fieldList.add(new ParameterType(KEY_SQLQUERY,String.class.getName(),sqlQuery));
        }else{
            final BeanFeatureSupplier supplier;
            try {
                supplier = ((BeanStore)store).getBeanSupplier(typeName);
            } catch (DataStoreException ex) {
                throw new IllegalStateException(ex.getMessage(),ex);
            }
            final Class beanClass = supplier.getBeanClass();
            //write the blean class name
            final List<Object> fieldList = offering.getOperationOrContentOrStyleSet();
            fieldList.add(new ParameterType(KEY_BEANCLASS,String.class.getName(),beanClass.getName()));
        }
        
        return offering;
    }
        
    private static Name getTypeName(MapLayer layer){
        if(layer instanceof FeatureMapLayer){
            final FeatureMapLayer fml = (FeatureMapLayer) layer;
            final Source source = fml.getCollection().getSource();
            if(source instanceof Selector){
                final Selector selector = (Selector)source;
                return selector.getFeatureTypeName();
            }
        }else if(layer instanceof CoverageMapLayer){
            final CoverageMapLayer cml = (CoverageMapLayer) layer;
            final CoverageReference covref = cml.getCoverageReference();
            return covref.getName();
        }
        return null;
    }
    
    private static String getSQLQuery(MapLayer layer){
        if(layer instanceof FeatureMapLayer){
            final FeatureMapLayer fml = (FeatureMapLayer) layer;
            Source source = fml.getCollection().getSource();
            if(source instanceof TextStatement){
                final TextStatement stmt = (TextStatement)source;
                return stmt.getStatement();
            }
        }
        return null;
    }
    
}
