package fr.sirs.map;

import com.vividsolutions.jts.geom.Geometry;
import fr.sirs.CorePlugin;
import fr.sirs.FXMainFrame;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.geotoolkit.display2d.Canvas2DSynchronizer;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import fr.sirs.Plugin;
import fr.sirs.Plugins;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.AbstractPositionDocument;
import fr.sirs.core.model.AbstractPositionDocumentAssociable;
import fr.sirs.core.model.AvecBornesTemporelles;
import fr.sirs.core.model.AvecGeometrie;
import org.geotoolkit.gui.javafx.util.TaskManager;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.PositionProfilTravers;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.map.style.FXStyleAggregatedPane;
import fr.sirs.ui.Growl;
import java.awt.Color;
import java.awt.RenderingHints;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
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
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import org.apache.sis.measure.NumberRange;
import org.apache.sis.util.ArgumentChecks;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.display2d.GO2Hints;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.display2d.canvas.painter.SolidColorPainter;
import org.geotoolkit.display2d.container.ContextContainer2D;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.feature.type.FeatureType;
import org.geotoolkit.feature.type.GeometryDescriptor;
import org.geotoolkit.feature.type.NamesExt;
import org.geotoolkit.filter.identity.DefaultFeatureId;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.geotoolkit.gui.javafx.contexttree.FXMapContextTree;
import org.geotoolkit.gui.javafx.contexttree.MapItemFilterColumn;
import org.geotoolkit.gui.javafx.contexttree.MapItemGlyphColumn;
import org.geotoolkit.gui.javafx.contexttree.MapItemNameColumn;
import org.geotoolkit.gui.javafx.contexttree.MapItemSelectableColumn;
import org.geotoolkit.gui.javafx.contexttree.MapItemVisibleColumn;
import org.geotoolkit.gui.javafx.contexttree.menu.OpacityItem;
import org.geotoolkit.gui.javafx.contexttree.menu.ZoomToItem;
import org.geotoolkit.gui.javafx.render2d.FXContextBar;
import org.geotoolkit.gui.javafx.render2d.FXCoordinateBar;
import org.geotoolkit.gui.javafx.render2d.FXGeoToolBar;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXNavigationBar;
import org.geotoolkit.gui.javafx.render2d.FXScaleBarDecoration;
import org.geotoolkit.gui.javafx.render2d.navigation.FXPanHandler;
import org.geotoolkit.gui.javafx.util.FXUtilities;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapLayer;
import org.opengis.filter.Id;
import org.opengis.geometry.Envelope;
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
        
    private final FXMap uiMap1 = new FXMap(false, MAPHINTS);
    private final FXCoordinateBar uiCoordBar1 = new FXCoordinateBar(uiMap1);
    private final BorderPane paneMap1 = new BorderPane(uiMap1, null, null, uiCoordBar1, null);
    private final FXMap uiMap2 = new FXMap(false, MAPHINTS);
    private final FXCoordinateBar uiCoordBar2 = new FXCoordinateBar(uiMap2);
    private final BorderPane paneMap2 = new BorderPane(uiMap2, null, null, uiCoordBar2, null);
    private final Canvas2DSynchronizer synchronizer = new Canvas2DSynchronizer();
    
    private final SplitPane mapsplit = new SplitPane();
    
    private final FXContextBar uiCtxBar;
    private final SIRSAddDataBar uiAddBar;
    private final FXNavigationBar uiNavBar;
    private final FXGeoToolBar uiToolBar;
    private final FXTronconEditBar uiEditBar;
    private final Button splitButton = new Button(null, new ImageView(ICON_SPLIT));
    private final ToolBar uiSplitBar = new ToolBar(splitButton);
    
    private final FXMapContextTree uiTree;
    
    public FXMapPane() {
        
        uiCoordBar2.setCrsButtonVisible(false);
        uiMap1.getCanvas().setBackgroundPainter(new SolidColorPainter(Color.WHITE));
        uiMap2.getCanvas().setBackgroundPainter(new SolidColorPainter(Color.WHITE));
        uiMap1.addDecoration(new FXScaleBarDecoration());
        uiMap2.addDecoration(new FXScaleBarDecoration());
        uiCoordBar1.setScaleBoxValues(new Long[]{200l,5000l,25000l,50000l});
        uiCoordBar2.setScaleBoxValues(new Long[]{200l,5000l,25000l,50000l});
        synchronizer.addCanvas(uiMap1.getCanvas(),true,true);
        synchronizer.addCanvas(uiMap2.getCanvas(),true,true);
        
        uiCtxBar = new FXContextBar(uiMap1);
        uiAddBar = new SIRSAddDataBar(uiMap1);
        uiNavBar = new FXNavigationBar(uiMap1);
        uiToolBar = new FXGeoToolBar(uiMap1);
        uiEditBar = new FXTronconEditBar(uiMap1);
        
        uiTree = new FXMapContextTree();
        uiTree.getTreetable().getColumns().clear();
        uiTree.getTreetable().getColumns().add(new MapItemNameColumn());
        uiTree.getTreetable().getColumns().add(new MapItemGlyphColumn(){
            @Override
            protected Pane createEditor(MapLayer candidate) {
                final FXStyleAggregatedPane pane = new FXStyleAggregatedPane();
                pane.init(candidate);
                return pane;
            }

            @Override
            protected void openEditor(MapLayer candidate) {
                final Pane panel = createEditor(candidate);
                panel.setPrefSize(900, 700);

                final Stage dialog = new Stage();
                dialog.getIcons().add(SIRS.ICON);
                dialog.setTitle(GeotkFX.getString("org.geotoolkit.gui.javafx.contexttree.MapItemGlyphColumn.dialogTitle") + candidate.getName());
                dialog.setResizable(true);
                dialog.initModality(Modality.NONE);
                dialog.initOwner(null);

                final Button cancelBtn = new Button(GeotkFX.getString("org.geotoolkit.gui.javafx.contexttree.menu.LayerPropertiesItem.close"));
                cancelBtn.setCancelButton(true);

                final ButtonBar bbar = new ButtonBar();
                bbar.setPadding(new Insets(5, 5, 5, 5));
                bbar.getButtons().addAll(cancelBtn);

                final BorderPane dialogContent = new BorderPane();
                dialogContent.setCenter(panel);
                dialogContent.setBottom(bbar);
                dialog.setScene(new Scene(dialogContent));

                cancelBtn.setOnAction((ActionEvent e) -> {
                    dialog.close();
                });

                dialog.show();
            }
        });
        uiTree.getTreetable().getColumns().add(new MapItemVisibleColumn());

        uiTree.getTreetable().setShowRoot(false);
        uiTree.getMenuItems().add(new OpacityItem());
        uiTree.getMenuItems().add(new SeparatorMenuItem());
        uiTree.getMenuItems().add(new EmptySelectionItem());
        uiTree.getMenuItems().add(new SeparatorMenuItem());
        uiTree.getMenuItems().add(new ZoomToItem(uiMap1));
        uiTree.getMenuItems().add(new StructureMenuItem());
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
            } else{
                mapsplit.setDividerPositions(0.5);
                mapsplit.getItems().add(paneMap2);
                splitButton.setTooltip(new Tooltip("Cacher la deuxième carte"));
            }
        });
        
        uiCtxBar.setMaxHeight(Double.MAX_VALUE);
        uiAddBar.setMaxHeight(Double.MAX_VALUE);
        uiNavBar.setMaxHeight(Double.MAX_VALUE);
        uiToolBar.setMaxHeight(Double.MAX_VALUE);
        uiEditBar.setMaxHeight(Double.MAX_VALUE);
        uiSplitBar.setMaxHeight(Double.MAX_VALUE);
        
        uiCtxBar.setBackground(Background.EMPTY);
        uiAddBar.setBackground(Background.EMPTY);
        uiNavBar.setBackground(Background.EMPTY);
        uiToolBar.setBackground(Background.EMPTY);
        uiEditBar.setBackground(Background.EMPTY);


        final BorderPane topgrid = new BorderPane();
        topgrid.setPadding(Insets.EMPTY);
        final FlowPane flowPane = new FlowPane(Orientation.HORIZONTAL,uiCtxBar,uiAddBar,uiNavBar,uiToolBar,uiEditBar);
        flowPane.setPadding(Insets.EMPTY);
        flowPane.setHgap(0);
        flowPane.setVgap(0);
        flowPane.setPrefWrapLength(Double.MAX_VALUE);
        flowPane.setMaxWidth(Double.MAX_VALUE);
        flowPane.getStyleClass().add("tool-bar");

        //add plugin toolbars
        final FXMainFrame frame = Injector.getSession().getFrame();
        frame.activePluginProperty().addListener(new ChangeListener<Plugin>() {
            @Override
            public void changed(ObservableValue<? extends Plugin> observable, Plugin oldValue, Plugin newValue) {
                flowPane.getChildren().clear();
                flowPane.getChildren().addAll(uiCtxBar,uiAddBar,uiNavBar,uiToolBar,uiEditBar);
                final List<ToolBar> toolbars = newValue.getMapToolBars(FXMapPane.this);
                if(toolbars!=null){
                    for(ToolBar t : toolbars){
                        t.setMaxHeight(Double.MAX_VALUE);
                        t.setBackground(Background.EMPTY);
                        flowPane.getChildren().add(t);
                    }
                }
            }
        });

        topgrid.setCenter(flowPane);
        topgrid.setRight(uiSplitBar);

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

        uiMap1.setHandler(new FXPanHandler(false));
        uiMap2.setHandler(new FXPanHandler(false));

        //ajout des ecouteurs souris sur click droit
        uiMap1.addEventHandler(MouseEvent.MOUSE_CLICKED, new MapActionHandler(uiMap1));
        uiMap2.addEventHandler(MouseEvent.MOUSE_CLICKED, new MapActionHandler(uiMap2));

        //Affiche le contexte carto et le déplace à la date du jour
        TaskManager.INSTANCE.submit("Initialisation de la carte", () -> {

            final MapContext context = Injector.getSession().getMapContext();
                        final Task t = new Task() {
                @Override
                protected Object call() throws Exception {
                    uiMap1.getContainer().setContext(context);
                    uiMap1.getCanvas().setObjectiveCRS(Injector.getSession().getProjection());
                    uiMap1.getCanvas().setVisibleArea(context.getAreaOfInterest());
                    setTemporalRange(LocalDate.now(), null);
                    return null;
                }
            };

        if (Platform.isFxApplicationThread()) {
            t.run();
        } else {
            Platform.runLater(t);
        }

            return null;
        });
    }

    /**
     * Déplace la temporalité de la carte sélectionnée sur la date demandée.
     * @param ldt La Date pour la carte. Ne doit pas être nulle.
     * @param map La carte à mettre à jour. Nulle pour mettre à jour les deux cartes 
     * par défaut.
     */
    public void setTemporalRange(final LocalDate ldt, FXMap map) {
        final Task t = new Task() {
            @Override
            protected Object call() throws Exception {
                if (map == null) {
                    uiCoordBar1.getDateField().valueProperty().setValue(ldt);
                } else {
                    if (uiMap1.equals(map)) {
                        uiCoordBar1.getDateField().valueProperty().setValue(ldt);
                    } else if (uiMap2.equals(map)) {
                        uiCoordBar2.getDateField().valueProperty().setValue(ldt);
                    }
                }
                return null;
            }
        };

        if (Platform.isFxApplicationThread()) {
            t.run();
        } else {
            Platform.runLater(t);
        }
    }
    
    public FXMap getUiMap() {
        return uiMap1;
    }
    
    public void focusOnElement(Element target) {
        TaskManager.INSTANCE.submit(new FocusOnMap(target));
    }
    
    /** 
     * Try to get the map layer which contains {@link Element}s of given class.
     * @param element The element we want to retrieve on map.
     * @return The Map layer in which are contained elements of input type, or null.
     */
    private MapLayer getMapLayerForElement(Element element) {
        if (element.getClass().equals(TronconDigue.class)) {
            return getMapLayerForElement(CorePlugin.TRONCON_LAYER_NAME);
        } else if (element instanceof BorneDigue) {
            return getMapLayerForElement(CorePlugin.BORNE_LAYER_NAME);
        } else if (element instanceof AbstractPositionDocumentAssociable) {
            final Previews previews = Injector.getSession().getPreviews();
            final String documentId = ((AbstractPositionDocumentAssociable) element).getSirsdocument(); // IL est nécessaire qu'un document soit associé pour déterminer le type de la couche.
            final Preview previewLabel = previews.get(documentId);
            Class documentClass = null;
            try {
                documentClass = Class.forName(previewLabel.getElementClass(), true, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException ex) {
                SIRS.LOGGER.log(Level.WARNING, null, ex);
            }

            final LabelMapper mapper = LabelMapper.get(documentClass);
            return getMapLayerForElement(mapper.mapClassName());

        } else {
            final LabelMapper mapper = LabelMapper.get(element.getClass());
            final MapLayer foundLayer = getMapLayerForElement(mapper.mapClassName());
            if (foundLayer == null) {
                return getMapLayerForElement(mapper.mapClassNamePlural());
            } else {
                return foundLayer;
            }
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

                final Growl growlInfo = new Growl(Growl.Type.WARNING, "L'objet n'est présent dans aucune couche cartographique.");
                Platform.runLater(growlInfo::showAndFade);

                if(toFocusOn instanceof AvecGeometrie){
                    Geometry geom = ((AvecGeometrie)toFocusOn).getGeometry();
                    if(geom!=null){
                        final JTSEnvelope2D env = JTS.toEnvelope(geom);
                        final Envelope selectionEnvelope = SIRS.pseudoBuffer(env);
                        final TaskManager.MockTask displayUpdate = new TaskManager.MockTask(() -> {
                                uiMap1.getCanvas().setVisibleArea(selectionEnvelope);
                                return null;
                        });
                        Platform.runLater(displayUpdate);
                        displayUpdate.get();
                    }
                }

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


            // Envelope spatiale
            final FeatureType fType = fLayer.getCollection().getFeatureType();
            final GenericName typeName = fType.getName();
            QueryBuilder queryBuilder = new QueryBuilder(
                    NamesExt.create(typeName.scope().toString(), typeName.head().toString()));
            queryBuilder.setFilter(idFilter);
            GeometryDescriptor geomDescriptor = fType.getGeometryDescriptor();
            if (geomDescriptor != null) {
                queryBuilder.setProperties(new GenericName[]{geomDescriptor.getName()});
            } else {
                return false; // no zoom possible
            }
            FeatureCollection subCollection =
                    fLayer.getCollection().subCollection(queryBuilder.buildQuery());
            
            Envelope tmpEnvelope = subCollection.getEnvelope();
            if (tmpEnvelope == null) {
                return false;
            }
            final Envelope selectionEnvelope = SIRS.pseudoBuffer(tmpEnvelope);
            
            // Envelope temporelle
            final LocalDateTime selectionTime;
            if (toFocusOn instanceof AvecBornesTemporelles) {
                
                final AvecBornesTemporelles abtToFocusOn = (AvecBornesTemporelles) toFocusOn;
                long minTime = Long.MIN_VALUE;
                long maxTime = Long.MAX_VALUE;
                
                LocalDateTime tmpTime = abtToFocusOn.getDate_debut()==null ? null : abtToFocusOn.getDate_debut().atTime(LocalTime.MIDNIGHT);
                if (tmpTime != null) {
                    minTime = Timestamp.valueOf(tmpTime).getTime();
                }
                
                tmpTime = abtToFocusOn.getDate_fin()==null ? null : abtToFocusOn.getDate_fin().atTime(LocalTime.MIDNIGHT);
                if (tmpTime != null) {
                    maxTime = Timestamp.valueOf(tmpTime).getTime();
                }

                final NumberRange<Long> elementRange = NumberRange.create(minTime, true, maxTime, true);

                minTime = Long.MIN_VALUE;
                maxTime = Long.MAX_VALUE;
                Date[] temporalRange = uiMap1.getCanvas().getTemporalRange();
                if (temporalRange != null && temporalRange.length > 0) {
                    minTime = temporalRange[0].getTime();
                    if (temporalRange.length > 1) {
                        maxTime = temporalRange[1].getTime();
                    }
                }

                final NumberRange<Long> mapRange = NumberRange.create(minTime, true, maxTime, true);

                // If map temporal envelope does not intersect our element, we must 
                // change it.
                if (!mapRange.intersects(elementRange))
                    selectionTime = new Timestamp(elementRange.getMinValue()).toLocalDateTime();
                else 
                    selectionTime = null;
            } else {
                selectionTime = null;
            }
            
            updateProgress(currentProgress++, maxProgress);
            updateMessage("Mise à jour de l'affichage");
            
            final TaskManager.MockTask displayUpdate = new TaskManager.MockTask(() -> {
                    uiMap1.getCanvas().setVisibleArea(selectionEnvelope);
                    if (selectionTime != null) {
                        setTemporalRange(selectionTime.toLocalDate(), uiMap1);
                    }
                    return null;
            });
            
            Platform.runLater(displayUpdate);
            displayUpdate.get();

            return true;
        }
    }
            
}
