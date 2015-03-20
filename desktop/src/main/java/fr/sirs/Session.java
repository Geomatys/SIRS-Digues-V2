package fr.sirs;

import fr.sirs.core.SirsCore;
import fr.sirs.core.component.AbstractSIRSRepository;
import org.geotoolkit.gui.javafx.util.TaskManager;
import fr.sirs.core.component.DigueRepository;

import java.util.List;

import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.sirs.core.component.SessionGen;
import fr.sirs.core.component.PreviewLabelRepository;
import fr.sirs.core.component.ReferenceUsageRepository;
import fr.sirs.core.component.SQLQueryRepository;
import fr.sirs.core.component.SystemeEndiguementRepository;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.component.ValiditySummaryRepository;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.SystemeEndiguement;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.core.model.Utilisateur;
import fr.sirs.core.model.AvecLibelle;
import fr.sirs.core.model.ReferenceType;
import fr.sirs.core.model.Role;
import fr.sirs.digue.DiguesTab;
import fr.sirs.theme.Theme;
import fr.sirs.theme.ui.FXTronconThemePane;
import fr.sirs.util.FXFreeTab;
import fr.sirs.util.SirsStringConverter;
import fr.sirs.util.property.Internal;
import fr.sirs.util.property.SirsPreferences;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.logging.Level;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.apache.sis.util.collection.Cache;
import org.apache.sis.util.iso.SimpleInternationalString;

import org.ektorp.CouchDbConnector;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.CoverageStore;
import org.geotoolkit.display2d.ext.DefaultBackgroundTemplate;
import org.geotoolkit.display2d.ext.legend.DefaultLegendTemplate;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.osmtms.OSMTileMapClient;
import org.geotoolkit.style.DefaultDescription;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * La session contient toutes les données chargées dans l'instance courante de 
 * l'application.
 * 
 * Notamment, elle doit réferencer l'ensemble des thèmes ouvert, ainsi que les 
 * onglets associés. De même pour les {@link Element}s et leurs éditeurs.
 * 
 * La session fournit également un point d'accès centralisé à tous les documents
 * de la base CouchDB.
 * 
 * @author Johann Sorel
 */
@Component
public class Session extends SessionGen {
    
    public static String FLAG_SIRSLAYER = "SirsLayer";
    
    ////////////////////////////////////////////////////////////////////////////
    // GESTION DES REFERENCES
    ////////////////////////////////////////////////////////////////////////////
    private final ReferenceChecker referenceChecker;
    public ReferenceChecker getReferenceChecker(){return referenceChecker;}
    
    ////////////////////////////////////////////////////////////////////////////
    // GESTION DES DROITS
    ////////////////////////////////////////////////////////////////////////////
    private final ObjectProperty<Utilisateur> utilisateurProperty = new SimpleObjectProperty<>(null);
    public ObjectProperty<Utilisateur> utilisateurProperty() {return utilisateurProperty;}
    public Utilisateur getUtilisateur() {return utilisateurProperty.get();}
    public void setUtilisateur(final Utilisateur utilisateur){
        utilisateurProperty.set(utilisateur);
        if(utilisateur!=null){
            role.set(utilisateur.getRole());
            needValidationProperty.set(false);
            geometryEditionProperty.set(false);
            nonGeometryEditionProperty.set(false);
            if(role.get()==Role.ADMIN || role.get()==Role.EXTERN){
                geometryEditionProperty.set(true);
                nonGeometryEditionProperty.set(true);
                if(role.get()==Role.EXTERN){
                    needValidationProperty.set(true);
                }
            }
            else if(role.get()==Role.USER){
                geometryEditionProperty.set(false);
                nonGeometryEditionProperty.set(true);
            }
            else if(role.get()==Role.GUEST){
                geometryEditionProperty.set(false);
                nonGeometryEditionProperty.set(false);
            }
        }else{
            this.role.set(null);
            geometryEditionProperty.set(false);
            nonGeometryEditionProperty.set(false);
        }
    }
    
    private final BooleanProperty geometryEditionProperty = new SimpleBooleanProperty(false);
    public BooleanProperty geometryEditionProperty() {return geometryEditionProperty;}
    private final BooleanProperty nonGeometryEditionProperty = new SimpleBooleanProperty(false);
    public BooleanProperty nonGeometryEditionProperty() {return nonGeometryEditionProperty;}
    private final BooleanProperty needValidationProperty = new SimpleBooleanProperty(true);
    public BooleanProperty needValidationProperty() {return needValidationProperty;}
    
    
    private final ObjectProperty<Role> role = new SimpleObjectProperty();
    public Role getRole(){return role.get();}
    ////////////////////////////////////////////////////////////////////////////
    
    private ClassPathXmlApplicationContext applicationContext;
    public ClassPathXmlApplicationContext getApplicationContext(){return applicationContext;}
    public void setApplicationContext(final ClassPathXmlApplicationContext applicationContext){
        this.applicationContext=applicationContext;
    }

    private static final Class[] SUPPORTED_TYPES = new Class[]{
        Boolean.class,
        String.class,
        Integer.class,
        Float.class,
        Double.class,
        boolean.class,
        int.class,
        float.class,
        double.class,
        LocalDateTime.class
    };
        
    private static final Map<String,Map<String,Integer>> SORTED_FIELDS = new HashMap<>();
    static {
        final Properties SORTED_PROPERTIES = new Properties();
        final Properties SORTED_OVERRIDES = new Properties();
//        SORTED_PROPERTIES.load(Session.class.getResourceAsStream("/fr/sirs/model/fields.properties"));
//        SORTED_OVERRIDES.load(Session.class.getResourceAsStream("/fr/sirs/model/fields.properties"));
        
        for(Entry entry : SORTED_PROPERTIES.entrySet()){
            final String name = (String) entry.getKey();
            final String fields = (String) entry.getValue();
            final String[] split = fields.split(",");
            final Map<String,Integer> s = new HashMap<>();
            for(int i=0;i<split.length;i++){
                s.put(split[i].trim().toLowerCase(), i);
            }
            SORTED_FIELDS.put(name.toLowerCase(), s);
        }
        
        for(Entry entry : SORTED_OVERRIDES.entrySet()){
            final String name = (String) entry.getKey();
            final String fields = (String) entry.getValue();
            final String[] split = fields.split(",");
            final Map<String,Integer> s = new HashMap<>();
            for(int i=0;i<split.length;i++){
                s.put(split[i].trim().toLowerCase(), i);
            }
            SORTED_FIELDS.put(name.toLowerCase(), s);
        }
    }
    
    private Object objectToPrint = null;
    
    private MapContext mapContext;
    private final MapItem sirsGroup = MapBuilder.createItem();
    private final MapItem backgroundGroup = MapBuilder.createItem();

    private final CouchDbConnector connector;
    
    ////////////////////////////////////////////////////////////////////////////
    // NON-GENERATED REPOSITORIES
    ////////////////////////////////////////////////////////////////////////////
    private final PreviewLabelRepository previewLabelRepository;
    private final ReferenceUsageRepository referenceUsageRepository;
    private final ValiditySummaryRepository validitySummaryRepository;
    private final SQLQueryRepository sqlQueryRepository;

    private FXMainFrame frame = null;
    
    private final Cache<Theme, FXFreeTab> openThemes = new Cache<>(12, 0, false);
    private final Cache<Element, FXFreeTab> openEditors = new Cache<>(12, 0, false);
    
    //generate a template for the legend
    final DefaultLegendTemplate legendTemplate = new DefaultLegendTemplate(
            new DefaultBackgroundTemplate( //legend background
                    new BasicStroke(2), //stroke
                    Color.BLUE, //stroke paint
                    Color.WHITE, // fill paint
                    new Insets(10, 10, 10, 10), //border margins
                    8 //round border
            ),
            2, //gap between legend elements
            null, //glyph size, we can let it to null for the legend to use the best size
            new Font("Serial", Font.PLAIN, 10), //Font used for style rules
            true, // show layer names
            new Font("Serial", Font.BOLD, 12), //Font used for layer names
            true // display only visible layers
    );
        
    /**
     * 
     * @return the application task manager, designed to start users tasks in a 
     * separate thread pool.
     */
    public TaskManager getTaskManager() {
        return SirsCore.getTaskManager();
    }
    
    /**
     * Clear session cache.
     */
    public void clearCache(){
        openEditors.clear();
        openThemes.clear();
    }
    
    @Autowired
    public Session(CouchDbConnector couchDbConnector) {
        super(couchDbConnector);
        this.connector = couchDbConnector;
        
        previewLabelRepository = new PreviewLabelRepository(connector);
        referenceUsageRepository = new ReferenceUsageRepository(connector);
        validitySummaryRepository = new ValiditySummaryRepository(connector);
        sqlQueryRepository = new SQLQueryRepository(connector);
        
        sirsGroup.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
        final String referenceUrl;
        if(SirsPreferences.INSTANCE.getPropertySafe(SirsPreferences.PROPERTIES.REFERENCE_URL)!=null){
            referenceUrl = SirsPreferences.INSTANCE.getPropertySafe(SirsPreferences.PROPERTIES.REFERENCE_URL);
        }
        else {
            referenceUrl = SirsPreferences.PROPERTIES.REFERENCE_URL.getDefaultValue();
        }
        referenceChecker = new ReferenceChecker(referenceUrl);
    }

    public CouchDbConnector getConnector() {
        return connector;
    }
    
    public PreviewLabelRepository getPreviewLabelRepository() {
        return previewLabelRepository;
    }    
    
    public ReferenceUsageRepository getReferenceUsageRepository(){
        return referenceUsageRepository;
    }
    
    public ValiditySummaryRepository getValiditySummaryRepository(){
        return validitySummaryRepository;
    }

    public SQLQueryRepository getSqlQueryRepository() {
        return sqlQueryRepository;
    }
    
    public Collection<AbstractSIRSRepository> getModelRepositories(){
        return repositories.values();
    }
    
    void setFrame(FXMainFrame frame) {
        this.frame = frame;
    }

    public FXMainFrame getFrame() {
        return frame;
    }
        
    // REFERENCES
    private static final List<Class> REFERENCES = new ArrayList<>();
    private static final List<Class> ELEMENTS = new ArrayList<>();
    private static void initReferences(){
        
        final ServiceLoader<ReferenceType> serviceLoader = ServiceLoader.load(ReferenceType.class);
        serviceLoader.forEach(new Consumer<ReferenceType>() {

            @Override
            public void accept(ReferenceType t) {
                REFERENCES.add(t.getClass());
            }
        });
    }
    private static void initElements(){
        
        final ServiceLoader<Element> serviceLoader = ServiceLoader.load(Element.class);
        serviceLoader.forEach(new Consumer<Element>() {

            @Override
            public void accept(Element t) {
                ELEMENTS.add(t.getClass());
            }
        });
    }
    
    static{
        initReferences();
        initElements();
    }
    
    public static List<Class> getReferences(){return REFERENCES;}
    public static List<Class> getElements(){return ELEMENTS;}

    /**
     * MapContext affiché pour toute l'application.
     *
     * @return MapContext
     */
    public synchronized MapContext getMapContext() {
        if(mapContext==null){
            mapContext = MapBuilder.createContext(SirsCore.getEpsgCode());
            mapContext.setName("Carte");

            try {
                //sirs layers
                sirsGroup.setName("Système de digue");
                mapContext.items().add(0,sirsGroup);

                for(Plugin plugin : Plugins.getPlugins()){
                    sirsGroup.items().addAll(plugin.getMapItems());
                }
                mapContext.setAreaOfInterest(mapContext.getBounds(true));

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
            try{
                //Fond de plan
                backgroundGroup.setName("Fond de plan");
                mapContext.items().add(0,backgroundGroup);
                final CoverageStore store = new OSMTileMapClient(new URL("http://tile.openstreetmap.org"), null, 18, true);

                for (Name n : store.getNames()) {
                    final CoverageReference cr = store.getCoverageReference(n);
                    final CoverageMapLayer cml = MapBuilder.createCoverageLayer(cr);
                    cml.setName("Open Street Map");
                    cml.setDescription(new DefaultDescription(
                            new SimpleInternationalString("Open Street Map"),
                            new SimpleInternationalString("Open Street Map")));
                    cml.setVisible(false);
                    backgroundGroup.items().add(cml);
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }
            
        }
        return mapContext;
    }

    public synchronized MapItem getSirsLayerGroup() {
        getMapContext();
        return sirsGroup;
    }

    public synchronized MapItem getBackgroundLayerGroup() {
        getMapContext();
        return backgroundGroup;
    }
    
    public List<Digue> getDigues() {
        return ((DigueRepository) repositories.get("DigueRepository")).getAll();
    }
    
    public Digue getDigueById(final String digueId){
        return ((DigueRepository) repositories.get("DigueRepository")).get(digueId);
    }
    
    public SystemeEndiguement getSystemeEndiguementById(final String systemeEndiguementId){
        return ((SystemeEndiguementRepository) repositories.get("SystemeEndiguementRepository")).get(systemeEndiguementId);
    }

    public List<TronconDigue> getTroncons() {
        return ((TronconDigueRepository) repositories.get("TronconDigueRepository")).getAll();
    }

    public List<TronconDigue> getTronconDigueByDigue(final Digue digue) {
        return ((TronconDigueRepository) repositories.get("TronconDigueRepository")).getByDigue(digue);
    }
    
    public void update(final Digue digue){
        digue.setDateMaj(LocalDateTime.now());
        this.repositories.get("DigueRepository").update(digue);
    }
    
    /**
     * Update a section of the database.
     * @param tronconDigue 
     */
    public void update(final TronconDigue tronconDigue){
        tronconDigue.setDateMaj(LocalDateTime.now());
        SIRS.LOGGER.log(Level.FINE, "enregistrement de "+tronconDigue+" : : "+tronconDigue.getDigueId());
        repositories.get("TronconDigueRepository").update(tronconDigue);
    }
    
    /**
     * Update a list of sections of the database.
     * @param troncons 
     */
    public void update(final List<TronconDigue> troncons){
        troncons.stream().forEach((troncon) -> {
            this.update(troncon);
        });
    }
    
    /**
     * Add a troncon to the database.
     * @param tronconDigue 
     */
    public void add(final TronconDigue tronconDigue){
        tronconDigue.setDateMaj(LocalDateTime.now());
        repositories.get("TronconDigueRepository").add(tronconDigue);
    }
    
    /**
     * Remove a section from the database.
     * @param tronconDigue 
     */
    public void delete(final TronconDigue tronconDigue){
        repositories.get("TronconDigueRepository").remove(tronconDigue);
    }

    public void prepareToPrint(final Object object){
        objectToPrint=object;
    }
    
    public Object getObjectToPrint(){return objectToPrint;}
    
    /**
     * Récupération des attributes simple pour affichage dans les tables.
     * 
     * @param clazz
     * @return liste des propriétés simples
     */
    public static List<PropertyDescriptor> listSimpleProperties(Class clazz) {
        final List<PropertyDescriptor> properties = new ArrayList<>();
        try {
            for (java.beans.PropertyDescriptor pd : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
                final Method m = pd.getReadMethod();
                
                if(m==null || m.getAnnotation(Internal.class)!=null) continue;
                
                final Class propClass = m.getReturnType();
                if(propClass.isEnum()){
                    properties.add(pd);
                }
                else{
                    for(Class c : SUPPORTED_TYPES){
                        if(c.isAssignableFrom(propClass)){
                            properties.add(pd);
                            break;
                        }
                    }
                }
            }
        } catch (IntrospectionException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        
        final Map<String,Integer> fields = SORTED_FIELDS.get(clazz.getSimpleName().toLowerCase());
        if(fields!=null){
            Collections.sort(properties, new Comparator<PropertyDescriptor>() {
                @Override
                public int compare(PropertyDescriptor o1, PropertyDescriptor o2) {
                    Integer idx1 = fields.get(o1.getName().toLowerCase());
                    if(idx1==null) idx1= Integer.MAX_VALUE;
                    Integer idx2 = fields.get(o2.getName().toLowerCase());
                    if(idx2==null) idx2= Integer.MAX_VALUE;
                    return idx1.compareTo(idx2);
                }
            });
        }
        
        return properties;
    }
    
    public FXFreeTab getOrCreateThemeTab(final Theme theme) {
        try {
            return openThemes.getOrCreate(theme, new Callable<FXFreeTab>() {
                @Override
                public FXFreeTab call() throws Exception {
                    final FXFreeTab tab = new FXFreeTab(theme.getName());
                    Parent parent = theme.createPane();
                    tab.setContent(parent);
                    if (parent instanceof FXTronconThemePane) {
                        ((FXTronconThemePane) parent).currentTronconProperty().addListener(new ChangeListener<TronconDigue>() {
                            @Override
                            public void changed(ObservableValue<? extends TronconDigue> observable, TronconDigue oldValue, TronconDigue newValue) {
                                tab.setTextAbrege(theme.getName() + " (" + getPreviewLabelRepository().getPreview(newValue.getId()) + ")");
                            }
                        });
                    }
                    return tab;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public FXFreeTab getOrCreateElementTab(final Element element) {
        if (element instanceof TronconDigue) {
            DiguesTab diguesTab = Injector.getSession().getFrame().getDiguesTab();
            diguesTab.getDiguesController().displayTronconDigue((TronconDigue) element);
            return diguesTab;
        } else {
            try {
                return openEditors.getOrCreate(element, new Callable<FXFreeTab>() {
                    @Override
                    public FXFreeTab call() throws Exception {
                        final FXFreeTab tab = new FXFreeTab();
                        Node content = (Node) SIRS.generateEditionPane(element);
                        if (content == null) {
                            content = new BorderPane(new Label("Pas d'éditeur pour le type : " + element.getClass().getSimpleName()));
                        }

                        tab.setContent(content);
                        element.designationProperty().addListener(new ChangeListener<String>() {

                            @Override
                            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                                tab.setTextAbrege(generateElementTitle(element));
                            }
                        });
                        tab.setTextAbrege(generateElementTitle(element));
                        tab.setOnSelectionChanged((Event event) -> {
                            if (tab.isSelected()) {
                                prepareToPrint(element);
                            }
                        });
                        return tab;
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    public String generateElementTitle(final Element element) {
        String title="";
        
        final String libelle = new SirsStringConverter().toString(element);
        if (libelle != null && !libelle.isEmpty()) {
            title += libelle;
        }
        
        final Element parent = element.getParent();
        if (parent instanceof AvecLibelle) {
            title+=" ("+((AvecLibelle)parent).getLibelle()+")";
        }
        return title;
    }
    
    // TODO : Implement
    public Element getCompleteElement(Element e) {
        if (e.getCouchDBDocument() != null) {
            return e;
        } else {
            String documentId = e.getDocumentId();
        }
        throw new IllegalArgumentException("Complete element cannot be rebuilt for : "+e);
    }
    
    public void focusOnMap(Element target) {
        if (target == null || frame == null || frame.getMapTab() == null || frame.getMapTab().getMap() == null) {
            return;
        }
        frame.getMapTab().getMap().focusOnElement(target);
    }
    
    public DefaultLegendTemplate getLegendTemplate() {
        return legendTemplate;
    }
}
