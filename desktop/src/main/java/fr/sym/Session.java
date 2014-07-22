

package fr.sym;

import java.net.URL;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.CoverageStore;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
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

        try{
            final CoverageStore store = new OSMTileMapClient(new URL("http://tile.openstreetmap.org"), null, 18, true);

            for(Name n : store.getNames()){
                final CoverageReference cr = store.getCoverageReference(n);
                final CoverageMapLayer cml = MapBuilder.createCoverageLayer(cr);
                cml.setDescription(new DefaultDescription(
                        new SimpleInternationalString("Open Street Map"),
                        new SimpleInternationalString("Open Street Map")));
                mapContext.layers().add(cml);
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
    
}
