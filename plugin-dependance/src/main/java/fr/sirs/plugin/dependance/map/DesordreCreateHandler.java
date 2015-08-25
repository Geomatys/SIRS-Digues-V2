package fr.sirs.plugin.dependance.map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import fr.sirs.Injector;
import fr.sirs.core.component.DesordreDependanceRepository;
import fr.sirs.core.model.DesordreDependance;
import fr.sirs.plugin.dependance.PluginDependance;
import javafx.event.ActionEvent;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
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
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.map.FeatureMapLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.scene.control.ButtonType.NO;
import static javafx.scene.control.ButtonType.YES;

/**
 * @author Cédric Briançon (Geomatys)
 */
public class DesordreCreateHandler extends AbstractNavigationHandler {
    private final MouseListen mouseInputListener = new MouseListen();
    private final FXGeometryLayer decorationLayer = new FXGeometryLayer();

    /**
     * Le désordre en cours.
     */
    private DesordreDependance desordre;

    /**
     * Outil d'aide pour éditer une {@linkplain #editGeometry géométrie} existante.
     */
    private EditionHelper helper;

    /**
     * Géométrie en cours d'édition.
     */
    private final EditionHelper.EditionGeometry editGeometry = new EditionHelper.EditionGeometry();

    /**
     * Coordonnées de la {@linkplain #editGeometry géométrie}.
     */
    private final List<Coordinate> coords = new ArrayList<>();

    /**
     * Vrai si un désordre vient d'être créée.
     */
    private boolean newDesordre = false;

    /**
     * Vrai si la {@linkplain #coords liste des coordonnées} de la {@linkplain #editGeometry géométrie}
     * vient d'être créée.
     */
    private boolean justCreated = false;

    /**
     * Couches présentant les désordres sur la carte.
     */
    private FeatureMapLayer desordresLayer;

    public DesordreCreateHandler() {
        super();
    }

    @Override
    public void install(FXMap component) {
        super.install(component);
        component.addEventHandler(MouseEvent.ANY, mouseInputListener);
        component.addEventHandler(ScrollEvent.ANY, mouseInputListener);
        map.setCursor(Cursor.CROSSHAIR);
        map.addDecoration(0, decorationLayer);

        desordresLayer = PluginDependance.getDesordreLayer();
        helper = new EditionHelper(map, desordresLayer);
    }

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


    /**
     * Ecoute les évènements lancés par la souris.
     */
    private class MouseListen extends FXPanMouseListen {
        private final ContextMenu popup = new ContextMenu();
        private MouseButton pressed;

        public MouseListen() {
            super(DesordreCreateHandler.this);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            final double x = e.getX();
            final double y = e.getY();

            if (MouseButton.PRIMARY.equals(e.getButton())) {
                if (desordre == null) {
                    final Feature feature = helper.grabFeature(x, y, true);
                    if (feature != null) {
                        // Il y a une feature là où l'utilisateur a cliqué, vérification du type
                        final Object candidate = feature.getUserData().get(BeanFeature.KEY_BEAN);
                        if (candidate instanceof DesordreDependance) {
                            desordre = (DesordreDependance) candidate;
                            // On récupère la géométrie de cet objet pour passer en mode édition
                            editGeometry.geometry.set(desordre.getGeometry());
                            // Ajout de cette géométrie dans la couche d'édition sur la carte.
                            decorationLayer.getGeometries().add(editGeometry.geometry.get());
                            newDesordre = false;
                        }
                    } else {
                        // Pas de désordre à l'endroit cliqué, on en créé un nouveau.
                        // Le clic correspond en fait à l'ajout d'un point de cette nouvelle géométrie.
                        final DesordreDependanceRepository repodes = Injector.getBean(DesordreDependanceRepository.class);
                        desordre = repodes.create();
                        coords.clear();
                        coords.add(helper.toCoord(x,y));
                        coords.add(helper.toCoord(x,y));
                        coords.add(helper.toCoord(x, y));
                        // Création de la géométrie à éditer à partir des coordonnées
                        editGeometry.geometry.set(EditionHelper.createPolygon(coords));
                        JTS.setCRS(editGeometry.geometry.get(), map.getCanvas().getObjectiveCRS2D());
                        decorationLayer.getGeometries().setAll(editGeometry.geometry.get());
                        newDesordre = true;
                    }
                } else {
                    // Le désordre existe, on peut travailler avec sa géométrie.
                    if (newDesordre) {
                        // On vient de créer le désordre, le clic gauche va permettre d'ajouter des points.

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
                            // On ajoute le point en plus.
                            justCreated = false;
                            coords.add(helper.toCoord(x,y));
                        }

                        // Création de la géométrie à éditer à partir des coordonnées
                        editGeometry.geometry.set(EditionHelper.createPolygon(coords));
                        JTS.setCRS(editGeometry.geometry.get(), map.getCanvas().getObjectiveCRS2D());
                        decorationLayer.getGeometries().setAll(editGeometry.geometry.get());
                    } else {
                        // On réédite une géométrie existante, le double clic gauche va nous permettre d'ajouter un nouveau
                        // point à la géométrie.
                        if (e.getClickCount() >= 2) {
                            final Geometry result = helper.insertNode((Polygon)editGeometry.geometry.get(), x, y);
                            editGeometry.geometry.set(result);
                            decorationLayer.getGeometries().setAll(editGeometry.geometry.get());
                        }
                    }
                }
            } else if (MouseButton.SECONDARY.equals(e.getButton())) {
                if (desordre != null) {
                    // popup :
                    // -suppression d'un noeud
                    // -sauvegarder
                    // -annuler édition
                    // -supprimer désordre
                    popup.getItems().clear();

                    //action : suppression d'un noeud
                    helper.grabGeometryNode(e.getX(), e.getY(), editGeometry);
                    if (editGeometry.selectedNode[0] >= 0) {
                        final MenuItem item = new MenuItem("Supprimer noeud");
                        item.setOnAction((ActionEvent event) -> {
                            editGeometry.deleteSelectedNode();
                            decorationLayer.setNodeSelection(null);
                            decorationLayer.getGeometries().setAll(editGeometry.geometry.get());
                        });
                        popup.getItems().add(item);
                    }

                    // action : sauvegarde
                    // Sauvegarde du désordre ainsi que sa géométrie qui a éventuellement été éditée.
                    final MenuItem saveItem = new MenuItem("Sauvegarder");
                    saveItem.setOnAction((ActionEvent event) -> {
                        desordre.setGeometry(editGeometry.geometry.get());
                        final DesordreDependanceRepository repodes = Injector.getBean(DesordreDependanceRepository.class);
                        if (desordre.getDocumentId() != null) {
                            repodes.update(desordre);
                        } else {
                            repodes.add(desordre);
                        }
                        // On quitte le mode d'édition.
                        reset();
                    });
                    popup.getItems().add(saveItem);

                    // action : annuler édition
                    final MenuItem cancelItem = new MenuItem("Annuler l'édition");
                    cancelItem.setOnAction(event -> reset());
                    popup.getItems().add(cancelItem);

                    // action : suppression dépendance
                    final MenuItem deleteItem = new MenuItem("Supprimer désordre", new ImageView(GeotkFX.ICON_DELETE));
                    deleteItem.setOnAction((ActionEvent event) -> {
                        final Alert alert = new Alert(CONFIRMATION, "Voulez-vous vraiment supprimer le désordre sélectionné ?", YES, NO);
                        final Optional<ButtonType> result = alert.showAndWait();
                        if (result.isPresent() && result.get() == YES) {
                            Injector.getBean(DesordreDependanceRepository.class).remove(desordre);
                        }
                        // On quitte le mode d'édition.
                        reset();
                    });
                    popup.getItems().add(deleteItem);

                    popup.show(decorationLayer, Side.TOP, e.getX(), e.getY());
                }
            }
        }

        @Override
        public void mousePressed(final MouseEvent e) {
            pressed = e.getButton();

            if(desordre != null && !newDesordre && pressed == MouseButton.PRIMARY){
                // On va sélectionner un noeud sur lequel l'utilisateur a cliqué, s'il y en a un.
                helper.grabGeometryNode(e.getX(), e.getY(), editGeometry);
                decorationLayer.setNodeSelection(editGeometry);
            }

            super.mousePressed(e);
        }

        @Override
        public void mouseDragged(final MouseEvent e) {

            if(desordre != null && !newDesordre && pressed == MouseButton.PRIMARY){
                // On déplace le noeud sélectionné
                editGeometry.moveSelectedNode(helper.toCoord(e.getX(), e.getY()));
                decorationLayer.getGeometries().setAll(editGeometry.geometry.get());
                return;
            }

            super.mouseDragged(e);
        }
    }

    /**
     * Réinitialise la carte et vide la géométrie en cours d'édition.
     */
    private void reset() {
        newDesordre = false;
        justCreated = false;
        decorationLayer.getGeometries().clear();
        decorationLayer.setNodeSelection(null);
        coords.clear();
        editGeometry.reset();
        desordre = null;
    }
}
