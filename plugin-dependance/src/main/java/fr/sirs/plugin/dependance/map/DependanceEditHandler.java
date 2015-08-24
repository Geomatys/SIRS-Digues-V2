package fr.sirs.plugin.dependance.map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.AireStockageDependanceRepository;
import fr.sirs.core.model.AbstractDependance;
import fr.sirs.core.model.AireStockageDependance;
import fr.sirs.core.model.AutreDependance;
import fr.sirs.core.model.CheminAccesDependance;
import fr.sirs.core.model.OuvrageVoirieDependance;
import fr.sirs.plugin.dependance.PluginDependance;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import org.apache.sis.internal.referencing.j2d.AffineTransform2D;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.bean.BeanFeature;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.gui.javafx.render2d.AbstractNavigationHandler;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXPanMouseListen;
import org.geotoolkit.gui.javafx.render2d.edition.EditionHelper;
import org.geotoolkit.gui.javafx.render2d.shape.FXGeometryLayer;
import org.geotoolkit.map.FeatureMapLayer;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Contrôle les actions possibles pour le bouton d'édition et de modification de dépendances
 * sur la carte.
 *
 * @author Cédric Briançon (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
public class DependanceEditHandler extends AbstractNavigationHandler {
    private static final FilterFactory2 FF = (FilterFactory2) FactoryFinder.getFilterFactory(
            new Hints(Hints.FILTER_FACTORY, FilterFactory2.class));

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
        private final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
        private MouseButton pressed;

        public MouseListen() {
            super(DependanceEditHandler.this);
        }

        /**
         * Construit un filtre d'intersection entre une géométrie polygone et une couche de la carte.
         *
         * @param poly Le polygone choisi pour l'intersection
         * @param fl La couche de données de la carte.
         * @return Le filtre d'intersection.
         */
        private Filter toFilter(final Geometry poly, final FeatureMapLayer fl) {
            final String geoStr = fl.getCollection().getFeatureType().getGeometryDescriptor().getLocalName();
            final Expression geomField = FF.property(geoStr);

            final Geometry dataPoly = poly;
            JTS.setCRS(dataPoly, map.getCanvas().getObjectiveCRS2D());

            final Expression geomData = FF.literal(dataPoly);
            return FF.intersects(geomData,geomField);
        }

        /**
         * Transforme les coordonées fournies en géométrie exprimée dans le CRS de la carte.
         *
         * @param x Coordonnée x de la souris
         * @param y Coordonnée y de la souris
         * @return Les coordonnées de ce point.
         */
        public Coordinate toCoord(final double x, final double y) {
            final AffineTransform2D trs = map.getCanvas().getObjectiveToDisplay();
            AffineTransform dispToObj;
            try {
                dispToObj = trs.createInverse();
            } catch (NoninvertibleTransformException ex) {
                dispToObj = new AffineTransform();
                SIRS.LOGGER.log(Level.WARNING, null, ex);
            }
            final double[] crds = new double[]{x, y};
            dispToObj.transform(crds, 0, crds, 0, 1);

            return new Coordinate(crds[0], crds[1]);
        }

        /**
         * Transforme un point cliqué à la souris en un polygone.
         *
         * @param mx : Coordonnée x de la souris sur la carte (en pixel)
         * @param my : Coordonnée y de la souris sur la carte (en pixel)
         * @return une géométrie JTS (correspondant à un carré de 6x6 pixels autour des coordonnées de la souris)
         */
        private Polygon mousePositionToGeometry(final double mx, final double my) throws NoninvertibleTransformException {
            int mousePointerSize = 4;
            final Coordinate[] coord = new Coordinate[5];
            coord[0] = toCoord(mx - mousePointerSize, my - mousePointerSize);
            coord[1] = toCoord(mx - mousePointerSize, my + mousePointerSize);
            coord[2] = toCoord(mx + mousePointerSize, my + mousePointerSize);
            coord[3] = toCoord(mx + mousePointerSize, my - mousePointerSize);
            coord[4] = coord[0];

            final LinearRing lr1 = GEOMETRY_FACTORY.createLinearRing(coord);
            return GEOMETRY_FACTORY.createPolygon(lr1, null);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            final double x = e.getX();
            final double y = e.getY();

            if (MouseButton.PRIMARY.equals(e.getButton())) {
                if (dependance == null) {
                    // Recherche d'une couche de la carte qui contiendrait une géométrie là où l'utilisateur a cliqué
                    try {
                        final Polygon polygonClicked = mousePositionToGeometry(x, y);
                        final FeatureCollection aireCol = aireLayer.getCollection().subCollection(
                                QueryBuilder.filtered(aireLayer.getCollection().getFeatureType().getName(),
                                toFilter(polygonClicked, aireLayer)));
                        if (!aireCol.isEmpty()) {
                            helper = new EditionHelper(map, aireLayer);
                        } else {
                            final FeatureCollection autreCol = autreLayer.getCollection().subCollection(
                                    QueryBuilder.filtered(autreLayer.getCollection().getFeatureType().getName(),
                                    toFilter(polygonClicked, autreLayer)));
                            if (!autreCol.isEmpty()) {
                                helper = new EditionHelper(map, autreLayer);
                            } else {
                                final FeatureCollection cheminCol = cheminLayer.getCollection().subCollection(
                                        QueryBuilder.filtered(cheminLayer.getCollection().getFeatureType().getName(),
                                        toFilter(polygonClicked, cheminLayer)));
                                if (!cheminCol.isEmpty()) {
                                    helper = new EditionHelper(map, cheminLayer);
                                } else {
                                    helper = new EditionHelper(map, ouvrageLayer);
                                }
                            }
                        }
                    } catch (NoninvertibleTransformException | DataStoreException ex) {
                        SIRS.LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
                        return;
                    }

                    // La dépendance n'est pas sélectionnée, on essaye de la trouver par rapport aux coordonnées
                    // du clic de la souris.
                    final Feature feature = helper.grabFeature(x, y, true);
                    if (feature != null) {
                        // Il y a une feature là où l'utilisateur a cliqué, vérification du type
                        final Object candidate = feature.getUserData().get(BeanFeature.KEY_BEAN);
                        if (candidate instanceof AbstractDependance) {
                            dependance = (AbstractDependance)candidate;
                            // On récupère la géométrie de cet objet pour passer en mode édition
                            editGeometry.geometry.set(dependance.getGeometry());
                            // Ajout de cette géométrie dans la couche d'édition sur la carte.
                            decorationLayer.getGeometries().add(editGeometry.geometry.get());
                            newDependance = false;
                        }
                    }
                } else {
                    // La dépendance existe, on peut travailler avec sa géométrie.
                    if (newDependance) {
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
                    // Sauvegarde de la dépendance de stockage ainsi que sa géométrie qui a éventuellement été éditée.
                    dependance.setGeometry(editGeometry.geometry.get());
                    final AbstractSIRSRepository repodep = Injector.getSession().getRepositoryForClass(dependance.getClass());

                    if (dependance.getDocumentId() != null) {
                        repodep.update(dependance);
                    } else {
                        repodep.add(dependance);
                    }
                    // On quitte le mode d'édition.
                    reset();
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
    }
}
