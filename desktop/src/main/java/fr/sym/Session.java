package fr.sym;

import com.geomatys.json.GeometryDeserializer;

import fr.sym.digue.dto.Dam;
import fr.sym.digue.dto.DamSystem;
import fr.sym.digue.dto.Section;
import fr.sym.store.SymadremStore;
import fr.symadrem.sirs.core.Repository;
import fr.symadrem.sirs.core.component.DigueRepository;
import fr.symadrem.sirs.core.component.TronconDigueRepository;
import fr.symadrem.sirs.core.model.Digue;
import fr.symadrem.sirs.core.model.TronconDigue;

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

/**
 *
 * @author Johann Sorel
 */
@Component
public class Session {

    private final static CoordinateReferenceSystem PROJECTION;
    
    static {
        try {
            PROJECTION  = CRS.decode("EPSG:2154");
        } catch (FactoryException e) {
            throw new IllegalStateException(e);
        }
    }
    
    private MapContext mapContext;

    
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
            mapContext = MapBuilder.createContext(CommonCRS.WGS84.normalizedGeographic());
            mapContext.setName("Carte");

            try {
                //symadrem layers
                final MapItem symadrem = MapBuilder.createItem();
                symadrem.setName("Systeme de digue");
                mapContext.items().add(0,symadrem);

                final SymadremStore symStore = new SymadremStore(this,null,PROJECTION);
                final org.geotoolkit.data.session.Session symSession = symStore.createSession(false);
                for(Name name : symStore.getNames()){
                    final FeatureCollection col = symSession.getFeatureCollection(QueryBuilder.all(name));
                    final MutableStyle style = RandomStyleBuilder.createRandomVectorStyle(col.getFeatureType());
                    final FeatureMapLayer fml = MapBuilder.createFeatureLayer(col, style);
                    fml.setName(name.getLocalPart());
                    symadrem.items().add(fml);
                }

                //Fond de plan
                final MapItem fond = MapBuilder.createItem();
                fond.setName("Fond de plan");
                mapContext.items().add(0,fond);
                final CoverageStore store = new OSMTileMapClient(new URL("http://tile.openstreetmap.org"), null, 18, true);

                for (Name n : store.getNames()) {
                    final CoverageReference cr = store.getCoverageReference(n);
                    final CoverageMapLayer cml = MapBuilder.createCoverageLayer(cr);
                    cml.setName("Open Street Map");
                    cml.setDescription(new DefaultDescription(
                            new SimpleInternationalString("Open Street Map"),
                            new SimpleInternationalString("Open Street Map")));
                    fond.items().add(cml);
                }
                mapContext.setAreaOfInterest(mapContext.getBounds());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return mapContext;
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
        //TODO database view ?
        final String digueId = digue.getId();
        final List<TronconDigue> troncons = this.getTroncons();
        final List<TronconDigue> tronconsDeLaDigue = new ArrayList<>();
        for (final TronconDigue troncon : troncons) {
            if (troncon.getDigueId().equals(digueId)) {
                tronconsDeLaDigue.add(troncon);
            }
        }
        return tronconsDeLaDigue;
    }

    /**
     * DamSystem can contain Dams or Sections.
     *
     * @param digue
     * @return
     */
    public List<?> getChildren(Digue digue) {
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
