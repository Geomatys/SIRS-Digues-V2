

package fr.sym;

import fr.sym.digue.dto.Dam;
import fr.sym.digue.dto.DamSystem;
import fr.sym.digue.dto.Section;
import fr.symadrem.sirs.core.model.Troncon;
import fr.symadrem.sirs.core.model.Digue;
import java.net.URL;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    
    private List<Digue> digues = null;
    public List<Digue> getDigues(){
        //TODO database binding
        int nbDigues = 10;
        if(this.digues == null){
            final List<Digue> digs = new ArrayList<>();
            for(int i=0; i<nbDigues; i++){
                final Digue digue = new Digue();
                digue.setLabel("La digue "+i);
                digue.setComment(i+" : Lorem ipsum dolor sit amet, consectetur "
                        + "adipiscing elit. Sed non risus. Suspendisse lectus "
                        + "tortor, dignissim sit amet, adipiscing nec, ultricies "
                        + "sed, dolor. Cras elementum ultrices diam. Maecenas "
                        + "ligula massa, varius a, semper congue, euismod non, "
                        + "mi. Proin porttitor, orci nec nonummy molestie, enim "
                        + "est eleifend mi, non fermentum diam nisl sit amet "
                        + "erat. Duis semper. Duis arcu massa, scelerisque "
                        + "vitae, consequat in, pretium a, enim. Pellentesque "
                        + "congue. Ut in risus volutpat libero pharetra tempor. "
                        + "Cras vestibulum bibendum augue. Praesent egestas leo "
                        + "in pede. Praesent blandit odio eu enim. Pellentesque "
                        + "sed dui ut augue blandit sodales. Vestibulum ante "
                        + "ipsum primis in faucibus orci luctus et ultrices "
                        + "posuere cubilia Curae; Aliquam nibh. Mauris ac mauris "
                        + "sed pede pellentesque fermentum. Maecenas adipiscing "
                        + "ante non diam sodales hendrerit.");
                digue.setTronconsIds(new HashSet<>());
                digs.add(digue);
            }
            this.digues = digs;
        }
        return this.digues;
    }
    
    private Map<Digue, List<Troncon>> digueToTroncons = new HashMap<>();
    private List<Troncon> tronconsGestionDigue = null;
    public List<Troncon> getTroncons(){
        //TODO database binding
        int nbTroncons = 30;
        if (this.tronconsGestionDigue == null){
            final List<Troncon> troncons = new ArrayList<>();
            for(int i=0; i<nbTroncons; i++){
                final Troncon tron = new Troncon();
                tron.setName("Le tronçon "+i);
                tron.setDigue(Long.toString(i%3));
                System.out.println("Jojo : "+tron.getJojo());
                if(i%2==0)
                    tron.setJojo(Troncon.jojoenum.oui);
                else 
                    tron.setJojo((Troncon.jojoenum.bof));
                troncons.add(tron);
            }
            this.tronconsGestionDigue = troncons;
        
            // Linking troncons to digues
            List<Digue> digues = this.getDigues();
            int nbDigues = digues.size();
            for (int i=0; i<nbTroncons; i++){
                Troncon tron = this.tronconsGestionDigue.get(i);
                Digue digue = digues.get(i%nbDigues);
                
                tron.setDigue(String.valueOf(i%nbDigues));
                Set<String> tronconsIds = digue.getTronconsIds();
                tronconsIds.add(String.valueOf(i));
                digue.setTronconsIds(tronconsIds);

                if(!this.digueToTroncons.containsKey(digue)){
                    List<Troncon> trons = new ArrayList<>();
                    trons.add(tron);
                    this.digueToTroncons.put(digue, trons);
                } else {
                    this.digueToTroncons.get(digue).add(tron);
                }
            }
        }
        return this.tronconsGestionDigue;
    }
    
    public List<Troncon> getTronconGestionDigueTrysByDigueTry(Digue digue){
        //TODO database binding
        //return this.digueToTroncons.get(digue);
        //TODO database binding
        this.getTroncons();
        return this.digueToTroncons.get(digue);
    }
    
    /*public List<TronconGestionDigueTry> getTronconGestionDigueTrysByDigueTry(DigueTry digue){
        //TODO database binding
        final List<TronconGestionDigueTry> troncons = this.getTronconGestionDigueTrys();
        final List<TronconGestionDigueTry> tronconsDeLaDigue = new ArrayList<>();
        for(TronconGestionDigueTry troncon : troncons){
            if(troncon.getIdDigue().equals(digue.getIdDigue())) {
                tronconsDeLaDigue.add(troncon);
            }
        }
        return tronconsDeLaDigue;
    }*/
    
    /**
     * DamSystem can contain Dams or Sections.
     * 
     * @param ds
     * @return 
     */
    public List<?> getChildren(Digue digue){
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
