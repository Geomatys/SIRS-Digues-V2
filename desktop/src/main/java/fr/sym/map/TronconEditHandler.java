
package fr.sym.map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import fr.sym.Session;
import fr.sym.digue.Injector;
import fr.symadrem.sirs.core.model.TronconDigue;
import java.awt.geom.Point2D;
import javafx.event.ActionEvent;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import org.geotoolkit.data.bean.BeanFeature;
import org.geotoolkit.display2d.container.ContextContainer2D;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.gui.javafx.render2d.FXAbstractNavigationHandler;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.edition.EditionHelper;
import org.geotoolkit.gui.javafx.render2d.navigation.AbstractMouseHandler;
import org.geotoolkit.gui.javafx.render2d.shape.FXGeometryLayer;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapLayer;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class TronconEditHandler extends FXAbstractNavigationHandler {

    private static final int CROSS_SIZE = 5;
    
    private final MouseListen mouseInputListener = new MouseListen();
    private final FXGeometryLayer geomlayer= new FXGeometryLayer(){
        @Override
        protected Node createVerticeNode(Coordinate c){
            final Line h = new Line(c.x-CROSS_SIZE, c.y, c.x+CROSS_SIZE, c.y);
            final Line v = new Line(c.x, c.y-CROSS_SIZE, c.x, c.y+CROSS_SIZE);
            h.setStroke(Color.RED);
            v.setStroke(Color.RED);
            return new Group(h,v);
        }
    };
    private final double zoomFactor = 2;
    
    //edition variables
    private FeatureMapLayer tronconLayer = null;
    private TronconDigue troncon = null;
    private EditionHelper helper;
    private final EditionHelper.EditionGeometry editGeometry = new EditionHelper.EditionGeometry();
        
    
    public TronconEditHandler(final FXMap map) {
        super(map);
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
        troncon = null;
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
    public void uninstall(final FXMap component) {
        super.uninstall(component);
        component.removeEventHandler(MouseEvent.ANY, mouseInputListener);
        component.removeEventHandler(ScrollEvent.ANY, mouseInputListener);
        map.removeDecoration(geomlayer);
        component.setBottom(null);
    }
    
    private void updateGeometry(){
        if(editGeometry.geometry==null){
            geomlayer.getGeometries().clear();
        }else{
            geomlayer.getGeometries().setAll(editGeometry.geometry);
        }
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
                        
            if(troncon==null){
                //actions en l'absence de troncon
                
                if(mousebutton == MouseButton.PRIMARY){
                    //selection d'un troncon
                    final Feature feature = helper.grabFeature(e.getX(), e.getY(), false);
                    if(feature !=null){
                        final Object bean = feature.getUserData().get(BeanFeature.KEY_BEAN);
                        if(bean instanceof TronconDigue){
                            troncon = (TronconDigue) bean;
                        }
                    }

                    if(troncon!=null){
                        editGeometry.reset();
                        editGeometry.geometry = troncon.getGeometry();
                        updateGeometry();
                    }
                }else if(mousebutton == MouseButton.PRIMARY){
                    
                }
                
            }else{
                //actions sur troncon
                
                if(mousebutton == MouseButton.PRIMARY && e.getClickCount()>=2){
                    //ajout d'un noeud
                    
                    final Geometry result;
                    if(editGeometry.geometry instanceof LineString){
                        result = helper.insertNode((LineString)editGeometry.geometry, startX, startY);
                    }else if(editGeometry.geometry instanceof Polygon){
                        result = helper.insertNode((Polygon)editGeometry.geometry, startX, startY);
                    }else if(editGeometry.geometry instanceof GeometryCollection){
                        result = helper.insertNode((GeometryCollection)editGeometry.geometry, startX, startY);
                    }else{
                        result = editGeometry.geometry;
                    }
                    editGeometry.geometry = result;
                    updateGeometry();
                }else if(mousebutton == MouseButton.SECONDARY){
                    // popup : 
                    // -suppression d'un noeud
                    // -terminer édition
                    // -annuler édition
                    popup.getItems().clear();
                    
                    helper.grabGeometryNode(e.getX(), e.getY(), editGeometry);                    
                    if(editGeometry.selectedNode[0]>=0){
                        final MenuItem item = new MenuItem("Supprimer noeud");
                        item.setOnAction((ActionEvent event) -> {
                            editGeometry.deleteSelectedNode(); 
                            updateGeometry();
                        });
                        popup.getItems().add(item);
                    }
                    
                    final MenuItem saveItem = new MenuItem("Sauvegarder les modifications");
                    saveItem.setOnAction((ActionEvent event) -> {
                        troncon.setGeometry(editGeometry.geometry);
                        final Session session = Injector.getBean(Session.class);
                        session.getTronconDigueRepository().update(troncon);
                        
                        troncon = null;
                        editGeometry.reset();
                        updateGeometry();
                    });
                    popup.getItems().add(saveItem);
                    final MenuItem cancelItem = new MenuItem("Annuler les modifications");
                    cancelItem.setOnAction((ActionEvent event) -> {
                        troncon = null;
                        editGeometry.reset();
                        updateGeometry();
                    });
                    popup.getItems().add(cancelItem);

                    popup.show(geomlayer, Side.TOP, e.getX(), e.getY());
                }
            }
            
            if(troncon==null && mousebutton == MouseButton.PRIMARY){
                
            }else if(troncon!=null && mousebutton == MouseButton.PRIMARY && e.getClickCount()>=2){
                //ajout d'un noeud
                final Geometry result;
                if(editGeometry.geometry instanceof LineString){
                    result = helper.insertNode((LineString)editGeometry.geometry, startX, startY);
                }else if(editGeometry.geometry instanceof Polygon){
                    result = helper.insertNode((Polygon)editGeometry.geometry, startX, startY);
                }else if(editGeometry.geometry instanceof GeometryCollection){
                    result = helper.insertNode((GeometryCollection)editGeometry.geometry, startX, startY);
                }else{
                    result = editGeometry.geometry;
                }
                editGeometry.geometry = result;
                updateGeometry();
            }else if(troncon!=null && mousebutton == MouseButton.SECONDARY && e.getClickCount()>=2){
                
            }
        }

        @Override
        public void mousePressed(final MouseEvent e) {            
            if(troncon==null) return;
            
            startX = getMouseX(e);
            startY = getMouseY(e);
            mousebutton = e.getButton();
            
            if(editGeometry.geometry!=null && mousebutton == MouseButton.PRIMARY){
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
                helper.moveGeometry(editGeometry.geometry, diffX, diffY);
                updateGeometry();
            }
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
