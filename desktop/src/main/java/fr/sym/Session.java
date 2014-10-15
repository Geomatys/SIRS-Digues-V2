package fr.sym;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.CoverageStore;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.osmtms.OSMTileMapClient;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.style.DefaultDescription;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.RandomStyleBuilder;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.sym.digue.dto.Dam;
import fr.sym.digue.dto.DamSystem;
import fr.sym.digue.dto.Section;
import fr.sym.store.SymadremStore;
import fr.symadrem.sirs.core.Repository;
import fr.symadrem.sirs.core.component.DigueRepository;
import fr.symadrem.sirs.core.component.TronconDigueRepository;
import fr.symadrem.sirs.core.model.Digue;
import fr.symadrem.sirs.core.model.TronconDigue;
import java.time.LocalDateTime;

/**
 *
 * @author Johann Sorel
 */
@Component
public class Session {

    public static final CoordinateReferenceSystem PROJECTION;
    
    static {
        try {
            PROJECTION  = CRS.decode("EPSG:2154");
        } catch (FactoryException e) {
            throw new IllegalStateException(e);
        }
    }
    
    private MapContext mapContext;
    private final MapItem symadremGroup = MapBuilder.createItem();
    private final MapItem backgroundGroup = MapBuilder.createItem();

    
    @Autowired
    private List<Repository<?>> repositories;
    
    @Autowired
    private DigueRepository digueRepository;

    @Autowired
    private TronconDigueRepository tronconDigueRepository;

    public Session() {
        
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

                final SymadremStore symStore = new SymadremStore(this,null,PROJECTION);
                final org.geotoolkit.data.session.Session symSession = symStore.createSession(false);
                for(Name name : symStore.getNames()){
                    final FeatureCollection col = symSession.getFeatureCollection(QueryBuilder.all(name));
                    final MutableStyle style = RandomStyleBuilder.createRandomVectorStyle(col.getFeatureType());
                    final FeatureMapLayer fml = MapBuilder.createFeatureLayer(col, style);
                    fml.setName(name.getLocalPart());
                    symadremGroup.items().add(fml);
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

    public synchronized MapItem getSymadremLayerGroup() {
        getMapContext();
        return symadremGroup;
    }

    public synchronized MapItem getBackgroundLayerGroup() {
        getMapContext();
        return backgroundGroup;
    }

    /**
     * Liste de tout les repositories CouchDB.
     */
    public List<Repository<?>> getRepositories() {
        return repositories;
    }
    
    public List<DamSystem> getDamSystems() {
        //TODO database binding
        final List<DamSystem> damSystems = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final DamSystem ds = new DamSystem();
            ds.getName().set("Dam system " + i);
            damSystems.add(ds);
        }

        return damSystems;
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
        digue.setDate_maj(LocalDateTime.now());
        this.digueRepository.update(digue);
    }
    
    /**
     * Update a section of the database.
     * @param tronconDigue 
     */
    public void update(final TronconDigue tronconDigue){
        tronconDigue.setDate_maj(LocalDateTime.now());
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
        tronconDigue.setDate_maj(LocalDateTime.now());
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

    /**
     * DamSystem can contain Dams or Sections.
     *
     * @param ds
     * @return
     */
    public List<?> getChildren(DamSystem ds) {
        final List children = new ArrayList();
        for (int i = 0; i < 8; i++) {
            if (Math.random() < 0.5) {
                final Section section = new Section();
                section.getName().set("Section " + i);
                children.add(section);
            } else {
                final Dam dam = new Dam();
                dam.getName().set("Dam " + i);
                children.add(dam);
            }
        }
        return children;
    }

    /**
     * DamSystem can contain Dams or Sections.
     *
     * @param ds
     * @return
     */
    public List<Section> getChildren(Dam ds) {
        final List children = new ArrayList();
        for (int i = 0; i < 8; i++) {
            final Section section = new Section();
            section.getName().set("Section " + i);
            children.add(section);
        }
        return children;
    }
}
