package fr.sirs.plugin.dependance.map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;
import fr.sirs.Injector;
import fr.sirs.core.component.AireStockageDependanceRepository;
import fr.sirs.core.model.AireStockageDependance;
import fr.sirs.plugin.dependance.PluginDependance;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import org.geotoolkit.data.bean.BeanFeature;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.gui.javafx.render2d.AbstractNavigationHandler;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXPanMouseListen;
import org.geotoolkit.gui.javafx.render2d.edition.EditionHelper;
import org.geotoolkit.gui.javafx.render2d.shape.FXGeometryLayer;
import org.geotoolkit.map.FeatureMapLayer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class DependanceEditHandler extends AbstractNavigationHandler {

    private final MouseListen mouseInputListener = new MouseListen();
    private final FXGeometryLayer decorationLayer = new FXGeometryLayer();

    private FeatureMapLayer aireLayer;
    private EditionHelper helper;
    private final EditionHelper.EditionGeometry editGeometry = new EditionHelper.EditionGeometry();
    private AireStockageDependance aire;
    private final List<Coordinate> coords = new ArrayList<>();
    private boolean newDependance = false;
    private boolean justCreated = false;

    public DependanceEditHandler() {
        super();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void install(FXMap component) {
        super.install(component);
        component.addEventHandler(MouseEvent.ANY, mouseInputListener);
        component.addEventHandler(ScrollEvent.ANY, mouseInputListener);
        map.setCursor(Cursor.CROSSHAIR);
        map.addDecoration(0, decorationLayer);

        aireLayer = PluginDependance.getAireLayer();
        helper = new EditionHelper(map, aireLayer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean uninstall(FXMap component) {
        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Confirmer la fin du mode édition ? Les modifications non sauvegardées seront perdues.",
                ButtonType.YES,ButtonType.NO);
        if (ButtonType.YES.equals(alert.showAndWait().get())) {
            super.uninstall(component);
            component.removeDecoration(decorationLayer);
            component.removeEventHandler(MouseEvent.ANY, mouseInputListener);
            component.removeEventHandler(ScrollEvent.ANY, mouseInputListener);
            return true;
        }

        return false;
    }

    private class MouseListen extends FXPanMouseListen {
        private MouseButton pressed;

        public MouseListen() {
            super(DependanceEditHandler.this);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            final double x = e.getX();
            final double y = e.getY();
            if (MouseButton.PRIMARY.equals(e.getButton())) {
                if (aire == null) {
                    final Feature feature = helper.grabFeature(x, y, true);
                    if (feature != null) {
                        final Object candidate = feature.getUserData().get(BeanFeature.KEY_BEAN);
                        if (candidate instanceof AireStockageDependance) {
                            aire = (AireStockageDependance)candidate;
                            editGeometry.geometry.set(aire.getGeometry());
                            decorationLayer.getGeometries().add(editGeometry.geometry.get());
                            newDependance = false;
                        }
                    }
                } else {
                    if (newDependance) {
                        if(justCreated){
                            justCreated = false;
                            //we must modify the second point since two point where added at the start
                            coords.remove(2);
                            coords.remove(1);
                            coords.add(helper.toCoord(x,y));
                            coords.add(helper.toCoord(x,y));

                        }else if(coords.isEmpty()){
                            justCreated = true;
                            //this is the first point of the geometry we create
                            //add 3 points that will be used when moving the mouse around
                            coords.add(helper.toCoord(x,y));
                            coords.add(helper.toCoord(x,y));
                            coords.add(helper.toCoord(x,y));
                        }else{
                            justCreated = false;
                            coords.add(helper.toCoord(x,y));
                        }

                        editGeometry.geometry.set(EditionHelper.createPolygon(coords));
                        JTS.setCRS(editGeometry.geometry.get(), map.getCanvas().getObjectiveCRS2D());
                        decorationLayer.getGeometries().setAll(editGeometry.geometry.get());
                    } else {

                    }
                }
            } else if (MouseButton.SECONDARY.equals(e.getButton())) {
                if (aire == null) {
                    aire = Injector.getBean(AireStockageDependanceRepository.class).create();
                    newDependance = true;
                } else {
                    // Save
                    aire.setGeometry(editGeometry.geometry.get());
                    if (aire.getDocumentId() != null) {
                        Injector.getBean(AireStockageDependanceRepository.class).update(aire);
                    } else {
                        Injector.getBean(AireStockageDependanceRepository.class).add(aire);
                    }
                    reset();
                }
            }
        }

        @Override
        public void mousePressed(final MouseEvent e) {
            pressed = e.getButton();

            if(aire != null && !newDependance && pressed == MouseButton.PRIMARY){
                //single click with a geometry = select a node
                helper.grabGeometryNode(e.getX(), e.getY(), editGeometry);
                decorationLayer.setNodeSelection(editGeometry);
            }

            super.mousePressed(e);
        }

        @Override
        public void mouseDragged(final MouseEvent e) {

            if(aire != null && !newDependance && pressed == MouseButton.PRIMARY){
                //dragging node
                editGeometry.moveSelectedNode(helper.toCoord(e.getX(), e.getY()));
                decorationLayer.getGeometries().setAll(editGeometry.geometry.get());
                return;
            }

            super.mouseDragged(e);
        }
    }

    private void reset() {
        newDependance = false;
        justCreated = false;
        decorationLayer.getGeometries().clear();
        decorationLayer.setNodeSelection(null);
        coords.clear();
        editGeometry.reset();
        aire = null;
    }
}
