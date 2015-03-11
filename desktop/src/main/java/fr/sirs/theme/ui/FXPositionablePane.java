
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
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Objet;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
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
 *
 * @author Johann Sorel (Geomatys)
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
    @FXML private BorderPane uiPane;
    
    //Mode borne
    @FXML private GridPane uiBornePane;
    @FXML private ComboBox<SystemeReperage> uiSRs;
    @FXML private ComboBox<BorneDigue> uiBorneStart;
    @FXML private ComboBox<BorneDigue> uiBorneEnd;
    @FXML private CheckBox uiAvalSart;
    @FXML private CheckBox uiAvalEnd;
    @FXML private FXNumberSpinner uiDistanceStart;
    @FXML private FXNumberSpinner uiDistanceEnd;    
    //Mode coordonées
    @FXML private GridPane uiCoordPane;
    @FXML private ComboBox<CoordinateReferenceSystem> uiCRSs;
    @FXML private FXNumberSpinner uiLongitudeStart;
    @FXML private FXNumberSpinner uiLongitudeEnd;
    @FXML private FXNumberSpinner uiLatitudeStart;
    @FXML private FXNumberSpinner uiLatitudeEnd;
    
    
    private final ObjectProperty<Positionable> positionableProperty = new SimpleObjectProperty<>();
    private final BooleanProperty disableFieldsProperty = new SimpleBooleanProperty(true);
    private boolean initializing = false;
    private final Map<String,SystemeReperage> cacheSystemeReperage = new HashMap<>();
    private final Map<String,BorneDigue> cacheBorneDigue = new HashMap<>();
    private final CoordinateReferenceSystem baseCrs = SirsCore.getEpsgCode();
            
    public FXPositionablePane() {
        SIRS.loadFXML(this, Positionable.class);
        
        positionableProperty.addListener(this::updateField);
        
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
        
        uiCoordPane.visibleProperty().bind(uiTypeCoord.selectedProperty());
        uiBornePane.visibleProperty().bind(uiTypeBorne.selectedProperty());
        
        //binding de l'etat editable
        final BooleanProperty binding = disableFieldsProperty;
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
        
        uiCRSs.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends CoordinateReferenceSystem> observable, CoordinateReferenceSystem oldValue, CoordinateReferenceSystem newValue) -> {
            updateGeoCoord();
        });
        uiSRs.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends SystemeReperage> observable, SystemeReperage oldValue, SystemeReperage newValue) -> {
            updateBorneList();
        });
        
    }
    
    public ObjectProperty<Positionable> positionableProperty(){
        return positionableProperty;
    }
    
    public Positionable getPositionable(){
        return positionableProperty.get();
    }
    
    public void setPositionable(Positionable positionable){
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

    @FXML
    void viewAllSR(ActionEvent event) {
        final Positionable pos = (Positionable) positionableProperty.get();
        if(pos==null) return;
        
        final Session session = Injector.getBean(Session.class);
        final TronconDigue troncon = session.getTronconDigueRepository().get(pos.getDocumentId());
        final Geometry linear = troncon.getGeometry();
        
        //calcule de la position geographique
        Point startPoint = pos.getPositionDebut();
        Point endPoint = pos.getPositionFin();
        if(startPoint==null){
            //calcule a partir des bornes
            
            final BorneDigue borneStart = cacheBorneDigue.get(pos.borneDebutIdProperty().get());
            final Point borneStartPoint = borneStart.getGeometry();
            double distStart = pos.getBorne_debut_distance();
            if(pos.getBorne_debut_aval()) distStart *= -1;
            
            final BorneDigue borneEnd = cacheBorneDigue.get(pos.borneFinIdProperty().get());
            final Point borneEndPoint = borneEnd.getGeometry();            
            double distEnd = pos.getBorne_fin_distance();
            if(pos.getBorne_fin_aval()) distEnd *= -1;
            
            startPoint = LinearReferencingUtilities.calculateCoordinate(linear, borneStartPoint, distStart, 0);
            endPoint = LinearReferencingUtilities.calculateCoordinate(linear, borneEndPoint, distEnd, 0);
            
        }
        
        
        final StringBuilder page = new StringBuilder();
        page.append("<html><body>");
        
        
        //DataBase coord
        page.append("<h2>Projection de la base ("+baseCrs.getName()+")</h2>");
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
        for(SystemeReperage sr : cacheSystemeReperage.values()){
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
            page.append(startAval ? "en amont":"en aval");
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

    private void updateGeoCoord() {
        if (initializing) return;
        
        final CoordinateReferenceSystem crs = uiCRSs.getSelectionModel().getSelectedItem();

        final Point ptStart, ptEnd;
        // On a un point de début valide
        if (uiLongitudeStart.valueProperty().get() != null && uiLatitudeStart != null) {
            ptStart = GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(
                    fxNumberValue(uiLongitudeStart),
                    fxNumberValue(uiLatitudeStart)
            ));
        } else {
            ptStart = null;
        }

        // On a un point de fin valide
        if (uiLongitudeEnd.valueProperty().get() != null && uiLatitudeEnd != null) {
            ptEnd = GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(
                    fxNumberValue(uiLongitudeEnd),
                    fxNumberValue(uiLatitudeEnd)
            ));
        } else {
            ptEnd = null;
        }

        final Callable pointConverter = () -> {
            final MathTransform conversion;
            if (crs == CRS_WGS84) {
                //conversion vers WGS84
                conversion = CRS.findMathTransform(baseCrs, CRS_WGS84, true);
            } else if (crs == baseCrs) {
                //conversion vers base
                conversion = CRS.findMathTransform(CRS_WGS84, baseCrs, true);
            } else {
                throw new IllegalStateException("Pas de CRS valide.");
            }

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
            return null;
        };
        
        try {
            TaskManager.INSTANCE.submit("Conversion de points géographiques", pointConverter).get();
        } catch (Exception ex) {
            SIRS.LOGGER.log(Level.WARNING, "La conversion des positions a échouée.", ex);
            SIRS.newExceptionDialog("La conversion des positions a échouée.", ex).show();
        }
        
    }
    
    private double fxNumberValue(FXNumberSpinner spinner){
        if(spinner.valueProperty().get()==null) return 0;
        return spinner.valueProperty().get().doubleValue();
    }
    
    private void updateSRList(){
        cacheSystemeReperage.clear();
        
        final Positionable pos = (Positionable) positionableProperty.get();
        if(pos==null) return;
        
        final TronconDigue troncon;
        if (pos.getParent() instanceof TronconDigue) {
            troncon = (TronconDigue) pos.getParent();
        } else {
            troncon = Injector.getSession().getTronconDigueRepository().get(pos.getDocumentId());
        }
        
        if (troncon != null) {
            final List<SystemeReperage> srs = Injector.getSession().getSystemeReperageRepository().getByTroncon(troncon);

            for (SystemeReperage sr : srs) {
                cacheSystemeReperage.put(sr.getId(), sr);
            }

            Runnable srComboUpdate = () -> {
                uiSRs.setItems(FXCollections.observableArrayList(cacheSystemeReperage.values()));
                uiSRs.getSelectionModel().select(cacheSystemeReperage.get(pos.getSystemeRepId()));
            };
            if (Platform.isFxApplicationThread()) {
                srComboUpdate.run();
            } else {
                Platform.runLater(srComboUpdate);
            }
        }
    }
    
    private void updateBorneList() {
        final Positionable pos = (Positionable) positionableProperty.get();
        if (pos == null) return;

        cacheBorneDigue.clear();
        final ArrayList<BorneDigue> bornes = new ArrayList<>();
        final SystemeReperage sr = uiSRs.getSelectionModel().getSelectedItem();
        if (sr != null) {
            BorneDigueRepository borneRepo = Injector.getSession().getBorneDigueRepository();
            for (SystemeReperageBorne srb : sr.systemereperageborneId) {
                final String bid = srb.getBorneId();
                final BorneDigue bd = borneRepo.get(bid);
                if (bd != null) {
                    cacheBorneDigue.put(bid, bd);
                    bornes.add(bd);
                }
            }
        }

        Runnable borneComboUpdate = () -> {
            ObservableList<BorneDigue> observableBornes = FXCollections.observableList(bornes);
            uiBorneStart.setItems(observableBornes);
            uiBorneEnd.setItems(observableBornes);
            if (sr != null && sr.getId().equals(pos.getSystemeRepId())) {
                uiBorneStart.getSelectionModel().select(cacheBorneDigue.get(pos.getBorneDebutId()));
                uiBorneEnd.getSelectionModel().select(cacheBorneDigue.get(pos.getBorneFinId()));
            }
        };
        if (Platform.isFxApplicationThread()) {
            borneComboUpdate.run();
        } else {
            Platform.runLater(borneComboUpdate);
        }
    }
    
    private void updateField(ObservableValue<? extends Positionable> observable, Positionable oldValue, Positionable newValue) {
        if (oldValue != null) {
            uiAvalSart.selectedProperty().unbindBidirectional(oldValue.borne_debut_avalProperty());
            uiAvalEnd.selectedProperty().unbindBidirectional(oldValue.borne_fin_avalProperty());
            uiDistanceStart.valueProperty().unbindBidirectional(oldValue.borne_debut_distanceProperty());
            uiDistanceEnd.valueProperty().unbindBidirectional(oldValue.borne_fin_distanceProperty());
        }
        
        if(newValue==null) return;
        
        initializing = true;
        uiLoading.setVisible(true);
        uiPane.setDisable(true);
        
        new Thread(){
            @Override
            public void run() {
                //creation de la liste des systemes de reperage
                updateSRList();
                updateBorneList();
                
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
                        if(startPos!=null || endPos!=null){
                            uiTypeCoord.setSelected(true);
                        }else{
                            uiTypeBorne.setSelected(true);
                        }

                        uiPane.setDisable(false);
                        uiLoading.setVisible(false);
                        initializing = false;
                    }
                });
            }
        }.start();
    }
        
    public void preSave() {
        final Positionable pos = (Positionable) positionableProperty.get();
        if (pos == null) {
            return;
        }

        // Si un CRS est défini, on essaye de sauvegarder les positions géographiques.
        CoordinateReferenceSystem selectedCRS = uiCRSs.getSelectionModel().getSelectedItem();
        if (selectedCRS != null) {

            Point ptStart, ptEnd;
            // On a un point de début valide : on l'enregistre.
            if (uiLongitudeStart.valueProperty().get() != null && uiLatitudeStart != null) {
                ptStart = GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(
                        fxNumberValue(uiLongitudeStart),
                        fxNumberValue(uiLatitudeStart)
                ));
            } else {
                ptStart = null;
            }

            // On a un point de fin valide : on l'enregistre.
            if (uiLongitudeEnd.valueProperty().get() != null && uiLatitudeEnd != null) {
                ptEnd = GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(
                        fxNumberValue(uiLongitudeEnd),
                        fxNumberValue(uiLatitudeEnd)
                ));
            } else {
                ptEnd = null;
            }

            if (ptStart != null || ptEnd != null) {
                if (!CRS.equalsApproximatively(selectedCRS, baseCrs)) {
                    try {
                        final MathTransform trs = CRS.findMathTransform(selectedCRS, baseCrs, true);
                        if (ptStart != null) {
                            ptStart = (Point) JTS.transform(ptStart, trs);
                        }
                        if (ptEnd != null) {
                            ptEnd = (Point) JTS.transform(ptEnd, trs);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                //on sauvegarde la position geo
                pos.setPositionDebut(ptStart);
                pos.setPositionFin(ptEnd);
            }
            
            //sauvegarde de la position par borne
            final SystemeReperage selectedSR = uiSRs.getSelectionModel().selectedItemProperty().get();
            final BorneDigue borneStart = uiBorneStart.getSelectionModel().selectedItemProperty().get();
            final BorneDigue borneEnd = uiBorneEnd.getSelectionModel().selectedItemProperty().get();
            
            pos.setSystemeRepId((selectedSR != null)? selectedSR.getId() : null);
            
            pos.setBorneDebutId((borneStart != null)? borneStart.getId() : null);
            pos.setBorneFinId((borneEnd != null)? borneEnd.getId() : null);
        }
        
        //maj de la geometrie du positionable
        final TronconDigue troncon;
        
        if (pos.getParent() != null) {
            Element tmp = pos.getParent();
            while (tmp != null && !(tmp instanceof TronconDigue)) {
                tmp = tmp.getParent();
            }
            troncon = (TronconDigue) tmp;
        } else {
            troncon = Injector.getSession().getTronconDigueRepository().get(pos.getDocumentId());
        }
        final LineString structGeom = LinearReferencingUtilities.buildGeometry(
                troncon.getGeometry(), pos, Injector.getSession().getBorneDigueRepository());
        pos.setGeometry(structGeom);
        
    }
}
