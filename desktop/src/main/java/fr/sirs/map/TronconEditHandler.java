
package fr.sirs.map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import fr.sirs.CorePlugin;
import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import fr.sirs.core.LinearReferencingUtilities;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.core.TronconUtils;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.geotoolkit.data.bean.BeanFeature;
import org.geotoolkit.display2d.container.ContextContainer2D;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.gui.javafx.render2d.FXAbstractNavigationHandler;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXPanMouseListen;
import org.geotoolkit.gui.javafx.render2d.edition.EditionHelper;
import org.geotoolkit.gui.javafx.render2d.shape.FXGeometryLayer;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.referencing.CRS;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

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
    
    //edition variables
    private FeatureMapLayer tronconLayer;
    private final SimpleObjectProperty<TronconDigue> troncon = new SimpleObjectProperty<>();
    private EditionHelper helper;
    private final EditionHelper.EditionGeometry editGeometry = new EditionHelper.EditionGeometry();
    private final Session session;
        
    
    public TronconEditHandler(final FXMap map) {
        super(map);
        session = Injector.getSession();
        troncon.addListener((ObservableValue<? extends TronconDigue> observable, TronconDigue oldValue, TronconDigue newValue) -> {
            if (newValue != null && !newValue.getStructures().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING,
                        "Attention, ce tronçon contient des structures. Toute modification du tracé risque de changer leur position.", ButtonType.CANCEL, ButtonType.OK);
                alert.setResizable(true);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && ButtonType.OK.equals(result.get())) {
                } else {
                    troncon.set(null);
                }
            }

            editGeometry.reset();
            if (troncon.get() != null) {
                editGeometry.geometry = (Geometry) troncon.get().getGeometry().clone();
            }
            updateGeometry();

            if (Platform.isFxApplicationThread()) {
                map.getCanvas().repaint();
            } else {
                Platform.runLater(() -> map.getCanvas().repaint());
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
        
        //recuperation du layer de troncon
        tronconLayer = null;
        troncon.set(null);
        final ContextContainer2D cc = (ContextContainer2D) map.getCanvas().getContainer();
        final MapContext context = cc.getContext();
        for(MapLayer layer : context.layers()){
            layer.setSelectable(false);
            if(layer.getName().equalsIgnoreCase(CorePlugin.TRONCON_LAYER_NAME)){
                tronconLayer = (FeatureMapLayer) layer;
                try {
                    tronconLayer.setSelectionStyle(CorePlugin.createTronconSelectionStyle(true));
                    updateGeometry();
                } catch (URISyntaxException ex) {
                    SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                }
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
        if (troncon.get()==null || 
                ButtonType.YES.equals(new Alert(Alert.AlertType.CONFIRMATION, "Confirmer la fin du mode édition ? Les modifications non sauvegardées seront perdues.", 
                        ButtonType.YES,ButtonType.NO).showAndWait().get())) {
            super.uninstall(component);
            component.removeEventHandler(MouseEvent.ANY, mouseInputListener);
            component.removeEventHandler(ScrollEvent.ANY, mouseInputListener);
            map.removeDecoration(geomlayer);
            if (tronconLayer != null) {
                try {
                    tronconLayer.setSelectionStyle(CorePlugin.createTronconSelectionStyle(false));
                } catch (URISyntaxException ex) {
                    SIRS.LOGGER.log(Level.WARNING, null, ex);
                }
            }
            return true;
        }
        
        return false;
    }
    
    private void updateGeometry(){
        if(editGeometry.geometry==null){
            geomlayer.getGeometries().clear();
        }else{
            geomlayer.getGeometries().setAll(editGeometry.geometry);
        }
    }
    
    public static Entry<String,Digue> showTronconDialog() {
        final Session session = Injector.getBean(Session.class);
        final List<Digue> digues = session.getDigues();
        final ChoiceBox<Digue> choiceBox = new ChoiceBox<>(FXCollections.observableList(digues));
        choiceBox.setConverter(new StringConverter<Digue>() {
            @Override
            public String toString(Digue object) {
                return object.getLibelle();
            }
            @Override
            public Digue fromString(String string) {
                return null;
            }
        });

        final TextField nameField = new TextField();
        
        final Stage dialog = new Stage();
        dialog.setTitle("Nouveau tronçon");
        dialog.setResizable(true);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(session.getFrame().getScene().getWindow());
        
        //choix de la digue
        final GridPane bp = new GridPane();
        bp.getRowConstraints().setAll(
                new RowConstraints(),
                new RowConstraints(),
                new RowConstraints(),
                new RowConstraints()
        );
        bp.setPadding(new Insets(10, 10, 10, 10));
        bp.setHgap(10);
        bp.setVgap(10);
        bp.add(new Label("Nom du tronçon"), 0, 0);
        bp.add(nameField, 0, 1);
        bp.add(new Label("Rattacher à la digue"), 0, 2);
        bp.add(choiceBox, 0, 3);
        
        final Button finishBtn = new Button("Terminer");
        // Do not allow creation of a troncon without a name.
        finishBtn.disableProperty().bind(nameField.textProperty().isEmpty());
        
        final Button cancelBtn = new Button("Annuler");
        cancelBtn.setCancelButton(true);
        
        finishBtn.setOnAction((ActionEvent e)-> {
            dialog.close();
        });
        
        cancelBtn.setOnAction((ActionEvent e)-> {
            dialog.close();
        });
        
        final ButtonBar babar = new ButtonBar();
        babar.getButtons().addAll(cancelBtn, finishBtn);

        final BorderPane main = new BorderPane(bp);
        main.setBottom(babar);
        
        dialog.setScene(new Scene(main));
        dialog.showAndWait();

        final Digue digue = choiceBox.getValue();        
        return new AbstractMap.SimpleImmutableEntry<>(nameField.getText(),digue);
    }
    
    private class MouseListen extends FXPanMouseListen {

        private final ContextMenu popup = new ContextMenu();
        private double startX;
        private double startY;
        private double diffX;
        private double diffY;

        public MouseListen() {
            super(TronconEditHandler.this);
            popup.setAutoHide(true);
        }
        
        @Override
        public void mouseClicked(final MouseEvent e) {
            if(tronconLayer==null) return;
            
            startX = getMouseX(e);
            startY = getMouseY(e);
            mousebutton = e.getButton();

            if (troncon.get() == null) {
                //actions en l'absence de troncon

                if (mousebutton == MouseButton.PRIMARY) {
                    //selection d'un troncon
                    final Feature feature = helper.grabFeature(e.getX(), e.getY(), false);
                    if (feature != null) {
                        final Object bean = feature.getUserData().get(BeanFeature.KEY_BEAN);
                        if (bean instanceof TronconDigue) {
                            //on recupere le troncon complet, celui ci n'est qu'une mise a plat
                            troncon.set(session.getTronconDigueRepository().get(((TronconDigue) bean).getDocumentId()));
                        }
                    }

                } else if (mousebutton == MouseButton.SECONDARY) {
                    // popup :
                    // -commencer un nouveau troncon
                    popup.getItems().clear();
                    
                    final MenuItem createItem = new MenuItem("Créer un nouveau tronçon");
                    createItem.setOnAction((ActionEvent event) -> {
                        final Entry<String, Digue> entry = showTronconDialog();
                        if (entry.getKey() == null || entry.getKey().isEmpty()) {
                            return;
                        }
                        TronconDigue tmpTroncon = new TronconDigue();
                        tmpTroncon.setLibelle(entry.getKey());

                        final Coordinate coord1 = helper.toCoord(e.getX() - 20, e.getY());
                        final Coordinate coord2 = helper.toCoord(e.getX() + 20, e.getY());
                        try {
                            Geometry geom = EditionHelper.createLine(coord1, coord2);
                            //convertion from base crs
                            geom = JTS.transform(geom, CRS.findMathTransform(map.getCanvas().getObjectiveCRS2D(), SirsCore.getEpsgCode(), true));
                            JTS.setCRS(geom, SirsCore.getEpsgCode());
                            if (entry.getValue() != null) {
                                tmpTroncon.setDigueId(entry.getValue().getId());
                            }
                            tmpTroncon.setGeometry(geom);

                            //sauvegarde du troncon
                            session.getTronconDigueRepository().add(tmpTroncon);
                            TronconUtils.updateSRElementaire(tmpTroncon, session);
                            
                            // Prepare l'edition du tronçon
                            troncon.set(tmpTroncon);

                        } catch (TransformException | FactoryException ex) {
                            // TODO : better error management
                            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                        }
                        
                    });
                    popup.getItems().add(createItem);
                    
                    popup.show(geomlayer, Side.TOP, e.getX(), e.getY());
                }
                
            } else {
                //actions sur troncon                
                if (mousebutton == MouseButton.PRIMARY && e.getClickCount() >= 2) {
                    //ajout d'un noeud                    
                    final Geometry result;
                    if (editGeometry.geometry instanceof LineString) {
                        result = helper.insertNode((LineString) editGeometry.geometry, startX, startY);
                    } else if (editGeometry.geometry instanceof Polygon) {
                        result = helper.insertNode((Polygon) editGeometry.geometry, startX, startY);
                    } else if (editGeometry.geometry instanceof GeometryCollection) {
                        result = helper.insertNode((GeometryCollection) editGeometry.geometry, startX, startY);
                    } else {
                        result = editGeometry.geometry;
                    }
                    editGeometry.geometry = result;
                    updateGeometry();
                } else if (mousebutton == MouseButton.SECONDARY) {
                    // popup : 
                    // -suppression d'un noeud
                    // -terminer édition
                    // -annuler édition
                    // -supprimer troncon
                    popup.getItems().clear();

                    //action : sauvegarder edition
                    //action : suppression d'un noeud
                    helper.grabGeometryNode(e.getX(), e.getY(), editGeometry);
                    if (editGeometry.selectedNode[0] >= 0) {
                        final MenuItem item = new MenuItem("Supprimer noeud");
                        item.setOnAction((ActionEvent event) -> {
                            editGeometry.deleteSelectedNode();
                            updateGeometry();
                        });
                        popup.getItems().add(item);
                    }
                    
                    if (editGeometry.geometry != null) {
                        // Si le tronçon est vide, on peut inverser son tracé
                        if (troncon.get().getStructures().isEmpty()) {
                            if (!popup.getItems().isEmpty()) {
                                popup.getItems().add(new SeparatorMenuItem());
                            }
                            final MenuItem invert = new MenuItem("Inverser le tracé du tronçon");
                            invert.setOnAction((ActionEvent ae) -> {
                                // HACK : On est forcé de sauvegarder le tronçon pour mettre à jour le SR élémentaire.
                                troncon.get().setGeometry(editGeometry.geometry.reverse());
                                session.getTronconDigueRepository().update(troncon.get());
                                TronconUtils.updateSRElementaire(troncon.get(), session);
                                troncon.set(null);
                            });
                            popup.getItems().add(invert);
                        }

                    // On peut sauvegarder ou annuler nos changements si la geometrie du tronçon
                        // diffère de celle de l'éditeur.
                        if (!editGeometry.geometry.equals(troncon.get().getGeometry())) {
                            final MenuItem saveItem = new MenuItem("Sauvegarder les modifications");
                            saveItem.setOnAction((ActionEvent event) -> {
                                troncon.get().setGeometry(editGeometry.geometry);
                                session.getTronconDigueRepository().update(troncon.get());

                                TronconUtils.updateSRElementaire(troncon.get(), session);
                                //on recalcule les geometries des positionables du troncon.
                                TronconUtils.updatePositionableGeometry(troncon.get(), session);

                                troncon.set(null);
                            });
                            popup.getItems().add(saveItem);

                        }

                        //action : annuler edition
                        final String cancelTitle = (!editGeometry.geometry.equals(troncon.get().getGeometry()))?
                                "Annuler les modifications" : "Désélectionner le tronçon";
                                
                        final MenuItem cancelItem = new MenuItem(cancelTitle);
                        cancelItem.setOnAction((ActionEvent event) -> {
                            troncon.set(null);
                        });
                        popup.getItems().add(cancelItem);
                    }

                    //action : suppression du troncon
                    if (!popup.getItems().isEmpty()) {
                        popup.getItems().add(new SeparatorMenuItem());
                    }
                    final MenuItem deleteItem = new MenuItem("Supprimer tronçon", new ImageView(GeotkFX.ICON_DELETE));
                    deleteItem.setOnAction((ActionEvent event) -> {
                        session.getTronconDigueRepository().remove(troncon.get());
                        troncon.set(null);
                    });
                    popup.getItems().add(deleteItem);

                    popup.show(geomlayer, Side.TOP, e.getX(), e.getY());
                }
            }
        }

        @Override
        public void mousePressed(final MouseEvent e) {
            super.mousePressed(e);
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
            } else {
                super.mouseDragged(me);
            }
        }
    }
        
}
