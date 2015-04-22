
package fr.sirs.theme.ui;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import static fr.sirs.SIRS.ICON_IMPORT;
import static fr.sirs.SIRS.ICON_VIEWOTHER;
import fr.sirs.core.LinearReferencingUtilities;
import fr.sirs.core.SirsCore;
import fr.sirs.core.TronconUtils;
import org.geotoolkit.gui.javafx.util.TaskManager;
import fr.sirs.core.component.BorneDigueRepository;
import fr.sirs.core.component.SystemeReperageRepository;
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
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebView;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.util.ArgumentChecks;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.gui.javafx.util.ComboBoxCompletion;
import org.geotoolkit.gui.javafx.util.FXNumberSpinner;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.LinearReferencing;
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
    
    public static final CoordinateReferenceSystem CRS_WGS84 = CommonCRS.WGS84.normalizedGeographic();
    
    
    @FXML private Button uiImport;
    @FXML private Button uiView;
    
    @FXML private ToggleButton uiTypeBorne;
    @FXML private ToggleButton uiTypeCoord;
    @FXML private ProgressIndicator uiLoading;
    
    // PR Information
    @FXML private Label uiPRDebut;
    @FXML private Label uiPRFin;
    private final SimpleFloatProperty prDebut = new SimpleFloatProperty();
    private volatile SimpleFloatProperty prFin = new SimpleFloatProperty();
    
    // Borne mode
    @FXML private GridPane uiBornePane;
    @FXML private ComboBox<SystemeReperage> uiSRs;
    @FXML private ComboBox<BorneDigue> uiBorneStart;
    private final ComboBoxCompletion startBorneCompletion;
    @FXML private ComboBox<BorneDigue> uiBorneEnd;
    private final ComboBoxCompletion endBorneCompletion;
    @FXML private CheckBox uiAmontStart;
    @FXML private CheckBox uiAmontEnd;
    @FXML private FXNumberSpinner uiDistanceStart;
    @FXML private FXNumberSpinner uiDistanceEnd;
    
    // Coordinate mode
    @FXML private GridPane uiCoordPane;
    @FXML private ComboBox<CoordinateReferenceSystem> uiCRSs;
    @FXML private FXNumberSpinner uiLongitudeStart;
    @FXML private FXNumberSpinner uiLongitudeEnd;
    @FXML private FXNumberSpinner uiLatitudeStart;
    @FXML private FXNumberSpinner uiLatitudeEnd;
    
    /** A flag to notify when a component update is running, used to lock graphic components */
    private final SimpleIntegerProperty computingRunning = new SimpleIntegerProperty(0);
    
    private final ObjectProperty<Positionable> positionableProperty = new SimpleObjectProperty<>();
    private final SimpleBooleanProperty disableFieldsProperty = new SimpleBooleanProperty(true);
    private final CoordinateReferenceSystem baseCrs = SirsCore.getEpsgCode();
    
    /**
     * Reference to TronconDigue parent of the current positionable
     */
    private TronconDigue currentTroncon;
    private LinearReferencing.SegmentInfo[] tronconSegments;

    public FXPositionablePane() {
        SIRS.loadFXML(this, Positionable.class);
                
        uiImport.setGraphic(new ImageView(ICON_IMPORT));
        uiView.setGraphic(new ImageView(ICON_VIEWOTHER));
        
        final SirsStringConverter sirsStringConverter = new SirsStringConverter();     
        uiSRs.setConverter(sirsStringConverter);
        uiCRSs.setConverter(sirsStringConverter);
        uiBorneStart.setConverter(sirsStringConverter);
        uiBorneStart.setEditable(true);
        uiBorneEnd.setConverter(sirsStringConverter);
        uiBorneEnd.setEditable(true);
        
        startBorneCompletion = new ComboBoxCompletion(uiBorneStart);
        endBorneCompletion = new ComboBoxCompletion(uiBorneEnd);
        
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
        uiAmontEnd.disableProperty().bind(disabledBinding);
        uiDistanceStart.disableProperty().bind(disabledBinding);
        uiDistanceEnd.disableProperty().bind(disabledBinding);
        
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
        uiCRSs.getSelectionModel().selectedItemProperty().addListener(this::updateGeoCoord);        
        uiSRs.getSelectionModel().selectedItemProperty().addListener(this::updateBorneList);
        positionableProperty.addListener(this::updateField);
        
        // compute back linear referencing when geographic point is changed
        
        //compute back geographic points when linear referencing changes.
        final GeographicStartFromLinear geoStartUpdater = new GeographicStartFromLinear();
        uiBorneStart.valueProperty().addListener(geoStartUpdater);
        uiBorneStart.focusedProperty().addListener(geoStartUpdater);
        uiAmontStart.selectedProperty().addListener(geoStartUpdater);
        uiDistanceStart.valueProperty().addListener(geoStartUpdater);
        
        final GeographicEndFromLinear geoEndUpdater = new GeographicEndFromLinear();
        uiBorneEnd.valueProperty().addListener(geoEndUpdater);
        uiBorneEnd.focusedProperty().addListener(geoEndUpdater);
        uiAmontEnd.selectedProperty().addListener(geoEndUpdater);
        uiDistanceEnd.valueProperty().addListener(geoEndUpdater);
        
        // Compute back linear referencing when geographic changes
        final LinearStartFromGeographic linearStartUpdater = new LinearStartFromGeographic();
        uiLongitudeStart.valueProperty().addListener(linearStartUpdater);
        uiLatitudeStart.valueProperty().addListener(linearStartUpdater);
        
        final LinearEndFromGeographic linearEndUpdater = new LinearEndFromGeographic();
        uiLongitudeEnd.valueProperty().addListener(linearEndUpdater);
        uiLatitudeEnd.valueProperty().addListener(linearEndUpdater);
        
        // Update PR information
        uiPRDebut.textProperty().bind(prDebut.asString("%.2f"));
        uiPRFin.textProperty().bind(prFin.asString("%.2f"));
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

        final Session session = Injector.getBean(Session.class);
        final StringBuilder page = new StringBuilder();
        page.append("<html><body>");

        //calcul de la position geographique
        Point startPoint = getOrCreateStartPoint();
        Point endPoint = getOrCreateEndPoint();
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
                float computedPR = TronconUtils.computePR(getSourceLinear(sr), sr, startPoint, Injector.getSession().getBorneDigueRepository());

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
                    computedPR = TronconUtils.computePR(getSourceLinear(sr), sr, endPoint, Injector.getSession().getBorneDigueRepository());
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
        HashMap<Point, BorneDigue> availableBornes = getAvailableBornes(targetSR);
        Point[] arrayGeom = availableBornes.keySet().toArray(new Point[0]);
        
        // Get nearest borne from our start geographic point.
        Entry<Integer, Double> computedRelative = LinearReferencingUtilities.computeRelative(linearSource, arrayGeom, geoPoint);
        final int borneIndex = computedRelative.getKey();
        if (borneIndex < 0 || borneIndex >= availableBornes.size()) {
            throw new RuntimeException("Computing failed : no valid borne found.");
        }
        double foundDistance = computedRelative.getValue();
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
        if (tronconSegments == null) {
            Geometry linearSource = (currentTroncon == null) ? null : currentTroncon.getGeometry();
            if (linearSource == null) {
                if (source != null && source.getTronconId() != null) {
                    TronconDigue tmpTroncon = Injector.getSession().getTronconDigueRepository().get(source.getTronconId());
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
        BorneDigueRepository borneRepo = Injector.getSession().getBorneDigueRepository();
        final HashMap<Point, BorneDigue> availableBornes = new HashMap<>(source.systemeReperageBorne.size());
        for (final SystemeReperageBorne pr : source.systemeReperageBorne) {
            if (pr.getBorneId() != null) {
                BorneDigue borne = borneRepo.get(pr.getBorneId());
                if (borne != null && borne.getGeometry() != null) {
                    availableBornes.put(borne.getGeometry(), borne);
                }
            }
        }
        return availableBornes;
    }
    
    /**
     * Compute current positionable start point using linear referencing information
     * defined in the form. Returned point is expressed with Database CRS.
     * 
     * @return The point computed from starting borne. If we cannot, we return null.
     */
    private Point computeGeoStartFromLinear() {
        final Number distanceStart = uiDistanceStart.valueProperty().get();
        if (distanceStart != null && uiBorneStart.getValue() != null && currentTroncon != null) {
            //calcul à partir des bornes
            final Point borneStartPoint = uiBorneStart.getValue().getGeometry();
            double distStart = distanceStart.doubleValue();
            if (uiAmontStart.isSelected()) {
                distStart *= -1;
            }

            return LinearReferencingUtilities.computeCoordinate(currentTroncon.getGeometry(), borneStartPoint, distStart, 0);
        } else {
            return null;
        }
    }
    
    /**
     * Compute current positionable end point using linear referencing information
     * defined in the form. Returned point is expressed with Database CRS.
     * 
     * @return The point computed from ending borne. If we cannot, we return null.
     */
    private Point computeGeoEndFromLinear() {
        final Number distanceEnd = uiDistanceEnd.valueProperty().get();
        if (distanceEnd != null && uiBorneEnd.getValue() != null && currentTroncon != null) {
            //calcul à partir des bornes
            final Point borneEndPoint = uiBorneEnd.getValue().getGeometry();
            double distEnd = distanceEnd.doubleValue();
            if (uiAmontEnd.isSelected()) {
                distEnd *= -1;
            }

            return LinearReferencingUtilities.computeCoordinate(currentTroncon.getGeometry(), borneEndPoint, distEnd, 0);
        } else {
            return null;
        }
    }
        
    /**
     * Try to get geographic / projected start position from this component fields.
     * If geographic pane fields are not filled, we will try to compute them from
     * linear referencing information, using {@link #computeGeoStartFromLinear() }.
     * 
     * @return Geographic / projected start point defined, or null. Returned point 
     * CRS is always {@link #baseCrs}.
     */
    private Point getOrCreateStartPoint() {
        Point ptStart = null;
        // Si un CRS est défini, on essaye de récupérer les positions géographiques depuis le formulaire.
        CoordinateReferenceSystem selectedCRS = uiCRSs.getSelectionModel().getSelectedItem();
        if (selectedCRS != null) {
            // On a un point de début valide : on le prends.
            if (uiLongitudeStart.valueProperty().get() != null && uiLatitudeStart != null) {
                ptStart = GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(
                        fxNumberValue(uiLongitudeStart),
                        fxNumberValue(uiLatitudeStart)
                ));
            }

            if (ptStart != null && !CRS.equalsApproximatively(selectedCRS, baseCrs)) {
                try {
                    ptStart = (Point) JTS.transform(ptStart,
                            CRS.findMathTransform(selectedCRS, baseCrs, true));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        
        if (ptStart == null) {
            ptStart = computeGeoStartFromLinear();
        }
        
        return ptStart;
    }
            
    /**
     * Try to get geographic / projected end position from this component fields.
     * If geographic pane fields are not filled, we will try to compute them from
     * linear referencing information, using {@link #computeGeoEndFromLinear() }.
     * 
     * @return Geographic / projected end point defined, or null. Returned point 
     * CRS is always {@link #baseCrs}.
     */
    private Point getOrCreateEndPoint() {
        Point ptEnd = null;
        // Si un CRS est défini, on essaye de récupérer les positions géographiques depuis le formulaire.
        CoordinateReferenceSystem selectedCRS = uiCRSs.getSelectionModel().getSelectedItem();
        if (selectedCRS != null) {
            // On a un point de fin valide : on le prends.
            if (uiLongitudeEnd.valueProperty().get() != null && uiLatitudeEnd != null) {
                ptEnd = GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(
                        fxNumberValue(uiLongitudeEnd),
                        fxNumberValue(uiLatitudeEnd)
                ));
            }

            if (ptEnd != null && !CRS.equalsApproximatively(selectedCRS, baseCrs)) {
                try {
                    ptEnd = (Point) JTS.transform(ptEnd,
                            CRS.findMathTransform(selectedCRS, baseCrs, true));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if (ptEnd == null) {
            ptEnd = computeGeoEndFromLinear();
        }

        return ptEnd;
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
        if (currentTroncon == null) {
            Positionable pos = positionableProperty.get();
            if (pos == null) {
                return null;
            }
            if (pos.getParent() != null) {
                Element tmp = pos.getParent();
                while (tmp != null && !(tmp instanceof TronconDigue)) {
                    tmp = tmp.getParent();
                }
                currentTroncon = (TronconDigue) tmp;
            }
            // Maybe we have an incomplete version of the document, so we try by querying repository.
            if (currentTroncon == null) {
                try {
                    currentTroncon = Injector.getSession().getTronconDigueRepository().get(pos.getDocumentId());
                } catch (Exception e) {
                    // Last chance, we must try to get it from SR
                    if (currentTroncon == null && pos.getSystemeRepId() != null) {
                        SystemeReperage sr = Injector.getSession().getSystemeReperageRepository().get(pos.getSystemeRepId());
                        if (sr.getTronconId() != null) {
                            currentTroncon = Injector.getSession().getTronconDigueRepository().get(sr.getTronconId());
                        }
                    }
                }
            }
        }
        return currentTroncon;
    }
    
    private Optional<SystemeReperage> getDefaultSR() {
        currentTroncon = getTroncon();
        if (currentTroncon.getSystemeRepDefautId() != null) {
            return Optional.of(Injector.getSession().getSystemeReperageRepository().get(currentTroncon.getSystemeRepDefautId()));
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
                    fxNumberValue(uiLongitudeStart),
                    fxNumberValue(uiLatitudeStart)
            ));
        } else {
            ptStart = null;
        }

        // On a un point de fin valide
        if (uiLongitudeEnd.valueProperty().get() != null && uiLatitudeEnd.valueProperty().get() != null) {
            ptEnd = GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(
                    fxNumberValue(uiLongitudeEnd),
                    fxNumberValue(uiLatitudeEnd)
            ));
        } else {
            ptEnd = null;
        }

        // If we've got at least one valid point, we transform it. Otherwise, just return.
        if (ptStart != null || ptEnd != null) {
            final Runnable pointConverter = () -> {
                try {
                    final MathTransform conversion = CRS.findMathTransform(oldValue, newValue, true);

                    if (ptStart != null) {
                        final Point tmpStart = (Point) JTS.transform(ptStart, conversion);
                        Platform.runLater(() -> {
                            uiLongitudeStart.valueProperty().set(tmpStart.getX());
                            uiLatitudeStart.valueProperty().set(tmpStart.getY());
                        });
                    }
                    if (ptEnd != null) {
                        final Point tmpEnd = (Point) JTS.transform(ptEnd, conversion);
                        Platform.runLater(() -> {
                            uiLongitudeEnd.valueProperty().set(tmpEnd.getX());
                            uiLatitudeEnd.valueProperty().set(tmpEnd.getY());
                        });
                    }
                } catch (Exception ex) {
                    GeotkFX.newExceptionDialog("La conversion des positions a échouée.", ex).show();
                    throw new RuntimeException("La conversion des positions a échouée.", ex);
                } finally {
                    Platform.runLater(() -> computingRunning.set(computingRunning.get()-1));
                }
            };

            computingRunning.set(computingRunning.get()+1);
            TaskManager.INSTANCE.submit("Conversion de points géographiques", pointConverter);
        }
               
    }
    
    private double fxNumberValue(FXNumberSpinner spinner){
        if(spinner.valueProperty().get()==null) return 0;
        return spinner.valueProperty().get().doubleValue();
    }
    
    /**
     * Set list of available {@link SystemeReperage} in {@link #uiSRs}.
     */
    private void updateSRList() {        
        final Positionable pos = (Positionable) positionableProperty.get();
        if(pos==null) return;
        
        currentTroncon = getTroncon();
        
        if (currentTroncon != null) {
            computingRunning.set(computingRunning.get()+1);
            TaskManager.INSTANCE.submit("Mise à jour d'une position", () -> {
                try {
                    final SystemeReperageRepository srRepo = Injector.getSession().getSystemeReperageRepository();
                    final List<SystemeReperage> srs = srRepo.getByTroncon(currentTroncon);
                    final SystemeReperage defaultSR;
                    if (pos.getSystemeRepId() != null) {
                        defaultSR = srRepo.get(pos.getSystemeRepId());
                    } else if (currentTroncon.getSystemeRepDefautId() != null) {
                        defaultSR = srRepo.get(currentTroncon.getSystemeRepDefautId());
                    } else {
                        defaultSR = null;
                    }
                    Platform.runLater(() -> {
                        uiSRs.setItems(FXCollections.observableList(srs));
                        uiSRs.getSelectionModel().select(defaultSR);
                    });
                } finally {
                    Platform.runLater(() -> computingRunning.set(computingRunning.get()-1));
                }
            });
        }
    }
    
    /**
     * Update list of available bornes when selected SR changes. If no SR is selected, empty lists are set.
     * 
     * Note : Should ALWAYS be launched from FX-thread.
     * 
     * @param observable
     * @param oldValue Previously selected SR.
     * @param newValue Newly selected SR.
     */
    private void updateBorneList(ObservableValue<? extends SystemeReperage> observable, SystemeReperage oldValue, SystemeReperage newValue) {
        final Positionable pos = (Positionable) positionableProperty.get();
        if (pos == null) {
            return;
        }

        if (newValue == null) {
            ObservableList<BorneDigue> emptyBornes = FXCollections.emptyObservableList();
            uiBorneStart.setItems(emptyBornes);
            uiBorneEnd.setItems(emptyBornes);
        } else {
            computingRunning.set(computingRunning.get()+1);
            TaskManager.INSTANCE.submit("Mise à jour d'une position", () -> {
                try {
                    BorneDigue defaultStart = null, defaultEnd = null;
                    final ArrayList<BorneDigue> bornes = new ArrayList<>();
                    BorneDigueRepository borneRepo = Injector.getSession().getBorneDigueRepository();
                    for (SystemeReperageBorne srb : newValue.systemeReperageBorne) {
                        final String bid = srb.getBorneId();
                        final BorneDigue bd = borneRepo.get(bid);
                        if (bd != null) {
                            bornes.add(bd);
                            if (bd.getId().equals(pos.getBorneDebutId())) {
                                defaultStart = bd;
                            }
                            if (bd.getId().equals(pos.getBorneFinId())) {
                                defaultEnd = bd;
                            }
                        }
                    }

                    final BorneDigue startCopy = defaultStart;
                    final BorneDigue endopy = defaultEnd;
                    Platform.runLater(() -> {
                        ObservableList<BorneDigue> observableBornes = FXCollections.observableList(bornes);
                        uiBorneStart.setItems(observableBornes);
                        uiBorneEnd.setItems(observableBornes);
                        uiBorneStart.getSelectionModel().select(startCopy);
                        uiBorneEnd.getSelectionModel().select(endopy);
                    });
                } finally {
                    Platform.runLater(() -> computingRunning.set(computingRunning.get()-1));
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
            uiDistanceStart.valueProperty().unbindBidirectional(oldValue.borne_debut_distanceProperty());
            uiDistanceEnd.valueProperty().unbindBidirectional(oldValue.borne_fin_distanceProperty());
        }
        currentTroncon = null;
        
        if(newValue==null) return;
        
        computingRunning.set(computingRunning.get()+1);
        final Runnable updater = () -> {
            try {
                //creation de la liste des systemes de reperage
                updateSRList();
                
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        //Bindings
                        uiAmontStart.selectedProperty().bindBidirectional(newValue.borne_debut_avalProperty());
                        uiAmontEnd.selectedProperty().bindBidirectional(newValue.borne_fin_avalProperty());
                        uiDistanceStart.valueProperty().bindBidirectional(newValue.borne_debut_distanceProperty());
                        uiDistanceEnd.valueProperty().bindBidirectional(newValue.borne_fin_distanceProperty());
                        
                        prDebut.set(newValue.getPR_debut());
                        prFin.set(newValue.getPR_fin());
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
                                if(startPos != null){
                                    uiLongitudeStart.valueProperty().set(startPos.getX());
                                    uiLatitudeStart.valueProperty().set(startPos.getY());
                                }
                                if(endPos != null){
                                    uiLongitudeEnd.valueProperty().set(endPos.getX());
                                    uiLatitudeEnd.valueProperty().set(endPos.getY());
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
                    }
                });
            } finally {
                Platform.runLater(() -> computingRunning.set(computingRunning.get()-1));
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
        pos.setPositionDebut(getOrCreateStartPoint());
        pos.setPositionFin(getOrCreateEndPoint());
        
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
        pos.pR_debutProperty().setValue(prDebut.get());
        pos.pR_finProperty().setValue(prFin.get());
        
        //maj de la geometrie du positionable
        final LineString structGeom = LinearReferencingUtilities.buildGeometry(
                currentTroncon.getGeometry(), pos, Injector.getSession().getBorneDigueRepository());
        pos.setGeometry(structGeom);        
    }
    
    /**
     * Compute geographic start point from linear start position.
     */
    private class GeographicStartFromLinear implements ChangeListener {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            // Update only if linear mode is selected. Otherwise, it means a modification on geographic
            // panel modified linear one, which throw back an event. We do not update if borne combo box 
            // is currently selected, as user could be browsing in borne list.
            if (uiTypeBorne.isSelected() && !uiBorneStart.isFocused()) {
                computingRunning.set(computingRunning.get()+1);
                TaskManager.INSTANCE.submit(() -> {
                    try {
                        Point startPoint = computeGeoStartFromLinear();
                        if (startPoint != null) {
                            CoordinateReferenceSystem selectedCRS = uiCRSs.getSelectionModel().getSelectedItem();
                            if (!CRS.equalsApproximatively(baseCrs, selectedCRS)) {
                                startPoint = (Point) JTS.transform(startPoint, CRS.findMathTransform(baseCrs, selectedCRS));
                            }
                            final double x = startPoint.getX();
                            final double y = startPoint.getY();
                            final Optional<SystemeReperage> sr = getDefaultSR();
                            float computedPR = sr.isPresent()? 
                                    TronconUtils.computePR(getSourceLinear(sr.get()), sr.get(), startPoint, Injector.getSession().getBorneDigueRepository())
                                    : Float.NaN;
                            Platform.runLater(() -> {
                                uiLongitudeStart.valueProperty().set(x);
                                uiLatitudeStart.valueProperty().set(y);
                                prDebut.set(computedPR);
                            });
                        }
                        return null;
                    } finally {
                        Platform.runLater(() -> computingRunning.set(computingRunning.get()-1));
                    }
                });
            }
        }
    }

    /**
     * Compute geographic end point from linear ending position.
     */
    private class GeographicEndFromLinear implements ChangeListener {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            // Update only if linear mode is selected. Otherwise, it means a modification on geographic
            // panel modified linear one, which throw back an event. We do not update if borne combo box 
            // is currently selected, as user could be browsing in borne list.
            if (uiTypeBorne.isSelected() && !uiBorneEnd.isFocused()) {
                computingRunning.set(computingRunning.get()+1);
                TaskManager.INSTANCE.submit(() -> {
                    try {
                        Point endPoint = computeGeoEndFromLinear();
                        if (endPoint != null) {
                            CoordinateReferenceSystem selectedCRS = uiCRSs.getSelectionModel().getSelectedItem();
                            if (!CRS.equalsApproximatively(baseCrs, selectedCRS)) {
                                endPoint = (Point) JTS.transform(endPoint, CRS.findMathTransform(baseCrs, selectedCRS));
                            }
                            final double x = endPoint.getX();
                            final double y = endPoint.getY();
                            final Optional<SystemeReperage> sr = getDefaultSR();
                            float computedPR = sr.isPresent()? 
                                    TronconUtils.computePR(getSourceLinear(sr.get()), sr.get(), endPoint, Injector.getSession().getBorneDigueRepository())
                                    : Float.NaN;
                            Platform.runLater(() -> {
                                uiLongitudeEnd.valueProperty().set(x);
                                uiLatitudeEnd.valueProperty().set(y);
                                prFin.set(computedPR);
                            });
                        }
                        return null;
                    } finally {
                        Platform.runLater(() -> computingRunning.set(computingRunning.get()-1));
                    }
                });
            }
        }
    }

    /**
     * Compute linear starting position from geographic/projected start point.
     */
    private class LinearStartFromGeographic implements ChangeListener {

        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            // Update only if geographic mode is selected. Otherwise, it means a 
            // modification on linear panel has thrown back an event.
            if (uiTypeCoord.isSelected()) {
                computingRunning.set(computingRunning.get()+1);
                TaskManager.INSTANCE.submit(() -> {
                    try {
                        Point startPoint = getOrCreateStartPoint();
                        final Optional<SystemeReperage> sr = getDefaultSR();
                        if (startPoint != null && sr.isPresent()) {
                            final Entry<BorneDigue, Double> computedLinear = computeLinearFromGeo(sr.get(), startPoint);
                            final float computedPR = TronconUtils.computePR(getSourceLinear(sr.get()), sr.get(), startPoint, Injector.getSession().getBorneDigueRepository());
                            Platform.runLater(() -> {
                                uiAmontStart.setSelected(computedLinear.getValue() < 0);
                                uiDistanceStart.valueProperty().set(StrictMath.abs(computedLinear.getValue()));
                                uiBorneStart.setValue(computedLinear.getKey());
                                prDebut.set(computedPR);
                            });
                        }
                        return null;
                    } finally {
                        Platform.runLater(() -> computingRunning.set(computingRunning.get()-1));
                    }
                });
            }
        }
    }
    
    /**
     * Compute linear ending position from geographic/projected end point.
     */
    private class LinearEndFromGeographic implements ChangeListener {

        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            // Update only if geographic mode is selected. Otherwise, it means a 
            // modification on linear panel has thrown back an event.
            if (uiTypeCoord.isSelected()) {
                computingRunning.set(computingRunning.get()+1);
                TaskManager.INSTANCE.submit(() -> {
                    try {
                        Point endPoint = getOrCreateEndPoint();
                        final Optional<SystemeReperage> sr = getDefaultSR();
                        if (endPoint != null && sr.isPresent()) {
                            final Entry<BorneDigue, Double> computedLinear = computeLinearFromGeo(sr.get(), endPoint);
                            final float computedPR = TronconUtils.computePR(getSourceLinear(sr.get()), sr.get(), endPoint, Injector.getSession().getBorneDigueRepository());
                            Platform.runLater(() -> {
                                uiAmontEnd.setSelected(computedLinear.getValue() < 0);
                                uiDistanceEnd.valueProperty().set(StrictMath.abs(computedLinear.getValue()));
                                uiBorneEnd.setValue(computedLinear.getKey());
                                prFin.set(computedPR);
                            });
                        }
                        return null;
                    } finally {
                        Platform.runLater(() -> computingRunning.set(computingRunning.get()-1));
                    }
                });
            }
        }
    }
}
