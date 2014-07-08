
package fr.sym.map;

import fr.sym.Symadrem;
import java.net.URL;
import java.util.logging.Level;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.CoverageStore;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.osmtms.OSMTileMapClient;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.style.DefaultDescription;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 *
 * @author Johann Sorel
 */
public class FXMapPane extends BorderPane {
    
    private final MapContext context;
    private final FXMap uiMap;
    private final FXNavigationBar uiNavBar;
    private final FXCoordinateBar uiCoordBar;
    private final FXMapItemPane uiTree;

    public FXMapPane() {
        context = createOSMTMSContext();
        
        uiMap = new FXMap(false);
        uiMap.getContainer().setContext(context);
        uiNavBar = new FXNavigationBar(uiMap);
        uiCoordBar = new FXCoordinateBar(uiMap);
        
        uiTree = new FXMapItemPane(context);
        
        try {
            uiMap.getCanvas().setObjectiveCRS(CRS.decode("EPSG:3857"));
        } catch (FactoryException | TransformException ex) {
            Symadrem.LOGGER.log(Level.WARNING, ex.getMessage(),ex);
        }
        
        final SplitPane split = new SplitPane();
                
        final BorderPane border = new BorderPane();
        border.setTop(uiNavBar);
        border.setCenter(uiMap);
        border.setBottom(uiCoordBar);
        
        
        split.setOrientation(Orientation.HORIZONTAL);
        split.getItems().add(new ScrollPane(uiTree));
        split.getItems().add(border);
        split.setDividerPositions(0.3);
        
        setCenter(split);
        
    }

    public MapContext getMapContext() {
        return context;
    }

    public FXMap getUiMap() {
        return uiMap;
    }
        
    public static MapContext createOSMTMSContext() {
        final MapContext context = MapBuilder.createContext(CommonCRS.WGS84.normalizedGeographic());
        context.setName("Carte");

        try{
            final CoverageStore store = new OSMTileMapClient(new URL("http://tile.openstreetmap.org"), null, 18, true);

            for(Name n : store.getNames()){
                final CoverageReference cr = store.getCoverageReference(n);
                final CoverageMapLayer cml = MapBuilder.createCoverageLayer(cr);
                cml.setDescription(new DefaultDescription(
                        new SimpleInternationalString(n.getLocalPart()),
                        new SimpleInternationalString("")));
                context.layers().add(cml);
            }
            context.setAreaOfInterest(context.getBounds());
        }catch(Exception ex){
            ex.printStackTrace();
        }

        //Other available OSM TMS
        // http://a.tah.openstreetmap.org/Tiles/tile/   17
        // http://tile.opencyclemap.org/cycle/ 18
        // http://tile.cloudmade.com/fd093e52f0965d46bb1c6c6281022199/3/256/ 18

        return context;
    }
    
}
