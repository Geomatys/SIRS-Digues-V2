package fr.sirs;

import fr.sirs.util.json.GeometryDeserializer;
import fr.sirs.core.component.BorneDigueRepository;
import fr.sirs.core.component.ContactRepository;

import java.net.URL;
import java.util.List;

import org.apache.sis.util.iso.SimpleInternationalString;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.CoverageStore;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.osmtms.OSMTileMapClient;
import org.geotoolkit.style.DefaultDescription;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.sirs.core.component.DigueRepository;
import fr.sirs.core.component.OrganismeRepository;
import fr.sirs.core.component.PreviewLabelRepository;
import fr.sirs.core.component.ProfilTraversRepository;
import fr.sirs.core.component.RefOrigineProfilTraversRepository;
import fr.sirs.core.component.RefSystemeReleveProfilRepository;
import fr.sirs.core.component.RefTypeProfilTraversRepository;
import fr.sirs.core.component.SystemeReperageRepository;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.RefSystemeReleveProfil;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.util.property.Internal;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.time.LocalDateTime;

import org.ektorp.CouchDbConnector;

/**
 *
 * @author Johann Sorel
 */
@Component
public class Session {

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
    
    public static final CoordinateReferenceSystem PROJECTION = GeometryDeserializer.PROJECTION;
    
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
    private final DigueRepository digueRepository;
    private final TronconDigueRepository tronconDigueRepository;
    private final BorneDigueRepository borneDigueRepository;
    private final SystemeReperageRepository systemeReperageRepository;
    private final ContactRepository contactRepository;
    private final OrganismeRepository organismeRepository;
    private final PreviewLabelRepository previewLabelRepository;
    private final ProfilTraversRepository profilTraversRepository;
    private final RefOrigineProfilTraversRepository refOrigineProfilTraversRepository;
    private final RefSystemeReleveProfilRepository refSystemeReleveProfilRepository;
    private final RefTypeProfilTraversRepository refTypeProfilTraversRepository;

    private FXMainFrame frame = null;
    
    @Autowired
    public Session(CouchDbConnector couchDbConnector) {
        this.connector = couchDbConnector;
        digueRepository = new DigueRepository(connector);
        tronconDigueRepository = new TronconDigueRepository(connector);
        systemeReperageRepository = new SystemeReperageRepository(connector);
        borneDigueRepository = new BorneDigueRepository(connector);
        contactRepository = new ContactRepository(connector);
        organismeRepository = new OrganismeRepository(connector);
        previewLabelRepository = new PreviewLabelRepository(connector);
        profilTraversRepository = new ProfilTraversRepository(connector);
        refOrigineProfilTraversRepository = new RefOrigineProfilTraversRepository(connector);
        refSystemeReleveProfilRepository = new RefSystemeReleveProfilRepository(connector);
        refTypeProfilTraversRepository = new RefTypeProfilTraversRepository(connector);
    }

    public CouchDbConnector getConnector() {
        return connector;
    }
    
    public DigueRepository getDigueRepository() {
        return digueRepository;
    }

    public TronconDigueRepository getTronconDigueRepository() {
        return tronconDigueRepository;
    }
    
    public BorneDigueRepository getBorneDigueRepository(){
        return borneDigueRepository;
    }

    public SystemeReperageRepository getSystemeReperageRepository(){
        return systemeReperageRepository;
    }

    public ContactRepository getContactRepository() {
        return contactRepository;
    }

    public OrganismeRepository getOrganismeRepository() {
        return organismeRepository;
    }
    
    public PreviewLabelRepository getPreviewLabelRepository() {
        return previewLabelRepository;
    }
    
    public ProfilTraversRepository getProfilTraversRepository() {
        return profilTraversRepository;
    }
    
    public RefOrigineProfilTraversRepository getRefOrigineProfilTraversRepository(){
        return refOrigineProfilTraversRepository;
    }
    
    public RefSystemeReleveProfilRepository getSystemeReleveProfilRepository(){
        return refSystemeReleveProfilRepository;
    }
    
    public RefTypeProfilTraversRepository getRefTypeProfilTraversRepository(){
        return refTypeProfilTraversRepository;
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
            mapContext = MapBuilder.createContext(PROJECTION);
            mapContext.setName("Carte");

            try {
                //sirs layers
                sirsGroup.setName("Systeme de digue");
                mapContext.items().add(0,sirsGroup);

                for(Plugin plugin : Plugins.getPlugins()){
                    sirsGroup.items().addAll(plugin.getMapItems());
                }
                mapContext.setAreaOfInterest(mapContext.getBounds());

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
                    backgroundGroup.items().add(cml);
                }
            } catch (Exception ex) {
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
