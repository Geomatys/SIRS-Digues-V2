

package fr.sym;

import fr.sym.digue.dto.Dam;
import fr.sym.digue.dto.DamSystem;
import fr.sym.digue.dto.Section;
import fr.symadrem.sirs.core.component.DigueRepository;
import fr.symadrem.sirs.core.model.Digue;
import fr.symadrem.sirs.core.model.TronconDigue;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.misc.Contended;

/**
 *
 * @author Johann Sorel
 */

@Component
public class Session {
    
    private final MapContext mapContext = MapBuilder.createContext(CommonCRS.WGS84.normalizedGeographic());
    
    @Autowired
    private DigueRepository digueRepository;
    
    public Session(){
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
        
        //this.digues = this.digueRepository.getAll();
        //TODO database binding
        int nbDigues = 10;
        if(this.digues == null){
            final List<Digue> digs = new ArrayList<>();
            for(int i=0; i<nbDigues; i++){
                final Digue digue = new Digue();
                digue.setLibelle("La digue "+i);
                digue.setCommentaire(i+" : Lorem ipsum dolor sit amet, consectetur "
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
                digue.setTronconsIds(new ArrayList<>());
                digs.add(digue);
            }
            this.digues = digs;
        }
        return this.digues;
    }
    
    private Map<Digue, List<TronconDigue>> digueToTroncons = new HashMap<>();
    private List<TronconDigue> tronconsGestionDigue = null;
    public List<TronconDigue> getTroncons(){
        //TODO database binding
        int nbTroncons = 30;
        if (this.tronconsGestionDigue == null){
            final List<TronconDigue> troncons = new ArrayList<>();
            for(int i=0; i<nbTroncons; i++){
                final TronconDigue tron = new TronconDigue();
                tron.setLibelle("Le tronçon "+i);
                tron.setDigue(Long.toString(i%3));
                /*System.out.println("Jojo : "+tron.getJojo());
                if(i%2==0)
                    tron.setJojo(Troncon.jojoenum.oui);
                else 
                    tron.setJojo((Troncon.jojoenum.bof));*/
                tron.setCommentaire("Tronçon "+i+" : Lorem ipsum dolor sit amet, consectetur "
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
                troncons.add(tron);
            }
            this.tronconsGestionDigue = troncons;
        
            // Linking troncons to digues
            List<Digue> digs = this.getDigues();
            int nbDigues = digs.size();
            for (int i=0; i<nbTroncons; i++){
                TronconDigue tron = this.tronconsGestionDigue.get(i);
                Digue digue = digs.get(i%nbDigues);
                
                tron.setDigue(String.valueOf(i%nbDigues));
                List<String> tronconsIds = digue.getTronconsIds();
                tronconsIds.add(String.valueOf(i));
                digue.setTronconsIds(tronconsIds);

                if(!this.digueToTroncons.containsKey(digue)){
                    List<TronconDigue> trons = new ArrayList<>();
                    trons.add(tron);
                    this.digueToTroncons.put(digue, trons);
                } else {
                    this.digueToTroncons.get(digue).add(tron);
                }
            }
        }
        return this.tronconsGestionDigue;
    }
    
    public List<TronconDigue> getTronconGestionDigueTrysByDigueTry(Digue digue){
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
     * @param digue
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
     * @param ds
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
