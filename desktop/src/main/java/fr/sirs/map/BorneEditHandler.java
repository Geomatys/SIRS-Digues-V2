
package fr.sirs.map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.CorePlugin;
import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.SystemeReperageBorne;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.core.TronconUtils;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
import org.geotoolkit.data.bean.BeanFeature;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.display2d.container.ContextContainer2D;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.filter.identity.DefaultFeatureId;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.gui.javafx.render2d.FXAbstractNavigationHandler;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXPanMouseListen;
import org.geotoolkit.gui.javafx.render2d.edition.EditionHelper;
import org.geotoolkit.gui.javafx.render2d.navigation.AbstractMouseHandler;
import org.geotoolkit.gui.javafx.render2d.shape.FXGeometryLayer;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapLayer;
import org.opengis.filter.Id;
import org.opengis.filter.identity.Identifier;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class BorneEditHandler extends FXAbstractNavigationHandler {

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
    private final Session session;
    
    //edition variables
    private FeatureMapLayer tronconLayer = null;
    private FeatureMapLayer borneLayer = null;
    private BorneDigue borne = null;
    private EditionHelper helperTroncon;
    private EditionHelper helperBorne;
    private final EditionHelper.EditionGeometry editGeometry = new EditionHelper.EditionGeometry();
    
    private final Dialog dialog = new Dialog();
    private final FXSystemeReperagePane editPane;
        
    
    public BorneEditHandler(final FXMap map) {
        super(map);
        session = Injector.getSession();
        
        editPane = new FXSystemeReperagePane(map);
        
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
        
        //on ecoute la selection du troncon et des bornes pour les mettre en surbrillant
        editPane.tronconProperty().addListener(new ChangeListener<TronconDigue>() {
            @Override
            public void changed(ObservableValue<? extends TronconDigue> observable, TronconDigue oldValue, TronconDigue newValue) {
                if(tronconLayer==null) return;
                
                borne = null;
                updateGeometry();
                if(borneLayer!=null){
                    borneLayer.setSelectionFilter(null);
                }
                    
                if(newValue==null){
                    tronconLayer.setSelectionFilter(null);
                }else{
                    final Identifier id = new DefaultFeatureId(newValue.getDocumentId());
                    tronconLayer.setSelectionFilter(GO2Utilities.FILTER_FACTORY.id(Collections.singleton(id)));
                }
            }
        });
        
        editPane.borneProperties().addListener(new ListChangeListener<SystemeReperageBorne>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends SystemeReperageBorne> c) {
                if(borneLayer==null) return;
                
                final ObservableList<SystemeReperageBorne> lst = editPane.borneProperties();
                final Set<Identifier> ids = new HashSet<>();
                for(SystemeReperageBorne borne : lst){
                    ids.add(new DefaultFeatureId(borne.getBorneId()));
                }
                
                final Id filter = GO2Utilities.FILTER_FACTORY.id(ids);
                borneLayer.setSelectionFilter(filter);
                                
                if(ids.size()==1){
                    //borne edition mode
                    final String borneId = lst.get(0).getBorneId();
                    borne = session.getBorneDigueRepository().get(borneId);
                    updateGeometry();
                }else{
                    borne = null;
                    updateGeometry();
                }
                
            }
        });
        
        //fin de l'edition
        dialog.resultProperty().addListener((ObservableValue observable, Object oldValue, Object newValue) -> {
            TronconDigue troncon = editPane.tronconProperty().get();
            if(troncon!=null){
                //on recupere la derniere version, la maj des sr entraine la maj des troncons
                troncon = session.getTronconDigueRepository().get(troncon.getDocumentId());
                //on recalcule les geometries des positionables du troncon.
                TronconUtils.updatePositionableGeometry(troncon,session);
            }
            dialog.close();
            editPane.reset();
        });
        
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
        
        dialog.setWidth(500);
        dialog.setHeight(700);
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
        
        //on passe en mode sélection de troncon
        editPane.reset();
        
        final ContextContainer2D cc = (ContextContainer2D) map.getCanvas().getContainer();
        final MapContext context = cc.getContext();
        for(MapLayer layer : context.layers()){
            layer.setSelectable(false);
            if(layer.getName().equalsIgnoreCase(CorePlugin.TRONCON_LAYER_NAME)){
                tronconLayer = (FeatureMapLayer) layer;
                layer.setSelectable(true);
            }else if(layer.getName().equalsIgnoreCase(CorePlugin.BORNE_LAYER_NAME)){
                borneLayer = (FeatureMapLayer) layer;
                layer.setSelectable(true);
            }
        }
        
        helperTroncon = new EditionHelper(map, tronconLayer);
        helperTroncon.setMousePointerSize(6);
        
        helperBorne = new EditionHelper(map, borneLayer);
        helperBorne.setMousePointerSize(6);
        
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
            component.setBottom(null);

            //déselection borne et troncon
            if(tronconLayer!=null){
                tronconLayer.setSelectionFilter(null);
            }
            if(borneLayer!=null){
                borneLayer.setSelectionFilter(null);
            }

            dialog.close();
            return true;
        }
        
        return false;
    }
    
    private void updateGeometry(){
        if(borne==null){
            editGeometry.reset();
        }else{
            editGeometry.geometry = borne.getGeometry();
        }
        
        if(editGeometry.geometry==null){
            geomlayer.getGeometries().clear();
        }else{
            geomlayer.getGeometries().setAll(editGeometry.geometry);
        }
    }
    
    private class MouseListen extends FXPanMouseListen {

        private final ContextMenu popup = new ContextMenu();
        private double startX;
        private double startY;

        public MouseListen() {
            super(BorneEditHandler.this);
            popup.setAutoHide(true);
        }
        
        @Override
        public void mouseClicked(final MouseEvent e) {
            if(tronconLayer==null) return;
            
            startX = getMouseX(e);
            startY = getMouseY(e);
            mousebutton = e.getButton();
                        
            
            final FXSystemeReperagePane.Mode mode = editPane.modeProperty().get();
            
            if(FXSystemeReperagePane.Mode.PICK_TRONCON.equals(mode)){
                if(mousebutton == MouseButton.PRIMARY){
                    //selection d'un troncon
                    final Feature feature = helperTroncon.grabFeature(e.getX(), e.getY(), false);
                    if(feature !=null){
                        Object bean = feature.getUserData().get(BeanFeature.KEY_BEAN);
                        if(bean instanceof TronconDigue){
                            bean = session.getTronconDigueRepository().get(((TronconDigue)bean).getDocumentId());
                            editPane.tronconProperty().set((TronconDigue)bean);
                        }
                    }
                }
            }else if(FXSystemeReperagePane.Mode.EDIT_BORNE.equals(mode)){
                final TronconDigue troncon = editPane.tronconProperty().get();
                final SystemeReperage sr = editPane.systemeReperageProperty().get();
                
                if(borne==null || editGeometry.selectedNode[0] < 0){                    
                    //selection d'une borne
                    final Feature feature = helperBorne.grabFeature(e.getX(), e.getY(), false);
                    if(feature !=null){
                        final Object bean = feature.getUserData().get(BeanFeature.KEY_BEAN);
                        if(bean instanceof BorneDigue){
                            final BorneDigue candidate = (BorneDigue) bean;
                            final String candidateId = candidate.getDocumentId();
                            
                            //on vérifie que la borne fait bien partie du SR sélectionné
                            final List<SystemeReperageBorne> srbs = sr.getSystemereperageborneId();   
                            for(SystemeReperageBorne srb : srbs){
                                if(srb.getBorneId().equals(candidateId)){
                                    editPane.selectSRB(srb);
                                    break;
                                }
                            }
                        }
                    }
                }
                
            }else if(FXSystemeReperagePane.Mode.CREATE_BORNE.equals(mode)){
                
                final Coordinate coord = helperBorne.toCoord(startX,startY);
                final Point point = GO2Utilities.JTS_FACTORY.createPoint(coord);
                JTS.setCRS(point, SirsCore.getEpsgCode());
                //les event vont induire le repaint de la carte
                editPane.createBorne(point);
            }
            
        }

        @Override
        public void mousePressed(final MouseEvent e) {
            super.mousePressed(e);
            startX = getMouseX(e);
            startY = getMouseY(e);
            
            if(editGeometry.geometry!=null){
                helperBorne.grabGeometryNode(startX, startY, editGeometry);
            }
        }

        @Override
        public void mouseDragged(MouseEvent me) {
            //do not use getX/getY to calculate difference
            //JavaFX Bug : https://javafx-jira.kenai.com/browse/RT-34608
            
            //calcul du deplacement
            startX = getMouseX(me);
            startY = getMouseY(me);
            
            if(borne!=null && editGeometry.selectedNode[0]>=0){
                //deplacement d'une borne
                editGeometry.moveSelectedNode(helperBorne.toCoord(startX,startY));
                updateGeometry();
            } else {
                super.mouseDragged(me);
            }
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            mouseDragged(me);
            if(borne!=null && editGeometry.selectedNode[0]>=0){
                borne.setGeometry((Point) editGeometry.geometry);
                session.getBorneDigueRepository().update(borne);
                editPane.selectSRB(null);
                //les event vont induire le repaint de la carte
                editPane.sortBorneTable();
                
                final TronconDigue troncon = editPane.tronconProperty().get();
                if(troncon!=null){
                    //on recalcule les geometries des positionables du troncon.
                    TronconUtils.updatePositionableGeometry(troncon,session);
                }
            } else {
                super.mouseReleased(me);
            }
        }
    }
        
}
