package fr.sirs;

import com.geomatys.json.GeometryDeserializer;
import fr.sirs.core.component.BorneDigueRepository;
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
import fr.sirs.core.component.SystemeReperageRepository;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.TronconDigue;
import java.time.LocalDateTime;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Johann Sorel
 */
@Component
public class Session {

    public static final CoordinateReferenceSystem PROJECTION = GeometryDeserializer.PROJECTION;
    
    private Object objectToPrint = null;
    
    private MapContext mapContext;
    private final MapItem sirsGroup = MapBuilder.createItem();
    private final MapItem backgroundGroup = MapBuilder.createItem();

    
    private final DigueRepository digueRepository;
    private final TronconDigueRepository tronconDigueRepository;
    private final BorneDigueRepository borneDigueRepository;
    private final SystemeReperageRepository systemeReperageRepository;

    private MainFrame frame = null;
    
    @Autowired
    public Session(CouchDbConnector couchDbConnector) {
        digueRepository = new DigueRepository(couchDbConnector);
        tronconDigueRepository = new TronconDigueRepository(couchDbConnector);
        systemeReperageRepository = new SystemeReperageRepository(couchDbConnector);
        borneDigueRepository = new BorneDigueRepository(couchDbConnector);
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
    
    void setFrame(MainFrame frame) {
        this.frame = frame;
    }

    public MainFrame getFrame() {
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
}
