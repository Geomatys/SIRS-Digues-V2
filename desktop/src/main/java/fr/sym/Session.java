

package fr.sym;

import fr.sym.digue.dto.Dam;
import fr.sym.digue.dto.DamSystem;
import fr.sym.digue.dto.DigueTry;
import fr.sym.digue.dto.Section;
import fr.sym.digue.dto.TronconGestionDigueTry;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.sis.referencing.CommonCRS;
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

/**
 *
 * @author Johann Sorel
 */
public class Session {
    
    private static final Session INSTANCE = new Session();

    private final MapContext mapContext = MapBuilder.createContext(CommonCRS.WGS84.normalizedGeographic());
    
    private Session(){
        mapContext.setName("Carte");

        //Fond de plan
        final MapItem fond = MapBuilder.createItem();
        fond.setName("Fond de plan");
        mapContext.items().add(fond);
        try{
            final CoverageStore store = new OSMTileMapClient(new URL("http://tile.openstreetmap.org"), null, 18, true);

            for(Name n : store.getNames()){
                final CoverageReference cr = store.getCoverageReference(n);
                final CoverageMapLayer cml = MapBuilder.createCoverageLayer(cr);
                cml.setName("Open Street Map");
                cml.setDescription(new DefaultDescription(
                        new SimpleInternationalString("Open Street Map"),
                        new SimpleInternationalString("Open Street Map")));
                fond.items().add(cml);
            }
            mapContext.setAreaOfInterest(mapContext.getBounds());
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    public static Session getInstance() {
        return INSTANCE;
    }
    
    /**
     * MapContext affiché pour toute l'application.
     * 
     * @return MapContext
     */
    public MapContext getMapContext(){
        return mapContext;
    }
    
    public List<DamSystem> getDamSystems(){
        //TODO database binding
        final List<DamSystem> damSystems = new ArrayList<>();
        for(int i=0;i<10;i++){
            final DamSystem ds = new DamSystem();
            ds.getName().set("Dam system "+i);
            damSystems.add(ds);
        }
        
        return damSystems;
    }
    
    private List<DigueTry> digueTrys = null;
    public List<DigueTry> getDigueTrys(){
        //TODO database binding
        if(digueTrys == null){
            final List<DigueTry> digues = new ArrayList<>();
            for(int i=0;i<3;i++){
                final DigueTry ds = new DigueTry();
                ds.setIdDigue(new Long(i));
                ds.setLibelleDigue("La digue "+i);
                digues.add(ds);
            }
            digueTrys = digues;
        }
        return digueTrys;
    }
    
    private List<TronconGestionDigueTry> tronconGestionDigueTrys = null;
    public List<TronconGestionDigueTry> getTronconGestionDigueTrys(){
        //TODO database binding
        if (tronconGestionDigueTrys == null){
            final List<TronconGestionDigueTry> troncons = new ArrayList<>();
            for(int i=0;i<9;i++){
                final TronconGestionDigueTry ds = new TronconGestionDigueTry();
                ds.setIdTronconGestion(new Long(i));
                ds.setLibelleTronconGestion("Le tronçon "+i);
                ds.setIdDigue(new Long(i%3));
                troncons.add(ds);
            }
            tronconGestionDigueTrys = troncons;
        }
        return tronconGestionDigueTrys;
    }
    
    
    
    public List<TronconGestionDigueTry> getTronconGestionDigueTrysByDigueTry(DigueTry digue){
        //TODO database binding
        final List<TronconGestionDigueTry> troncons = this.getTronconGestionDigueTrys();
        final List<TronconGestionDigueTry> tronconsDeLaDigue = new ArrayList<>();
        for(TronconGestionDigueTry troncon : troncons){
            if(troncon.getIdDigue().equals(digue.getIdDigue())) {
                tronconsDeLaDigue.add(troncon);
            }
        }
        return tronconsDeLaDigue;
    }
    
    /**
     * DamSystem can contain Dams or Sections.
     * 
     * @param ds
     * @return 
     */
    public List<?> getChildren(DigueTry digue){
        return this.getTronconGestionDigueTrysByDigueTry(digue);
    }
    
    /**
     * DamSystem can contain Dams or Sections.
     * 
     * @param ds
     * @return 
     */
    public List<?> getChildren(DamSystem ds){
        final List children = new ArrayList();
        for(int i=0;i<8;i++){
            if(Math.random()<0.5){
                final Section section = new Section();
                section.getName().set("Section "+i);
                children.add(section);
            }else{
                final Dam dam = new Dam();
                dam.getName().set("Dam "+i);
                children.add(dam);
            }
        }
        return children;
    }
    
    /**
     * DamSystem can contain Dams or Sections.
     * 
     * @param Dam
     * @return 
     */
    public List<Section> getChildren(Dam ds){
        final List children = new ArrayList();
        for(int i=0;i<8;i++){
            final Section section = new Section();
            section.getName().set("Section "+i);
            children.add(section);
        }
        return children;
    }
    
}
