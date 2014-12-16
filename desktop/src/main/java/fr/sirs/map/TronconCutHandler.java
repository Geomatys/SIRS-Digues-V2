
package fr.sirs.map;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.SIRS;
import fr.sirs.core.LinearReferencingUtilities;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.TronconDigue;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import org.geotoolkit.data.bean.BeanFeature;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.display2d.container.ContextContainer2D;
import org.geotoolkit.display2d.container.fx.FXFeature;
import org.geotoolkit.display2d.container.fx.FXMapContainerPane;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.feature.FeatureTypeBuilder;
import org.geotoolkit.feature.FeatureUtilities;
import org.geotoolkit.feature.type.FeatureType;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.gui.javafx.render2d.FXAbstractNavigationHandler;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.edition.EditionHelper;
import org.geotoolkit.gui.javafx.render2d.navigation.AbstractMouseHandler;
import org.geotoolkit.gui.javafx.util.FXUtilities;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.style.MutableStyleFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.style.LineSymbolizer;
import org.opengis.util.FactoryException;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class TronconCutHandler extends FXAbstractNavigationHandler {
    
    private static final Color[] PALETTE = new Color[]{
        Color.BLUE,
        Color.CYAN,
        Color.MAGENTA,
        Color.RED,
        Color.GREEN
    };
    
    
    private static final MutableStyleFactory SF = GO2Utilities.STYLE_FACTORY;
    private final MouseListen mouseInputListener = new MouseListen();
    private final FXMapContainerPane geomlayer= new FXMapContainerPane();
    private final double zoomFactor = 2;
    
    //edition variables
    private FeatureMapLayer tronconLayer = null;
    private EditionHelper helper;
    private final EditionHelper.EditionGeometry editGeometry = new EditionHelper.EditionGeometry();
    
    private final Dialog dialog = new Dialog();
    private final FXTronconCut editPane;
    private final FeatureType featureType;
    
    public TronconCutHandler(final FXMap map) {
        super(map);
        geomlayer.setMap2D(map);
        
        editPane = new FXTronconCut();
        
        final DialogPane subpane = new DialogPane();
        subpane.setContent(editPane);
        subpane.getButtonTypes().add(ButtonType.FINISH);
        dialog.setResizable(true);
        dialog.setOnCloseRequest(new EventHandler() {
            @Override
            public void handle(Event event) {
                dialog.hide();
            }
        });
        dialog.initModality(Modality.NONE);
        dialog.initOwner(map.getScene().getWindow());
        dialog.setDialogPane(subpane);
        dialog.setWidth(500);
        dialog.setHeight(700);
        
        editPane.tronconProperty().addListener(new ChangeListener<TronconDigue>() {
            @Override
            public void changed(ObservableValue<? extends TronconDigue> observable, TronconDigue oldValue, TronconDigue newValue) {
                if(newValue!=null){
                    dialog.show();
                }else{
                    dialog.hide();
                }
            }
        });
        
        //recalcule des segment lors d'un changement de point de coupe
        editPane.getCutpoints().addListener(this::cutPointChanged);
        
        //recalcule de l'affichage sur changement d'un segment
        editPane.tronconProperty().addListener((ObservableValue<? extends TronconDigue> observable, TronconDigue oldValue, TronconDigue newValue) -> {
            segmentChanged(null);
        });
        editPane.getSegments().addListener(this::segmentChanged);
        
        final FeatureTypeBuilder ftb = new FeatureTypeBuilder();
        ftb.setName("name");
        ftb.add("geom", LineString.class,SirsCore.getEpsgCode());
        ftb.setDefaultGeometry("geom");
        featureType = ftb.buildFeatureType();
    }

    private void cutPointChanged(ListChangeListener.Change c){
        final TronconDigue troncon = editPane.tronconProperty().get();
        if(troncon==null) return;
        
        final FXTronconCut.CutPoint[] array = editPane.getCutpoints().toArray(new FXTronconCut.CutPoint[0]);
        final LineString linear = LinearReferencingUtilities.asLineString(troncon.getGeometry());
        
        final List<FXTronconCut.Segment> segments = new ArrayList<>();
                
        int colorIndex = 0;
        
        double distanceDebut = 0.0;
        double distanceFin = 0.0;
        for(int i=0;i<array.length;i++){
            if(i>0){
                distanceDebut = distanceFin;
            }
            distanceFin = array[i].distance.doubleValue();
            
            if(distanceDebut!=distanceFin){
                final FXTronconCut.Segment segment = new FXTronconCut.Segment();
                segment.colorProp.set(PALETTE[colorIndex++%PALETTE.length]);
                segment.typeProp.set(FXTronconCut.SegmentType.CONSERVER);
                segment.geometryProp.set(LinearReferencingUtilities.cut(linear, distanceDebut, distanceFin));
                segments.add(segment);
            }
        }
        //dernier segment
        distanceDebut = distanceFin;
        distanceFin = Double.MAX_VALUE;
        if(distanceDebut!=distanceFin){
            final FXTronconCut.Segment segment = new FXTronconCut.Segment();
            segment.colorProp.set(PALETTE[colorIndex++%PALETTE.length]);
            segment.typeProp.set(FXTronconCut.SegmentType.CONSERVER);
            segment.geometryProp.set(LinearReferencingUtilities.cut(linear, distanceDebut, distanceFin));
            segments.add(segment);
        }
        
        editPane.getSegments().setAll(segments);
    }
    
    private void segmentChanged(ListChangeListener.Change c){
        final TronconDigue troncon = editPane.tronconProperty().get();
        if(troncon==null) return;
        
        geomlayer.getChildren().clear();
        
        final FXTronconCut.Segment[] segments = editPane.getSegments().toArray(new FXTronconCut.Segment[0]);
        for(FXTronconCut.Segment segment : segments){
            final Feature feature = FeatureUtilities.defaultFeature(featureType, "id-0");
            feature.getProperty("geom").setValue(segment.geometryProp.get());
            
            final LineSymbolizer ls = SF.lineSymbolizer(SF.stroke(FXUtilities.toSwingColor(segment.colorProp.get()), 4), null);
            final FXFeature fxf = new FXFeature(geomlayer.getContext(), feature);
            fxf.setSymbolizers(ls);
            geomlayer.getChildren().add(fxf);
        }
        
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
        
        //recuperation du layer de troncon
        tronconLayer = null;
        final ContextContainer2D cc = (ContextContainer2D) map.getCanvas().getContainer();
        final MapContext context = cc.getContext();
        for(MapLayer layer : context.layers()){
            layer.setSelectable(false);
            if(layer.getName().equalsIgnoreCase("TronconDigue")){
                tronconLayer = (FeatureMapLayer) layer;
                layer.setSelectable(true);
            }
        }
        
        helper = new EditionHelper(map, tronconLayer);
        helper.setMousePointerSize(6);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean uninstall(final FXMap component) {
        if(editPane.tronconProperty().get()==null || 
                ButtonType.YES.equals(new Alert(Alert.AlertType.CONFIRMATION, "Confirmer la fin du mode édition.", 
                        ButtonType.YES,ButtonType.NO).showAndWait().get())){
            super.uninstall(component);
            component.removeEventHandler(MouseEvent.ANY, mouseInputListener);
            component.removeEventHandler(ScrollEvent.ANY, mouseInputListener);
            map.removeDecoration(geomlayer);
            return true;
        }
        
        dialog.hide();
        return false;
    }
        
    private class MouseListen extends AbstractMouseHandler {

        private final ContextMenu popup = new ContextMenu();
        private double startX;
        private double startY;
        private double diffX;
        private double diffY;
        private MouseButton mousebutton;

        public MouseListen() {
            popup.setAutoHide(true);
        }
        
        private double getMouseX(MouseEvent event){
            final javafx.geometry.Point2D pt = map.localToScreen(0, 0);
            return event.getScreenX()- pt.getX();
        }
        
        private double getMouseY(MouseEvent event){
            final javafx.geometry.Point2D pt = map.localToScreen(0, 0);
            return event.getScreenY() - pt.getY();
        }
        
        @Override
        public void mouseClicked(final MouseEvent e) {            
            if(tronconLayer==null) return;
            
            startX = getMouseX(e);
            startY = getMouseY(e);
            mousebutton = e.getButton();
                
            final TronconDigue troncon = editPane.tronconProperty().get();
            if(troncon==null){
                //actions en l'absence de troncon
                
                if(mousebutton == MouseButton.PRIMARY){
                    //selection d'un troncon
                    final Feature feature = helper.grabFeature(e.getX(), e.getY(), false);
                    if(feature !=null){
                        final Object bean = feature.getUserData().get(BeanFeature.KEY_BEAN);
                        if(bean instanceof TronconDigue){
                            editPane.tronconProperty().set((TronconDigue)bean);
                        }
                    }
                }
            }else if(editPane.isCutMode()){
                try{
                    //ajout d'un point de coupe
                    Point pt = helper.toJTS(startX, startY);
                    //to data crs
                    final MathTransform ptToData =  CRS.findMathTransform(map.getCanvas().getObjectiveCRS2D(),JTS.findCoordinateReferenceSystem(troncon.getGeometry()));
                    pt = (Point) JTS.transform(pt,ptToData);

                    final LineString linear = LinearReferencingUtilities.asLineString(troncon.getGeometry());
                    final LinearReferencingUtilities.SegmentInfo[] segments = LinearReferencingUtilities.buildSegments(linear);
                    final LinearReferencingUtilities.ProjectedReference proj = LinearReferencingUtilities.projectReference(segments, pt);
                
                    final List<FXTronconCut.CutPoint> cuts = new ArrayList<>(editPane.getCutpoints());
                    
                    final FXTronconCut.CutPoint cutPoint = new FXTronconCut.CutPoint();
                    cutPoint.distance.set(proj.distanceAlongLinear);
                    cuts.add(cutPoint);
                    Collections.sort(cuts);
                    editPane.getCutpoints().setAll(cuts);
                    
                }catch(TransformException | FactoryException ex){
                    SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                }
                
            }
            
        }

        @Override
        public void mousePressed(final MouseEvent e) {       
            startX = getMouseX(e);
            startY = getMouseY(e);
            mousebutton = e.getButton();
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
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            super.mouseReleased(me);
        }
        
        @Override
        public void mouseExited(final MouseEvent e) {
            decorationPane.setFill(false);
            decorationPane.setCoord(-10, -10,-10, -10, true);
        }

        @Override
        public void mouseMoved(final MouseEvent e){
            startX = getMouseX(e);
            startY = getMouseY(e);
        }
        
        @Override
        public void mouseWheelMoved(final ScrollEvent e) {
            final double rotate = -e.getDeltaY();

            if(rotate<0){
                scale(new Point2D.Double(startX, startY),zoomFactor);
            }else if(rotate>0){
                scale(new Point2D.Double(startX, startY),1d/zoomFactor);
            }

        }
    }
        
}
