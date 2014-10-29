
package fr.sym.map;

import org.geotoolkit.display2d.Canvas2DSynchronizer;
import org.geotoolkit.gui.javafx.contexttree.MapItemSelectableColumn;
import fr.sym.Session;
import fr.sym.Symadrem;
import fr.sym.digue.Injector;
import java.awt.Color;
import java.awt.geom.NoninvertibleTransformException;
import java.util.Date;
import java.util.logging.Level;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import org.geotoolkit.display2d.canvas.painter.SolidColorPainter;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.gui.javafx.contexttree.FXMapContextTree;
import org.geotoolkit.gui.javafx.contexttree.MapItemFilterColumn;
import org.geotoolkit.gui.javafx.contexttree.menu.DeleteItem;
import org.geotoolkit.gui.javafx.contexttree.menu.LayerPropertiesItem;
import org.geotoolkit.gui.javafx.contexttree.menu.OpacityItem;
import org.geotoolkit.gui.javafx.contexttree.menu.ZoomToItem;
import org.geotoolkit.gui.javafx.render2d.FXAddDataBar;
import org.geotoolkit.gui.javafx.render2d.FXColorDecoration;
import org.geotoolkit.gui.javafx.render2d.FXCoordinateBar;
import org.geotoolkit.gui.javafx.render2d.FXGeoToolBar;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXNavigationBar;
import org.geotoolkit.gui.javafx.render2d.navigation.FXPanHandler;
import org.geotoolkit.map.MapContext;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXMapPane extends BorderPane {
    
    public static final Image ICON_SPLIT= SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_COLUMNS,16,FontAwesomeIcons.DEFAULT_COLOR),null);
    
    private final MapContext context;
    private final SplitPane mapsplit = new SplitPane();
    private final FXAddDataBar uiAddBar;
    private final FXNavigationBar uiNavBar;
    private final FXGeoToolBar uiToolBar;
    private final TronconEditBar uiEditBar;
    private final FXMapContextTree uiTree;
    private final Button splitButton = new Button(null, new ImageView(ICON_SPLIT));
    private final ToolBar uiSplitBar = new ToolBar(splitButton);
    
    private final FXMap uiMap1 = new FXMap(false);
    private final FXMap uiMap2 = new FXMap(false);
    private final FXCoordinateBar uiCoordBar1 = new FXCoordinateBar(uiMap1);
    private final FXCoordinateBar uiCoordBar2 = new FXCoordinateBar(uiMap2);
    private final BorderPane paneMap1 = new BorderPane(uiMap1, null, null, uiCoordBar1, null);
    private final BorderPane paneMap2 = new BorderPane(uiMap2, null, null, uiCoordBar2, null);
    private final Canvas2DSynchronizer synchronizer = new Canvas2DSynchronizer();

    @Autowired
    private Session session;
    
    public FXMapPane() {
        Injector.injectDependencies(this);        
        context = session.getMapContext();
        
        uiCoordBar2.setCrsButtonVisible(false);
        uiMap1.getContainer().setContext(context);
        uiMap2.getContainer().setContext(context);
        uiMap1.getCanvas().setBackgroundPainter(new SolidColorPainter(Color.WHITE));
        uiMap2.getCanvas().setBackgroundPainter(new SolidColorPainter(Color.WHITE));
        synchronizer.addCanvas(uiMap1.getCanvas(),true,true);
        synchronizer.addCanvas(uiMap2.getCanvas(),true,true);
        
        uiAddBar = new FXAddDataBar(uiMap1);
        uiNavBar = new FXNavigationBar(uiMap1);
        uiToolBar = new FXGeoToolBar(uiMap1);
        uiEditBar = new TronconEditBar(uiMap1);
        uiCoordBar1.setScaleBoxValues(new Long[]{200l,5000l,25000l,50000l});
        uiCoordBar2.setScaleBoxValues(new Long[]{200l,5000l,25000l,50000l});
        uiTree = new FXMapContextTree(context);
        uiTree.getTreetable().setShowRoot(false);
        uiTree.getMenuItems().add(new OpacityItem());
        uiTree.getMenuItems().add(new SeparatorMenuItem());
        uiTree.getMenuItems().add(new LayerPropertiesItem(uiMap1));
        uiTree.getMenuItems().add(new SeparatorMenuItem());
        uiTree.getMenuItems().add(new ZoomToItem(uiMap1));
        uiTree.getMenuItems().add(new DeleteItem());
        uiTree.getTreetable().getColumns().add(2,new MapItemFilterColumn());
        uiTree.getTreetable().getColumns().add(3,new MapItemSelectableColumn());
                
        splitButton.setOnAction((ActionEvent event) -> {
            if(mapsplit.getItems().contains(paneMap2)){
                mapsplit.getItems().remove(paneMap2);
            }else{
                mapsplit.setDividerPositions(0.5);
                mapsplit.getItems().add(paneMap2);
            }
        });
        
        
        final GridPane topgrid = new GridPane();
        uiAddBar.setMaxHeight(Double.MAX_VALUE);
        uiNavBar.setMaxHeight(Double.MAX_VALUE);
        uiToolBar.setMaxHeight(Double.MAX_VALUE);
        uiEditBar.setMaxHeight(Double.MAX_VALUE);
        uiSplitBar.setMaxHeight(Double.MAX_VALUE);
        topgrid.add(uiAddBar,  0, 0);
        topgrid.add(uiNavBar,  1, 0);
        topgrid.add(uiToolBar, 2, 0);
        topgrid.add(uiEditBar, 3, 0);
        topgrid.add(uiSplitBar, 5, 0);
        
        final ColumnConstraints col0 = new ColumnConstraints();
        final ColumnConstraints col1 = new ColumnConstraints();
        final ColumnConstraints col2 = new ColumnConstraints();
        final ColumnConstraints col3 = new ColumnConstraints();
        final ColumnConstraints col4 = new ColumnConstraints();
        final ColumnConstraints col5 = new ColumnConstraints();
        col3.setHgrow(Priority.ALWAYS);
        final RowConstraints row0 = new RowConstraints();
        row0.setVgrow(Priority.ALWAYS);
        topgrid.getColumnConstraints().addAll(col0,col1,col2,col3,col4,col5);
        topgrid.getRowConstraints().addAll(row0);
        
        mapsplit.getItems().add(paneMap1);
        
        final BorderPane border = new BorderPane();
        border.setTop(topgrid);
        border.setCenter(mapsplit);
        
        
        final SplitPane split = new SplitPane();
        split.setOrientation(Orientation.HORIZONTAL);
        split.getItems().add(uiTree);
        split.getItems().add(border);
        split.setDividerPositions(0.3);
        
        setCenter(split);
        
        uiMap1.setHandler(new FXPanHandler(uiMap1, false));
        uiMap2.setHandler(new FXPanHandler(uiMap2, false));
        
        //deplacer à la date du jour
        final Date time = new Date();
        try {
            uiMap1.getCanvas().setObjectiveCRS(Session.PROJECTION);
            uiMap1.getCanvas().setVisibleArea(session.getMapContext().getAreaOfInterest());
            uiMap1.getCanvas().setTemporalRange(time,time);
        } catch (NoninvertibleTransformException | TransformException ex) {
            Symadrem.LOGGER.log(Level.WARNING, ex.getMessage(),ex);
        }
        
        //ajout des ecouteurs souris sur click droit
        uiMap1.addEventHandler(MouseEvent.MOUSE_CLICKED, new MapActionHandler(uiMap1));
        uiMap2.addEventHandler(MouseEvent.MOUSE_CLICKED, new MapActionHandler(uiMap2));
    }

    public MapContext getMapContext() {
        return context;
    }

    public FXMap getUiMap() {
        return uiMap1;
    }
            
}
