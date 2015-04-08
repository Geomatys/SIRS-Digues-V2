package fr.sirs.owc;

import fr.sirs.CorePlugin;
import fr.sirs.Injector;
import fr.sirs.Plugin;
import fr.sirs.Plugins;
import org.geotoolkit.owc.xml.OwcExtension;
import fr.sirs.Session;
import fr.sirs.core.SirsCore;
import fr.sirs.core.h2.H2Helper;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.storage.DataStoreException;
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
import org.geotoolkit.feature.type.DefaultName;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.owc.gtkext.ParameterType;
import static org.geotoolkit.owc.xml.OwcMarshallerPool.*;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.owc.xml.v10.OfferingType;
import org.geotoolkit.se.xml.v110.ParameterValueType;
import org.geotoolkit.sld.xml.StyleXmlIO;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.PropertyName;
import org.opengis.util.FactoryException;
import org.w3._2005.atom.ContentType;
import org.w3c.dom.Element;

/**
 * Extension OWC pour SIRS.
 * 
 * @author Samuel Andrés (Geomatys)
 * @author Johann Sorel (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class OwcExtensionSirs extends OwcExtension {
    
    public static final String CODE = "http://www.france-digues.fr/owc";
    public static final String KEY_LAYER_NAME = "beanClass";
    public static final String KEY_SQLQUERY = "sqlQuery";
    public static final String ATT_KEY_EXTRA_DIMENSIONS = "extraDimensions";
    public static final String ATT_KEY_LOWER_EXTRA_DIMENSION = "lower";
    public static final String ATT_KEY_UPPER_EXTRA_DIMENSION = "upper";
    
    private static final StyleXmlIO STYLE_XML_IO = new StyleXmlIO();
    
    public OwcExtensionSirs() {
        super(CODE, 10);
    }
    
    @Override
    public boolean canHandle(MapLayer layer) {
        return layer.getUserProperty(Plugin.PLUGIN_FLAG) != null || getSQLQuery(layer) != null;
    }
    
    @Override
    public MapLayer createLayer(OfferingType offering) throws DataStoreException {
        final MapLayer mapLayer;
        FeatureMapLayer.DimensionDef dateFilter = null;
        final List<Object> fields = offering.getOperationOrContentOrStyleSet();
        
        //rebuild parameters map
        String layerName = null;
        String pluginName = null;
        String sqlQuery = null;
        Filter filter = null;
        for(Object field : fields){
            if(field instanceof JAXBElement){
                field = ((JAXBElement)field).getValue();
            }
            if(field instanceof Element){
                final Element content = (Element) field;
                if(content.getAttribute(ATT_KEY_EXTRA_DIMENSIONS)!=null){
                    if(content.getAttribute(ATT_KEY_LOWER_EXTRA_DIMENSION)!=null
                        && content.getAttribute(ATT_KEY_UPPER_EXTRA_DIMENSION)!=null){
                        dateFilter = new FeatureMapLayer.DimensionDef(
                        CommonCRS.Temporal.JAVA.crs(), 
                        GO2Utilities.FILTER_FACTORY.property(content.getAttribute(ATT_KEY_LOWER_EXTRA_DIMENSION)), 
                        GO2Utilities.FILTER_FACTORY.property(content.getAttribute(ATT_KEY_UPPER_EXTRA_DIMENSION)));
                    }
                }
            }
            if (field instanceof ParameterType) {
                final ParameterType param = (ParameterType) field;
                if (Plugin.PLUGIN_FLAG.equals(param.getKey())) {
                    pluginName = param.getValue();
                } else if (KEY_LAYER_NAME.equals(param.getKey())) {
                    layerName = param.getValue();
                } else if (KEY_SQLQUERY.equals(param.getKey())) {
                    sqlQuery = param.getValue();
                }
            }
            if(field instanceof FilterType){
                try {
//                    final StyleXmlIO io = new StyleXmlIO();
                    filter = STYLE_XML_IO.getTransformer110().visitFilter((FilterType)field);
                } catch (FactoryException ex) {
                    SirsCore.LOGGER.log(Level.WARNING, null, ex);
                }
            }
        }
        
        if (layerName != null && pluginName != null) {
            final Plugin plugin = Plugins.getPlugin(pluginName);
            if (plugin == null) {
                throw new DataStoreException("No SIRS plugin found for name "+pluginName);
            }
            mapLayer = getLayerByName(plugin.getMapItems(), layerName);
            if (mapLayer == null) {
                throw new DataStoreException("No layer named "+layerName+" found in plugin "+pluginName);
            } else {
                mapLayer.getUserProperties().put(Plugin.PLUGIN_FLAG, pluginName);
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
            throw new DataStoreException("Invalid configuration, missing plugin source or sql query");
        }
        
        if (mapLayer instanceof FeatureMapLayer && (filter != null || dateFilter != null)) {
            final FeatureMapLayer featureMapLayer = (FeatureMapLayer) mapLayer;
            if (filter != null) {
                QueryBuilder queryBuilder = new QueryBuilder(featureMapLayer.getQuery());
                queryBuilder.setFilter(filter);
                featureMapLayer.setQuery(queryBuilder.buildQuery());
            }
            if (dateFilter != null) {
                featureMapLayer.getExtraDimensions().add(dateFilter);
            }
        }
        return mapLayer;
    }
    
    @Override
    public OfferingType createOffering(MapLayer mapLayer) {
        String sqlQuery = null, pluginName = null;
        final Object pluginProvider = mapLayer.getUserProperty(Plugin.PLUGIN_FLAG);
        if (pluginProvider instanceof String) {
            pluginName = (String) pluginProvider;
        } else {
            sqlQuery = getSQLQuery(mapLayer);
        }
        
        if (sqlQuery == null && pluginName == null) {
            throw new IllegalArgumentException("Provided layer is neither provided by a SIRS plugin, nor by an SQL query.");
        }
              
        final OfferingType offering = OWC_FACTORY.createOfferingType();
        offering.setCode(CODE);
        
        if (sqlQuery != null) {
            offering.getOperationOrContentOrStyleSet().add(new ParameterType(KEY_SQLQUERY, String.class.getName(), sqlQuery));
            
        } else {
            //write source plugin and layer names.
            offering.getOperationOrContentOrStyleSet().add(new ParameterType(Plugin.PLUGIN_FLAG, String.class.getName(), pluginName));
            offering.getOperationOrContentOrStyleSet().add(new ParameterType(KEY_LAYER_NAME, String.class.getName(), mapLayer.getName()));
        }

        if (mapLayer instanceof FeatureMapLayer) {
            final FeatureMapLayer fml = (FeatureMapLayer) mapLayer;
            final Query query = fml.getQuery();
            if (query != null) {
                final Filter filter = query.getFilter();
                if (filter != null) {
                    offering.getOperationOrContentOrStyleSet().add(toFilter(filter));
                }
            }

            // ADDITIONAL DIMENSIONS
            final List<FeatureMapLayer.DimensionDef> extraDimensions = fml.getExtraDimensions();
            for (final FeatureMapLayer.DimensionDef extraDimension : extraDimensions) {

                if (extraDimension.getLower() != null || extraDimension.getUpper() != null) {
                    final ContentType ct = ATOM_FACTORY.createContentType();
                    ct.getContent();
                    ct.getOtherAttributes().put(new QName(ATT_KEY_EXTRA_DIMENSIONS), ATT_KEY_EXTRA_DIMENSIONS);

                    if (extraDimension.getLower() instanceof PropertyName) {
                        ct.getOtherAttributes().put(new QName(ATT_KEY_LOWER_EXTRA_DIMENSION), ((PropertyName) extraDimension.getLower()).getPropertyName());
                    }

                    if (extraDimension.getUpper() instanceof PropertyName) {
                        ct.getOtherAttributes().put(new QName(ATT_KEY_UPPER_EXTRA_DIMENSION), ((PropertyName) extraDimension.getUpper()).getPropertyName());
                    }

                    offering.getOperationOrContentOrStyleSet().add(ATOM_FACTORY.createEntryTypeContent(ct));
                }
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
    
    /**
     * Find a map layer named as requested in input map item list.
     * @param items The item to search into. Their children will be searched recursively.
     * @param layerName Name of the layer to retrieve.
     * @return The first map layer found for given name, or null if we cannot find any.
     */
    private static MapLayer getLayerByName(final List<MapItem> items, final String layerName) {
        for (final MapItem item : items) {
            if (item instanceof MapLayer && layerName.equals(item.getName())) {
                return (MapLayer) item;
            } else if (!item.items().isEmpty()) {
                MapLayer layerByName = getLayerByName(item.items(), layerName);
                if (layerByName != null) return layerByName;
            }
        }
        return null;
    }
    
}
