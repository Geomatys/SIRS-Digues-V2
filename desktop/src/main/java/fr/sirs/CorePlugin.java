

package fr.sirs;

import com.vividsolutions.jts.geom.Geometry;
import java.io.IOException;
import java.sql.SQLException;
import org.geotoolkit.data.bean.BeanStore;

import fr.sirs.core.SirsCore;
import fr.sirs.core.component.DocumentChangeEmiter;
import fr.sirs.core.component.DocumentListener;
import fr.sirs.theme.ContactsTheme;
import fr.sirs.theme.DesordreTheme;
import fr.sirs.theme.DocumentsTheme;
import fr.sirs.theme.EvenementsHydrauliquesTheme;
import fr.sirs.theme.FrancBordTheme;
import fr.sirs.theme.MesureEvenementsTheme;
import fr.sirs.theme.PrestationsTheme;
import fr.sirs.theme.ProfilsEnTraversTheme;
import fr.sirs.theme.ReseauxDeVoirieTheme;
import fr.sirs.theme.ReseauxEtOuvragesTheme;
import fr.sirs.theme.StructuresTheme;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.CommuneTroncon;
import fr.sirs.core.model.Crete;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Deversoir;
import fr.sirs.core.model.DocumentTroncon;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Epi;
import fr.sirs.core.model.Fondation;
import fr.sirs.core.model.FrontFrancBord;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.LaisseCrue;
import fr.sirs.core.model.LigneEau;
import fr.sirs.core.model.MonteeEaux;
import fr.sirs.core.model.OuvertureBatardable;
import fr.sirs.core.model.OuvrageFranchissement;
import fr.sirs.core.model.OuvrageHydrauliqueAssocie;
import fr.sirs.core.model.OuvrageParticulier;
import fr.sirs.core.model.OuvrageRevanche;
import fr.sirs.core.model.OuvrageTelecomEnergie;
import fr.sirs.core.model.OuvrageVoirie;
import fr.sirs.core.model.PiedDigue;
import fr.sirs.core.model.PiedFrontFrancBord;
import fr.sirs.core.model.Prestation;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.ReseauHydroCielOuvert;
import fr.sirs.core.model.ReseauTelecomEnergie;
import fr.sirs.core.model.SommetRisberme;
import fr.sirs.core.model.StationPompage;
import fr.sirs.core.model.TalusDigue;
import fr.sirs.core.model.TalusRisberme;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.core.model.VoieAcces;
import fr.sirs.core.model.VoieDigue;
import fr.sirs.theme.DocumentTronconTheme;

import java.awt.Color;
import java.beans.PropertyDescriptor;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;

import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;

import javax.measure.unit.NonSI;

import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.ArraysExt;
import org.geotoolkit.cql.CQLException;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.bean.BeanFeatureSupplier;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.display2d.ext.graduation.GraduationSymbolizer;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.filter.DefaultLiteral;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.style.MutableFeatureTypeStyle;
import org.geotoolkit.style.MutableRule;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.MutableStyleFactory;
import org.geotoolkit.style.RandomStyleBuilder;
import org.geotoolkit.style.StyleConstants;

import static org.geotoolkit.style.StyleConstants.*;

import org.opengis.filter.FilterFactory2;
import org.opengis.filter.Id;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.identity.FeatureId;
import org.opengis.style.Fill;
import org.opengis.style.Graphic;
import org.opengis.style.GraphicStroke;
import org.opengis.style.GraphicalSymbol;
import org.opengis.style.LineSymbolizer;
import org.opengis.style.Mark;
import org.opengis.style.PointSymbolizer;
import org.opengis.style.Stroke;
import org.opengis.style.TextSymbolizer;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class CorePlugin extends Plugin {
    
    public static final String TRONCON_LAYER_NAME = "Tronçons";
    public static final String BORNE_LAYER_NAME = "Bornes";
    private static final FilterFactory2 FF = GO2Utilities.FILTER_FACTORY;
    private static final MutableStyleFactory SF = GO2Utilities.STYLE_FACTORY;
    
    /**
     * Plugin correspondant au desktop et au launcher.
     */
    public static final String NAME = "core";
    
    public static final Class[] VALID_CLASSES = new Class[]{
        byte.class,
        short.class,
        int.class,
        long.class,
        float.class,
        double.class,
        boolean.class,
        Boolean.class,
        Byte.class,
        Short.class,
        Integer.class,
        Long.class,
        Float.class,
        Double.class,
        String.class,
        LocalDateTime.class
    };
    
    public static final Predicate<PropertyDescriptor> MAPPROPERTY_PREDICATE = new Predicate<PropertyDescriptor>(){

        @Override
        public boolean test(PropertyDescriptor t) {
            final Class c = t.getReadMethod().getReturnType();
            return ArraysExt.contains(VALID_CLASSES, c) || Geometry.class.isAssignableFrom(c);
        }
        
    };
    
    private static final HashMap<Class, BeanFeatureSupplier> SUPPLIERS = new HashMap<Class, BeanFeatureSupplier>();
    
    public CorePlugin() {
        name = NAME;
    }

    private static synchronized HashMap<Class,BeanFeatureSupplier> getSuppliers() {
        if (SUPPLIERS.isEmpty()) {
            final TronconDigueRepository repository = Injector.getSession().getTronconDigueRepository();

            //troncons
            SUPPLIERS.put(TronconDigue.class, new StructBeanSupplier(TronconDigue.class,() -> repository::getAllLightIterator));

            //bornes
            SUPPLIERS.put(BorneDigue.class, new StructBeanSupplier(BorneDigue.class, () -> Injector.getSession().getBorneDigueRepository().getAll()));

            //structures
            SUPPLIERS.put(Crete.class, new StructBeanSupplier(Crete.class, () -> repository.getAllFromView(Crete.class)));
            SUPPLIERS.put(OuvrageRevanche.class, new StructBeanSupplier(OuvrageRevanche.class, () -> repository.getAllFromView(OuvrageRevanche.class)));
            SUPPLIERS.put(TalusDigue.class, new StructBeanSupplier(TalusDigue.class, () -> repository.getAllFromView(TalusDigue.class)));
            SUPPLIERS.put(SommetRisberme.class, new StructBeanSupplier(SommetRisberme.class, () -> repository.getAllFromView(SommetRisberme.class)));
            SUPPLIERS.put(TalusRisberme.class, new StructBeanSupplier(TalusRisberme.class, () -> repository.getAllFromView(TalusRisberme.class)));
            SUPPLIERS.put(PiedDigue.class, new StructBeanSupplier(PiedDigue.class, () -> repository.getAllFromView(PiedDigue.class)));
            SUPPLIERS.put(Fondation.class, new StructBeanSupplier(Fondation.class, () -> repository.getAllFromView(Fondation.class)));
            SUPPLIERS.put(Epi.class, new StructBeanSupplier(Epi.class, () -> repository.getAllFromView(Epi.class)));
            SUPPLIERS.put(Deversoir.class, new StructBeanSupplier(Deversoir.class, () -> repository.getAllFromView(Deversoir.class)));

            // Franc-bords
            SUPPLIERS.put(FrontFrancBord.class, new StructBeanSupplier(FrontFrancBord.class, () -> repository.getAllFromView(FrontFrancBord.class)));
            SUPPLIERS.put(PiedFrontFrancBord.class, new StructBeanSupplier(PiedFrontFrancBord.class, () -> repository.getAllFromView(PiedFrontFrancBord.class)));

            // Réseaux de voirie
            SUPPLIERS.put(VoieAcces.class, new StructBeanSupplier(VoieAcces.class, () -> repository.getAllFromView(VoieAcces.class)));
            SUPPLIERS.put(OuvrageFranchissement.class, new StructBeanSupplier(OuvrageFranchissement.class, () -> repository.getAllFromView(OuvrageFranchissement.class)));
            SUPPLIERS.put(OuvertureBatardable.class, new StructBeanSupplier(OuvertureBatardable.class, () -> repository.getAllFromView(OuvertureBatardable.class)));
            SUPPLIERS.put(VoieDigue.class, new StructBeanSupplier(VoieDigue.class, () -> repository.getAllFromView(VoieDigue.class)));
            SUPPLIERS.put(OuvrageVoirie.class, new StructBeanSupplier(OuvrageVoirie.class, () -> repository.getAllFromView(OuvrageVoirie.class)));

            // Réseaux et ouvrages
            SUPPLIERS.put(StationPompage.class, new StructBeanSupplier(StationPompage.class, () -> repository.getAllFromView(StationPompage.class)));
            SUPPLIERS.put(ReseauHydrauliqueFerme.class, new StructBeanSupplier(ReseauHydrauliqueFerme.class, () -> repository.getAllFromView(ReseauHydrauliqueFerme.class)));
            SUPPLIERS.put(OuvrageHydrauliqueAssocie.class, new StructBeanSupplier(OuvrageHydrauliqueAssocie.class, () -> repository.getAllFromView(OuvrageHydrauliqueAssocie.class)));
            SUPPLIERS.put(ReseauTelecomEnergie.class, new StructBeanSupplier(ReseauTelecomEnergie.class, () -> repository.getAllFromView(ReseauTelecomEnergie.class)));
            SUPPLIERS.put(OuvrageTelecomEnergie.class, new StructBeanSupplier(OuvrageTelecomEnergie.class, () -> repository.getAllFromView(OuvrageTelecomEnergie.class)));
            SUPPLIERS.put(ReseauHydroCielOuvert.class, new StructBeanSupplier(ReseauHydroCielOuvert.class, () -> repository.getAllFromView(ReseauHydroCielOuvert.class)));
            SUPPLIERS.put(OuvrageParticulier.class, new StructBeanSupplier(OuvrageParticulier.class, () -> repository.getAllFromView(OuvrageParticulier.class)));

            // Désordres
            SUPPLIERS.put(Desordre.class, new StructBeanSupplier(Desordre.class, () -> repository.getAllFromView(Desordre.class)));

            // Prestations
            SUPPLIERS.put(Prestation.class, new StructBeanSupplier(Prestation.class, () -> repository.getAllFromView(Prestation.class)));

            // Mesures d'évènements
            SUPPLIERS.put(LaisseCrue.class, new StructBeanSupplier(LaisseCrue.class, () -> repository.getAllFromView(LaisseCrue.class)));
            SUPPLIERS.put(MonteeEaux.class, new StructBeanSupplier(MonteeEaux.class, () -> repository.getAllFromView(MonteeEaux.class)));
            SUPPLIERS.put(LigneEau.class, new StructBeanSupplier(LigneEau.class, () -> repository.getAllFromView(LigneEau.class)));
            
            // Documents positionnés
            SUPPLIERS.put(DocumentTroncon.class, new StructBeanSupplier(DocumentTroncon.class, () -> repository.getAllDocumentTroncons()));

            // Emprises communales
            SUPPLIERS.put(CommuneTroncon.class, new StructBeanSupplier(CommuneTroncon.class, () -> repository.getAllFromView(CommuneTroncon.class)));
        }
        return SUPPLIERS;
    }
    
    private static synchronized BeanFeatureSupplier getSupplier(Class clazz){
        return getSuppliers().get(clazz);
    }
    
    @Override
    public List<MapItem> getMapItems() {
        final List<MapItem> items = new ArrayList<>();
                
        try{
            final Map<String,String> nameMap = new HashMap<>();
            for(Class elementClass : getSuppliers().keySet()){
                final LabelMapper mapper = new LabelMapper(elementClass);
                nameMap.put(elementClass.getSimpleName(), mapper.mapClassName());
            }
            
            final Color[] colors = new Color[]{
                Color.BLACK,
                Color.BLUE,
                Color.CYAN,
                Color.RED,
                Color.DARK_GRAY,
                Color.GREEN,
                Color.MAGENTA,
                Color.ORANGE,
                Color.PINK,
                Color.RED
            };
            
            //troncons
            final BeanStore tronconStore = new BeanStore(getSupplier(TronconDigue.class));
            items.addAll(buildLayers(tronconStore,TRONCON_LAYER_NAME,createTronconStyle(),createTronconSelectionStyle(false),true));
            
            //bornes
            final BeanStore borneStore = new BeanStore(getSupplier(BorneDigue.class));
            items.addAll(buildLayers(borneStore,BORNE_LAYER_NAME,createBorneStyle(),createBorneSelectionStyle(),true));
            
            //structures
            final BeanStore structStore = new BeanStore(
                    getSupplier(Crete.class),
                    getSupplier(OuvrageRevanche.class),
                    getSupplier(TalusDigue.class),
                    getSupplier(SommetRisberme.class),
                    getSupplier(TalusRisberme.class),
                    getSupplier(PiedDigue.class),
                    getSupplier(Epi.class),
                    getSupplier(Deversoir.class),
                    getSupplier(Fondation.class));
            final MapItem structLayer = MapBuilder.createItem();
            structLayer.setName("Structures");
            structLayer.items().addAll( buildLayers(structStore, nameMap, colors, createStructureSelectionStyle(),false));
            structLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            items.add(structLayer);
            
            // Franc-bords
            final BeanStore fbStore = new BeanStore(
                    getSupplier(FrontFrancBord.class),
                    getSupplier(PiedFrontFrancBord.class));
            final MapItem fbLayer = MapBuilder.createItem();
            fbLayer.setName("Francs-bords");
            fbLayer.items().addAll( buildLayers(fbStore, nameMap, colors, createStructureSelectionStyle(),false) );
            fbLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            items.add(fbLayer);
            
            // Réseaux de voirie
            final BeanStore rvStore = new BeanStore(
                    getSupplier(VoieAcces.class),
                    getSupplier(OuvrageFranchissement.class),
                    getSupplier(OuvertureBatardable.class),
                    getSupplier(VoieDigue.class),
                    getSupplier(OuvrageVoirie.class));
            final MapItem rvLayer = MapBuilder.createItem();
            rvLayer.setName("Réseaux de voirie");
            rvLayer.items().addAll( buildLayers(rvStore, nameMap, colors, createStructureSelectionStyle(),false) );
            rvLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            items.add(rvLayer);
            
            // Réseaux et ouvrages
            final BeanStore roStore = new BeanStore(
                    getSupplier(StationPompage.class),
                    getSupplier(ReseauHydrauliqueFerme.class),
                    getSupplier(OuvrageHydrauliqueAssocie.class),
                    getSupplier(ReseauTelecomEnergie.class),
                    getSupplier(OuvrageTelecomEnergie.class),
                    getSupplier(ReseauHydroCielOuvert.class),
                    getSupplier(OuvrageParticulier.class));  
            final MapItem roLayer = MapBuilder.createItem();
            roLayer.setName("Réseaux et ouvrages");
            roLayer.items().addAll( buildLayers(roStore, nameMap, colors, createStructureSelectionStyle(),false) );
            roLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            items.add(roLayer);          
            
            // Désordres
            final BeanStore desordreStore = new BeanStore(getSupplier(Desordre.class));
            final MapItem desordresLayer = MapBuilder.createItem();
            desordresLayer.setName("Désordres");
            desordresLayer.items().addAll( buildLayers(desordreStore, nameMap, colors, createStructureSelectionStyle(),false) );
            desordresLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            items.add(desordresLayer);
            
            // Prestations
            final BeanStore prestaStore = new BeanStore(getSupplier(Prestation.class));
            final MapItem prestaLayer = MapBuilder.createItem();
            prestaLayer.setName("Prestations");
            prestaLayer.items().addAll( buildLayers(prestaStore, nameMap, colors, createStructureSelectionStyle(),false) );
            prestaLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            items.add(prestaLayer);
                        
            // Mesures d'évènements
            final BeanStore mesuresStore = new BeanStore(
                    getSupplier(LaisseCrue.class),
                    getSupplier(MonteeEaux.class),
                    getSupplier(LigneEau.class));
            final MapItem mesuresLayer = MapBuilder.createItem();
            mesuresLayer.setName("Mesures d'évènements");
            mesuresLayer.items().addAll( buildLayers(mesuresStore, nameMap, colors, createStructureSelectionStyle(),false) );
            mesuresLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            items.add(mesuresLayer);
                        
            // Positionnement des documents
            final BeanStore documentsStore = new BeanStore(getSupplier(DocumentTroncon.class));
            final MapItem documentsLayer = MapBuilder.createItem();
            documentsLayer.setName("Documents");
            documentsLayer.items().addAll( buildLayers(documentsStore, nameMap, colors, createStructureSelectionStyle(),false) );
            documentsLayer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            items.add(documentsLayer);
            
            // Emprises communales
            //final BeanStore communesStore = new BeanStore(getSupplier(CommuneTroncon.class));               
            
        }catch(Exception ex){
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
        
        return items;
    }

    /**
     * A data supplier for {@link BeanStore}. It listens on application {@link DocumentChangeEmiter} to be notified when data is updated.
     * 
     * TODO : Optimization : the change emitter fire too many events, which will cause CPU overload at update.
     */
    private static class StructBeanSupplier extends BeanFeatureSupplier implements DocumentListener {

        public StructBeanSupplier(Class clazz, final Supplier<Iterable> callable) {
            super(clazz, "id", "geometry", 
                (PropertyDescriptor t) -> MAPPROPERTY_PREDICATE.test(t),
                null, SirsCore.getEpsgCode(), callable::get);
            Injector.getDocumentChangeEmiter().addListener(this);
        }

        @Override
        public void documentCreated(Map<Class, List<Element>> added) {
            if (added == null) return;
            final Id filter = getIdFilter(added);
            if (filter != null) {
                fireFeaturesAdded(filter);
            }
        }

        @Override
        public void documentChanged(Map<Class, List<Element>> changed) {
            if (changed == null) return;
            final Id filter = getIdFilter(changed);
            if (filter != null) {
                fireFeaturesUpdated(filter);
            }
        }

        @Override
        public void documentDeleted(Map<Class, List<Element>> deleteObject) {
            if (deleteObject == null) return;
            final Id filter = getIdFilter(deleteObject);
            if (filter != null) {
                fireFeaturesDeleted(filter);
            }
        }
        
        private final Id getIdFilter(final Map<Class, List<Element>> elementMap) {
            final List<Element> elements = elementMap.get(getBeanClass());
            if (elements == null || elements.isEmpty()) return null;
            final HashSet<FeatureId> fIds = new HashSet<>();
            for (Element e : elements) {
                fIds.add(FF.featureId(e.getId()));
            }
            return FF.id(fIds);
        }
    }
    
    private List<MapLayer> buildLayers(FeatureStore store, String layerName, MutableStyle baseStyle, MutableStyle selectionStyle, boolean visible) throws DataStoreException{
        final List<MapLayer> layers = new ArrayList<>();
        final org.geotoolkit.data.session.Session symSession = store.createSession(false);
        for(Name name : store.getNames()){
            final FeatureCollection col = symSession.getFeatureCollection(QueryBuilder.all(name));
            final MutableStyle style = (baseStyle==null) ? RandomStyleBuilder.createRandomVectorStyle(col.getFeatureType()) : baseStyle;
            final FeatureMapLayer fml = MapBuilder.createFeatureLayer(col, style);
            
            if(col.getFeatureType().getDescriptor("date_debut")!=null && col.getFeatureType().getDescriptor("date_fin")!=null){
                final FeatureMapLayer.DimensionDef datefilter = new FeatureMapLayer.DimensionDef(
                        CommonCRS.Temporal.JAVA.crs(), 
                        GO2Utilities.FILTER_FACTORY.property("date_debut"), 
                        GO2Utilities.FILTER_FACTORY.property("date_fin")
                );
                fml.getExtraDimensions().add(datefilter);
            }
            fml.setVisible(visible);
            fml.setName(layerName);
            fml.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            
            if(selectionStyle!=null) fml.setSelectionStyle(selectionStyle);
            
            layers.add(fml);
        }
        return layers;
    }
    
    private List<MapLayer> buildLayers(BeanStore store, Map<String,String> nameMap, Color[] colors, MutableStyle selectionStyle, boolean visible) throws DataStoreException{
        final List<MapLayer> layers = new ArrayList<>();
        final org.geotoolkit.data.session.Session symSession = store.createSession(false);
        int i=0;
        for(Name name : store.getNames()){
            final FeatureCollection col = symSession.getFeatureCollection(QueryBuilder.all(name));
            final int d = (int)((i%colors.length)*1.5);
            final MutableStyle baseStyle = createStructureStyle(colors[i%colors.length]);
            final MutableStyle style = (baseStyle==null) ? RandomStyleBuilder.createRandomVectorStyle(col.getFeatureType()) : baseStyle;
            final FeatureMapLayer fml = MapBuilder.createFeatureLayer(col, style);
            
            if(col.getFeatureType().getDescriptor("date_debut")!=null && col.getFeatureType().getDescriptor("date_fin")!=null){
                final FeatureMapLayer.DimensionDef datefilter = new FeatureMapLayer.DimensionDef(
                        CommonCRS.Temporal.JAVA.crs(), 
                        GO2Utilities.FILTER_FACTORY.property("date_debut"), 
                        GO2Utilities.FILTER_FACTORY.property("date_fin")
                );
                fml.getExtraDimensions().add(datefilter);
            }
            fml.setVisible(visible);
            fml.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
            
            final String str = nameMap.get(name.getLocalPart());
            fml.setName(str!=null ? str : name.getLocalPart());
            
            if(selectionStyle!=null) fml.setSelectionStyle(selectionStyle);
            
            layers.add(fml);
            i++;
        }
        return layers;
    }

    @Override
    public List<MenuItem> getMapActions(Object obj) {
        final List<MenuItem> lst = new ArrayList<>();
        
        if(obj instanceof Element) {
            lst.add(new ViewFormItem((Element)obj));
        }
        
        return lst;
    }
    
    @Override
    public void load() throws SQLException, IOException {
        themes.add(new StructuresTheme());
        themes.add(new FrancBordTheme());
        themes.add(new ReseauxDeVoirieTheme());
        themes.add(new ReseauxEtOuvragesTheme());
        themes.add(new DesordreTheme());
        themes.add(new PrestationsTheme());
        themes.add(new MesureEvenementsTheme());
        themes.add(new DocumentTronconTheme());
        themes.add(new ProfilsEnTraversTheme());
        themes.add(new ContactsTheme());
        themes.add(new EvenementsHydrauliquesTheme());
        themes.add(new DocumentsTheme());
        
    }
    
    public static MapLayer createLayer(final Class beanClass, final Query query){
        final BeanFeatureSupplier supplier = getSupplier(beanClass);
        final BeanStore store = new BeanStore(supplier);
        
        final FeatureMapLayer layer = MapBuilder.createFeatureLayer(store.createSession(true)
                .getFeatureCollection(query));
        layer.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
        layer.setSelectable(true);
        return layer;
    }

    public static MapLayer createLayer(Class beanClass) throws DataStoreException {
        final BeanFeatureSupplier supplier = getSupplier(beanClass);
        final BeanStore store = new BeanStore(supplier);
        return createLayer(beanClass, QueryBuilder.all(store.getNames().iterator().next()));
    }
    
    private static MutableStyle createTronconStyle() throws CQLException, URISyntaxException{
        final Stroke stroke1 = SF.stroke(SF.literal(Color.BLACK),LITERAL_ONE_FLOAT,FF.literal(9),
                STROKE_JOIN_BEVEL, STROKE_CAP_SQUARE, null,LITERAL_ZERO_FLOAT);
        final LineSymbolizer line1 = SF.lineSymbolizer("symbol",
                (String)null,DEFAULT_DESCRIPTION,NonSI.PIXEL,stroke1,LITERAL_ONE_FLOAT);
        
        final Stroke stroke2 = SF.stroke(SF.literal(new Color(0.9f, 0.9f,0.9f)),LITERAL_ONE_FLOAT,FF.literal(7),
                STROKE_JOIN_BEVEL, STROKE_CAP_SQUARE, null,LITERAL_ZERO_FLOAT);
        final LineSymbolizer line2 = SF.lineSymbolizer("symbol",
                (String)null,DEFAULT_DESCRIPTION,NonSI.PIXEL,stroke2,LITERAL_ONE_FLOAT);
        
        final Stroke stroke3 = SF.stroke(SF.literal(Color.BLACK),LITERAL_ONE_FLOAT,FF.literal(1),
                STROKE_JOIN_BEVEL, STROKE_CAP_SQUARE, null,LITERAL_ZERO_FLOAT);
        final LineSymbolizer line3 = SF.lineSymbolizer("symbol",
                (String)null,DEFAULT_DESCRIPTION,NonSI.PIXEL,stroke3,LITERAL_ONE_FLOAT);
        
//        final Expression size = GO2Utilities.FILTER_FACTORY.literal(18);
//        final List<GraphicalSymbol> symbols = new ArrayList<>();
//        final GraphicalSymbol external = SF.externalGraphic(
//                    SF.onlineResource(CorePlugin.class.getResource("/fr/sirs/arrow-white.png").toURI()),
//                    "image/png",null);
//        symbols.add(external);        
//        final Graphic graphic = SF.graphic(symbols, LITERAL_ONE_FLOAT, 
//                size, DEFAULT_GRAPHIC_ROTATION, DEFAULT_ANCHOR_POINT, DEFAULT_DISPLACEMENT);
//
//        final Expression initialGap = FF.literal(10);
//        final Expression strokeGap = FF.literal(100);
//        final GraphicStroke graphicStroke = SF.graphicStroke(graphic,strokeGap,initialGap);
//        
//        final Stroke gstroke = SF.stroke(graphicStroke,DEFAULT_FILL_COLOR,LITERAL_ONE_FLOAT,LITERAL_ONE_FLOAT,
//                STROKE_JOIN_BEVEL,STROKE_CAP_ROUND,null,LITERAL_ZERO_FLOAT);
//        final LineSymbolizer direction = SF.lineSymbolizer("",(Expression)null,null,null,gstroke,null);
        
        return SF.style(line1,line2,line3);
    }
    
    public static MutableStyle createTronconSelectionStyle(boolean graduation) throws URISyntaxException{
        final Stroke stroke1 = SF.stroke(SF.literal(Color.GREEN),LITERAL_ONE_FLOAT,FF.literal(7),
                STROKE_JOIN_BEVEL, STROKE_CAP_BUTT, null,LITERAL_ZERO_FLOAT);
        final LineSymbolizer line1 = SF.lineSymbolizer("symbol",
                (String)null,DEFAULT_DESCRIPTION,NonSI.PIXEL,stroke1,LITERAL_ONE_FLOAT);
                
        
        final Expression size = GO2Utilities.FILTER_FACTORY.literal(18);
        final List<GraphicalSymbol> symbols = new ArrayList<>();
        final GraphicalSymbol external = SF.externalGraphic(
                    SF.onlineResource(CorePlugin.class.getResource("/fr/sirs/arrow-green.png").toURI()),
                    "image/png",null);
        symbols.add(external);        
        final Graphic graphic = SF.graphic(symbols, LITERAL_ONE_FLOAT, 
                size, DEFAULT_GRAPHIC_ROTATION, DEFAULT_ANCHOR_POINT, DEFAULT_DISPLACEMENT);

        final Expression initialGap = FF.literal(10);
        final Expression strokeGap = FF.literal(200);
        final GraphicStroke graphicStroke = SF.graphicStroke(graphic,strokeGap,initialGap);
        
        final Stroke gstroke = SF.stroke(graphicStroke,DEFAULT_FILL_COLOR,LITERAL_ONE_FLOAT,LITERAL_ONE_FLOAT,
                STROKE_JOIN_BEVEL,STROKE_CAP_ROUND,null,LITERAL_ZERO_FLOAT);
        final LineSymbolizer direction = SF.lineSymbolizer("",(Expression)null,null,null,gstroke,null);
        
        if(graduation){
            final GraduationSymbolizer bigGrad = new GraduationSymbolizer();
            //tous les 100metres
            final GraduationSymbolizer.Graduation g1 = new GraduationSymbolizer.Graduation();
            g1.setUnit(new DefaultLiteral("m"));
            g1.setStep(FF.literal(100));
            g1.setStroke(SF.stroke(Color.RED, 3));
            g1.setFont(SF.font(12));
            g1.setSize(FF.literal(12));
            bigGrad.getGraduations().add(g1);
            //tous les 10metres
            final GraduationSymbolizer littleGrad = new GraduationSymbolizer();
            final GraduationSymbolizer.Graduation g2 = new GraduationSymbolizer.Graduation();
            g2.setUnit(new DefaultLiteral("m"));
            g2.setStep(FF.literal(10));
            g2.setStroke(SF.stroke(Color.BLACK, 1));
            g2.setFont(SF.font(10));
            g2.setSize(FF.literal(4));
            littleGrad.getGraduations().add(g2);
            
            final MutableRule ruleClose = SF.rule(littleGrad);
            ruleClose.setMaxScaleDenominator(3000);
        
            final MutableRule ruleDistant = SF.rule(bigGrad);
            ruleDistant.setMinScaleDenominator(3000);
            
            MutableRule others = SF.rule(line1, direction);
            
            MutableFeatureTypeStyle ftStyle = SF.featureTypeStyle();
            ftStyle.rules().add(ruleClose);
            ftStyle.rules().add(ruleDistant);
            ftStyle.rules().add(others);            
            
            MutableStyle style = SF.style();
            style.featureTypeStyles().add(ftStyle);
            
            return style;
        }else{
            return SF.style(line1,direction);
        }
    }
    
    private static MutableStyle createBorneStyle() throws URISyntaxException{
        final Expression size = GO2Utilities.FILTER_FACTORY.literal(10);

        final List<GraphicalSymbol> symbols = new ArrayList<>();
        final Stroke stroke = SF.stroke(SF.literal(Color.DARK_GRAY),LITERAL_ONE_FLOAT,LITERAL_ONE_FLOAT,
                STROKE_JOIN_BEVEL, STROKE_CAP_BUTT, null,LITERAL_ZERO_FLOAT);
        final Fill fill = SF.fill(Color.LIGHT_GRAY);
        
        //final Mark mark = SF.mark(StyleConstants.MARK_CIRCLE, fill, stroke);
        final Expression external = FF.literal("ttf:Dialog?char=0x2A");
//        final ExternalMark external = SF.externalMark(
//                    SF.onlineResource(IconBuilder.FONTAWESOME.toURI()),
//                    "ttf",FontAwesomeIcons.ICON_ASTERISK.codePointAt(0));
        final Mark mark = SF.mark(external, fill, stroke);
        symbols.add(mark);
        final Graphic graphic = SF.graphic(symbols, LITERAL_ONE_FLOAT, 
                size, LITERAL_ONE_FLOAT, DEFAULT_ANCHOR_POINT, DEFAULT_DISPLACEMENT);

        final PointSymbolizer pointSymbolizer = SF.pointSymbolizer("symbol",(String)null,DEFAULT_DESCRIPTION,NonSI.PIXEL,graphic);
        
        final TextSymbolizer ts = SF.textSymbolizer(
                SF.fill(Color.BLACK), DEFAULT_FONT, 
                SF.halo(Color.WHITE, 2), 
                FF.property("libelle"), 
                SF.pointPlacement(SF.anchorPoint(0, 0.25), SF.displacement(5, 0), FF.literal(0)), null);
        
        final MutableRule ruleClose = SF.rule(pointSymbolizer, ts);
        ruleClose.setMaxScaleDenominator(70000);
        
        final MutableFeatureTypeStyle fts = SF.featureTypeStyle();
        fts.rules().add(ruleClose);
        final MutableStyle style = SF.style();
        style.featureTypeStyles().add(fts);
        return style;
    }
    
    private static MutableStyle createBorneSelectionStyle(){
        final Expression size = GO2Utilities.FILTER_FACTORY.literal(10);

        final List<GraphicalSymbol> symbols = new ArrayList<>();
        final Stroke stroke = SF.stroke(Color.BLACK, 1);
        final Fill fill = SF.fill(Color.GREEN);
        final Mark mark = SF.mark(StyleConstants.MARK_CIRCLE, fill, stroke);
        symbols.add(mark);
        final Graphic graphic = SF.graphic(symbols, LITERAL_ONE_FLOAT, 
                size, LITERAL_ONE_FLOAT, DEFAULT_ANCHOR_POINT, DEFAULT_DISPLACEMENT);

        final PointSymbolizer pointSymbolizer = SF.pointSymbolizer("symbol",(String)null,DEFAULT_DESCRIPTION,NonSI.PIXEL,graphic);
        
        final TextSymbolizer ts = SF.textSymbolizer(
                SF.fill(Color.BLACK), SF.font(13), 
                SF.halo(Color.GREEN, 2), 
                FF.property("libelle"), 
                SF.pointPlacement(SF.anchorPoint(0, 0.25), SF.displacement(5, 0), FF.literal(0)), null);
        
        final MutableRule ruleClose = SF.rule(pointSymbolizer, ts);
        ruleClose.setMaxScaleDenominator(50000);
        
        final MutableFeatureTypeStyle fts = SF.featureTypeStyle();
        fts.rules().add(ruleClose);
        final MutableStyle style = SF.style();
        style.featureTypeStyles().add(fts);
        return style;
    }
    
    private static MutableStyle createStructureSelectionStyle(){
        // Stroke to use for lines and point perimeter
        final Stroke stroke = SF.stroke(SF.literal(Color.GREEN),LITERAL_ONE_FLOAT,FF.literal(7),
                STROKE_JOIN_BEVEL, STROKE_CAP_BUTT, null,LITERAL_ZERO_FLOAT);
        final LineSymbolizer line1 = SF.lineSymbolizer("symbol",
                (String)null,DEFAULT_DESCRIPTION,NonSI.PIXEL,stroke,LITERAL_ONE_FLOAT);
                
        // Definition of point symbolizer
        final Expression size = GO2Utilities.FILTER_FACTORY.literal(24);
        final List<GraphicalSymbol> symbols = new ArrayList<>();
        final Fill fill = SF.fill(new Color(0, 0, 0, 0));
        final Mark mark = SF.mark(StyleConstants.MARK_CIRCLE, fill, stroke);
        symbols.add(mark);
        final Graphic graphic = SF.graphic(symbols, LITERAL_ONE_FLOAT, 
                size, LITERAL_ONE_FLOAT, DEFAULT_ANCHOR_POINT, DEFAULT_DISPLACEMENT);

        final PointSymbolizer pointSymbolizer = SF.pointSymbolizer("symbol",(String)null,DEFAULT_DESCRIPTION,NonSI.PIXEL,graphic);
        
        final MutableRule ruleLongObjects = SF.rule(line1);
        ruleLongObjects.setFilter(
                FF.greater(
                        FF.function("length", FF.property("geometry")),
                        FF.literal(2.0)
                )
        );
        
        final MutableRule ruleSmallObjects = SF.rule(pointSymbolizer);
        ruleSmallObjects.setFilter(
                FF.less(
                        FF.function("length", FF.property("geometry")),
                        FF.literal(2.0)
                )
        );
        
        final MutableFeatureTypeStyle fts = SF.featureTypeStyle();
        fts.rules().add(ruleLongObjects);
        fts.rules().add(ruleSmallObjects);
        
        final MutableStyle style = SF.style();
        style.featureTypeStyles().add(fts);
        return style;
    }
    
    private static MutableStyle createStructureStyle(Color col){
        final Stroke line1Stroke = SF.stroke(SF.literal(col),LITERAL_ONE_FLOAT,GO2Utilities.FILTER_FACTORY.literal(8),
                STROKE_JOIN_BEVEL, STROKE_CAP_ROUND, null,LITERAL_ZERO_FLOAT);
        final LineSymbolizer line1 = SF.lineSymbolizer("symbol",
                (String)null,DEFAULT_DESCRIPTION,NonSI.PIXEL,line1Stroke,LITERAL_ZERO_FLOAT);
        
        
        final Stroke line2Stroke = SF.stroke(SF.literal(Color.BLACK),LITERAL_ONE_FLOAT,GO2Utilities.FILTER_FACTORY.literal(1),
                STROKE_JOIN_BEVEL, STROKE_CAP_ROUND, null,LITERAL_ZERO_FLOAT);
        final LineSymbolizer line2 = SF.lineSymbolizer("symbol",
                (String)null,DEFAULT_DESCRIPTION,NonSI.PIXEL,line2Stroke,LITERAL_ZERO_FLOAT);
        
        //the visual element
        final Expression size = GO2Utilities.FILTER_FACTORY.literal(16);

        final List<GraphicalSymbol> symbols = new ArrayList<>();
        final Stroke stroke = null;
        final Fill fill = SF.fill(col);
        final Mark mark = SF.mark(StyleConstants.MARK_TRIANGLE, fill, stroke);
        symbols.add(mark);
        final Graphic graphic = SF.graphic(symbols, LITERAL_ONE_FLOAT, 
                size, LITERAL_ONE_FLOAT, DEFAULT_ANCHOR_POINT, DEFAULT_DISPLACEMENT);

        final PointSymbolizer pointSymbolizer = SF.pointSymbolizer("symbol",(String)null,DEFAULT_DESCRIPTION,NonSI.PIXEL,graphic);
        
        final MutableRule ruleLongObjects = SF.rule(line1,line2);
        ruleLongObjects.setFilter(
                FF.greater(
                        FF.function("length", FF.property("geometry")),
                        FF.literal(2.0)
                )
        );
        
        final MutableRule ruleSmallObjects = SF.rule(pointSymbolizer);
        ruleSmallObjects.setFilter(
                FF.less(
                        FF.function("length", FF.property("geometry")),
                        FF.literal(2.0)
                )
        );
        
        final MutableFeatureTypeStyle fts = SF.featureTypeStyle();
        fts.rules().add(ruleLongObjects);
        fts.rules().add(ruleSmallObjects);
        
        final MutableStyle style = SF.style();
        style.featureTypeStyles().add(fts);
        return style;
    }
    
    private static class ViewFormItem extends MenuItem {

        public ViewFormItem(Element candidate) {
            setText(Injector.getSession().generateElementTitle(candidate));

            setOnAction((ActionEvent event) -> {
                Injector.getSession().getFrame().addTab(
                        Injector.getSession().getOrCreateElementTab(candidate));
            });
        }
    }
    
}
