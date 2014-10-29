package fr.sym;

import com.geomatys.json.GeometryDeserializer;
import fr.symadrem.sirs.core.component.BorneDigueRepository;
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

import fr.symadrem.sirs.core.component.DigueRepository;
import fr.symadrem.sirs.core.component.TronconDigueRepository;
import fr.symadrem.sirs.core.model.Digue;
import fr.symadrem.sirs.core.model.TronconDigue;
import java.time.LocalDateTime;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Johann Sorel
 */
@Component
public class Session {

    public static final CoordinateReferenceSystem PROJECTION = GeometryDeserializer.PROJECTION;
    
    private MapContext mapContext;
    private final MapItem symadremGroup = MapBuilder.createItem();
    private final MapItem backgroundGroup = MapBuilder.createItem();

    
    private DigueRepository digueRepository;
    private TronconDigueRepository tronconDigueRepository;
    private BorneDigueRepository borneDigueRepository;

    @Autowired
    public Session(CouchDbConnector couchDbConnector) {
        digueRepository = new DigueRepository(couchDbConnector);
        tronconDigueRepository = new TronconDigueRepository(couchDbConnector);
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
                //symadrem layers
                symadremGroup.setName("Systeme de digue");
                mapContext.items().add(0,symadremGroup);

                for(Plugin plugin : Plugins.getPlugins()){
                    symadremGroup.items().addAll(plugin.getMapItems());
                }
                mapContext.setAreaOfInterest(mapContext.getBounds());

                //Fond de plan
                backgroundGroup.setName("Fond de plan");
                mapContext.items().add(0,backgroundGroup);
                final CoverageStore store = new OSMTileMapClient(new URL("http://tile.openstreetmap.org"), null, 18, true);

                for (Name n : store.getNames()) {
                    final CoverageReference cr = store.getCoverageReference(n);
                    final CoverageMapLayer cml = MapBuilder.createCoverageLayer(cr);
                    cml.setOpacity(0.4);
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

    public synchronized MapItem getSymadremLayerGroup() {
        getMapContext();
        return symadremGroup;
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

}
