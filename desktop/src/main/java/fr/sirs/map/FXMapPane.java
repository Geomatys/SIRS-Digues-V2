
package fr.sirs.map;

import fr.sirs.CorePlugin;
import org.geotoolkit.display2d.Canvas2DSynchronizer;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import fr.sirs.core.SirsCore;
import fr.sirs.core.component.PreviewLabelRepository;
import org.geotoolkit.gui.javafx.util.TaskManager;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.PositionDocument;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.PreviewLabel;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.map.style.FXStyleClassifSinglePane;
import java.awt.Color;
import java.awt.RenderingHints;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
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
import org.apache.sis.util.ArgumentChecks;
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
import org.geotoolkit.gui.javafx.contexttree.MapItemGlyphColumn;
import org.geotoolkit.gui.javafx.contexttree.MapItemNameColumn;
import org.geotoolkit.gui.javafx.contexttree.MapItemSelectableColumn;
import org.geotoolkit.gui.javafx.contexttree.MapItemVisibleColumn;
import org.geotoolkit.gui.javafx.contexttree.menu.EmptySelectionItem;
import org.geotoolkit.gui.javafx.contexttree.menu.OpacityItem;
import org.geotoolkit.gui.javafx.contexttree.menu.ZoomToItem;
import org.geotoolkit.gui.javafx.layer.FXLayerStylesPane;
import org.geotoolkit.gui.javafx.layer.FXPropertiesPane;
import org.geotoolkit.gui.javafx.layer.style.FXStyleAdvancedPane;
import org.geotoolkit.gui.javafx.layer.style.FXStyleClassifRangePane;
import org.geotoolkit.gui.javafx.layer.style.FXStyleColorMapPane;
import org.geotoolkit.gui.javafx.layer.style.FXStyleSimplePane;
import org.geotoolkit.gui.javafx.layer.style.FXStyleXMLPane;
import org.geotoolkit.gui.javafx.render2d.FXAddDataBar;
import org.geotoolkit.gui.javafx.render2d.FXContextBar;
import org.geotoolkit.gui.javafx.render2d.FXCoordinateBar;
import org.geotoolkit.gui.javafx.render2d.FXGeoToolBar;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXNavigationBar;
import org.geotoolkit.gui.javafx.render2d.navigation.FXPanHandler;
import org.geotoolkit.gui.javafx.util.FXUtilities;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.temporal.object.TemporalConstants;
import org.opengis.filter.Id;
import org.opengis.geometry.Envelope;
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
    
    public FXMapPane() {
        
        uiCoordBar2.setCrsButtonVisible(false);
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
        uiTree.getTreetable().getColumns().clear();
        uiTree.getTreetable().getColumns().add(new MapItemNameColumn());
        uiTree.getTreetable().getColumns().add(new MapItemGlyphColumn(){
            @Override
            protected FXPropertiesPane createEditor(MapLayer candidate) {
                return new FXPropertiesPane(
                    candidate,
                    new FXLayerStylesPane(
                            new FXStyleSimplePane(),
                            new FXStyleColorMapPane(),
                            new FXStyleClassifSinglePane(),
                            new FXStyleClassifRangePane(),
                            new FXStyleAdvancedPane(),
                            new FXStyleXMLPane()
                    )
                );
            }
        });
        uiTree.getTreetable().getColumns().add(new MapItemVisibleColumn());

        uiTree.getTreetable().setShowRoot(false);
        uiTree.getMenuItems().add(new OpacityItem());
        uiTree.getMenuItems().add(new SeparatorMenuItem());
        uiTree.getMenuItems().add(new EmptySelectionItem());
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
        
        //Affiche le contexte carto et le déplace à la date du jour
        TaskManager.INSTANCE.submit("Initialisation de la carte", () -> {
            
            final MapContext context = Injector.getSession().getMapContext();
            
            // Affect context and bounds in a JavaFX thread, because it will affect UI (Note : it should not, and may be fixed in a future version).
            final Task t = new Task() {
                @Override
                protected Object call() throws Exception {
                    uiMap1.getContainer().setContext(context);
                    final Date time = new Date();
                    uiMap1.getCanvas().setObjectiveCRS(SirsCore.getEpsgCode());
                    uiMap1.getCanvas().setVisibleArea(context.getAreaOfInterest());
                    uiMap1.getCanvas().setTemporalRange(time, time);
                    uiMap2.getCanvas().setTemporalRange(time, time);
                    uiCoordBar1.getSliderview().moveTo(time.getTime() - TemporalConstants.DAY_MS * 8);
                    uiCoordBar2.getSliderview().moveTo(time.getTime() - TemporalConstants.DAY_MS * 8);
                    return null;
                }
            };
            Platform.runLater(t);
            return null;
        });
    }

    public FXMap getUiMap() {
        return uiMap1;
    }

    /**
     * Ask map to display valid element at specified date.
     * @param newTime Date to focus on map.
     */
    public void setTemporalRange(final Date newTime) throws TransformException {
        uiMap1.getCanvas().setTemporalRange(newTime, newTime);
        uiCoordBar1.getSliderview().moveTo(newTime.getTime() - TemporalConstants.DAY_MS * 8);
    }
    
    public void focusOnElement(Element target) {
        TaskManager.INSTANCE.submit(new FocusOnMap(target));
    }
    
    /** 
     * Try to get the map layer which contains {@link Element}s of given class.
     * @param elementClass The particular class of element we want to retrieve on map.
     * @return The Map layer in which are contained elements of input type, or null.
     */
    private MapLayer getMapLayerForElement(Element element) {
        if (element instanceof TronconDigue) {
            return getMapLayerForElement(CorePlugin.TRONCON_LAYER_NAME);
        } else if (element instanceof BorneDigue) {
            return getMapLayerForElement(CorePlugin.BORNE_LAYER_NAME);
        } else if (element instanceof PositionDocument) {
            final PreviewLabelRepository previewLabelRepository = Injector.getSession().getPreviewLabelRepository();
            final String documentId = ((PositionDocument)element).getSirsdocument();
            final PreviewLabel previewLabel = previewLabelRepository.get(documentId);
            Class documentClass = null; 
            try {
                documentClass = Class.forName(previewLabel.getType());
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(FXMapPane.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            final LabelMapper mapper = new LabelMapper(documentClass);
            return getMapLayerForElement(mapper.mapClassName());
        } else {
            final LabelMapper mapper = new LabelMapper(element.getClass());
            return getMapLayerForElement(mapper.mapClassName());
        }
    }
    
    /** 
     * Try to get the map layer using its name.
     * @param layerName Identifier of the map layer to retrieve
     * @return The matching map layer, or null.
     */
    private MapLayer getMapLayerForElement(String layerName) {
        final MapContext context = Injector.getSession().getMapContext();
        if (context == null) return null;
        for (MapLayer layer : context.layers()) {
            if (layer.getName().equalsIgnoreCase(layerName)) {
                return layer;
            }
        }
        return null;
    }
    
    /**
     * A task which select and zoom on given element on the map.
     * Task returns false if the element cannot be focused on.
     */
    private class FocusOnMap extends Task<Boolean> {
        
        final Element toFocusOn;

        public FocusOnMap(final Element toFocusOn) {
            ArgumentChecks.ensureNonNull("Element to focus on", toFocusOn);
            this.toFocusOn = toFocusOn;
            
            updateTitle("Recherche un élément sur la carte");
        }
        
        @Override
        protected Boolean call() throws Exception {
            final int maxProgress = 3;
            int currentProgress = 0;
            
            updateProgress(currentProgress++, maxProgress);
            updateMessage("Recherche de la couche correspondante");
            
            final MapLayer container = getMapLayerForElement(toFocusOn);
            if (!(container instanceof FeatureMapLayer)) {
                return false;
            }

            final FeatureMapLayer fLayer = (FeatureMapLayer) container;

            updateProgress(currentProgress++, maxProgress);
            updateMessage("Filtrage sur l'élément");
            
            final Id idFilter = GO2Utilities.FILTER_FACTORY.id(
                    Collections.singleton(new DefaultFeatureId(toFocusOn.getId())));
            fLayer.setSelectionFilter(idFilter);
            fLayer.setVisible(true);
            
            updateProgress(currentProgress++, maxProgress);
            updateMessage("Calcul de la zone à afficher");
            
            final FeatureType fType = fLayer.getCollection().getFeatureType();
            final GenericName typeName = fType.getName();
            QueryBuilder queryBuilder = new QueryBuilder(
                    new DefaultName(typeName.scope().toString(), typeName.head().toString()));
            queryBuilder.setFilter(idFilter);
            GeometryDescriptor geomDescriptor = fType.getGeometryDescriptor();
            if (geomDescriptor != null) {
                queryBuilder.setProperties(new Name[]{geomDescriptor.getName()});
            } else {
                return false; // no zoom possible
            }
            FeatureCollection<? extends Feature> subCollection
                    = fLayer.getCollection().subCollection(queryBuilder.buildQuery());
            
            Envelope tmpEnvelope = subCollection.getEnvelope();
            if (tmpEnvelope == null) {
                return false;
            }
            final Envelope selectionEnvelope = SIRS.pseudoBuffer(tmpEnvelope);
            
            updateProgress(currentProgress++, maxProgress);
            updateMessage("Mise à jour de l'affichage");
            
            final TaskManager.MockTask displayUpdate = new TaskManager.MockTask(() -> {
                    uiMap1.getCanvas().setVisibleArea(selectionEnvelope);
                    return null;
            });
            
            Platform.runLater(displayUpdate);
            displayUpdate.get();

            return true;
        }
    }
            
}
