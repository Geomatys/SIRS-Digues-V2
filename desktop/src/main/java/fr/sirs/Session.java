package fr.sirs;

import fr.sirs.core.SirsCore;

import java.util.List;

import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.sirs.core.component.SessionGen;
import fr.sirs.core.component.PreviewLabelRepository;
import fr.sirs.core.component.RefSystemeReleveProfilRepository;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.core.model.Utilisateur;
import fr.sirs.util.property.Internal;
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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.sis.util.iso.SimpleInternationalString;

import org.ektorp.CouchDbConnector;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.CoverageStore;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.osmtms.OSMTileMapClient;
import org.geotoolkit.style.DefaultDescription;

/**
 *
 * @author Johann Sorel
 */
@Component
public class Session extends SessionGen {
    
    public static String FLAG_SIRSLAYER = "SirsLayer";
    
    ////////////////////////////////////////////////////////////////////////////
    // GESTION DES DROITS
    ////////////////////////////////////////////////////////////////////////////
    private Utilisateur utilisateur = null;
    public Utilisateur getUtilisateur() {return utilisateur;}
    public void setUtilisateur(final Utilisateur utilisateur){
        this.utilisateur = utilisateur;
        if(utilisateur!=null){
            this.role.set(Role.valueOf(utilisateur.getRole()));
            needValidationProperty.set(false);
            geometryEditionProperty.set(false);
            nonGeometryEditionProperty.set(false);
            if(role.get()==Role.ADMIN || role.get()==Role.EXTERNE){
                geometryEditionProperty.set(true);
                nonGeometryEditionProperty.set(true);
                if(role.get()==Role.EXTERNE){
                    needValidationProperty.set(true);
                }
            }
            else if(role.get()==Role.USER){
                geometryEditionProperty.set(false);
                nonGeometryEditionProperty.set(true);
            }
            else if(role.get()==Role.CONSULTANT){
                geometryEditionProperty.set(false);
                nonGeometryEditionProperty.set(false);
            }
        } 
        else{
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
    
    public enum Role{ADMIN, USER, CONSULTANT, EXTERNE};
    private ObjectProperty<Role> role = new SimpleObjectProperty();
    public Role getRole(){return role.get();}
    ////////////////////////////////////////////////////////////////////////////

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
    
    private final PreviewLabelRepository previewLabelRepository;

    private FXMainFrame frame = null;
    
    @Autowired
    public Session(CouchDbConnector couchDbConnector) {
        super(couchDbConnector);
        this.connector = couchDbConnector;
        
        previewLabelRepository = new PreviewLabelRepository(connector);
        
        sirsGroup.setUserProperty(Session.FLAG_SIRSLAYER, Boolean.TRUE);
    }

    public CouchDbConnector getConnector() {
        return connector;
    }
    
    public PreviewLabelRepository getPreviewLabelRepository() {
        return previewLabelRepository;
    }    
    
    public RefSystemeReleveProfilRepository getSystemeReleveProfilRepository(){
        return refSystemeReleveProfilRepository;
    }
    
    void setFrame(FXMainFrame frame) {
        this.frame = frame;
    }

    public FXMainFrame getFrame() {
        return frame;
    }

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
        return this.digueRepository.getAll();
    }
    
    public Digue getDigueById(final String digueId){
        return this.digueRepository.get(digueId);
    }

    public List<TronconDigue> getTroncons() {
        return this.tronconDigueRepository.getAll();
    }

    public List<TronconDigue> getTronconDigueByDigue(final Digue digue) {
        return this.tronconDigueRepository.getByDigue(digue);
    }
    
    public void update(final Digue digue){
        digue.setDateMaj(LocalDateTime.now());
        this.digueRepository.update(digue);
    }
    
    /**
     * Update a section of the database.
     * @param tronconDigue 
     */
    public void update(final TronconDigue tronconDigue){
        tronconDigue.setDateMaj(LocalDateTime.now());
        System.out.println("enregistrement de "+tronconDigue+" : : "+tronconDigue.getDigueId());
        this.tronconDigueRepository.update(tronconDigue);
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
        this.tronconDigueRepository.add(tronconDigue);
    }
    
    /**
     * Remove a section from the database.
     * @param tronconDigue 
     */
    public void delete(final TronconDigue tronconDigue){
        this.tronconDigueRepository.remove(tronconDigue);
    }

    /**
     * Levee can contain Sections.
     *
     * @param digue
     * @return
     */
    public List<?> getChildren(final Digue digue) {
        return this.getTronconDigueByDigue(digue);
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
                for(Class c : SUPPORTED_TYPES){
                    if(c.isAssignableFrom(propClass)){
                        properties.add(pd);
                        break;
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
    
}
