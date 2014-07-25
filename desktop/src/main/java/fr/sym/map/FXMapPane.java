
package fr.sym.map;

import fr.sym.Session;
import fr.sym.Symadrem;
import java.util.logging.Level;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import org.geotoolkit.gui.javafx.contexttree.FXMapContextTree;
import org.geotoolkit.gui.javafx.render2d.FXCoordinateBar;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXNavigationBar;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.referencing.CRS;
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
    private final FXMapContextTree uiTree;

    public FXMapPane() {
        context = Session.getInstance().getMapContext();
        
        uiMap = new FXMap(false);
        uiMap.getContainer().setContext(context);
        uiNavBar = new FXNavigationBar(uiMap);
        uiCoordBar = new FXCoordinateBar(uiMap);
        uiTree = new FXMapContextTree(context);
        
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
        split.getItems().add(uiTree);
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
            
}
