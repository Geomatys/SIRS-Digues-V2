
package fr.sirs.map;

import fr.sirs.CorePlugin;
import org.geotoolkit.display2d.Canvas2DSynchronizer;
import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.TronconDigue;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.geom.NoninvertibleTransformException;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Level;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import org.apache.sis.geometry.GeneralEnvelope;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.display2d.GO2Hints;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.display2d.canvas.painter.SolidColorPainter;
import org.geotoolkit.display2d.container.ContextContainer2D;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.feature.type.DefaultName;
import org.geotoolkit.feature.type.FeatureType;
import org.geotoolkit.feature.type.GeometryDescriptor;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.filter.identity.DefaultFeatureId;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.gui.javafx.contexttree.FXMapContextTree;
import org.geotoolkit.gui.javafx.contexttree.MapItemFilterColumn;
import org.geotoolkit.gui.javafx.contexttree.MapItemSelectableColumn;
import org.geotoolkit.gui.javafx.contexttree.menu.LayerPropertiesItem;
import org.geotoolkit.gui.javafx.contexttree.menu.OpacityItem;
import org.geotoolkit.gui.javafx.contexttree.menu.ZoomToItem;
import org.geotoolkit.gui.javafx.render2d.FXAddDataBar;
import org.geotoolkit.gui.javafx.render2d.FXContextBar;
import org.geotoolkit.gui.javafx.render2d.FXCoordinateBar;
import org.geotoolkit.gui.javafx.render2d.FXGeoToolBar;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXNavigationBar;
import org.geotoolkit.gui.javafx.render2d.navigation.FXPanHandler;
import org.geotoolkit.gui.javafx.util.FXUtilities;
import org.geotoolkit.internal.referencing.CRSUtilities;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.temporal.object.TemporalConstants;
import org.opengis.filter.Id;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.GenericName;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXMapPane extends BorderPane {
    
    public static final Image ICON_SPLIT= SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_COLUMNS,16,FontAwesomeIcons.DEFAULT_COLOR),null);
    
    private static final Hints MAPHINTS = new Hints();
    static {
        MAPHINTS.put(GO2Hints.KEY_VIEW_TILE, GO2Hints.VIEW_TILE_ON);
        MAPHINTS.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        MAPHINTS.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        MAPHINTS.put(GO2Hints.KEY_BEHAVIOR_MODE, GO2Hints.BEHAVIOR_KEEP_TILE);
    }
    
    private MapContext context;
    private final SplitPane mapsplit = new SplitPane();
    private final FXContextBar uiCtxBar;
    private final FXAddDataBar uiAddBar;
    private final FXNavigationBar uiNavBar;
    private final FXGeoToolBar uiToolBar;
    private final FXTronconEditBar uiEditBar;
    private final FXMapContextTree uiTree;
    private final Button splitButton = new Button(null, new ImageView(ICON_SPLIT));
    private final ToolBar uiSplitBar = new ToolBar(splitButton);
        
    private final FXMap uiMap1 = new FXMap(false, MAPHINTS);
    private final FXMap uiMap2 = new FXMap(false, MAPHINTS);
    private final FXCoordinateBar uiCoordBar1 = new FXCoordinateBar(uiMap1);
    private final FXCoordinateBar uiCoordBar2 = new FXCoordinateBar(uiMap2);
    private final BorderPane paneMap1 = new BorderPane(uiMap1, null, null, uiCoordBar1, null);
    private final BorderPane paneMap2 = new BorderPane(uiMap2, null, null, uiCoordBar2, null);
    private final Canvas2DSynchronizer synchronizer = new Canvas2DSynchronizer();

    private Session session;
    
    public FXMapPane() {
        session = Injector.getSession();
        context = session.getMapContext();
        
        uiCoordBar2.setCrsButtonVisible(false);
        uiMap1.getContainer().setContext(context);
        uiMap2.getContainer().setContext(context);
        uiMap1.getCanvas().setBackgroundPainter(new SolidColorPainter(Color.WHITE));
        uiMap2.getCanvas().setBackgroundPainter(new SolidColorPainter(Color.WHITE));
        synchronizer.addCanvas(uiMap1.getCanvas(),true,true);
        synchronizer.addCanvas(uiMap2.getCanvas(),true,true);
        
        uiCtxBar = new FXContextBar(uiMap1);
        uiAddBar = new FXAddDataBar(uiMap1,true);
        uiNavBar = new FXNavigationBar(uiMap1);
        uiToolBar = new FXGeoToolBar(uiMap1);
        uiEditBar = new FXTronconEditBar(uiMap1);
        uiCoordBar1.setScaleBoxValues(new Long[]{200l,5000l,25000l,50000l});
        uiCoordBar2.setScaleBoxValues(new Long[]{200l,5000l,25000l,50000l});
        uiTree = new FXMapContextTree();
        uiTree.getTreetable().setShowRoot(false);
        uiTree.getMenuItems().add(new OpacityItem());
        uiTree.getMenuItems().add(new SeparatorMenuItem());
        uiTree.getMenuItems().add(new LayerPropertiesItem(uiMap1));
        uiTree.getMenuItems().add(new SeparatorMenuItem());
        uiTree.getMenuItems().add(new ZoomToItem(uiMap1));
        uiTree.getMenuItems().add(new ExportMenu());
        uiTree.getMenuItems().add(new DeleteItem());
        uiTree.getTreetable().getColumns().add(2,new MapItemFilterColumn());
        uiTree.getTreetable().getColumns().add(3,new MapItemSelectableColumn());
        final Property<MapContext> prop1 = FXUtilities.beanProperty(uiMap1.getContainer(),ContextContainer2D.CONTEXT_PROPERTY, MapContext.class);
        uiTree.mapItemProperty().bind(prop1);
        prop1.addListener(new ChangeListener<MapContext>() {
            @Override
            public void changed(ObservableValue<? extends MapContext> observable, MapContext oldValue, MapContext newValue) {
                uiMap2.getContainer().setContext(newValue);
            }
        });
        
        splitButton.setOnAction((ActionEvent event) -> {
            if(mapsplit.getItems().contains(paneMap2)){
                mapsplit.getItems().remove(paneMap2);
                splitButton.setTooltip(new Tooltip("Afficher la deuxième carte"));
            }else{
                mapsplit.setDividerPositions(0.5);
                mapsplit.getItems().add(paneMap2);
                splitButton.setTooltip(new Tooltip("Cacher la deuxième carte"));
            }
        });
        
        
        final GridPane topgrid = new GridPane();
        uiCtxBar.setMaxHeight(Double.MAX_VALUE);
        uiAddBar.setMaxHeight(Double.MAX_VALUE);
        uiNavBar.setMaxHeight(Double.MAX_VALUE);
        uiToolBar.setMaxHeight(Double.MAX_VALUE);
        uiEditBar.setMaxHeight(Double.MAX_VALUE);
        uiSplitBar.setMaxHeight(Double.MAX_VALUE);
        topgrid.add(uiCtxBar,  0, 0);
        topgrid.add(uiAddBar,  1, 0);
        topgrid.add(uiNavBar,  2, 0);
        topgrid.add(uiToolBar, 3, 0);
        topgrid.add(uiEditBar, 4, 0);
        topgrid.add(uiSplitBar, 5, 0);
        
        final ColumnConstraints col0 = new ColumnConstraints();
        final ColumnConstraints col1 = new ColumnConstraints();
        final ColumnConstraints col2 = new ColumnConstraints();
        final ColumnConstraints col3 = new ColumnConstraints();
        final ColumnConstraints col4 = new ColumnConstraints();
        final ColumnConstraints col5 = new ColumnConstraints();
        final ColumnConstraints col6 = new ColumnConstraints();
        col4.setHgrow(Priority.ALWAYS);
        final RowConstraints row0 = new RowConstraints();
        row0.setVgrow(Priority.ALWAYS);
        topgrid.getColumnConstraints().addAll(col0,col1,col2,col3,col4,col5,col6);
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
        
        //ajout des ecouteurs souris sur click droit
        uiMap1.addEventHandler(MouseEvent.MOUSE_CLICKED, new MapActionHandler(uiMap1));
        uiMap2.addEventHandler(MouseEvent.MOUSE_CLICKED, new MapActionHandler(uiMap2));
        
        
        //deplacer à la date du jour
        new Thread(){
            @Override
            public void run() {
                final Date time = new Date();
                try {
                    uiMap1.getCanvas().setObjectiveCRS(SirsCore.getEpsgCode());
                    uiMap1.getCanvas().setVisibleArea(session.getMapContext().getAreaOfInterest());
                    uiMap1.getCanvas().setTemporalRange(time,time);
                    uiMap2.getCanvas().setTemporalRange(time,time);
                    uiCoordBar1.getSliderview().moveTo(time.getTime() - TemporalConstants.DAY_MS*8);
                    uiCoordBar2.getSliderview().moveTo(time.getTime() - TemporalConstants.DAY_MS*8);
                } catch (NoninvertibleTransformException | TransformException ex) {
                    SIRS.LOGGER.log(Level.WARNING, ex.getMessage(),ex);
                }
            }
        }.start();
    }

    public MapContext getMapContext() {
        return context;
    }
    
    public void setMapContext(final MapContext mapContext){
        context = mapContext;
        uiMap1.getContainer().setContext(context);
        uiMap2.getContainer().setContext(context);
        uiTree.setMapItem(mapContext);
    }

    public FXMap getUiMap() {
        return uiMap1;
    }

    public void focusOnElement(Element target) {
        if (context == null) return;
        
        final MapLayer container = getMapLayerForElement(target.getClass());
        if (!(container instanceof FeatureMapLayer)) return;

        final FeatureMapLayer fLayer = (FeatureMapLayer) container;
        
        final Id idFilter = GO2Utilities.FILTER_FACTORY.id(
                Collections.singleton(new DefaultFeatureId(target.getId())));
        fLayer.setSelectionFilter(idFilter);
        fLayer.setVisible(true);
        try {
            final FeatureType fType = fLayer.getCollection().getFeatureType();
            final GenericName typeName = fType.getName();
            QueryBuilder queryBuilder = new QueryBuilder(
                    new DefaultName(typeName.scope().toString(), typeName.head().toString()));
            queryBuilder.setFilter(idFilter);
            GeometryDescriptor geomDescriptor = fType.getGeometryDescriptor();
            if (geomDescriptor != null) {
                queryBuilder.setProperties(new Name[]{geomDescriptor.getName()});
            }
            FeatureCollection<? extends Feature> subCollection = 
                    fLayer.getCollection().subCollection(queryBuilder.buildQuery());
            Envelope selectionEnvelope = pseudoBuffer(subCollection.getEnvelope());
            uiMap1.getCanvas().setVisibleArea(selectionEnvelope);
        } catch (Exception e) {
            SIRS.LOGGER.log(Level.WARNING, "Error on zoom at layer selection.", e);
        }
    }

    /**
     * Try to expand a little an envelope. Main purpose is to ensure we won't 
     * have an envelope which is merely a point.
     * @param input The input to expand.
     * @return An expanded envelope. If we cannot analyze CRS or it's unit on
     * horizontal axis, the same envelope is returned.
     */
    private Envelope pseudoBuffer(final Envelope input) {
        double additionalDistance = 0.01;
        if (input.getCoordinateReferenceSystem() != null) {
            CoordinateReferenceSystem crs = input.getCoordinateReferenceSystem();
            int firstAxis = CRSUtilities.firstHorizontalAxis(crs);
            
            if (firstAxis >=0) {
                Unit unit = crs.getCoordinateSystem().getAxis(firstAxis).getUnit();
                if (unit != null && SI.METRE.isCompatible(unit)) {
                    additionalDistance = SI.METRE.getConverterTo(unit).convert(1);
                }
                
                final GeneralEnvelope result = new GeneralEnvelope(input);
                result.setRange(firstAxis, 
                        result.getLower(firstAxis)-additionalDistance, 
                        result.getUpper(firstAxis)+additionalDistance);
                final int secondAxis = firstAxis +1;
                result.setRange(secondAxis, 
                        result.getLower(secondAxis)-additionalDistance, 
                        result.getUpper(secondAxis)+additionalDistance);
                return result;
            }
        }
        return input;
    }
    
    /** 
     * Try to get the map layer which contains {@link Element}s of given class.
     * @param elementClass The particular class of element we want to retrieve on map.
     * @return The Map layer in which are contained elements of input type, or null.
     */
    public MapLayer getMapLayerForElement(Class elementClass) {
        if (TronconDigue.class.isAssignableFrom(elementClass)) {
            return getMapLayerForElement(CorePlugin.TRONCON_LAYER_NAME);
        } else if (BorneDigue.class.isAssignableFrom(elementClass)) {
            return getMapLayerForElement(CorePlugin.BORNE_LAYER_NAME);
        } else {
            final LabelMapper mapper = new LabelMapper(elementClass);
            return getMapLayerForElement(mapper.mapClassName());
        }
    }
    
    /** 
     * Try to get the map layer using its name.
     * @param layerName Identifier of the map layer to retrieve
     * @return The matching map layer, or null.
     */
    public MapLayer getMapLayerForElement(String layerName) {
        if (context == null) return null;
        for (MapLayer layer : context.layers()) {
            if (layer.getName().equalsIgnoreCase(layerName)) {
                return layer;
            }
        }
        return null;
    }
            
}
