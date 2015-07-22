package fr.sirs.theme.ui;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import static fr.sirs.SIRS.CRS_WGS84;
import static fr.sirs.SIRS.ICON_CHECK_CIRCLE;
import static fr.sirs.SIRS.ICON_EXCLAMATION_TRIANGLE;
import static fr.sirs.SIRS.ICON_IMPORT_WHITE;
import static fr.sirs.SIRS.ICON_VIEWOTHER_WHITE;
import fr.sirs.core.LinearReferencingUtilities;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.component.AbstractSIRSRepository;
import org.geotoolkit.gui.javafx.util.TaskManager;
import fr.sirs.core.component.SystemeReperageRepository;
import fr.sirs.core.model.AvecForeignParent;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.SystemeReperageBorne;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.util.SirsStringConverter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebView;
import org.apache.sis.util.ArgumentChecks;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.gui.javafx.util.ComboBoxCompletion;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.LinearReferencing;
import org.geotoolkit.referencing.LinearReferencing.ProjectedPoint;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 * Form editor allowing to update linear and geographic/projected position of a
 * {@link Positionable} element.
 *
 * @author Johann Sorel (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class FXPositionablePane extends BorderPane {

    private static final NumberFormat DISTANCE_FORMAT = new DecimalFormat("0.#");

    @FXML private Button uiImport;
    @FXML private Button uiView;

    @FXML private ToggleButton uiTypeBorne;
    @FXML private ToggleButton uiTypeCoord;
    @FXML private ProgressIndicator uiLoading;

    // PR Information
    @FXML private Label uiPRDebut;
    @FXML private Label uiPRFin;
    private final FloatProperty prDebut = new SimpleFloatProperty();
    private volatile FloatProperty prFin = new SimpleFloatProperty();

    // Borne mode
    @FXML private GridPane uiBornePane;
    @FXML private ComboBox<SystemeReperage> uiSRs;
    @FXML private ComboBox<BorneDigue> uiBorneStart;
    @FXML private ComboBox<BorneDigue> uiBorneEnd;
    @FXML private RadioButton uiAmontStart;
    @FXML private RadioButton uiAmontEnd;
    @FXML private RadioButton uiAvalStart;
    @FXML private RadioButton uiAvalEnd;
    @FXML private Spinner<Double> uiDistanceStart;
    @FXML private Spinner<Double> uiDistanceEnd;
    @FXML private ImageView uiImageStartValid;
    @FXML private ImageView uiImageEndValid;

    // Coordinate mode
    @FXML private GridPane uiCoordPane;
    @FXML private ComboBox<CoordinateReferenceSystem> uiCRSs;
    @FXML private Spinner<Double> uiLongitudeStart;
    @FXML private Spinner<Double> uiLongitudeEnd;
    @FXML private Spinner<Double> uiLatitudeStart;
    @FXML private Spinner<Double> uiLatitudeEnd;

    /** A flag to notify when a component update is running, used to lock graphic components */
    private final IntegerProperty computingRunning = new SimpleIntegerProperty(0);

    private final ObjectProperty<Positionable> positionableProperty = new SimpleObjectProperty<>();
    private final BooleanProperty disableFieldsProperty = new SimpleBooleanProperty(true);
    private final CoordinateReferenceSystem baseCrs = Injector.getSession().getProjection();

    /**
     * Reference to TronconDigue parent of the current positionable
     */
    private TronconDigue troncon;
    private LinearReferencing.SegmentInfo[] tronconSegments;

    private final LinearChangeListener geoStartUpdater;
    private final LinearChangeListener geoEndUpdater;
    private final GeographicChangeListener linearStartUpdater;
    private final GeographicChangeListener linearEndUpdater;
    private final SRChangeListener sRChangeListenerStart;
    private final SRChangeListener sRChangeListenerEnd;
    private final DistanceChangeListener distanceChangeListenerStart;
    private final DistanceChangeListener distanceChangeListenerEnd;
    private final ChangeListener<CoordinateReferenceSystem> crsChangeListener;

    public FXPositionablePane() {
        SIRS.loadFXML(this, Positionable.class);

        uiImport.setGraphic(new ImageView(ICON_IMPORT_WHITE));
        uiView.setGraphic(new ImageView(ICON_VIEWOTHER_WHITE));

        final SirsStringConverter sirsStringConverter = new SirsStringConverter();
        uiSRs.setConverter(sirsStringConverter);
        uiCRSs.setConverter(sirsStringConverter);
        uiBorneStart.setConverter(sirsStringConverter);
        uiBorneStart.setEditable(true);
        uiBorneEnd.setConverter(sirsStringConverter);
        uiBorneEnd.setEditable(true);

        ComboBoxCompletion.autocomplete(uiBorneStart);
        ComboBoxCompletion.autocomplete(uiBorneEnd);

        //liste par défaut des systemes de coordonnées
        final ObservableList<CoordinateReferenceSystem> crss = FXCollections.observableArrayList();
        crss.add(CRS_WGS84);
        crss.add(baseCrs);
        uiCRSs.setItems(crss);
        uiCRSs.getSelectionModel().clearAndSelect(1);

        //affichage des panneaux coord/borne
        final ToggleGroup group = new ToggleGroup();
        uiTypeBorne.setToggleGroup(group);
        uiTypeCoord.setToggleGroup(group);
        group.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
            if(newValue==null) group.selectToggle(uiTypeCoord);
        });

        /*
         * COMPONENT ACTIVATION BINDINGS
         */
        // Editability over view mode (consultation or edition).
        final BooleanBinding disabledBinding = disableFieldsProperty.or(disabledProperty()).or(positionableProperty.isNull());
        uiImport.visibleProperty().bind(disableFieldsProperty.not().and(uiTypeCoord.selectedProperty()));


        uiSRs.disableProperty().bind(disabledBinding);
        uiBorneStart.disableProperty().bind(disabledBinding);
        uiBorneEnd.disableProperty().bind(disabledBinding);

        uiAmontStart.disableProperty().bind(disabledBinding);
        uiAvalStart.disableProperty().bind(disabledBinding);
        uiAmontStart.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(uiAvalStart.isSelected()==uiAmontStart.isSelected()) uiAvalStart.setSelected(!uiAmontStart.isSelected());
            }
        });
        uiAmontStart.setSelected(true);
        uiAmontEnd.disableProperty().bind(disabledBinding);
        uiAvalEnd.disableProperty().bind(disabledBinding);
        uiAmontEnd.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(uiAvalEnd.isSelected()==uiAmontEnd.isSelected()) uiAvalEnd.setSelected(!uiAmontEnd.isSelected());
            }
        });
        uiAmontEnd.setSelected(true);

        uiDistanceStart.disableProperty().bind(disabledBinding);
        uiDistanceEnd.disableProperty().bind(disabledBinding);

        uiDistanceStart.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE, 0,1));
        uiDistanceEnd.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE, 0,1));
        uiLongitudeStart.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE, 0,1));
        uiLatitudeStart.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE, 0,1));
        uiLongitudeEnd.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE, 0,1));
        uiLatitudeEnd.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE, 0,1));
        uiDistanceStart.setEditable(true);
        uiDistanceEnd.setEditable(true);
        uiLongitudeStart.setEditable(true);
        uiLatitudeStart.setEditable(true);
        uiLongitudeEnd.setEditable(true);
        uiLatitudeEnd.setEditable(true);

        uiLongitudeStart.disableProperty().bind(disabledBinding);
        uiLatitudeStart.disableProperty().bind(disabledBinding);
        uiLongitudeEnd.disableProperty().bind(disabledBinding);
        uiLatitudeEnd.disableProperty().bind(disabledBinding);

        // Bind progress display and field disabling to computing state. It allows to "lock" the component when updating its content.
        BooleanBinding loadingState = computingRunning.greaterThan(0);
        uiLoading.visibleProperty().bind(loadingState);
        disableProperty().bind(loadingState);

        // Position mode : linear or geographic
        uiCoordPane.visibleProperty().bind(uiTypeCoord.selectedProperty());
        uiBornePane.visibleProperty().bind(uiTypeBorne.selectedProperty());

        /*
         * DATA LISTENERS
         */
        crsChangeListener = this::updateGeoCoord;
        sRChangeListenerStart = new SRChangeListener(Borne.START);
        sRChangeListenerEnd = new SRChangeListener(Borne.END);
        positionableProperty.addListener(this::updateField);

        // compute back linear referencing when geographic point is changed

        //compute back geographic points when linear referencing changes.
        geoStartUpdater = new LinearChangeListener(
                uiBorneStart, uiLongitudeStart.getValueFactory().valueProperty(),
                uiLatitudeStart.getValueFactory().valueProperty(), prDebut,
                uiDistanceStart.getValueFactory().valueProperty(), uiAmontStart.selectedProperty());

        geoEndUpdater = new LinearChangeListener(
                uiBorneEnd, uiLongitudeEnd.getValueFactory().valueProperty(),
                uiLatitudeEnd.getValueFactory().valueProperty(), prFin,
                uiDistanceEnd.getValueFactory().valueProperty(), uiAmontEnd.selectedProperty());

        // Compute back linear referencing when geographic changes
        linearStartUpdater = new GeographicChangeListener(
                uiBorneStart.valueProperty(), uiDistanceStart.getValueFactory().valueProperty(),
                uiAmontStart.selectedProperty(), uiLongitudeStart.getValueFactory().valueProperty(),
                uiLatitudeStart.getValueFactory().valueProperty(), prDebut);

        linearEndUpdater = new GeographicChangeListener(
                uiBorneEnd.valueProperty(), uiDistanceEnd.getValueFactory().valueProperty(),
                uiAmontEnd.selectedProperty(), uiLongitudeEnd.getValueFactory().valueProperty(),
                uiLatitudeEnd.getValueFactory().valueProperty(), prFin);

        // Update PR information
        uiPRDebut.textProperty().bind(prDebut.asString("%.2f"));
        uiPRFin.textProperty().bind(prFin.asString("%.2f"));

        // Check if the positionable distances are on the troncon
        distanceChangeListenerStart = new DistanceChangeListener(uiBorneStart.valueProperty(), uiDistanceStart, uiAmontStart.selectedProperty(), uiImageStartValid.imageProperty());
        distanceChangeListenerEnd = new DistanceChangeListener(uiBorneEnd.valueProperty(), uiDistanceEnd, uiAmontEnd.selectedProperty(), uiImageEndValid.imageProperty());
    }

    public ObjectProperty<Positionable> positionableProperty() {
        return positionableProperty;
    }

    public Positionable getPositionable() {
        return positionableProperty.get();
    }

    public void setPositionable(Positionable positionable) {
        positionableProperty.set(positionable);
    }

    public BooleanProperty disableFieldsProperty(){
        return disableFieldsProperty;
    }

    @FXML
    void importCoord(ActionEvent event) {
        final FXImportCoordinate importCoord = new FXImportCoordinate(getPositionable());
        final Dialog dialog = new Dialog();
        final DialogPane pane = new DialogPane();
        pane.getButtonTypes().add(ButtonType.CLOSE);
        pane.setContent(importCoord);
        dialog.setDialogPane(pane);
        dialog.setResizable(true);
        dialog.setTitle("Import de coordonnée");
        dialog.setOnCloseRequest((Event event1) -> {dialog.hide();});
        dialog.show();
    }

    /**
     * TODO : check null pointer and computing
     *
     * @param event
     */
    @FXML
    void viewAllSR(ActionEvent event) {
        if (positionableProperty.get() == null) {
            return;
        }

        final StringBuilder page = new StringBuilder();
        page.append("<html><body>");

        //calcul de la position geographique
        Point startPoint = getOrCreatePoint(uiLongitudeStart.getValueFactory().valueProperty(),
                uiLatitudeStart.getValueFactory().valueProperty(),
                uiDistanceStart.getValueFactory().valueProperty(),
                uiBorneStart.valueProperty(), uiAmontStart.selectedProperty());
        Point endPoint = getOrCreatePoint(uiLongitudeEnd.getValueFactory().valueProperty(),
                uiLatitudeEnd.getValueFactory().valueProperty(),
                uiDistanceEnd.getValueFactory().valueProperty(),
                uiBorneEnd.valueProperty(), uiAmontEnd.selectedProperty());
        if (startPoint == null && endPoint == null) {
            page.append("<h2>No sufficient position information</h2>");
        } else {

            if (startPoint == null) {
                startPoint = endPoint;
            }
            if (endPoint == null) {
                endPoint = startPoint;
            }

            //DataBase coord
            page.append("<h2>Projection de la base (").append(baseCrs.getName()).append(")</h2>");
            page.append("<b>Début</b><br/>");
            page.append("X : ").append(startPoint.getX()).append("<br/>");
            page.append("Y : ").append(startPoint.getY()).append("<br/>");
            page.append("<b>Fin</b><br/>");
            page.append("X : ").append(endPoint.getX()).append("<br/>");
            page.append("Y : ").append(endPoint.getY()).append("<br/>");
            page.append("<br/>");

            //WGS84 coord
            try {
                final MathTransform trs = CRS.findMathTransform(baseCrs, CRS_WGS84, true);
                Point ptStart = (Point) JTS.transform(startPoint, trs);
                Point ptEnd = (Point) JTS.transform(endPoint, trs);

                page.append("<h2>Coordonnées géographique (WGS-84, EPSG:4326)</h2>");
                page.append("<b>Début</b><br/>");
                page.append("Longitude : ").append(ptStart.getX()).append("<br/>");
                page.append("Latitude&nbsp : ").append(ptStart.getY()).append("<br/>");
                page.append("<b>Fin</b><br/>");
                page.append("Longitude : ").append(ptEnd.getX()).append("<br/>");
                page.append("Latitude&nbsp : ").append(ptEnd.getY()).append("<br/>");
                page.append("<br/>");
            } catch (FactoryException | TransformException ex) {
                SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            }

            //pour chaque systeme de reperage
            for (SystemeReperage sr : uiSRs.getItems()) {
                Entry<BorneDigue, Double> computedLinear = computeLinearFromGeo(sr, startPoint);
                boolean aval = true;
                double distanceBorne = computedLinear.getValue();
                if (distanceBorne < 0) {
                    distanceBorne = -distanceBorne;
                    aval = false;
                }
                float computedPR = TronconUtils.computePR(getSourceLinear(sr), sr, startPoint, Injector.getSession().getRepositoryForClass(BorneDigue.class));

                page.append("<h2>SR : ").append(sr.getLibelle()).append("</h2>");
                page.append("<b>Début </b>");
                page.append(computedLinear.getKey().getLibelle()).append(" à ");
                page.append(DISTANCE_FORMAT.format(distanceBorne)).append("m ");
                page.append(aval ? "en aval" : "en amont").append('.');
                page.append(" Valeur du PR : ").append(computedPR).append('.');
                page.append("<br/>");

                if (!startPoint.equals(endPoint)) {
                    computedLinear = computeLinearFromGeo(sr, endPoint);
                    aval = true;
                    distanceBorne = computedLinear.getValue();
                    if (distanceBorne < 0) {
                        distanceBorne = -distanceBorne;
                        aval = false;
                    }
                    computedPR = TronconUtils.computePR(getSourceLinear(sr), sr, endPoint, Injector.getSession().getRepositoryForClass(BorneDigue.class));
                }

                page.append("<b>Fin&nbsp&nbsp </b>");
                page.append(computedLinear.getKey().getLibelle()).append(" à ");
                page.append(DISTANCE_FORMAT.format(distanceBorne)).append("m ");
                page.append(aval ? "en aval" : "en amont").append('.');
                page.append(" Valeur du PR : ").append(computedPR).append('.');
                page.append("<br/><br/>");
            }
        }
        page.append("</html></body>");

        final WebView view = new WebView();
        view.getEngine().loadContent(page.toString());
        view.getEngine().userStyleSheetLocationProperty().set(FXPositionablePane.class.getResource("/fr/sirs/web.css").toString());

        final Dialog dialog = new Dialog();
        final DialogPane pane = new DialogPane();
        pane.setContent(view);
        pane.getButtonTypes().add(ButtonType.CLOSE);
        dialog.setDialogPane(pane);
        dialog.setTitle("Position");
        dialog.setOnCloseRequest((Event event1) -> {
            dialog.hide();
        });
        dialog.show();
    }

    /**
     * Compute a linear position for the edited {@link Positionable} using defined
     * geographic position.
     *
     * @param targetSR The SR to use to generate linear position.
     * @return The borne to use as start point, and the distance from the borne
     * until the input geographic position. It's negative if we go from downhill
     * to uphill.
     *
     * @throws RuntimeException If the computing fails.
     */
    private Map.Entry<BorneDigue, Double> computeLinearFromGeo(final SystemeReperage targetSR, final Point geoPoint) {
        ArgumentChecks.ensureNonNull("Geographic point", geoPoint);

        // Get troncon geometry
        final LinearReferencing.SegmentInfo[] linearSource = getSourceLinear(targetSR);
        if (linearSource == null) throw new IllegalStateException("No computing can be done without a source linear object.");

        // Get list of bornes which can be possibly used.
        final HashMap<Point, BorneDigue> availableBornes = getAvailableBornes(targetSR);
        final Point[] arrayGeom = availableBornes.keySet().toArray(new Point[0]);

        // Get nearest borne from our start geographic point.
        final Entry<Integer, Double> computedRelative = LinearReferencingUtilities.computeRelative(linearSource, arrayGeom, geoPoint);
        final int borneIndex = computedRelative.getKey();
        if (borneIndex < 0 || borneIndex >= availableBornes.size()) {
            throw new RuntimeException("Computing failed : no valid borne found.");
        }
        final double foundDistance = computedRelative.getValue();
        if (Double.isNaN(foundDistance) || Double.isInfinite(foundDistance)) {
            throw new RuntimeException("Computing failed : no valid distance found.");
        }
        return new AbstractMap.SimpleEntry<>(availableBornes.get(arrayGeom[borneIndex]), foundDistance);
    }

    /**
     * Return the Linear geometry on which the input {@link SystemeReperage} is based on.
     * @param source The SR to get linear for. If null, we'll try to get tronçon
     * geometry of the currently edited {@link Positionable}.
     * @return The linear associated, or null if we cannot get it.
     */
    private LinearReferencing.SegmentInfo[] getSourceLinear(final SystemeReperage source) {
        final TronconDigue t = getTroncon();
        if (tronconSegments == null) {
            Geometry linearSource = (t == null) ? null : t.getGeometry();
            if (linearSource == null) {
                if (source != null && source.getLinearId() != null) {
                    final TronconDigue tmpTroncon = Injector.getSession().getRepositoryForClass(TronconDigue.class).get(source.getLinearId());
                    if (tmpTroncon != null) {
                        linearSource = tmpTroncon.getGeometry();
                    }
                }
            }
            if (linearSource != null) {
                tronconSegments = LinearReferencingUtilities.buildSegments(LinearReferencing.asLineString(linearSource));
            }
        }
        return tronconSegments;
    }

    /**
     * Return valid bornes defined by the input {@link SystemeReperage} PRs ({@link SystemeReperageBorne}).
     * Only bornes containing a geometry are returned.
     * @param source The SR to extract bornes from.
     * @return A map, whose values are found bornes, and keys are their associated geometry. Never null, but can be empty.
     */
    private HashMap<Point, BorneDigue> getAvailableBornes(final SystemeReperage source) {
        ArgumentChecks.ensureNonNull("Système de repérage source", source);
        final AbstractSIRSRepository<BorneDigue> borneRepo = Injector.getSession().getRepositoryForClass(BorneDigue.class);
        final HashMap<Point, BorneDigue> availableBornes = new HashMap<>(source.systemeReperageBornes.size());
        for (final SystemeReperageBorne pr : source.systemeReperageBornes) {
            if (pr.getBorneId() != null) {
                final BorneDigue borne = borneRepo.get(pr.getBorneId());
                if (borne != null && borne.getGeometry() != null) {
                    availableBornes.put(borne.getGeometry(), borne);
                }
            }
        }
        return availableBornes;
    }


    /**
     * Compute current positionable point using linear referencing information
     * defined in the form. Returned point is expressed with Database CRS.
     *
     * @return The point computed from starting borne. If we cannot, we return null.
     */
    private Point computeGeoFromLinear(final ObjectProperty<Double> distanceProperty,
            final ObjectProperty<BorneDigue> borneProperty,
            final BooleanProperty amontSelectedProperty) {
        final Number distance = distanceProperty.get();
        final TronconDigue t = getTroncon();

        if (distance != null && borneProperty.get() != null && t != null) {
            //calcul à partir des bornes
            final Point bornePoint = borneProperty.get().getGeometry();
            double dist = distance.doubleValue();
            if (amontSelectedProperty.get()) {
                dist *= -1;
            }

            return LinearReferencingUtilities.computeCoordinate(t.getGeometry(), bornePoint, dist, 0);
        } else {
            return null;
        }
    }

    /**
     * Try to get geographic / projected position from the corresponding component fields.
     * If geographic pane fields are not filled, we will try to compute them from
     * linear referencing information, using {@link #computeGeoFromLinear() }.
     *
     * @return Geographic / projected start point defined, or null. Returned point
     * CRS is always {@link #baseCrs}.
     */
    private Point getOrCreatePoint(final ObjectProperty<Double> longitudeProperty,
            final ObjectProperty<Double> latitudeProperty,
            final ObjectProperty<Double> distanceProperty,
            final ObjectProperty<BorneDigue> borneProperty,
            final BooleanProperty amontSelectedProperty) {

        Point point = null;
        // Si un CRS est défini, on essaye de récupérer les positions géographiques depuis le formulaire.
        CoordinateReferenceSystem selectedCRS = uiCRSs.getSelectionModel().getSelectedItem();

        // Priorite a l'information geographique absolue.
        if (selectedCRS != null) {
            // On a un point valide : on le prends.
            if (longitudeProperty.get() != null && latitudeProperty != null) {
                point = GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(
                        fxNumberValue(longitudeProperty),
                        fxNumberValue(latitudeProperty)
                ));
            }

            if (point != null && !CRS.equalsApproximatively(selectedCRS, baseCrs)) {
                try {
                    point = (Point) JTS.transform(point,
                            CRS.findMathTransform(selectedCRS, baseCrs, true));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        // Si on n'a pas l'information suffisante on se fie au lineaire.
        if (point == null) {
            point = computeGeoFromLinear(distanceProperty, borneProperty, amontSelectedProperty);
        }

        return point;
    }

    /**
     * Searche recursively the troncon of the positionable.
     *
     * @param pos
     * @return
     */
    private TronconDigue getTronconFromPositionable(final Positionable pos){
        final Element currentElement = getTronconFromElement(pos);
        if(currentElement instanceof TronconDigue) return (TronconDigue) currentElement;
        else return null;
    }

    private Element getTronconFromElement(final Element element){
        Element candidate = null;

        // Si on arrive sur un Troncon, on renvoie le troncon.
        if(element instanceof TronconDigue){
            candidate = element;
        }

        // Sinon on cherche un troncon dans les parents
        else {
            // On privilégie le chemin AvecForeignParent
            if(element instanceof AvecForeignParent){
                String id = ((AvecForeignParent) element).getForeignParentId();
                candidate = getTronconFromElement(Injector.getSession().getRepositoryForClass(TronconDigue.class).get(id));
            }
            // Si on n'a pas (ou pas trouvé) de troncon via la référence ForeignParent on cherche via le conteneur
            if (candidate==null && element.getParent()!=null) {
                candidate = getTronconFromElement(element.getParent());
            }
        }
        return candidate;
    }

    /**
     * Return the {@link TronconDigue} object associated to the current positionable.
     *
     * There's a high probability that our Positionable is contained in its
     * refering, so we start by that. If we cannot find it this way, we'll try
     * to get it through specified SR. If it fails again, we'll have to accept
     * our defeat and return a null value.
     *
     * @return The linear object on which the current object is placed, or null
     * if we cannot find it.
     */
    private TronconDigue getTroncon() {
        if (troncon == null) {
            final Positionable pos = positionableProperty.get();
            if (pos == null) {
                return null;
            }
            troncon = getTronconFromPositionable(pos);
            // Maybe we have an incomplete version of the document, so we try by querying repository.
            if (troncon == null) {
                try {
                    troncon = Injector.getSession().getRepositoryForClass(TronconDigue.class).get(pos.getDocumentId());
                } catch (Exception e) {
                    // Last chance, we must try to get it from SR
                    if (troncon == null && pos.getSystemeRepId() != null) {
                        final SystemeReperage sr = Injector.getSession().getRepositoryForClass(SystemeReperage.class).get(pos.getSystemeRepId());
                        if (sr.getLinearId() != null) {
                            troncon = Injector.getSession().getRepositoryForClass(TronconDigue.class).get(sr.getLinearId());
                        }
                    }
                }
            }
        }
        return troncon;
    }

    private Optional<SystemeReperage> getDefaultSR() {
        final TronconDigue t = getTroncon();
        if (t.getSystemeRepDefautId() != null) {
            return Optional.of(Injector.getSession().getRepositoryForClass(SystemeReperage.class).get(t.getSystemeRepDefautId()));
        }
        return Optional.empty();
    }

    /**
     * Update geographic/projected coordinate fields when current CRS change.
     * Note : listener method, should always be launched from FX-thread.
     *
     * @param observable
     * @param oldValue Previous value into {@link #uiCRSs}
     * @param newValue Current value into {@link #uiCRSs}
     */
    private void updateGeoCoord(ObservableValue<? extends CoordinateReferenceSystem> observable, CoordinateReferenceSystem oldValue, CoordinateReferenceSystem newValue) {
        // There's no available null value in CRS combobox, so old value will be
        // null only at first allocation, no transform needed in this case.
        if (oldValue == null || newValue == null) {
            return;
        }

        final Point ptStart, ptEnd;
        // On a un point de début valide
        if (uiLongitudeStart.valueProperty().get() != null && uiLatitudeStart.valueProperty().get() != null) {
            ptStart = GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(
                    fxNumberValue(uiLongitudeStart.getValueFactory().valueProperty()),
                    fxNumberValue(uiLatitudeStart.getValueFactory().valueProperty())
            ));
        } else {
            ptStart = null;
        }

        // On a un point de fin valide
        if (uiLongitudeEnd.valueProperty().get() != null && uiLatitudeEnd.valueProperty().get() != null) {
            ptEnd = GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(
                    fxNumberValue(uiLongitudeEnd.getValueFactory().valueProperty()),
                    fxNumberValue(uiLatitudeEnd.getValueFactory().valueProperty())
            ));
        } else {
            ptEnd = null;
        }

        // If we've got at least one valid point, we transform it. Otherwise, just return.
        if (ptStart != null || ptEnd != null) {
            final Runnable pointConverter = () -> {
                synchronized (computingRunning) {
                    try {
                        final MathTransform conversion = CRS.findMathTransform(oldValue, newValue, true);

                        if (ptStart != null) {
                            final Point tmpStart = (Point) JTS.transform(ptStart, conversion);
                            Platform.runLater(() -> {
                                uiLongitudeStart.getValueFactory().valueProperty().set(tmpStart.getX());
                                uiLatitudeStart.getValueFactory().valueProperty().set(tmpStart.getY());
                            });
                        }
                        if (ptEnd != null) {
                            final Point tmpEnd = (Point) JTS.transform(ptEnd, conversion);
                            Platform.runLater(() -> {
                                uiLongitudeEnd.getValueFactory().valueProperty().set(tmpEnd.getX());
                                uiLatitudeEnd.getValueFactory().valueProperty().set(tmpEnd.getY());
                            });
                        }
                    } catch (Exception ex) {
                        GeotkFX.newExceptionDialog("La conversion des positions a échouée.", ex).show();
                        throw new RuntimeException("La conversion des positions a échouée.", ex);
                    } finally {
                        Platform.runLater(() -> computingRunning.set(computingRunning.get() - 1));
                    }
                }
            };

            computingRunning.set(computingRunning.get()+1);
            TaskManager.INSTANCE.submit("Conversion de points géographiques", pointConverter);
        }

    }

    /**
     * Set list of available {@link SystemeReperage} in {@link #uiSRs}.
     */
    private Task updateSRList() {
        final Positionable pos = (Positionable) positionableProperty.get();
        if (pos == null) {
            return null;
        }

        final TronconDigue t = getTroncon();

        if (t != null) {
            computingRunning.set(computingRunning.get() + 1);
            return TaskManager.INSTANCE.submit("Mise à jour d'une position", () -> {
                // needed to synchronize field update and SR update
                synchronized (computingRunning) {
                    try {
                        final SystemeReperageRepository srRepo = (SystemeReperageRepository) Injector.getSession().getRepositoryForClass(SystemeReperage.class);
                        final List<SystemeReperage> srs = srRepo.getByLinear(t);
                        final SystemeReperage defaultSR;
                        if (pos.getSystemeRepId() != null) {
                            defaultSR = srRepo.get(pos.getSystemeRepId());
                        } else if (t.getSystemeRepDefautId() != null) {
                            defaultSR = srRepo.get(t.getSystemeRepDefautId());
                        } else {
                            defaultSR = null;
                        }

                        // Init list of bornes
                        final ObservableList<BorneDigue> bornes;
                        if (defaultSR != null) {
                            final AbstractSIRSRepository<BorneDigue> borneRepo = Injector.getSession().getRepositoryForClass(BorneDigue.class);
                            bornes = FXCollections.observableList(
                                    borneRepo.get(defaultSR.systemeReperageBornes.stream()
                                            .map(srb -> srb.getBorneId()).collect(Collectors.toList())));
                        } else {
                            bornes = null;
                        }

                        // Wait for JavaFX to update our fields.
                        TaskManager.MockTask mockTask = new TaskManager.MockTask(() -> {
                            uiSRs.setItems(FXCollections.observableList(srs));
                            uiSRs.getSelectionModel().select(defaultSR);
                            uiBorneStart.setItems(bornes);
                            uiBorneEnd.setItems(bornes);
                        });
                        Platform.runLater(mockTask);
                        mockTask.get();
                        return false;

                    } finally {
                        Platform.runLater(() -> computingRunning.set(computingRunning.get() - 1));
                    }
                }
            });
        }
        return null;
    }

    private enum Borne {START, END};
    private class SRChangeListener implements ChangeListener<SystemeReperage> {

        private final ComboBox<BorneDigue> uiBorne;
        private final Borne borne;
        private final ObjectProperty<Double> longitudeProperty;
        private final ObjectProperty<Double> latitudeProperty;
        private final ObjectProperty<Double> distanceProperty;
        private final BooleanProperty amontSelectedProperty;
        private final FloatProperty prProperty;

        SRChangeListener(final Borne borne){
            ArgumentChecks.ensureNonNull("Borne", borne);
            if(borne==Borne.END){
                uiBorne = uiBorneEnd;
                longitudeProperty = uiLongitudeEnd.getValueFactory().valueProperty();
                latitudeProperty = uiLatitudeEnd.getValueFactory().valueProperty();
                distanceProperty = uiDistanceEnd.getValueFactory().valueProperty();
                amontSelectedProperty = uiAmontEnd.selectedProperty();
                prProperty = prFin;
            } else {
                uiBorne = uiBorneStart;
                longitudeProperty = uiLongitudeStart.getValueFactory().valueProperty();
                latitudeProperty = uiLatitudeStart.getValueFactory().valueProperty();
                distanceProperty = uiDistanceStart.getValueFactory().valueProperty();
                amontSelectedProperty = uiAmontStart.selectedProperty();
                prProperty = prDebut;
            }
            this.borne = borne;
        }

        @Override
        public void changed(ObservableValue<? extends SystemeReperage> observable, SystemeReperage oldValue, SystemeReperage newSRValue) {
            final Positionable pos = (Positionable) positionableProperty.get();
            if (pos == null) {
                return;
            }

            final Point point = getOrCreatePoint(longitudeProperty, latitudeProperty, distanceProperty, uiBorne.valueProperty(), amontSelectedProperty);

            // Si la nouvelle valeur est nulle on passe une liste vide.
            if (newSRValue == null || newSRValue.systemeReperageBornes == null || newSRValue.systemeReperageBornes.isEmpty()) {
                final ObservableList<BorneDigue> emptyBornes = FXCollections.emptyObservableList();
                uiBorne.setItems(emptyBornes);
                return;
            }

            computingRunning.set(computingRunning.get() + 1);
            TaskManager.INSTANCE.submit("Mise à jour d'une position", () -> {
                synchronized (computingRunning) {
                    try {
                        final ArrayList<BorneDigue> bornes = new ArrayList<>();
                        final AbstractSIRSRepository<BorneDigue> borneRepo = Injector.getSession().getRepositoryForClass(BorneDigue.class);
                        BorneDigue defaultBorne = null;
                        for (final SystemeReperageBorne srb : newSRValue.systemeReperageBornes) {
                            final BorneDigue bd = borneRepo.get(srb.getBorneId());
                            if (bd != null) {
                                bornes.add(bd);
                                if ((borne == Borne.START && bd.getId().equals(pos.getBorneDebutId()))
                                        || borne == Borne.END && bd.getId().equals(pos.getBorneFinId())) {
                                    defaultBorne = bd;
                                }
                            }
                        }

                        // Si on a de quoi recalculer, on privilégie le recalcul de la borne, et pour être certain de la cohérence, de la distance, de la position relative et du PR.
                        if (point != null) {
                            final Entry<BorneDigue, Double> computedLinear = computeLinearFromGeo(newSRValue, point);
                            final float computedPR = TronconUtils.computePR(getSourceLinear(newSRValue), newSRValue, point, Injector.getSession().getRepositoryForClass(BorneDigue.class));
                            Platform.runLater(() -> {
                                // Mise à jour de la liste des bornes
                                uiBorne.setItems(FXCollections.observableList(bornes));

                                // Mise à jour de la position
                                amontSelectedProperty.set(computedLinear.getValue() < 0);
                                distanceProperty.set(StrictMath.abs(computedLinear.getValue()));
                                uiBorne.getSelectionModel().select(computedLinear.getKey());

                                // Mise à jour de l'affichage du PR
                                prProperty.set(computedPR);
                            });
                        } // Si on n'a pas de quoi recalculer mais que la nouvelle valeur n'est pas nulle, on va chercher la borne enregistrée en base.
                        else {
                            final BorneDigue finalCopy = defaultBorne;
                            Platform.runLater(() -> {
                                uiBorne.setItems(FXCollections.observableList(bornes));
                                uiBorne.getSelectionModel().select(finalCopy);
                            });
                        }

                    } finally {
                        Platform.runLater(() -> computingRunning.set(computingRunning.get() - 1));
                    }
                }
            });
        }
    }

    /**
     * Update component fields when the target {@link Positionable} is set/replaced.
     * @param observable
     * @param oldValue Previous positionable focused by the component.
     * @param newValue Current focused positionable.
     */
    private void updateField(ObservableValue<? extends Positionable> observable, Positionable oldValue, Positionable newValue) {

        if (oldValue != null) {
            uiAmontStart.selectedProperty().unbindBidirectional(oldValue.borne_debut_avalProperty());
            uiAmontEnd.selectedProperty().unbindBidirectional(oldValue.borne_fin_avalProperty());
            uiDistanceStart.getValueFactory().valueProperty().unbindBidirectional((Property) oldValue.borne_debut_distanceProperty());
            uiDistanceEnd.getValueFactory().valueProperty().unbindBidirectional((Property) oldValue.borne_fin_distanceProperty());

            // Remove listeners
            uiCRSs.getSelectionModel().selectedItemProperty().removeListener(crsChangeListener);

            uiSRs.getSelectionModel().selectedItemProperty().removeListener(sRChangeListenerStart);
            uiSRs.getSelectionModel().selectedItemProperty().removeListener(sRChangeListenerEnd);

            uiBorneStart.valueProperty().removeListener(geoStartUpdater);
            uiBorneStart.focusedProperty().removeListener(geoStartUpdater);
            uiAmontStart.selectedProperty().removeListener(geoStartUpdater);
            uiDistanceStart.getValueFactory().valueProperty().removeListener(geoStartUpdater);

            uiBorneEnd.valueProperty().removeListener(geoEndUpdater);
            uiBorneEnd.focusedProperty().removeListener(geoEndUpdater);
            uiAmontEnd.selectedProperty().removeListener(geoEndUpdater);
            uiDistanceEnd.getValueFactory().valueProperty().removeListener(geoEndUpdater);

            uiLongitudeStart.getValueFactory().valueProperty().removeListener(linearStartUpdater);
            uiLatitudeStart.getValueFactory().valueProperty().removeListener(linearStartUpdater);

            uiLongitudeEnd.getValueFactory().valueProperty().removeListener(linearEndUpdater);
            uiLatitudeEnd.getValueFactory().valueProperty().removeListener(linearEndUpdater);

            prDebut.removeListener(distanceChangeListenerStart);
            prFin.removeListener(distanceChangeListenerEnd);
        }
        troncon = null;

        if(newValue==null) return;

        computingRunning.set(computingRunning.get()+1);
        final Runnable updater = () -> {
            try {
                //creation de la liste des systemes de reperage
                updateSRList().get();

                synchronized (computingRunning) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            //Bindings

                            if (newValue.getBorneDebutId() != null && !newValue.getBorneDebutId().isEmpty()) {
                                final BorneDigue borneStart = Injector.getSession().getRepositoryForClass(BorneDigue.class).get(newValue.getBorneDebutId());
                                uiBorneStart.getSelectionModel().select(borneStart);
                            }

                            if (newValue.getBorneFinId() != null && !newValue.getBorneFinId().isEmpty()) {
                                final BorneDigue borneEnd = Injector.getSession().getRepositoryForClass(BorneDigue.class).get(newValue.getBorneFinId());
                                uiBorneEnd.getSelectionModel().select(borneEnd);
                            }

                            uiAmontStart.selectedProperty().bindBidirectional(newValue.borne_debut_avalProperty());
                            uiAmontEnd.selectedProperty().bindBidirectional(newValue.borne_fin_avalProperty());
                            uiDistanceStart.getValueFactory().valueProperty().bindBidirectional((Property) newValue.borne_debut_distanceProperty());
                            uiDistanceEnd.getValueFactory().valueProperty().bindBidirectional((Property) newValue.borne_fin_distanceProperty());

                            prDebut.set(newValue.getPrDebut());
                            prFin.set(newValue.getPrFin());
                            // Mise à jour automatique de la liste des SRs si le parent
                            // du positionable change.
                            if (newValue.parentProperty() != null) {
                                newValue.parentProperty().addListener((ObservableValue<? extends Element> observable, Element oldValue, Element newValue) -> {
                                    updateSRList();
                                });
                            }

                            //on ecoute les changements de geometrie pour mettre a jour les champs
                            final ChangeListener cl = new ChangeListener() {
                                @Override
                                public void changed(ObservableValue observable, Object oldPosition, Object newPosition) {
                                    //selectionner RGF93 par defaut
                                    uiCRSs.getSelectionModel().clearAndSelect(1);
                                    final Point startPos = newValue.getPositionDebut();
                                    final Point endPos = newValue.getPositionFin();
                                    if (startPos != null) {
                                        uiLongitudeStart.getValueFactory().valueProperty().set(startPos.getX());
                                        uiLatitudeStart.getValueFactory().valueProperty().set(startPos.getY());
                                    }
                                    if (endPos != null) {
                                        uiLongitudeEnd.getValueFactory().valueProperty().set(endPos.getX());
                                        uiLatitudeEnd.getValueFactory().valueProperty().set(endPos.getY());
                                    }
                                }
                            };
                            newValue.positionDebutProperty().addListener(cl);
                            newValue.positionFinProperty().addListener(cl);
                            cl.changed(null, null, null);

                            //on active le panneau qui a le positionnement
                            final Point startPos = newValue.getPositionDebut();
                            final Point endPos = newValue.getPositionFin();
                            if (startPos != null || endPos != null) {
                                uiTypeCoord.setSelected(true);
                            } else {
                                uiTypeBorne.setSelected(true);
                            }

                            // Add listeners
                            uiCRSs.getSelectionModel().selectedItemProperty().addListener(crsChangeListener);

                            uiSRs.getSelectionModel().selectedItemProperty().addListener(sRChangeListenerStart);
                            uiSRs.getSelectionModel().selectedItemProperty().addListener(sRChangeListenerEnd);

                            uiBorneStart.valueProperty().addListener(geoStartUpdater);
                            uiBorneStart.focusedProperty().addListener(geoStartUpdater);
                            uiAmontStart.selectedProperty().addListener(geoStartUpdater);
                            uiDistanceStart.getValueFactory().valueProperty().addListener(geoStartUpdater);

                            uiBorneEnd.valueProperty().addListener(geoEndUpdater);
                            uiBorneEnd.focusedProperty().addListener(geoEndUpdater);
                            uiAmontEnd.selectedProperty().addListener(geoEndUpdater);
                            uiDistanceEnd.getValueFactory().valueProperty().addListener(geoEndUpdater);

                            uiLongitudeStart.getValueFactory().valueProperty().addListener(linearStartUpdater);
                            uiLatitudeStart.getValueFactory().valueProperty().addListener(linearStartUpdater);

                            uiLongitudeEnd.getValueFactory().valueProperty().addListener(linearEndUpdater);
                            uiLatitudeEnd.getValueFactory().valueProperty().addListener(linearEndUpdater);

                            prDebut.addListener(distanceChangeListenerStart);
                            prFin.addListener(distanceChangeListenerEnd);
                        }
                    });
                }
            } catch (InterruptedException ex) {
                SIRS.LOGGER.log(Level.WARNING, null, ex);
            } catch (ExecutionException ex) {
                throw new RuntimeException(ex);
            } finally {
                Platform.runLater(() -> computingRunning.set(computingRunning.get() - 1));
            }
        };

        TaskManager.INSTANCE.submit("Mise à jour d'une position", updater);
    }

    /**
     * Affect edited position information into bound {@link Positionable } object.
     * The method try to affect both geographic and linear referencement.
     */
    public void preSave() {

        final Positionable pos = (Positionable) positionableProperty.get();
        if (pos == null) {
            return;
        }

        //on sauvegarde la position geo
        pos.setPositionDebut(getOrCreatePoint(uiLongitudeStart.getValueFactory().valueProperty(),
                uiLatitudeStart.getValueFactory().valueProperty(),
                uiDistanceStart.getValueFactory().valueProperty(),
                uiBorneStart.valueProperty(), uiAmontStart.selectedProperty()));
        pos.setPositionFin(getOrCreatePoint(uiLongitudeEnd.getValueFactory().valueProperty(),
                uiLatitudeEnd.getValueFactory().valueProperty(),
                uiDistanceEnd.getValueFactory().valueProperty(),
                uiBorneEnd.valueProperty(), uiAmontEnd.selectedProperty()));

        //sauvegarde de la position par borne
        final SystemeReperage selectedSR = uiSRs.getSelectionModel().selectedItemProperty().get();
        final BorneDigue borneStart = uiBorneStart.getSelectionModel().selectedItemProperty().get();
        final BorneDigue borneEnd = uiBorneEnd.getSelectionModel().selectedItemProperty().get();

        // Get PRs from selected SR and bornes.
        pos.setSystemeRepId((selectedSR != null) ? selectedSR.getId() : null);

        pos.setBorneDebutId((borneStart != null) ? borneStart.getId() : null);
        pos.setBorneFinId((borneEnd != null) ? borneEnd.getId() : null);

        // Do not bind them bidirectionally to avoid affecting cached objects if
        // we do not save changes.
        pos.prDebutProperty().setValue(prDebut.get());
        pos.prFinProperty().setValue(prFin.get());

        //maj de la geometrie du positionable
        final LineString structGeom = LinearReferencingUtilities.buildGeometry(getTroncon().getGeometry(), pos, Injector.getSession().getRepositoryForClass(BorneDigue.class));
        pos.setGeometry(structGeom);
    }

    /**
     * Compute geographic point from linear position.
     */
    private class LinearChangeListener implements ChangeListener {

        private final ComboBox<BorneDigue> uiBorne;
        private final ObjectProperty<Double> longitudeProperty;
        private final ObjectProperty<Double> latitudeProperty;
        private final FloatProperty prProperty;
        private final ObjectProperty<Double> distanceProperty;
        private final BooleanProperty amontSelectedProperty;

        public LinearChangeListener(final ComboBox<BorneDigue> uiBorne,
                final ObjectProperty<Double> longitudeProperty,
                final ObjectProperty<Double> latitudeProperty,
                final FloatProperty prProperty,
                final ObjectProperty<Double> distanceProperty,
                final BooleanProperty amontSelectedProperty) {
            this.uiBorne = uiBorne;
            this.longitudeProperty = longitudeProperty;
            this.latitudeProperty = latitudeProperty;
            this.prProperty = prProperty;
            this.distanceProperty = distanceProperty;
            this.amontSelectedProperty = amontSelectedProperty;
        }

        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            // Update only if linear mode is selected. Otherwise, it means a modification on geographic
            // panel modified linear one, which throw back an event. We do not update if borne combo box
            // is currently selected, as user could be browsing in borne list.
            if (uiTypeBorne.isSelected() && !uiBorne.isFocused()) {
                computingRunning.set(computingRunning.get()+1);

                try {
                    Point point = computeGeoFromLinear(distanceProperty, uiBorne.valueProperty(), amontSelectedProperty);
                    if (point != null) {
                        final CoordinateReferenceSystem selectedCRS = uiCRSs.getSelectionModel().getSelectedItem();
                        if (!CRS.equalsApproximatively(baseCrs, selectedCRS)) {
                            point = (Point) JTS.transform(point, CRS.findMathTransform(baseCrs, selectedCRS));
                        }

                        final Optional<SystemeReperage> sr = getDefaultSR();
                        float computedPR = sr.isPresent() ?
                                TronconUtils.computePR(getSourceLinear(sr.get()), sr.get(), point, Injector.getSession().getRepositoryForClass(BorneDigue.class))
                                : Float.NaN;

                            longitudeProperty.set(point.getX());
                            latitudeProperty.set(point.getY());
                            prProperty.set(computedPR);

                    }
                } catch (FactoryException | MismatchedDimensionException | TransformException ex) {
                    SIRS.LOGGER.log(Level.SEVERE, null, ex);
                } finally {
                    computingRunning.set(computingRunning.get()-1);
                }
            }
        }
    }


    /**
     * Compute linear position from geographic/projected start point.
     */
    private class GeographicChangeListener implements ChangeListener {

        private final ObjectProperty<BorneDigue> borneProperty;
        private final ObjectProperty<Double> distanceProperty;
        private final BooleanProperty amontSelectedProperty;
        private final ObjectProperty<Double> longitudeProperty;
        private final ObjectProperty<Double> latitudeProperty;
        private final FloatProperty prProperty;

        GeographicChangeListener(final ObjectProperty<BorneDigue> borneProperty,
                final ObjectProperty<Double> distanceProperty,
                final BooleanProperty amontSelectedProperty,
                final ObjectProperty<Double> longitudeProperty,
                final ObjectProperty<Double> latitudeProperty,
                final FloatProperty prProperty){
            this.borneProperty = borneProperty;
            this.distanceProperty = distanceProperty;
            this.amontSelectedProperty = amontSelectedProperty;
            this.longitudeProperty = longitudeProperty;
            this.latitudeProperty = latitudeProperty;
            this.prProperty = prProperty;
        }

        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            // Update only if geographic mode is selected. Otherwise, it means a
            // modification on linear panel has thrown back an event.
            if (uiTypeCoord.isSelected()) {
                computingRunning.set(computingRunning.get()+1);
                try {
                    final Point point = getOrCreatePoint(longitudeProperty, latitudeProperty, distanceProperty, borneProperty, amontSelectedProperty);
                    final Optional<SystemeReperage> sr = getDefaultSR();
                    if (point != null && sr.isPresent()) {
                        final Entry<BorneDigue, Double> computedLinear = computeLinearFromGeo(sr.get(), point);
                        final float computedPR = TronconUtils.computePR(getSourceLinear(sr.get()), sr.get(), point, Injector.getSession().getRepositoryForClass(BorneDigue.class));
                        amontSelectedProperty.set(computedLinear.getValue() < 0);
                        distanceProperty.set(StrictMath.abs(computedLinear.getValue()));
                        borneProperty.set(computedLinear.getKey());
                        prProperty.set(computedPR);
                    }
                } finally {
                    computingRunning.set(computingRunning.get()-1);
                }
            }
        }
    }

    private class DistanceChangeListener implements ChangeListener {

        private final ObjectProperty<BorneDigue> borneProperty;
        private final Spinner<Double> uiDistance;
        private final BooleanProperty amontSelectedProperty;
        private final ObjectProperty<Image> imageProperty;

        DistanceChangeListener(final ObjectProperty<BorneDigue> borneProperty,
                final Spinner<Double> uiDistance,
                final BooleanProperty amontSelectedProperty,
                final ObjectProperty<Image> imageProperty){
            this.borneProperty = borneProperty;
            this.uiDistance = uiDistance;
            this.amontSelectedProperty = amontSelectedProperty;
            this.imageProperty = imageProperty;
        }

        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {

            if(borneProperty.get()!=null
                    && borneProperty.get().getGeometry()!=null
                    && uiSRs.getValue()!=null
                    && uiDistance.valueProperty().get()!=null){

                final LinearReferencing.SegmentInfo[] refLinear = getSourceLinear(uiSRs.getValue());
                final ProjectedPoint startBorneProj = LinearReferencing.projectReference(refLinear, borneProperty.get().getGeometry());

                double tronconLength = 0.;
                for(int i=0; i<refLinear.length; i++){
                    tronconLength+=refLinear[i].length;
                }

                // Les cas suivants doivent alerter l'utilisateur :

                // cas amont pour le début : distance saisie supérieure à la distance de la borne de départ depuis le début du troncon
                if((amontSelectedProperty.get() && uiDistance.getValueFactory().valueProperty().get() > startBorneProj.distanceAlongLinear)
                        // cas aval pour le début : distance saisie supérieure à la longueur du tronçon - la distance de la borne de depart depuis le début du tronçon
                        || (!amontSelectedProperty.get() && uiDistance.valueProperty().get() > (tronconLength-startBorneProj.distanceAlongLinear))){
                    uiDistance.setStyle("-fx-text-fill: #cc0000");
                    uiDistance.setTooltip(new Tooltip("La distance saisie est en-dehors du tronçon."));
                    imageProperty.set(ICON_EXCLAMATION_TRIANGLE);
                }

                // Dans les autres cas, on restaure le texte en noir.
                else {
                    uiDistance.setStyle("-fx-text-fill: #000000");
                    uiDistance.setTooltip(null);
                    imageProperty.set(ICON_CHECK_CIRCLE);
                }
            }
        }
    }

    private double fxNumberValue(ObjectProperty<Double> spinnerNumber){
        if(spinnerNumber.get()==null) return 0;
        return spinnerNumber.get();
    }
}
