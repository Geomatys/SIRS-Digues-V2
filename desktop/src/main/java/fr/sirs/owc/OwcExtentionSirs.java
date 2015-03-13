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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.spi.ServiceRegistry;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.storage.DataStoreException;
import org.bouncycastle.asn1.crmf.ProofOfPossession;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.bean.BeanFeatureSupplier;
import org.geotoolkit.data.bean.BeanStore;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.data.query.Selector;
import org.geotoolkit.data.query.Source;
import org.geotoolkit.data.query.TextStatement;
import org.geotoolkit.db.JDBCFeatureStore;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.feature.type.DefaultName;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.filter.DefaultPropertyName;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.owc.gtkext.ParameterType;
import static org.geotoolkit.owc.xml.OwcMarshallerPool.*;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.owc.gtkext.ObjectFactory;
import org.geotoolkit.owc.xml.v10.OfferingType;
import org.geotoolkit.se.xml.v110.ParameterValueType;
import org.geotoolkit.sld.xml.StyleXmlIO;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.PropertyName;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;
import org.w3._2005.atom.ContentType;
import org.w3._2005.atom.EntryType;
import org.w3._2005.atom.TextType;

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
    public static final String ATT_KEY_EXTRA_DIMENSIONS = "extraDimensions";
    public static final String ATT_KEY_LOWER_EXTRA_DIMENSION = "lower";
    public static final String ATT_KEY_UPPER_EXTRA_DIMENSION = "upper";

    private static final List<MapItem> mapItems = new ArrayList();
    
    static {
        final Iterator<Plugin> ite = ServiceRegistry.lookupProviders(Plugin.class);
        while(ite.hasNext()){
            mapItems.addAll(ite.next().getMapItems());
        }
        
        final Hints hints = new Hints();
        hints.put(Hints.FILTER_FACTORY, FilterFactory2.class);
        filterFactory = (FilterFactory2) FactoryFinder.getFilterFactory(hints);
    }
    private static final StyleXmlIO STYLE_XML_IO = new StyleXmlIO();
    private static final ObjectFactory GEOTK_FACTORY = new ObjectFactory();

    private static final FilterFactory2 filterFactory;
    
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
        final MapLayer mapLayer;
        FeatureMapLayer.DimensionDef datefilter = null;
        final List<Object> fields = offering.getOperationOrContentOrStyleSet();
        
        //rebuild parameters map
        String beanClassName = null;
        String sqlQuery = null;
        Filter filter = null;
        for(Object field : fields){
            if(field instanceof JAXBElement){
                field = ((JAXBElement)field).getValue();
            }
            if(field instanceof ContentType){
                final ContentType content = (ContentType) field;
                if(content.getOtherAttributes().get(ATT_KEY_EXTRA_DIMENSIONS)!=null){
                    if(content.getOtherAttributes().get(ATT_KEY_LOWER_EXTRA_DIMENSION)!=null
                        && content.getOtherAttributes().get(ATT_KEY_UPPER_EXTRA_DIMENSION)!=null){
                        datefilter = new FeatureMapLayer.DimensionDef(
                        CommonCRS.Temporal.JAVA.crs(), 
                        GO2Utilities.FILTER_FACTORY.property(content.getOtherAttributes().get(ATT_KEY_LOWER_EXTRA_DIMENSION)), 
                        GO2Utilities.FILTER_FACTORY.property(content.getOtherAttributes().get(ATT_KEY_UPPER_EXTRA_DIMENSION)));
                    }
                }
            }
            if(field instanceof ParameterType){
                final ParameterType param = (ParameterType) field;
                if(KEY_BEANCLASS.equals(param.getKey())){
                    beanClassName = param.getValue();
                }else if(KEY_SQLQUERY.equals(param.getKey())){
                    sqlQuery = param.getValue();
                }
            }
            if(field instanceof FilterType){
                try {
//                    final StyleXmlIO io = new StyleXmlIO();
                    filter = STYLE_XML_IO.getTransformer110().visitFilter((FilterType)field);
                } catch (FactoryException ex) {
                    Logger.getLogger(OwcExtentionSirs.class.getName()).log(Level.SEVERE, null, ex);
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
            if(filter==null){
                mapLayer = CorePlugin.createLayer(clazz);
            }
            else{
                mapLayer = CorePlugin.createLayer(clazz, QueryBuilder.filtered( new DefaultName(clazz.getSimpleName()), filter));
            }
        }else if(sqlQuery!=null){
            final Session session = Injector.getSession();
            try {
                final FeatureStore h2Store = H2Helper.getStore(session.getConnector());
                final Query fsquery = org.geotoolkit.data.query.QueryBuilder.language(
                    JDBCFeatureStore.CUSTOM_SQL, sqlQuery, new DefaultName("requete"));
                final FeatureCollection col = h2Store.createSession(false).getFeatureCollection(fsquery);
                mapLayer = MapBuilder.createFeatureLayer(col);
            } catch (SQLException ex) {
                throw new DataStoreException(ex.getMessage(),ex);
            }
        }else{
            throw new DataStoreException("Invalid configuration, missing bean class name or sql query");
        }
        if(datefilter!=null){
            ((FeatureMapLayer)mapLayer).getExtraDimensions().add(datefilter);
        }
        return mapLayer;
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
            offering.getOperationOrContentOrStyleSet().add(new ParameterType(KEY_SQLQUERY, String.class.getName(), sqlQuery));
        }else{
            final BeanFeatureSupplier supplier;
            try {
                supplier = ((BeanStore)store).getBeanSupplier(typeName);
            } catch (DataStoreException ex) {
                throw new IllegalStateException(ex.getMessage(),ex);
            }
            final Class beanClass = supplier.getBeanClass();
            //write the blean class name
            offering.getOperationOrContentOrStyleSet().add(new ParameterType(KEY_BEANCLASS, String.class.getName(), beanClass.getName()));
        }
        
        final Query query = fml.getQuery();
        if (query!=null){
            final Filter filter = query.getFilter();
            if(filter!=null){
                offering.getOperationOrContentOrStyleSet().add(toFilter(filter));
            }
        }
        
        final List<FeatureMapLayer.DimensionDef> extraDimensions = fml.getExtraDimensions();
        for(final FeatureMapLayer.DimensionDef extraDimension : extraDimensions){
            
            if(extraDimension.getLower()!=null || extraDimension.getUpper()!=null){
                final ContentType ct = ATOM_FACTORY.createContentType();
                ct.getContent();
                ct.getOtherAttributes().put(new QName(ATT_KEY_EXTRA_DIMENSIONS), ATT_KEY_EXTRA_DIMENSIONS);
                
                if(extraDimension.getLower() instanceof PropertyName)
                    ct.getOtherAttributes().put(new QName(ATT_KEY_LOWER_EXTRA_DIMENSION), ((PropertyName) extraDimension.getLower()).getPropertyName());
                
                if(extraDimension.getUpper() instanceof PropertyName)
                    ct.getOtherAttributes().put(new QName(ATT_KEY_UPPER_EXTRA_DIMENSION), ((PropertyName) extraDimension.getUpper()).getPropertyName());
                
                offering.getOperationOrContentOrStyleSet().add(ATOM_FACTORY.createEntryTypeContent(ct));
            }
        }
        
        return offering;
    }
    
    private static ParameterValueType toParameterValue(final Expression expression){
        final ParameterValueType visit = STYLE_XML_IO.getTransformerXMLv110().visitExpression(expression);
        return visit;
    }
    
    private static FilterType toFilter(final Filter filter){
        return STYLE_XML_IO.getTransformerXMLv110().visit(filter);
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
