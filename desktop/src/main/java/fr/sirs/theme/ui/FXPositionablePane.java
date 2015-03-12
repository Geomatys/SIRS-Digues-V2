
package fr.sirs.theme.ui;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import fr.sirs.core.LinearReferencingUtilities;
import fr.sirs.core.SirsCore;
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
import java.awt.Color;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebView;
import org.apache.sis.referencing.CommonCRS;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.gui.javafx.util.FXNumberSpinner;
import org.geotoolkit.referencing.CRS;
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
    
    public static final Image ICON_IMPORT  = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_DOWNLOAD,22,Color.WHITE),null);
    public static final Image ICON_VIEWOTHER  = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_BARS,22,Color.WHITE),null);
    
    @FXML private Button uiImport;
    @FXML private Button uiView;
    
    @FXML private ToggleButton uiTypeBorne;
    @FXML private ToggleButton uiTypeCoord;
    @FXML private ProgressIndicator uiLoading;
    
    // Borne mode
    @FXML private GridPane uiBornePane;
    @FXML private ComboBox<SystemeReperage> uiSRs;
    @FXML private ComboBox<BorneDigue> uiBorneStart;
    @FXML private ComboBox<BorneDigue> uiBorneEnd;
    @FXML private CheckBox uiAvalSart;
    @FXML private CheckBox uiAvalEnd;
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
    private final SimpleBooleanProperty computingRunning = new SimpleBooleanProperty(false);
    
    private final ObjectProperty<Positionable> positionableProperty = new SimpleObjectProperty<>();
    private final SimpleBooleanProperty disableFieldsProperty = new SimpleBooleanProperty(true);
    private final CoordinateReferenceSystem baseCrs = SirsCore.getEpsgCode();
    
    /**
     * Reference to TronconDigue parent of the current positionable
     */
    private TronconDigue currentTroncon;

    public FXPositionablePane() {
        SIRS.loadFXML(this, Positionable.class);
                
        uiImport.setGraphic(new ImageView(ICON_IMPORT));
        uiView.setGraphic(new ImageView(ICON_VIEWOTHER));
        
        final SirsStringConverter sirsStringConverter = new SirsStringConverter();     
        uiSRs.setConverter(sirsStringConverter);
        uiCRSs.setConverter(sirsStringConverter);
        uiBorneStart.setConverter(sirsStringConverter);
        uiBorneEnd.setConverter(sirsStringConverter);
        
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
        final BooleanBinding binding = disableFieldsProperty.or(disabledProperty());
        uiImport.visibleProperty().bind(disableFieldsProperty.not().and(uiTypeCoord.selectedProperty()));
        
        uiSRs.disableProperty().bind(binding);
        uiBorneStart.disableProperty().bind(binding);
        uiBorneEnd.disableProperty().bind(binding);
        uiAvalSart.disableProperty().bind(binding);
        uiAvalEnd.disableProperty().bind(binding);
        uiDistanceStart.disableProperty().bind(binding);
        uiDistanceEnd.disableProperty().bind(binding);
        
        uiLongitudeStart.disableProperty().bind(binding);
        uiLatitudeStart.disableProperty().bind(binding);
        uiLongitudeEnd.disableProperty().bind(binding);
        uiLatitudeEnd.disableProperty().bind(binding);
        
        // Bind progress display and field disabling to computing state. It allows to "lock" the component when updating its content.        
        uiLoading.visibleProperty().bind(computingRunning);
        disableProperty().bind(computingRunning);
        
        // Position mode : linear or geographic
        uiCoordPane.visibleProperty().bind(uiTypeCoord.selectedProperty());
        uiBornePane.visibleProperty().bind(uiTypeBorne.selectedProperty());
        
        /*
         * DATA LISTENERS
         */        
        uiCRSs.getSelectionModel().selectedItemProperty().addListener(this::updateGeoCoord);        
        uiSRs.getSelectionModel().selectedItemProperty().addListener(this::updateBorneList);
        positionableProperty.addListener(this::updateField);
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
     * @param event 
     */
    @FXML
    void viewAllSR(ActionEvent event) {
        if(positionableProperty.get()==null) return;
        
        final Session session = Injector.getBean(Session.class);
        final Geometry linear = currentTroncon.getGeometry();
        
        final StringBuilder page = new StringBuilder();
        page.append("<html><body>");
            
        //calcul de la position geographique
        Point startPoint = getOrCreateStartPoint();
        Point endPoint = getOrCreateEndPoint();
        if (startPoint != null || endPoint != null) {

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
        }
        
        //pour chaque systeme de reperage
        for(SystemeReperage sr : uiSRs.getItems()) {
            final List<BorneDigue> bornes = new ArrayList<>();
            final List<Point> references = new ArrayList<>();
            for(SystemeReperageBorne srb : sr.systemereperageborneId){
                final String bid = srb.getBorneId();
                final BorneDigue bd = session.getBorneDigueRepository().get(bid);
                if(bd!=null){ 
                    bornes.add(bd);
                    references.add(bd.getGeometry());
                }
            }
            
            final Entry<Integer,double[]> startRef = LinearReferencingUtilities.calculateRelative(linear, references.toArray(new Point[0]), startPoint);
            final BorneDigue startBorne = bornes.get(startRef.getKey());
            double distanceStartBorne = startRef.getValue()[0];
            boolean startAval = false;
            if(distanceStartBorne<0){
                distanceStartBorne = -distanceStartBorne;
                startAval = true;
            }
            
            final Entry<Integer,double[]> endRef = LinearReferencingUtilities.calculateRelative(linear, references.toArray(new Point[0]), endPoint);
            final BorneDigue endBorne = bornes.get(endRef.getKey());
            double distanceEndBorne = endRef.getValue()[0];
            boolean endAval = false;
            if(distanceEndBorne<0){
                distanceEndBorne = -distanceEndBorne;
                endAval = true;
            }
            
            page.append("<h2>SR : ").append(sr.getLibelle()).append("</h2>");
            page.append("<b>Début </b>");
            page.append(startBorne.getLibelle()).append(' ');
            page.append(DISTANCE_FORMAT.format(distanceStartBorne)).append("m ");
            page.append(startAval ? "en aval":"en amont");
            page.append("<br/>");
            page.append("<b>Fin&nbsp&nbsp </b>");
            page.append(endBorne.getLibelle()).append(' ');
            page.append(DISTANCE_FORMAT.format(distanceEndBorne)).append("m ");
            page.append(endAval ? "en amont":"en aval");
            page.append("<br/><br/>");
        }
        
        page.append("</html></body>");
        
        final WebView view = new WebView();        
        view.getEngine().loadContent(page.toString());
        view.getEngine().userStyleSheetLocationProperty().set(FXPositionablePane.class.getResource("/fr/sirs/web.css").toString() );
        
        final Dialog dialog = new Dialog();
        final DialogPane pane = new DialogPane();
        pane.setContent(view);
        pane.getButtonTypes().add(ButtonType.CLOSE);
        dialog.setDialogPane(pane);
        dialog.setTitle("Position");
        dialog.setOnCloseRequest((Event event1) -> {dialog.hide();});
        dialog.showAndWait();
    }

    /**
     * Compute current positionable start point using linear referencing information
     * defined in the form. Returned point is expressed with Database CRS.
     * 
     * @return The point computed from starting borne. If we cannot, we return {@link Positionable#getPositionDebut() }
     */
    private Point computeGeoStartFromBornes() {
        final Number distanceStart = uiDistanceStart.valueProperty().get();
        if (distanceStart != null && uiBorneStart.getValue() != null && currentTroncon != null) {
            //calcul à partir des bornes
            final Point borneStartPoint = uiBorneStart.getValue().getGeometry();
            double distStart = distanceStart.doubleValue();
            if (uiAvalSart.isSelected()) {
                distStart *= -1;
            }

            return LinearReferencingUtilities.calculateCoordinate(currentTroncon.getGeometry(), borneStartPoint, distStart, 0);
        } else {
            return positionableProperty.get().getPositionDebut();
        }
    }
    
    /**
     * Compute current positionable end point using linear referencing information
     * defined in the form. Returned point is expressed with Database CRS.
     * 
     * @return The point computed from ending borne. If we cannot, we return {@link Positionable#getPositionFin() }
     */
    private Point computeGeoEndFromBornes() {
        final Number distanceEnd = uiDistanceEnd.valueProperty().get();
        if (distanceEnd != null && uiBorneEnd.getValue() != null && currentTroncon != null) {
            //calcul à partir des bornes
            final Point borneEndPoint = uiBorneEnd.getValue().getGeometry();
            double distEnd = distanceEnd.doubleValue();
            if (uiAvalEnd.isSelected()) {
                distEnd *= -1;
            }

            return LinearReferencingUtilities.calculateCoordinate(currentTroncon.getGeometry(), borneEndPoint, distEnd, 0);
        } else {
            return positionableProperty.get().getPositionFin();
        }
    }
        
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
            ptStart = computeGeoStartFromBornes();
        }
        
        return ptStart;
    }
    
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
            ptEnd = computeGeoEndFromBornes();
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
        Positionable pos = positionableProperty.get();
        if (pos == null) return null;
        TronconDigue troncon = null;
        if (pos.getParent() != null) {
            Element tmp = pos.getParent();
            while (tmp != null && !(tmp instanceof TronconDigue)) {
                tmp = tmp.getParent();
            }
            troncon = (TronconDigue) tmp;
        }
        // Maybe we have an incomplete version of the document, so we try by querying repository.
        if (troncon == null) {
            try {
                troncon = Injector.getSession().getTronconDigueRepository().get(pos.getDocumentId());
            } catch (Exception e) {
                troncon = null;
            }
        }
        // Last chance, we must try to get it from SR
        if (troncon == null && pos.getSystemeRepId() != null) {
            SystemeReperage sr = Injector.getSession().getSystemeReperageRepository().get(pos.getSystemeRepId());
            if (sr.getTronconId() != null) {
                troncon = Injector.getSession().getTronconDigueRepository().get(sr.getTronconId());
            }
        }
            
        return troncon;
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
                    // TODO : do we really need to log here ?
                    SIRS.LOGGER.log(Level.WARNING, "La conversion des positions a échouée.", ex);
                    SIRS.newExceptionDialog("La conversion des positions a échouée.", ex).show();
                    throw new RuntimeException("La conversion des positions a échouée.", ex);
                } finally {
                    Platform.runLater(() -> computingRunning.set(false));
                }
            };

            computingRunning.set(true);
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
            Platform.runLater(()->computingRunning.set(true));
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
                    Platform.runLater(() -> computingRunning.set(false));
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
            computingRunning.set(true);
            TaskManager.INSTANCE.submit("Mise à jour d'une position", () -> {
                try {
                    BorneDigue defaultStart = null, defaultEnd = null;
                    final ArrayList<BorneDigue> bornes = new ArrayList<>();
                    BorneDigueRepository borneRepo = Injector.getSession().getBorneDigueRepository();
                    for (SystemeReperageBorne srb : newValue.systemereperageborneId) {
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
                    Platform.runLater(() -> computingRunning.set(false));
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
            uiAvalSart.selectedProperty().unbindBidirectional(oldValue.borne_debut_avalProperty());
            uiAvalEnd.selectedProperty().unbindBidirectional(oldValue.borne_fin_avalProperty());
            uiDistanceStart.valueProperty().unbindBidirectional(oldValue.borne_debut_distanceProperty());
            uiDistanceEnd.valueProperty().unbindBidirectional(oldValue.borne_fin_distanceProperty());
        }
        
        if(newValue==null) return;
        
        computingRunning.set(true);        
        final Runnable updater = () -> {
            try {
                //creation de la liste des systemes de reperage
                updateSRList();
                
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        //Bindings
                        uiAvalSart.selectedProperty().bindBidirectional(newValue.borne_debut_avalProperty());
                        uiAvalEnd.selectedProperty().bindBidirectional(newValue.borne_fin_avalProperty());
                        uiDistanceStart.valueProperty().bindBidirectional(newValue.borne_debut_distanceProperty());
                        uiDistanceEnd.valueProperty().bindBidirectional(newValue.borne_fin_distanceProperty());
                        
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
                computingRunning.set(false);  
            }
        };
        
        TaskManager.INSTANCE.submit("Mise à jour d'une position", updater);
    }
    
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

        pos.setSystemeRepId((selectedSR != null) ? selectedSR.getId() : null);

        pos.setBorneDebutId((borneStart != null) ? borneStart.getId() : null);
        pos.setBorneFinId((borneEnd != null) ? borneEnd.getId() : null);
        
        //maj de la geometrie du positionable
        final LineString structGeom = LinearReferencingUtilities.buildGeometry(
                currentTroncon.getGeometry(), pos, Injector.getSession().getBorneDigueRepository());
        pos.setGeometry(structGeom);        
    }
}
