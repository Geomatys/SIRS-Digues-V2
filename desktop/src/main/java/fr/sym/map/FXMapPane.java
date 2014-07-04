
package fr.sym.map;

import fr.sym.Symadrem;
import java.util.logging.Level;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import org.geotoolkit.gui.swing.contexttree.JContextTree;
import org.geotoolkit.gui.swing.render2d.JMap2D;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.referencing.CRS;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 *
 * @author Johann Sorel
 */
public class FXMapPane extends BorderPane {
    
    private final MapContext context = MapBuilder.createContext();
    private final JContextTree uiTree = new JContextTree();
    private final JMap2D uiMap = new JMap2D(false);

    public FXMapPane() {
        uiMap.getContainer().setContext(context);
        uiTree.setContext(context);
        
        try {
            uiMap.getCanvas().setObjectiveCRS(CRS.decode("EPSG:3857"));
        } catch (FactoryException | TransformException ex) {
            Symadrem.LOGGER.log(Level.WARNING, ex.getMessage(),ex);
        }
        
        final SplitPane split = new SplitPane();
        
        final SwingNode bindmap = new SwingNode();
        final SwingNode bindtree = new SwingNode();
        bindmap.setContent(uiMap);
        bindtree.setContent(uiTree);
        
        split.setOrientation(Orientation.HORIZONTAL);
        split.getItems().addAll(bindtree,bindmap);
        
        
        setCenter(split);
        
    }

    public MapContext getMapContext() {
        return context;
    }

    public JMap2D getUiMap() {
        return uiMap;
    }
        
}
