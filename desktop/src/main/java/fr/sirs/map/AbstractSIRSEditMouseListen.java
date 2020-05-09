/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.core.model.AvecGeometrie;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseButton;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXPanMouseListen;
import org.geotoolkit.gui.javafx.render2d.edition.EditionHelper;
import org.geotoolkit.gui.javafx.render2d.shape.FXGeometryLayer;

/**
 *
 * Mouse listener pour l'édition de géométrie depuis la carte.
 *
 * @author Matthieu Bastianelli (Geomatys)
 * @param <G>
 */
public abstract class AbstractSIRSEditMouseListen<G extends AvecGeometrie> extends FXPanMouseListen {


    protected double startX;
    protected double startY;

    double initialX;
    double initialY;

    protected final ContextMenu popup = new ContextMenu();
    protected MouseButton pressed;
    protected final AbstractSIRSEditHandler editHandler;
    protected final FXMap map;
    protected final EditionHelper.EditionGeometry editGeometry;
    protected final FXGeometryLayer geomLayer;
    protected final Class<G> editedClass;
    protected G editedObjet = null;
    protected final SimpleObjectProperty<G> editedObjetProperty = new SimpleObjectProperty<>(editedObjet);
//    protected EditModeObjet mode = EditModeObjet.NONE;
    protected final ObjectProperty<EditModeObjet> modeProperty = new SimpleObjectProperty<>(EditModeObjet.NONE);

    protected EditionHelper objetHelper;

    protected final List<Coordinate> coords = new ArrayList<>();

    /**
     * Vrai si une dépendance vient d'être créée.
     */
//    private boolean newCreatedObjet = false;

    /**
     * Définit le type de géométries à dessiner, pour les dépendances de types
     * "ouvrages de voirie" ou "autres" pour lesquelles plusieurs choix sont
     * possibles.
     */
    protected Class newGeomType = Point.class;

    /**
     * Vrai si la {@linkplain #coords liste des coordonnées} de la
     * {@linkplain #editGeometry géométrie} vient d'être créée.
     */
    protected boolean justCreated = false;

    public AbstractSIRSEditMouseListen(final AbstractSIRSEditHandler sirsEditHandler) {
        super(sirsEditHandler);
        editedClass = sirsEditHandler.objetClass;
        editHandler = sirsEditHandler;
        map = editHandler.getMap();
        editGeometry = editHandler.getEditionGeometry();
        geomLayer = editHandler.getGeometryLayer();
//        editHandler.editedObjetProperty.addListener((ov, t, newValue) -> {
//            editedObjet =
//        });
        editedObjetProperty.bindBidirectional(editHandler.getEditedObjetProperty());
        modeProperty.bindBidirectional(editHandler.getModeProperty());

        objetHelper = editHandler.helperObjet; //Ou binding?

    }

//    public void setNewCreatedObjet(final boolean newCreatedObjet) {
//        this.newCreatedObjet = newCreatedObjet;
//    }

//    @Override
//    public void mouseClicked(final MouseEvent e) {
//        final double x = e.getX();
//        final double y = e.getY();
//
//        if (MouseButton.PRIMARY.equals(e.getButton())) {
//            if ( (editedObjet == null) || (NONE.equals(modeProperty.get())) ) {
//               selectObjet(x, y);
//            } else {
//                // L'objet existe, on peut travailler avec sa géométrie.
////                if (newCreatedObjet) {
//                switch (modeProperty.get()) {
//                    case CREATE_OBJET :
//                        createNewGeometryForObjet(x, y);
//                        break;
//
//                    case EDIT_OBJET :
//                        modifyObjetGeometry(e, x, y);
//                        break;
//                }
//            }
//        } else if (MouseButton.SECONDARY.equals(e.getButton())) {
//            if (editedObjet == null) {
//                chooseTypesAndCreate();
//            } else {
//                concludeTheEdition(x, y);
//            }
//        }
//    }
//
//    @Override
//    public void mousePressed(final MouseEvent e) {
//        pressed = e.getButton();
//
////        if (editedObjet != null && !CREATE_OBJET.equals(modeProperty.get()) && pressed == MouseButton.PRIMARY) {
//        if (EDIT_OBJET.equals(modeProperty.get()) && pressed == MouseButton.PRIMARY) {
//            // On va sélectionner un noeud sur lequel l'utilisateur a cliqué, s'il y en a un.
//
//            // Le helper peut être null si on a choisi d'activer ce handler pour une dépendance existante,
//            // sans passer par le clic droit pour choisir un type de dépendance.
//            objetHelper = editHandler.getHelperObjet();
//
//            objetHelper.grabGeometryNode(e.getX(), e.getY(), editGeometry);
//            geomLayer.setNodeSelection(editGeometry);
//        }
//
//        super.mousePressed(e);
//    }
//
//    @Override
//    public void mouseDragged(final MouseEvent e) {
//
////        if (editedObjet != null && !newCreatedObjet && pressed == MouseButton.PRIMARY) {
//        if (EDIT_OBJET.equals(modeProperty.get()) && pressed == MouseButton.PRIMARY) {
//            // On déplace le noeud sélectionné
//            editGeometry.moveSelectedNode(objetHelper.toCoord(e.getX(), e.getY()));
//            geomLayer.getGeometries().setAll(editGeometry.geometry.get());
//            return;
//        }
//
//        super.mouseDragged(e);
//    }

    /**
     * Réinitialise la carte et vide la géométrie en cours d'édition.
     */
    private void reset() {
//        newCreatedObjet = false;
        justCreated = false;
        geomLayer.getGeometries().clear();
        geomLayer.setNodeSelection(null);
        coords.clear();
        editGeometry.reset();
        editedObjet = null;
    }

    // ==========================  UTILITIES  ==================================

//    protected abstract void selectObjet(final double x, final double y) ;
//         // Recherche d'une couche de la carte qui contiendrait une géométrie là où l'utilisateur a cliqué
////                final Rectangle2D clickArea = new Rectangle2D.Double(e.getX() - 2, e.getY() - 2, 4, 4);
//
//        //recherche d'un object a editer
//        //selection d'un troncon
//        if (objetHelper == null) {
//            objetHelper = editHandler.getHelperObjet();
//        }
//        final Feature feature = objetHelper.grabFeature(x, y, false);
//        if (feature != null) {
//            Object bean = feature.getUserData().get(BeanFeature.KEY_BEAN);
//            if (editedClass.isInstance(bean)) {
//                editedObjet = editedClass.cast(bean);
//                // On récupère la géométrie de cet objet pour passer en mode édition
//                editGeometry.geometry.set((Geometry) editedObjet.getGeometry().clone());
//                // Ajout de cette géométrie dans la couche d'édition sur la carte.
//                geomLayer.getGeometries().setAll(editGeometry.geometry.get());
////                        newCreatedObjet = false;
//                modeProperty.setValue(EDIT_OBJET);
//
//            }
//
//        }
////                map.getCanvas().getGraphicsIn(clickArea, new AbstractGraphicVisitor() {
////                    @Override
////                    public void visit(ProjectedFeature graphic, RenderingContext2D context, SearchAreaJ2D area) {
////                        final Object bean = graphic.getCandidate().getUserData().get(BeanFeature.KEY_BEAN);
////                        if (editedClass.isInstance(bean)) {
////                            objetHelper = new EditionHelper(map, graphic.getLayer());
////                            editedObjet = editedClass.cast(bean);
////                            // On récupère la géométrie de cet objet pour passer en mode édition
////                            editGeometry.geometry.set((Geometry) editedObjet.getGeometry().clone());
////                            // Ajout de cette géométrie dans la couche d'édition sur la carte.
////                            geomLayer.getGeometries().setAll(editGeometry.geometry.get());
////                            newCreatedObjet = false;
////                        }
////                    }
////
////                    @Override
////                    public boolean isStopRequested() {
////                        return objetHelper != null;
////                    }
////
////                    @Override
////                    public void visit(ProjectedCoverage coverage, RenderingContext2D context, SearchAreaJ2D area) {
////                    }
////                }, VisitFilter.INTERSECTS);
//    }



//    protected abstract void createNewGeometryForObjet(final double x, final double y);
//
//                    // Le helper peut être null si on a choisi d'activer ce handler pour une dépendance existante,
//                    // sans passer par le clic droit pour choisir un type de dépendance.
//                    final Class clazz = editedObjet.getClass();
//                    if (objetHelper == null) {
//                        if (editedClass.isAssignableFrom(clazz)) {
////                            objetHelper = new EditionHelper(map, editHandler.objetLayer);
//                            objetHelper = editHandler.getHelperObjet();
//                        }
//                    }
//
//                    // le choix fait par l'utilisateur dans le panneau de création de dépendance.
//                    final Class geomClass = newGeomType;
//
//                    // On vient de créer la dépendance, le clic gauche va permettre d'ajouter des points.
//                    if (Point.class.isAssignableFrom(geomClass)) {
//                        coords.clear();
//                        coords.add(objetHelper.toCoord(x, y));
//                    } else {
//                        if (justCreated) {
//                            justCreated = false;
//                            //we must modify the second point since two point where added at the start
//                            if (Polygon.class.isAssignableFrom(geomClass)) {
//                                coords.remove(2);
//                            }
//                            coords.remove(1);
//                            coords.add(objetHelper.toCoord(x, y));
//                            if (Polygon.class.isAssignableFrom(geomClass)) {
//                                coords.add(objetHelper.toCoord(x, y));
//                            }
//                        } else if (coords.isEmpty()) {
//                            justCreated = true;
//                            //this is the first point of the geometry we create
//                            //add 3 points that will be used when moving the mouse around for polygons,
//                            //for lines just add 2 points.
//                            coords.add(objetHelper.toCoord(x, y));
//                            coords.add(objetHelper.toCoord(x, y));
//                            if (Polygon.class.isAssignableFrom(geomClass)) {
//                                coords.add(objetHelper.toCoord(x, y));
//                            }
//                        } else {
//                            // On ajoute le point en plus.
//                            justCreated = false;
//                            coords.add(objetHelper.toCoord(x, y));
//                        }
//                    }
//
//                    // Création de la géométrie à éditer à partir des coordonnées
//                    if (Polygon.class.isAssignableFrom(geomClass)) {
//                        editGeometry.geometry.set(EditionHelper.createPolygon(coords));
//                    } else if (LineString.class.isAssignableFrom(geomClass)) {
//                        editGeometry.geometry.set(EditionHelper.createLine(coords));
//                    } else {
//                        editGeometry.geometry.set(EditionHelper.createPoint(coords.get(0)));
//                    }
//                    JTS.setCRS(editGeometry.geometry.get(), map.getCanvas().getObjectiveCRS2D());
//                    geomLayer.getGeometries().setAll(editGeometry.geometry.get());
//
//                    if (Point.class.isAssignableFrom(geomClass)) {
//                        // Pour un nouveau point ajouté, on termine l'édition directement.
//                        final Geometry geometry = editGeometry.geometry.get();
//                        if (editedObjet instanceof AvecSettableGeometrie) {
//                            ((AvecSettableGeometrie) editedObjet).setGeometry(geometry);
//                        } else if ( (editedObjet instanceof BorneDigue) && (geometry instanceof Point) ) {
//                            ((BorneDigue) editedObjet).setGeometry((Point) geometry);
//                        } else {
//                            throw new IllegalStateException("Impossible d'associer le type de géométrie éditée au le type d'objet édité");
//                        }
//                        final AbstractSIRSRepository repo = Injector.getSession().getRepositoryForClass(editedObjet.getClass());
//
//                        if (editedObjet.getDocumentId() != null) {
//                            repo.update(editedObjet);
//                        } else {
//                            repo.add(editedObjet);
//                        }
//                        // On quitte le mode d'édition.
//                        reset();
//                    }
//    }

    /**
     * On réédite une géométrie existante, le double clic gauche va nous
     * permettre d'ajouter un nouveau point à la géométrie, si ce n'est pas un point.
     * @param e
     * @param x
     * @param y
     */
//    protected abstract void modifyObjetGeometry(final MouseEvent e, final double x, final double y);
//        final Geometry tempEditGeom = editGeometry.geometry.get();
//        if (!Point.class.isAssignableFrom(tempEditGeom.getClass()) && e.getClickCount() >= 2) {
//            final Geometry result;
//            if (tempEditGeom instanceof Polygon) {
//                result = objetHelper.insertNode((Polygon) editGeometry.geometry.get(), x, y);
//            } else {
//                result = objetHelper.insertNode((LineString) editGeometry.geometry.get(), x, y);
//            }
//            editGeometry.geometry.set(result);
//            geomLayer.getGeometries().setAll(editGeometry.geometry.get());
//        }
//    }

    /**
     * L'objet n'existe pas, on en créé une nouvelle après avoir choisi son type
     * et le type de géométrie à dessiner.
     */
//    protected void chooseTypesAndCreate() {

//                final Stage stage = new Stage();
//                stage.getIcons().add(SIRS.ICON);
//                stage.setTitle("Création d'objet");
//                stage.initModality(Modality.WINDOW_MODAL);
//                stage.setAlwaysOnTop(true);
//                final GridPane gridPane = new GridPane();
//                gridPane.setVgap(10);
//                gridPane.setHgap(5);
//                gridPane.setPadding(new Insets(10));
//                gridPane.add(new Label("Choisir un type de d'objet"), 0, 0);
//
//                final ComboBox<String> geomTypeBox = new ComboBox<>();
//                geomTypeBox.setItems(FXCollections.observableArrayList("Ponctuel", "Linéaire", "Surfacique"));
//                geomTypeBox.getSelectionModel().selectFirst();
//                final Label geomChoiceLbl = new Label("Choisir une forme géométrique");
//                geomChoiceLbl.visibleProperty().bind(geomTypeBox.visibleProperty());
//                gridPane.add(geomChoiceLbl, 0, 1);
//                gridPane.add(geomTypeBox, 1, 1);
//
//                final Button validateBtn = new Button("Valider");
//                validateBtn.setOnAction(event -> stage.close());
//                gridPane.add(validateBtn, 2, 3);
//
//                final Scene sceneChoices = new Scene(gridPane);
//                stage.setScene(sceneChoices);
//                stage.showAndWait();
//
////                final Class clazz = DesordreDependance.class;
////                objetHelper = new EditionHelper(map, objetLayer);
//                objetHelper = editHandler.getHelperObjet();
//
//                final AbstractSIRSRepository<G> repo = Injector.getSession().getRepositoryForClass(editedClass);
//                editedObjet = repo.create();
////                newCreatedObjet = true;
//                modeProperty.setValue(CREATE_OBJET);
//
//
//                switch (geomTypeBox.getSelectionModel().getSelectedItem()) {
//                    case "Ponctuel":
//                        newGeomType = Point.class;
//                        break;
//                    case "Linéaire":
//                        newGeomType = LineString.class;
//                        break;
//                    case "Surfacique":
//                        newGeomType = Polygon.class;
//                        break;
//                    default:
//                        newGeomType = Point.class;
//                }
//    }

//    protected abstract void concludeTheEdition(final double x, final double y);
//
//                // popup :
//                // -suppression d'un noeud
//                // -sauvegarder
//                // -annuler édition
//                // -supprimer dépendance
//                popup.getItems().clear();
//
////                objetHelper = new EditionHelper(map, objetLayer);
//                objetHelper = editHandler.getHelperObjet();
//
//                //action : suppression d'un noeud
//                if (editGeometry.geometry.get() != null) {
////                    objetHelper.grabGeometryNode(e.getX(), e.getY(), editGeometry);
//                    objetHelper.grabGeometryNode(x, y, editGeometry);
//                    if (editGeometry.selectedNode[0] >= 0) {
//                        final MenuItem item = new MenuItem("Supprimer noeud");
//                        item.setOnAction((ActionEvent event) -> {
//                            editGeometry.deleteSelectedNode();
//                            geomLayer.setNodeSelection(null);
//                            geomLayer.getGeometries().setAll(editGeometry.geometry.get());
//                        });
//                        popup.getItems().add(item);
//                    }
//                }
//
//                // action : sauvegarde
//                // Sauvegarde de l'objet de stockage ainsi que sa géométrie qui a éventuellement été éditée.
//                final MenuItem saveItem = new MenuItem("Sauvegarder");
//                saveItem.setOnAction((ActionEvent event) -> {
//                    ((AvecSettableGeometrie) editedObjet).setGeometry(editGeometry.geometry.get());
//                    final AbstractSIRSRepository repo = Injector.getSession().getRepositoryForClass(editedObjet.getClass());
//
//                    if (editedObjet.getDocumentId() != null) {
//                        repo.update(editedObjet);
//                    } else {
//                        repo.add(editedObjet);
//                    }
//                    // On quitte le mode d'édition.
//                    reset();
//                });
//                popup.getItems().add(saveItem);
//
//                // action : annuler édition
//                final MenuItem cancelItem = new MenuItem("Annuler l'édition");
//                cancelItem.setOnAction(event -> {
//                    reset();
//                });
//                popup.getItems().add(cancelItem);
//
//                // action : suppression de l'objet
//                final MenuItem deleteItem = new MenuItem("Supprimer l'élément", new ImageView(GeotkFX.ICON_DELETE));
//                deleteItem.setOnAction((ActionEvent event) -> {
//                    final Alert alert = new Alert(CONFIRMATION, "Voulez-vous vraiment supprimer l'élément sélectionné ?", YES, NO);
//                    final Optional<ButtonType> result = alert.showAndWait();
//                    if (result.isPresent() && result.get() == YES) {
//                        Injector.getSession().getRepositoryForClass(editedClass).remove(editedObjet);
//                        // On quitte le mode d'édition.
//                        reset();
//                    }
//                });
//                popup.getItems().add(deleteItem);
//
////                popup.show(geomLayer, Side.TOP, e.getX(), e.getY());
//                popup.show(geomLayer, Side.TOP, x, y);
//    }
    //============================ End Utilities ===============================
}
