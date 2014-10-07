
package fr.sym.map;

import org.geotoolkit.gui.javafx.contexttree.MapItemSelectableColumn;
import fr.sym.Session;
import fr.sym.Symadrem;
import fr.sym.digue.Injector;
import java.util.logging.Level;
import javafx.geometry.Orientation;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import org.geotoolkit.gui.javafx.contexttree.FXMapContextTree;
import org.geotoolkit.gui.javafx.contexttree.menu.DeleteItem;
import org.geotoolkit.gui.javafx.contexttree.menu.LayerPropertiesItem;
import org.geotoolkit.gui.javafx.contexttree.menu.OpacityItem;
import org.geotoolkit.gui.javafx.contexttree.menu.ZoomToItem;
import org.geotoolkit.gui.javafx.render2d.FXAddDataBar;
import org.geotoolkit.gui.javafx.render2d.FXCoordinateBar;
import org.geotoolkit.gui.javafx.render2d.FXGeoToolBar;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXNavigationBar;
import org.geotoolkit.gui.javafx.render2d.navigation.FXPanHandler;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.referencing.CRS;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXMapPane extends BorderPane {
    
    private final MapContext context;
    private final FXMap uiMap;
    private final FXAddDataBar uiAddBar;
    private final FXNavigationBar uiNavBar;
    private final FXGeoToolBar uiToolBar;
    private final FXCoordinateBar uiCoordBar;
    private final FXMapContextTree uiTree;

    @Autowired
    private Session session;
    
    public FXMapPane() {
        Injector.injectDependencies(this);
        
        context = session.getMapContext();
        
        uiMap = new FXMap(false);
        uiMap.getContainer().setContext(context);
        uiAddBar = new FXAddDataBar(uiMap);
        uiNavBar = new FXNavigationBar(uiMap);
        uiToolBar = new FXGeoToolBar(uiMap);
        uiCoordBar = new FXCoordinateBar(uiMap);
        uiCoordBar.setScaleBoxValues(new Long[]{200l,5000l,25000l,50000l});
        uiTree = new FXMapContextTree(context);
        uiTree.getTreetable().setShowRoot(false);
        uiTree.getMenuItems().add(new OpacityItem());
        uiTree.getMenuItems().add(new SeparatorMenuItem());
        uiTree.getMenuItems().add(new LayerPropertiesItem(uiMap));
        uiTree.getMenuItems().add(new SeparatorMenuItem());
        uiTree.getMenuItems().add(new ZoomToItem(uiMap));
        uiTree.getMenuItems().add(new DeleteItem());
        uiTree.getTreetable().getColumns().add(new MapItemSelectableColumn());
        
        try {
            uiMap.getCanvas().setObjectiveCRS(CRS.decode("EPSG:3857"));
        } catch (FactoryException | TransformException ex) {
            Symadrem.LOGGER.log(Level.WARNING, ex.getMessage(),ex);
        }
        
        final SplitPane split = new SplitPane();
                
        final GridPane topgrid = new GridPane();
        uiAddBar.setMaxHeight(Double.MAX_VALUE);
        uiNavBar.setMaxHeight(Double.MAX_VALUE);
        uiToolBar.setMaxHeight(Double.MAX_VALUE);
        topgrid.add(uiAddBar,  0, 0);
        topgrid.add(uiNavBar,  1, 0);
        topgrid.add(uiToolBar, 2, 0);
        
        final ColumnConstraints col0 = new ColumnConstraints();
        final ColumnConstraints col1 = new ColumnConstraints();
        final ColumnConstraints col2 = new ColumnConstraints();
        final ColumnConstraints col3 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        final RowConstraints row0 = new RowConstraints();
        row0.setVgrow(Priority.ALWAYS);
        topgrid.getColumnConstraints().addAll(col0,col1,col2,col3);
        topgrid.getRowConstraints().addAll(row0);
        
        
        final BorderPane border = new BorderPane();
        border.setTop(topgrid);
        border.setCenter(uiMap);
        border.setBottom(uiCoordBar);
        
        
        split.setOrientation(Orientation.HORIZONTAL);
        split.getItems().add(uiTree);
        split.getItems().add(border);
        split.setDividerPositions(0.3);
        
        setCenter(split);
        
        uiMap.setHandler(new FXPanHandler(uiMap, false));
    }

    public MapContext getMapContext() {
        return context;
    }

    public FXMap getUiMap() {
        return uiMap;
    }
            
}
