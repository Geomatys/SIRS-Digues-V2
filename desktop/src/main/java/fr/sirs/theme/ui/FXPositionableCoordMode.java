package fr.sirs.theme.ui;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import static fr.sirs.SIRS.CRS_WGS84;
import static fr.sirs.SIRS.ICON_IMPORT_WHITE;
import fr.sirs.core.LinearReferencingUtilities;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.TronconDigue;
import static fr.sirs.theme.ui.FXPositionableMode.fxNumberValue;
import fr.sirs.util.SirsStringConverter;
import java.util.Collections;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

/**
 * Edition des coordonées géographique d'un {@link Positionable}.
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXPositionableCoordMode extends BorderPane implements FXPositionableMode {

    private static final String MODE = "COORD";

    private final CoordinateReferenceSystem baseCrs = Injector.getSession().getProjection();
    
    private final ObjectProperty<Positionable> posProperty = new SimpleObjectProperty<>();
    private final BooleanProperty disableProperty = new SimpleBooleanProperty(true);

    @FXML private ComboBox<CoordinateReferenceSystem> uiCRSs;
    @FXML private Spinner<Double> uiLongitudeStart;
    @FXML private Spinner<Double> uiLongitudeEnd;
    @FXML private Spinner<Double> uiLatitudeStart;
    @FXML private Spinner<Double> uiLatitudeEnd;

    private Button uiImport;

    private boolean reseting = false;

    public FXPositionableCoordMode() {
        SIRS.loadFXML(this, Positionable.class);

        //bouton d'import
        uiImport = new Button();
        uiImport.setGraphic(new ImageView(ICON_IMPORT_WHITE));
        uiImport.getStyleClass().add("buttonbar-button");
        uiImport.setOnAction(this::importCoord);
        uiImport.visibleProperty().bind(disableProperty.not());

        //liste par défaut des systemes de coordonnées
        final ObservableList<CoordinateReferenceSystem> crss = FXCollections.observableArrayList();
        crss.add(CRS_WGS84);
        crss.add(baseCrs);
        uiCRSs.setItems(crss);
        uiCRSs.getSelectionModel().clearAndSelect(1);
        uiCRSs.disableProperty().bind(disableProperty);
        uiCRSs.setConverter(new SirsStringConverter());
        
        uiLongitudeStart.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE, 0,1));
        uiLatitudeStart.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE, 0,1));
        uiLongitudeEnd.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE, 0,1));
        uiLatitudeEnd.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE, 0,1));
        uiLongitudeStart.setEditable(true);
        uiLatitudeStart.setEditable(true);
        uiLongitudeEnd.setEditable(true);
        uiLatitudeEnd.setEditable(true);
        uiLongitudeStart.disableProperty().bind(disableProperty);
        uiLatitudeStart.disableProperty().bind(disableProperty);
        uiLongitudeEnd.disableProperty().bind(disableProperty);
        uiLatitudeEnd.disableProperty().bind(disableProperty);


        final ChangeListener<Geometry> geomListener = new ChangeListener<Geometry>() {
            @Override
            public void changed(ObservableValue<? extends Geometry> observable, Geometry oldValue, Geometry newValue) {
                if(reseting) return;
                updateFields();
            }
        };

        posProperty.addListener(new ChangeListener<Positionable>() {
            @Override
            public void changed(ObservableValue<? extends Positionable> observable, Positionable oldValue, Positionable newValue) {
                if(oldValue!=null){
                    oldValue.geometryProperty().removeListener(geomListener);
                }
                if(newValue!=null){
                    newValue.geometryProperty().addListener(geomListener);
                    updateFields();
                }
            }
        });

        final ChangeListener<Double> valListener = (ObservableValue<? extends Double> observable, Double oldValue, Double newValue) -> coordChange();
        uiLongitudeStart.valueProperty().addListener(valListener);
        uiLatitudeStart.valueProperty().addListener(valListener);
        uiLongitudeEnd.valueProperty().addListener(valListener);
        uiLatitudeEnd.valueProperty().addListener(valListener);

        uiCRSs.getSelectionModel().selectedItemProperty().addListener(this::crsChange);
    }

    @Override
    public String getID() {
        return MODE;
    }
    
    @Override
    public String getTitle() {
        return "Coordonnée";
    }

    public List<Node> getExtraButton(){
        return Collections.singletonList(uiImport);
    }

    @Override
    public Node getFXNode() {
        return this;
    }

    @Override
    public ObjectProperty<Positionable> positionableProperty() {
        return posProperty;
    }

    @Override
    public BooleanProperty disablingProperty() {
        return disableProperty;
    }

    private void importCoord(ActionEvent event) {
        final FXImportCoordinate importCoord = new FXImportCoordinate(posProperty.get());
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

    private void updateFields(){
        reseting = true;

        //selectionner RGF93 par defaut
        uiCRSs.getSelectionModel().clearAndSelect(1);

        final Positionable pos = posProperty.get();
        final String mode = pos.getGeometryMode();

        if(MODE.equals(mode)){
            //on peut réutiliser les points enregistré dans la position
            final Point startPos = pos.getPositionDebut();
            final Point endPos = pos.getPositionFin();
            if (startPos != null) {
                uiLongitudeStart.getValueFactory().valueProperty().set(startPos.getX());
                uiLatitudeStart.getValueFactory().valueProperty().set(startPos.getY());
            }else{
                uiLongitudeStart.getValueFactory().setValue(null);
                uiLatitudeStart.getValueFactory().setValue(null);
            }
            if (endPos != null) {
                uiLongitudeEnd.getValueFactory().valueProperty().set(endPos.getX());
                uiLatitudeEnd.getValueFactory().valueProperty().set(endPos.getY());
            }else{
                uiLongitudeEnd.getValueFactory().setValue(null);
                uiLatitudeEnd.getValueFactory().setValue(null);
            }
        }else if(pos.getGeometry()!=null){
            //on refait les points a partir de la géométrie
            final TronconDigue t = FXPositionableMode.getTronconFromPositionable(pos);
            final TronconUtils.PosInfo ps = new TronconUtils.PosInfo(pos, t, Injector.getSession());
            final Point geoPointStart = ps.getGeoPointStart();
            final Point geoPointEnd = ps.getGeoPointEnd();

            uiLongitudeStart.getValueFactory().setValue(geoPointStart==null ? null : geoPointStart.getX());
            uiLatitudeStart.getValueFactory().setValue(geoPointStart==null ? null : geoPointStart.getY());
            uiLongitudeEnd.getValueFactory().setValue(geoPointEnd==null ? null : geoPointEnd.getX());
            uiLatitudeEnd.getValueFactory().setValue(geoPointEnd==null ? null : geoPointEnd.getY());
        }else{
            //pas de geometrie
            uiLongitudeStart.getValueFactory().setValue(0.0);
            uiLatitudeStart.getValueFactory().setValue(0.0);
            uiLongitudeEnd.getValueFactory().setValue(0.0);
            uiLatitudeEnd.getValueFactory().setValue(0.0);
        }

        reseting = false;
    }

    private void buildGeometry(){
        // Si un CRS est défini, on essaye de récupérer les positions géographiques depuis le formulaire.
        final CoordinateReferenceSystem crs = uiCRSs.getSelectionModel().getSelectedItem();
        if(crs==null) return;

        Point startPoint = null;
        Point endPoint = null;
        if(uiLongitudeStart.getValue()!=null && uiLatitudeStart.getValue()!=null){
            startPoint = GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(
                    uiLongitudeStart.getValue(),uiLatitudeStart.getValue()));
            JTS.setCRS(startPoint, crs);
        }

        if(uiLongitudeEnd.getValue()!=null && uiLatitudeEnd.getValue()!=null){
            endPoint = GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(
                    uiLongitudeEnd.getValue(),uiLatitudeEnd.getValue()));
            JTS.setCRS(endPoint, crs);
        }

        if(startPoint==null && endPoint==null) return;
        if(startPoint==null) startPoint = endPoint;
        if(endPoint==null) endPoint = startPoint;

        final Positionable positionable = posProperty.get();
        final TronconDigue troncon = FXPositionableMode.getTronconFromPositionable(positionable);
        final LineString geometry = LinearReferencingUtilities.buildGeometryFromGeo(troncon.getGeometry(),startPoint,endPoint);

        //on sauvegarde les points dans le crs de la base
        positionable.setGeometry(geometry);
        if(!CRS.equalsIgnoreMetadata(crs, Injector.getSession().getProjection())){
            try{
                final MathTransform trs = CRS.findMathTransform(crs, Injector.getSession().getProjection());
                startPoint = (Point) JTS.transform(startPoint, trs);
                endPoint = (Point) JTS.transform(endPoint, trs);
            }catch(Exception ex){
                GeotkFX.newExceptionDialog("La conversion des positions a échouée.", ex).show();
                throw new RuntimeException("La conversion des positions a échouée.", ex);
            }
        }
        positionable.setPositionDebut(startPoint);
        positionable.setPositionDebut(endPoint);
        positionable.geometryModeProperty().set(MODE);
        positionable.geometryProperty().set(geometry);
    }

    private void coordChange(){
        if(reseting) return;
        
        reseting = true;
        buildGeometry();
        reseting = false;
    }


    /**
     * Update geographic/projected coordinate fields when current CRS change.
     * Note : listener method, should always be launched from FX-thread.
     *
     * @param observable
     * @param oldValue Previous value into {@link #uiCRSs}
     * @param newValue Current value into {@link #uiCRSs}
     */
    private void crsChange(ObservableValue<? extends CoordinateReferenceSystem> observable, 
            CoordinateReferenceSystem oldValue, CoordinateReferenceSystem newValue) {
        if(reseting) return;

        // There's no available null value in CRS combobox, so old value will be
        // null only at first allocation, no transform needed in this case.
        if (oldValue == null || newValue == null) {
            return;
        }

        reseting = true;

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
            }
        }

        buildGeometry();
        reseting = false;
    }

}
