
package fr.sirs.plugin.berge.map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.model.TraitBerge;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.plugin.berge.PluginBerge;
import java.beans.PropertyChangeEvent;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import org.geotoolkit.cql.CQLException;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.display2d.container.ContextContainer2D;
import org.geotoolkit.filter.identity.DefaultFeatureId;
import org.geotoolkit.gui.javafx.render2d.AbstractNavigationHandler;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXPanMouseListen;
import org.geotoolkit.gui.javafx.render2d.edition.EditionHelper;
import org.geotoolkit.gui.javafx.render2d.shape.FXGeometryLayer;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.ItemListener;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.collection.CollectionChangeEvent;
import org.opengis.filter.Id;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class TraitBergeEditHandler extends AbstractNavigationHandler implements ItemListener {

    private static final int CROSS_SIZE = 5;

    private final MouseListen mouseInputListener;
    private final FXGeometryLayer geomlayer= new FXGeometryLayer(){
        @Override
        protected Node createVerticeNode(Coordinate c, boolean selected){
            final Line h = new Line(c.x-CROSS_SIZE, c.y, c.x+CROSS_SIZE, c.y);
            final Line v = new Line(c.x, c.y-CROSS_SIZE, c.x, c.y+CROSS_SIZE);
            h.setStroke(Color.RED);
            v.setStroke(Color.RED);
            return new Group(h,v);
        }
    };

    //edition variables
    private FeatureMapLayer traitbergeLayer;
    private final ObjectProperty<TraitBerge> traitbergeProperty = new SimpleObjectProperty<>();
    private EditionHelper helper;
    private final EditionHelper.EditionGeometry editGeometry = new EditionHelper.EditionGeometry();
    private final Session session;

    private Id selectionFilter;

    // overriden variable by init();
    protected String layerName;
    protected MutableStyle style;
    protected String typeName;
    protected boolean maleGender;


    protected void init() {
        this.layerName = PluginBerge.LAYER_TRAIT_NAME;
        try {
            this.style = PluginBerge.createBergeStyle();
        } catch (URISyntaxException | CQLException ex) {
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
        this.typeName = "berge";
        this.maleGender = false;
    }


    /**
     * Constructor called directly by sub-classes
     * @param map
     */
    public TraitBergeEditHandler(final FXMap map) {
        super();
        init();
        mouseInputListener = new MouseListen(typeName);

        session = Injector.getSession();
        traitbergeProperty.addListener((ObservableValue<? extends TraitBerge> observable, TraitBerge oldValue, TraitBerge newValue) -> {

            editGeometry.reset();
            if (traitbergeProperty.get() != null) {
                editGeometry.geometry.set((Geometry) traitbergeProperty.get().getGeometry().clone());
            }
            updateGeometry();

            if (newValue != null) {
                selectionFilter = GO2Utilities.FILTER_FACTORY.id(
                        Collections.singleton(new DefaultFeatureId(newValue.getId())));
            } else {
                selectionFilter = null;
            }
            if (Platform.isFxApplicationThread()) {
                traitbergeLayer.setSelectionFilter(selectionFilter);
            } else {
                Platform.runLater(() -> traitbergeLayer.setSelectionFilter(selectionFilter));
            }
        });
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void install(final FXMap component) {
        super.install(component);
        component.addEventHandler(MouseEvent.ANY, mouseInputListener);
        component.addEventHandler(ScrollEvent.ANY, mouseInputListener);
        map.setCursor(Cursor.CROSSHAIR);
        map.addDecoration(0,geomlayer);

        //recuperation du layer de trait de berge
        traitbergeLayer = null;
        traitbergeProperty.set(null);
        final ContextContainer2D cc = (ContextContainer2D) map.getCanvas().getContainer();
        final MapContext context = cc.getContext();
        for(MapLayer layer : context.layers()){
            layer.setSelectable(false);
            if(layer.getName().equalsIgnoreCase(layerName)){
                traitbergeLayer = (FeatureMapLayer) layer;
                //TODO : activate back graduation after Geotk milestone MC0044
                traitbergeLayer.setSelectionStyle(style);
                updateGeometry();

                layer.setSelectable(true);
                traitbergeLayer.addItemListener(this);
            }
        }

        helper = new EditionHelper(map, traitbergeLayer);
        helper.setMousePointerSize(6);
    }

    /**
     * {@inheritDoc }
     * @param component
     * @return
     */
    @Override
    public boolean uninstall(final FXMap component) {
        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Confirmer la fin du mode édition ? Les modifications non sauvegardées seront perdues.",
                        ButtonType.YES,ButtonType.NO);
        if (traitbergeProperty.get()==null ||
                ButtonType.YES.equals(alert.showAndWait().get())) {
            super.uninstall(component);
            component.removeEventHandler(MouseEvent.ANY, mouseInputListener);
            component.removeEventHandler(ScrollEvent.ANY, mouseInputListener);
            component.removeDecoration(geomlayer);
            if (traitbergeLayer != null) {
                traitbergeLayer.setSelectionStyle(style);
                traitbergeLayer.removeItemListener(this);
                traitbergeLayer.setSelectionFilter(null);
                selectionFilter = null;
            }
            return true;
        }

        return false;
    }

    private void updateGeometry(){
        if(editGeometry.geometry==null){
            geomlayer.getGeometries().clear();
        }else{
            geomlayer.getGeometries().setAll(editGeometry.geometry.get());
        }
    }

    @Override
    public void itemChange(CollectionChangeEvent<MapItem> event) {
        // nothing to do;
    }

    /**
     * We force focus on currently edited {@link TronconDigue}.
     * @param evt
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt == null) return;
        if (MapLayer.SELECTION_FILTER_PROPERTY.equals(evt.getPropertyName())) {
            if (selectionFilter != null && !selectionFilter.equals(evt.getNewValue())) {
                traitbergeLayer.setSelectionFilter(selectionFilter);
            }
        }
    }

    private class MouseListen extends FXPanMouseListen {

        private final ContextMenu popup = new ContextMenu();
        private double startX;
        private double startY;
        private double diffX;
        private double diffY;
        private final String typeName;


        public MouseListen(String typeName) {
            super(TraitBergeEditHandler.this);
            popup.setAutoHide(true);
            this.typeName = typeName;
        }

        @Override
        public void mouseClicked(final MouseEvent e) {
            if(traitbergeLayer==null) return;

            startX = getMouseX(e);
            startY = getMouseY(e);
            mousebutton = e.getButton();

            if (traitbergeProperty.get() == null) {
                //actions en l'absence de troncon

                if (mousebutton == MouseButton.PRIMARY) {

                } else if (mousebutton == MouseButton.SECONDARY) {

                }

            } else {

            }
        }

        @Override
        public void mousePressed(final MouseEvent e) {
            super.mousePressed(e);
            if(traitbergeProperty==null) return;

            startX = getMouseX(e);
            startY = getMouseY(e);
            mousebutton = e.getButton();

            if(editGeometry.geometry.get()!=null && mousebutton == MouseButton.PRIMARY){
                //selection d'un noeud
                helper.grabGeometryNode(e.getX(), e.getY(), editGeometry);
            }
        }

        @Override
        public void mouseDragged(MouseEvent me) {
            //do not use getX/getY to calculate difference
            //JavaFX Bug : https://javafx-jira.kenai.com/browse/RT-34608

            //calcul du deplacement
            diffX = getMouseX(me)-startX;
            diffY = getMouseY(me)-startY;
            startX = getMouseX(me);
            startY = getMouseY(me);

            if(editGeometry.selectedNode[0] != -1){
                //deplacement d'un noeud
                editGeometry.moveSelectedNode(helper.toCoord(startX,startY));
                updateGeometry();
            }else if(editGeometry.numSubGeom != -1){
                //deplacement de la geometry
                helper.moveGeometry(editGeometry.geometry.get(), diffX, diffY);
                updateGeometry();
            } else {
                super.mouseDragged(me);
            }
        }
    }

}

