package fr.sirs.plugin.dependance.map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import fr.sirs.Injector;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.AireStockageDependanceRepository;
import fr.sirs.core.component.AutreDependanceRepository;
import fr.sirs.core.component.CheminAccesDependanceRepository;
import fr.sirs.core.component.OuvrageVoirieDependanceRepository;
import fr.sirs.core.model.AbstractDependance;
import fr.sirs.core.model.AireStockageDependance;
import fr.sirs.core.model.AutreDependance;
import fr.sirs.core.model.CheminAccesDependance;
import fr.sirs.core.model.OuvrageVoirieDependance;
import fr.sirs.plugin.dependance.PluginDependance;
import javafx.event.ActionEvent;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import org.geotoolkit.data.bean.BeanFeature;
import org.geotoolkit.display.VisitFilter;
import org.geotoolkit.display2d.canvas.AbstractGraphicVisitor;
import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.primitive.ProjectedCoverage;
import org.geotoolkit.display2d.primitive.ProjectedFeature;
import org.geotoolkit.display2d.primitive.SearchAreaJ2D;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.gui.javafx.render2d.AbstractNavigationHandler;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXPanMouseListen;
import org.geotoolkit.gui.javafx.render2d.edition.EditionHelper;
import org.geotoolkit.gui.javafx.render2d.shape.FXGeometryLayer;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.map.FeatureMapLayer;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.scene.control.ButtonType.NO;
import static javafx.scene.control.ButtonType.YES;

/**
 * Contrôle les actions possibles pour le bouton d'édition et de modification de dépendances
 * sur la carte.
 *
 * @author Cédric Briançon (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
public class DependanceEditHandler extends AbstractNavigationHandler {
    private final MouseListen mouseInputListener = new MouseListen();
    private final FXGeometryLayer decorationLayer = new FXGeometryLayer();

    /**
     * Couches présentant les dépendances sur la carte.
     */
    private FeatureMapLayer aireLayer;
    private FeatureMapLayer autreLayer;
    private FeatureMapLayer cheminLayer;
    private FeatureMapLayer ouvrageLayer;

    /**
     * La dépendance en cours.
     */
    private AbstractDependance dependance;

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
     * Vrai si une dépendance vient d'être créée.
     */
    private boolean newDependance = false;

    /**
     * Vrai si la {@linkplain #coords liste des coordonnées} de la {@linkplain #editGeometry géométrie}
     * vient d'être créée.
     */
    private boolean justCreated = false;

    public DependanceEditHandler() {
        super();
    }

    public DependanceEditHandler(final AbstractDependance dependance) {
        this();
        this.dependance = dependance;
        newDependance = true;
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
        autreLayer = PluginDependance.getAutreLayer();
        cheminLayer = PluginDependance.getCheminLayer();
        ouvrageLayer = PluginDependance.getOuvrageLayer();
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


    /**
     * Ecoute les évènements lancés par la souris.
     */
    private class MouseListen extends FXPanMouseListen {
        private final ContextMenu popup = new ContextMenu();
        private MouseButton pressed;

        public MouseListen() {
            super(DependanceEditHandler.this);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            final double x = e.getX();
            final double y = e.getY();

            if (MouseButton.PRIMARY.equals(e.getButton())) {
                if (dependance == null) {
                    // Recherche d'une couche de la carte qui contiendrait une géométrie là où l'utilisateur a cliqué
                    final Rectangle2D clickArea = new Rectangle2D.Double(e.getX()-2, e.getY()-2, 4, 4);

                    //recherche d'un object a editer
                    map.getCanvas().getGraphicsIn(clickArea, new AbstractGraphicVisitor() {
                        @Override
                        public void visit(ProjectedFeature graphic, RenderingContext2D context, SearchAreaJ2D area) {
                            final Object bean = graphic.getCandidate().getUserData().get(BeanFeature.KEY_BEAN);
                            if(bean instanceof AbstractDependance){
                                helper = new EditionHelper(map, graphic.getLayer());
                                dependance = (AbstractDependance)bean;
                                // On récupère la géométrie de cet objet pour passer en mode édition
                                editGeometry.geometry.set((Geometry)dependance.getGeometry().clone());
                                // Ajout de cette géométrie dans la couche d'édition sur la carte.
                                decorationLayer.getGeometries().setAll(editGeometry.geometry.get());
                                newDependance = false;
                            }
                        }
                        @Override
                        public boolean isStopRequested() {
                            return helper!=null;
                        }
                        @Override
                        public void visit(ProjectedCoverage coverage, RenderingContext2D context, SearchAreaJ2D area) {}
                    }, VisitFilter.INTERSECTS);
                } else {
                    // La dépendance existe, on peut travailler avec sa géométrie.
                    if (newDependance) {
                        // Le helper peut être null si on a choisi d'activer ce handler pour une dépendance existante,
                        // sans passer par le clic droit pour choisir un type de dépendance.
                        final Class clazz = dependance.getClass();
                        if (helper == null) {
                            if (AireStockageDependance.class.isAssignableFrom(clazz)) {
                                helper = new EditionHelper(map, aireLayer);
                            } else if (AutreDependance.class.isAssignableFrom(clazz)) {
                                helper = new EditionHelper(map, autreLayer);
                            } else if (CheminAccesDependance.class.isAssignableFrom(clazz)) {
                                helper = new EditionHelper(map, cheminLayer);
                            } else if (OuvrageVoirieDependance.class.isAssignableFrom(clazz)) {
                                helper = new EditionHelper(map, ouvrageLayer);
                            }
                        }

                        final boolean isPolygonGeom = AireStockageDependance.class.isAssignableFrom(clazz) ||
                                AutreDependance.class.isAssignableFrom(clazz);

                        // On vient de créer la dépendance, le clic gauche va permettre d'ajouter des points.

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
                        if (isPolygonGeom) {
                            editGeometry.geometry.set(EditionHelper.createPolygon(coords));
                        } else {
                            editGeometry.geometry.set(EditionHelper.createLine(coords));
                        }
                        JTS.setCRS(editGeometry.geometry.get(), map.getCanvas().getObjectiveCRS2D());
                        decorationLayer.getGeometries().setAll(editGeometry.geometry.get());
                    } else {
                        // On réédite une géométrie existante, le double clic gauche va nous permettre d'ajouter un nouveau
                        // point à la géométrie.
                        if (e.getClickCount() >= 2) {
                            final Geometry tempEditGeom = editGeometry.geometry.get();
                            final Geometry result;
                            if (tempEditGeom instanceof Polygon) {
                                result = helper.insertNode((Polygon)editGeometry.geometry.get(), x, y);
                            } else {
                                result = helper.insertNode((LineString)editGeometry.geometry.get(), x, y);
                            }
                            editGeometry.geometry.set(result);
                            decorationLayer.getGeometries().setAll(editGeometry.geometry.get());
                        }
                    }
                }
            } else if (MouseButton.SECONDARY.equals(e.getButton())) {
                if (dependance == null) {
                    // La dépendance n'existe pas, on en créé une nouvelle après avoir choisi le type.
                    final ChoiceDialog<String> dialog = new ChoiceDialog<>("Aires de stockage", "Aires de stockage", "Autres", "Chemins d'accès", "Ouvrages de voirie");
                    dialog.setTitle("Création de dépendance");
                    dialog.setContentText("Choisir un type de dépendance");
                    Optional<String> result = dialog.showAndWait();
                    result.ifPresent(type -> {
                        final Class clazz;
                        switch (type) {
                            case "Aires de stockage": clazz = AireStockageDependance.class; break;
                            case "Autres": clazz = AutreDependance.class; break;
                            case "Chemins d'accès": clazz = CheminAccesDependance.class; break;
                            case "Ouvrages de voirie": clazz = OuvrageVoirieDependance.class; break;
                            default: clazz = AireStockageDependance.class;
                        }

                        if (AireStockageDependance.class.isAssignableFrom(clazz)) {
                            helper = new EditionHelper(map, aireLayer);
                        } else if (AutreDependance.class.isAssignableFrom(clazz)) {
                            helper = new EditionHelper(map, autreLayer);
                        } else if (CheminAccesDependance.class.isAssignableFrom(clazz)) {
                            helper = new EditionHelper(map, cheminLayer);
                        } else if (OuvrageVoirieDependance.class.isAssignableFrom(clazz)) {
                            helper = new EditionHelper(map, ouvrageLayer);
                        }

                        final AbstractSIRSRepository<AbstractDependance> repodep = Injector.getSession().getRepositoryForClass(clazz);
                        dependance = repodep.create();
                        newDependance = true;
                    });
                } else {
                    // popup :
                    // -suppression d'un noeud
                    // -sauvegarder
                    // -annuler édition
                    // -supprimer dépendance
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
                    // Sauvegarde de la dépendance de stockage ainsi que sa géométrie qui a éventuellement été éditée.
                    final MenuItem saveItem = new MenuItem("Sauvegarder");
                    saveItem.setOnAction((ActionEvent event) -> {
                        dependance.setGeometry(editGeometry.geometry.get());
                        final AbstractSIRSRepository repodep = Injector.getSession().getRepositoryForClass(dependance.getClass());

                        if (dependance.getDocumentId() != null) {
                            repodep.update(dependance);
                        } else {
                            repodep.add(dependance);
                        }
                        // On quitte le mode d'édition.
                        reset();
                    });
                    popup.getItems().add(saveItem);

                    // action : annuler édition
                    final MenuItem cancelItem = new MenuItem("Annuler l'édition");
                    cancelItem.setOnAction(event -> {
                        reset();
                    });
                    popup.getItems().add(cancelItem);

                    // action : suppression dépendance
                    final MenuItem deleteItem = new MenuItem("Supprimer dépendance", new ImageView(GeotkFX.ICON_DELETE));
                    deleteItem.setOnAction((ActionEvent event) -> {
                        final Alert alert = new Alert(CONFIRMATION, "Voulez-vous vraiment supprimer la dépendance sélectionnée ?", YES, NO);
                        final Optional<ButtonType> result = alert.showAndWait();
                        if (result.isPresent() && result.get()==YES) {
                            if (AireStockageDependance.class.isAssignableFrom(dependance.getClass())) {
                                Injector.getBean(AireStockageDependanceRepository.class).remove((AireStockageDependance)dependance);
                            } else if (AutreDependance.class.isAssignableFrom(dependance.getClass())) {
                                Injector.getBean(AutreDependanceRepository.class).remove((AutreDependance)dependance);
                            } else if (CheminAccesDependance.class.isAssignableFrom(dependance.getClass())) {
                                Injector.getBean(CheminAccesDependanceRepository.class).remove((CheminAccesDependance)dependance);
                            } else if (OuvrageVoirieDependance.class.isAssignableFrom(dependance.getClass())) {
                                Injector.getBean(OuvrageVoirieDependanceRepository.class).remove((OuvrageVoirieDependance)dependance);
                            }
                            // On quitte le mode d'édition.
                            reset();
                        }
                    });
                    popup.getItems().add(deleteItem);
                    
                    popup.show(decorationLayer, Side.TOP, e.getX(), e.getY());
                }
            }
        }

        @Override
        public void mousePressed(final MouseEvent e) {
            pressed = e.getButton();

            if(dependance != null && !newDependance && pressed == MouseButton.PRIMARY){
                // On va sélectionner un noeud sur lequel l'utilisateur a cliqué, s'il y en a un.
                helper.grabGeometryNode(e.getX(), e.getY(), editGeometry);
                decorationLayer.setNodeSelection(editGeometry);
            }

            super.mousePressed(e);
        }

        @Override
        public void mouseDragged(final MouseEvent e) {

            if(dependance != null && !newDependance && pressed == MouseButton.PRIMARY){
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
        newDependance = false;
        justCreated = false;
        decorationLayer.getGeometries().clear();
        decorationLayer.setNodeSelection(null);
        coords.clear();
        editGeometry.reset();
        dependance = null;
        helper = null;
    }
}
