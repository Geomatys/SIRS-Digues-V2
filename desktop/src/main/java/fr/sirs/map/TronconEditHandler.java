package fr.sirs.map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import fr.sirs.CorePlugin;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.RefRive;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.util.SirsStringConverter;
import fr.sirs.util.property.Reference;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import static javafx.scene.control.ButtonType.NO;
import static javafx.scene.control.ButtonType.YES;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
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
import org.geotoolkit.data.bean.BeanFeature;
import org.geotoolkit.display.VisitFilter;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.display2d.canvas.AbstractGraphicVisitor;
import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.container.ContextContainer2D;
import org.geotoolkit.display2d.primitive.ProjectedCoverage;
import org.geotoolkit.display2d.primitive.ProjectedFeature;
import org.geotoolkit.display2d.primitive.SearchAreaJ2D;
import org.geotoolkit.filter.identity.DefaultFeatureId;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.gui.javafx.render2d.AbstractNavigationHandler;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.render2d.FXPanMouseListen;
import org.geotoolkit.gui.javafx.render2d.edition.EditionHelper;
import org.geotoolkit.gui.javafx.render2d.shape.FXGeometryLayer;
import org.geotoolkit.gui.javafx.util.ComboBoxCompletion;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.ItemListener;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.collection.CollectionChangeEvent;
import org.opengis.filter.Id;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class TronconEditHandler extends AbstractNavigationHandler implements ItemListener {

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
    private FeatureMapLayer tronconLayer;
    private final ObjectProperty<TronconDigue> tronconProperty = new SimpleObjectProperty<>();
    private EditionHelper helper;
    private final EditionHelper.EditionGeometry editGeometry = new EditionHelper.EditionGeometry();
    private final Session session;

    private Id selectionFilter;

    // overriden variable by init();
    protected String layerName;
    protected MutableStyle style;
    protected String typeName;
    protected boolean maleGender;
    protected Class<? extends TronconDigue> tronconClass;
    protected Class parentClass;
    protected String parentLabel;
    protected boolean showRive;

    /** List of layers deactivated on tool install. They will be activated back at uninstallation. */
    private List<MapLayer> toActivateBack;

    protected void init() {
        this.layerName = CorePlugin.TRONCON_LAYER_NAME;
        this.tronconClass = TronconDigue.class;
        try {
            this.style = CorePlugin.createTronconSelectionStyle(false);
        } catch (URISyntaxException ex) {
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
        this.typeName = "tronçon";
        this.maleGender = true;
        this.parentClass = Digue.class;
        this.showRive = true;
        this.parentLabel = "à la digue";
    }


    /**
     * Constructor called directly by sub-classes
     * @param map
     */
    public TronconEditHandler(final FXMap map) {
        super();
        init();
        mouseInputListener = new MouseListen(typeName, tronconClass);

        session = Injector.getSession();
        tronconProperty.addListener((ObservableValue<? extends TronconDigue> observable, TronconDigue oldValue, TronconDigue newValue) -> {
            // IL FAUT ÉGALEMENT VÉRIFIER LES AUTRE OBJETS "CONTENUS" : POSITIONS DE DOCUMENTS, PHOTOS, PROPRIETAIRES ET GARDIENS

            if (newValue != null && !TronconUtils.getPositionableList(newValue).isEmpty()) {
                final String s = maleGender ?  "ce " :  "cette ";
                final Alert alert = new Alert(Alert.AlertType.WARNING,
                        "Attention, " + s + typeName + " contient des données. Toute modification du tracé risque de changer leur position.", ButtonType.CANCEL, ButtonType.OK);
                alert.setResizable(true);
                final Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && ButtonType.OK.equals(result.get())) {
                } else {
                    tronconProperty.set(null);
                }
            }

            editGeometry.reset();
            if (tronconProperty.get() != null) {
                editGeometry.geometry.set((Geometry) tronconProperty.get().getGeometry().clone());
            }
            updateGeometry();

            if (newValue != null) {
                selectionFilter = GO2Utilities.FILTER_FACTORY.id(
                        Collections.singleton(new DefaultFeatureId(newValue.getId())));
            } else {
                selectionFilter = null;
            }
            if (Platform.isFxApplicationThread()) {
                tronconLayer.setSelectionFilter(selectionFilter);
            } else {
                Platform.runLater(() -> tronconLayer.setSelectionFilter(selectionFilter));
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
        tronconProperty.set(null);
        final ContextContainer2D cc = (ContextContainer2D) map.getCanvas().getContainer();
        final MapContext context = cc.getContext();
        toActivateBack = new ArrayList<>();
        for(MapLayer layer : context.layers()){
            if(layer.getName().equalsIgnoreCase(layerName)){
                tronconLayer = (FeatureMapLayer) layer;
                //TODO : activate back graduation after Geotk milestone MC0044
                tronconLayer.setSelectionStyle(style);
                updateGeometry();

                layer.setSelectable(true);
                tronconLayer.addItemListener(this);
            } else if (layer.isSelectable()) {
                toActivateBack.add(layer);
                layer.setSelectable(false);
            }
        }

        helper = new EditionHelper(map, tronconLayer);
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
        if (tronconProperty.get()==null ||
                ButtonType.YES.equals(alert.showAndWait().get())) {
            super.uninstall(component);
            if (toActivateBack != null) {
                for (final MapLayer layer : toActivateBack) {
                    layer.setSelectable(true);
                }
            }
            component.removeEventHandler(MouseEvent.ANY, mouseInputListener);
            component.removeEventHandler(ScrollEvent.ANY, mouseInputListener);
            component.removeDecoration(geomlayer);
            if (tronconLayer != null) {
                tronconLayer.setSelectionStyle(style);
                tronconLayer.removeItemListener(this);
                tronconLayer.setSelectionFilter(null);
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

    /**
     * Show a dialog allowing user to create a new {@link TronconDigue} by setting
     * its libelle, {@link Digue} and {@link RefRive}. Other fields as Geometry are
     * left null.
     * @param typeName
     * @param tronconClass
     * @param maleGender
     * @param parentClass
     * @param showRive
     * @param parentLabel
     * @return Return the new troncon, or null if user has cancelled dialog.
     */
    public static TronconDigue showTronconDialog(final String typeName,
            final Class<? extends TronconDigue> tronconClass,
            final boolean maleGender, final Class parentClass,
            final boolean showRive, final String parentLabel) {
        final Session session = Injector.getBean(Session.class);
        final SirsStringConverter strConverter = new SirsStringConverter();

        final TextField nameField = new TextField();

        final Stage dialog = new Stage();
        dialog.getIcons().add(SIRS.ICON);
        if (maleGender) {
            dialog.setTitle("Nouveau " + typeName);
        } else {
            dialog.setTitle("Nouvelle " + typeName);
        }
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
        if (maleGender) {
            bp.add(new Label("Nom du " + typeName), 0, 0);
        } else {
            bp.add(new Label("Nom de la " + typeName), 0, 0);
        }
        bp.add(nameField, 0, 1);

        final ComboBox<Preview> parentChoice;
        if (parentClass != null) {
            parentChoice = new ComboBox<>(FXCollections.observableList(session.getPreviews().getByClass(parentClass)));
            parentChoice.setConverter(strConverter);
            parentChoice.setEditable(true);
            ComboBoxCompletion.autocomplete(parentChoice);

            bp.add(new Label("Rattacher " + (parentLabel == null ? "" : parentLabel)), 0, 2);
            bp.add(parentChoice, 0, 3);
        } else {
            parentChoice = null;
        }

        final ComboBox<RefRive> rives;
        if (showRive) {
            rives = new ComboBox<>(FXCollections.observableList(session.getRepositoryForClass(RefRive.class).getAll()));
            rives.setConverter(strConverter);
            bp.add(new Label("Sur la rive"), 0, 4);
            bp.add(rives, 0, 5);
        } else {
            rives = null;
        }

        final Button finishBtn = new Button("Terminer");
        // Do not allow creation of a troncon without a name.
        finishBtn.disableProperty().bind(nameField.textProperty().isEmpty());

        final Button cancelBtn = new Button("Annuler");
        cancelBtn.setCancelButton(true);

        finishBtn.setOnAction((ActionEvent e)-> {
            dialog.close();
        });

        cancelBtn.setOnAction((ActionEvent e)-> {
            nameField.setText(null);
            dialog.close();
        });

        final ButtonBar babar = new ButtonBar();
        babar.getButtons().addAll(cancelBtn, finishBtn);

        final BorderPane main = new BorderPane(bp);
        main.setBottom(babar);

        dialog.setScene(new Scene(main));
        dialog.showAndWait();

        String tronconName = nameField.getText();
        if (tronconName == null || tronconName.isEmpty()) {
            return null;
        } else {
            final TronconDigue tmpTroncon = Injector.getSession().getElementCreator().createElement(tronconClass);
            tmpTroncon.setLibelle(tronconName);
            final Preview parentPreview = parentChoice == null? null : parentChoice.getValue();
            if (parentPreview != null) {
                try {
                    final Method parentMethod = findParentMethod(tronconClass, parentClass);
                    if(parentMethod!=null){
                        parentMethod.invoke(tmpTroncon, parentPreview.getElementId());
                    }
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException ex) {
                    SIRS.LOGGER.log(Level.WARNING, null, ex);
                }
            }

            final RefRive rive = rives == null? null : rives.getValue();
            if (rive != null) {
                tmpTroncon.setTypeRiveId(rive.getId());
            }
            return tmpTroncon;
        }
    }

    /**
     * Tries to found a method of the childClass which references the parentClass.
     * @param childClass
     * @param parentClass
     * @return
     * @throws java.lang.NoSuchMethodException
     */
    public static Method findParentMethod(final Class childClass, final Class parentClass) throws NoSuchMethodException{
        for(final Method method : childClass.getMethods()){
            final Reference ref = method.getAnnotation(Reference.class);
            if(ref!=null && ref.ref()!=null && ref.ref().equals(parentClass)) {
                // Les annotations Reference portent sur les accesseurs il faut retrouver le modifieur correspondant.
                return childClass.getMethod(method.getName().replaceFirst("get", "set"), String.class);
            }
        }
        return null;
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
                tronconLayer.setSelectionFilter(selectionFilter);
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
        private final Class tronconClass;


        public MouseListen(String typeName, Class<? extends TronconDigue> tronconClass) {
            super(TronconEditHandler.this);
            popup.setAutoHide(true);
            this.tronconClass = tronconClass;
            this.typeName = typeName;
        }

        @Override
        public void mouseClicked(final MouseEvent e) {
            if(tronconLayer==null) return;

            startX = getMouseX(e);
            startY = getMouseY(e);
            mousebutton = e.getButton();

            if (tronconProperty.get() == null) {
                //actions en l'absence de troncon

                if (mousebutton == MouseButton.PRIMARY) {
                    //selection d'un troncon
                    final AbstractGraphicVisitor visitor = new AbstractGraphicVisitor() {
                        final HashSet<TronconDigue> foundElements = new HashSet<>();
                        @Override
                        public void visit(ProjectedFeature feature, RenderingContext2D context, SearchAreaJ2D area) {
                            Object userData = feature.getCandidate().getUserData().get(BeanFeature.KEY_BEAN);
                            if (userData instanceof TronconDigue) {
                                //on recupere le troncon complet, celui ci n'est qu'une mise a plat
                                final TronconDigue trc = session.getRepositoryForClass(TronconDigue.class).get(((TronconDigue)userData).getDocumentId());
                                foundElements.add(trc);
                            }
                        }

                        @Override
                        public void visit(ProjectedCoverage coverage, RenderingContext2D context, SearchAreaJ2D area) {
                            SIRS.LOGGER.log(Level.FINE, "Coverage elements are not managed yet.");
                        }

                        @Override
                        public void endVisit() {
                            SIRS.LOGGER.log(Level.FINE, "End of visit.");
                            super.endVisit();
                            if (foundElements.size() == 1) {
                                tronconProperty.set(foundElements.iterator().next());
                            } else if (foundElements.size() > 1) {
                                final Iterator<TronconDigue> it = foundElements.iterator();
                                final ObservableList<MenuItem> items = popup.getItems();
                                items.clear();
                                while (it.hasNext()) {
                                    final TronconDigue current = it.next();
                                    final MenuItem item = new MenuItem(session.generateElementTitle(current));
                                    item.setOnAction((ActionEvent ae) -> tronconProperty.set(current));
                                    items.add(item);
                                }
                                popup.show(map, e.getScreenX(), e.getScreenY());
                            }
                        }
                    };

                    Rectangle2D.Double searchArea = new Rectangle2D.Double(
                            getMouseX(e) - 6, getMouseY(e) - 6, 6 * 2, 6 * 2);
                    map.getCanvas().getGraphicsIn(searchArea, visitor, VisitFilter.INTERSECTS);


                } else if (mousebutton == MouseButton.SECONDARY) {
                    // popup :
                    // -commencer un nouveau troncon
                    popup.getItems().clear();

                    final String title = maleGender ? "Créer un nouveau " + typeName : "Créer une nouvelle " + typeName;
                    final MenuItem createItem = new MenuItem(title);
                    createItem.setOnAction((ActionEvent event) -> {
                        final TronconDigue tmpTroncon = showTronconDialog(typeName, tronconClass, maleGender, parentClass, showRive, parentLabel);
                        if (tmpTroncon == null) {
                            return;
                        }

                        final Coordinate coord1 = helper.toCoord(e.getX() - 20, e.getY());
                        final Coordinate coord2 = helper.toCoord(e.getX() + 20, e.getY());
                        try {
                            Geometry geom = EditionHelper.createLine(coord1, coord2);
                            //convertion from base crs
                            geom = JTS.transform(geom, CRS.findMathTransform(map.getCanvas().getObjectiveCRS2D(), session.getProjection(), true));
                            JTS.setCRS(geom, session.getProjection());
                            tmpTroncon.setGeometry(geom);

                            //sauvegarde du troncon
                            session.getRepositoryForClass(tronconClass).add(tmpTroncon);
                            TronconUtils.updateSRElementaire(tmpTroncon, session);

                            // Prepare l'edition du tronçon
                            tronconProperty.set(tmpTroncon);

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
                    final Geometry geom = editGeometry.geometry.get();
                    if (geom instanceof LineString) {
                        result = helper.insertNode((LineString) geom, startX, startY);
                    } else if (geom instanceof Polygon) {
                        result = helper.insertNode((Polygon) geom, startX, startY);
                    } else if (geom instanceof GeometryCollection) {
                        result = helper.insertNode((GeometryCollection) geom, startX, startY);
                    } else {
                        result = geom;
                    }
                    editGeometry.geometry.set(result);
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

                    final String prefix = maleGender ? "le " : "la ";

                    if (editGeometry.geometry != null) {
                        // Si le tronçon est vide, on peut inverser son tracé
                        // IL FAUT ÉGALEMENT VÉRIFIER LES AUTRE OBJETS "CONTENUS" : POSITIONS DE DOCUMENTS, PHOTOS, PROPRIETAIRES ET GARDIENS
                        if (TronconUtils.getObjetList(tronconProperty.get()).isEmpty()) {
                            if (!popup.getItems().isEmpty()) {
                                popup.getItems().add(new SeparatorMenuItem());
                            }
                            final String title    = maleGender ? "Inverser le tracé du " + typeName : "Inverser le tracé de la " + typeName;
                            final MenuItem invert = new MenuItem(title);
                            invert.setOnAction((ActionEvent ae) -> {
                                // HACK : On est forcé de sauvegarder le tronçon pour mettre à jour le SR élémentaire.
                                tronconProperty.get().setGeometry(editGeometry.geometry.get().reverse());
                                session.getRepositoryForClass(tronconClass).update(tronconProperty.get());
                                TronconUtils.updateSRElementaire(tronconProperty.get(), session);
                                tronconProperty.set(null);
                            });
                            popup.getItems().add(invert);
                        }

                        // On peut sauvegarder ou annuler nos changements si la geometrie du tronçon
                        // diffère de celle de l'éditeur.
                        if (!editGeometry.geometry.get().equals(tronconProperty.get().getGeometry())) {
                            final MenuItem saveItem = new MenuItem("Sauvegarder les modifications");
                            saveItem.setOnAction((ActionEvent event) -> {
                                tronconProperty.get().setGeometry(editGeometry.geometry.get());
                                session.getRepositoryForClass(tronconClass).update(tronconProperty.get());

                                TronconUtils.updateSRElementaire(tronconProperty.get(), session);
                                //on recalcule les geometries des positionables du troncon.
                                TronconUtils.updatePositionableGeometry(tronconProperty.get(), session);

                                tronconProperty.set(null);
                            });
                            popup.getItems().add(saveItem);

                        }

                        //action : annuler edition
                        final String cancelTitle = (!editGeometry.geometry.get().equals(tronconProperty.get().getGeometry()))?
                                "Annuler les modifications" : "Désélectionner "+ prefix + typeName;

                        final MenuItem cancelItem = new MenuItem(cancelTitle);
                        cancelItem.setOnAction((ActionEvent event) -> {
                            tronconProperty.set(null);
                        });
                        popup.getItems().add(cancelItem);
                    }

                    //action : suppression du troncon
                    if (!popup.getItems().isEmpty()) {
                        popup.getItems().add(new SeparatorMenuItem());
                    }

                    final MenuItem deleteItem = new MenuItem("Supprimer " + typeName, new ImageView(GeotkFX.ICON_DELETE));
                    deleteItem.setOnAction((ActionEvent event) -> {
                        final Alert alert = new Alert(CONFIRMATION, "Voulez-vous vraiment supprimer " + prefix + typeName + " ainsi que les systèmes de repérage et tous les positionnables qui le réfèrent ?", YES, NO);
                        final Optional<ButtonType> result = alert.showAndWait();
                        if (result.isPresent() && result.get()==YES) {
                            session.getRepositoryForClass(tronconClass).remove(tronconProperty.get());
                            tronconProperty.set(null);
                        }
                    });
                    popup.getItems().add(deleteItem);

                    popup.show(geomlayer, Side.TOP, e.getX(), e.getY());
                }
            }
        }

        @Override
        public void mousePressed(final MouseEvent e) {
            super.mousePressed(e);
            if(tronconProperty==null) return;

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
