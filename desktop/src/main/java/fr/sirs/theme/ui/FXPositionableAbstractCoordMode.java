/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 *
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
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
import org.apache.sis.referencing.CRS;
import org.apache.sis.util.Utilities;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.internal.GeotkFX;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 * Edition des coordonées géographique d'un {@link Positionable}.
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class FXPositionableAbstractCoordMode extends BorderPane implements FXPositionableMode {

    private final CoordinateReferenceSystem baseCrs = Injector.getSession().getProjection();

    private final ObjectProperty<Positionable> posProperty = new SimpleObjectProperty<>();
    protected final BooleanProperty disableProperty = new SimpleBooleanProperty(true);

    @FXML protected ComboBox<CoordinateReferenceSystem> uiCRSs;
    @FXML protected Spinner<Double> uiLongitudeStart;
    @FXML protected Spinner<Double> uiLongitudeEnd;
    @FXML protected Spinner<Double> uiLatitudeStart;
    @FXML protected Spinner<Double> uiLatitudeEnd;

    private Button uiImport;

    private boolean reseting = false;
    public boolean isReseting(){return reseting;}
    public void setReseting(final boolean res){reseting = res;}

    public FXPositionableAbstractCoordMode() {
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

        // Listener pour les changements sur les points de début et de fin, dans le cadre de l'import de bornes par exemple.
        final ChangeListener<Point> pointListener = (obs, oldVal, newVal) ->  updateFields();

        posProperty.addListener(new ChangeListener<Positionable>() {
            @Override
            public void changed(ObservableValue<? extends Positionable> observable, Positionable oldValue, Positionable newValue) {
                if (oldValue != null) {
                    oldValue.geometryProperty().removeListener(geomListener);
                    oldValue.positionDebutProperty().removeListener(pointListener);
                    oldValue.positionFinProperty().removeListener(pointListener);
                }
                if (newValue != null) {
                    newValue.geometryProperty().addListener(geomListener);
                    newValue.positionDebutProperty().addListener(pointListener);
                    newValue.positionFinProperty().addListener(pointListener);
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
        dialog.setOnCloseRequest(event1 -> dialog.hide());
        dialog.show();
    }

    @Override
    public void updateFields(){

        reseting = true;

        //selectionner RGF93 par defaut
        uiCRSs.getSelectionModel().clearAndSelect(1);

        final Positionable pos = posProperty.get();
        final String mode = pos.getGeometryMode();

        if(mode == null || getID().equals(mode)){
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
            final TronconUtils.PosInfo ps = new TronconUtils.PosInfo(pos, t);
            final Point geoPointStart = ps.getGeoPointStart();
            final Point geoPointEnd = ps.getGeoPointEnd();

            uiLongitudeStart.getValueFactory().setValue(geoPointStart==null ? null : geoPointStart.getX());
            uiLatitudeStart.getValueFactory().setValue(geoPointStart==null ? null : geoPointStart.getY());
            uiLongitudeEnd.getValueFactory().setValue(geoPointEnd==null ? null : geoPointEnd.getX());
            uiLatitudeEnd.getValueFactory().setValue(geoPointEnd==null ? null : geoPointEnd.getY());
        }else{
            //pas de geometrie
            uiLongitudeStart.getValueFactory().setValue(null);
            uiLatitudeStart.getValueFactory().setValue(null);
            uiLongitudeEnd.getValueFactory().setValue(null);
            uiLatitudeEnd.getValueFactory().setValue(null);
        }

        reseting = false;
    }

    @Override
    public void buildGeometry(){

        final Positionable positionable = posProperty.get();

        // On ne met la géométrie à jour depuis ce panneau que si on est dans son mode.
        if(!getID().equals(positionable.getGeometryMode())) return;

        // Si un CRS est défini, on essaye de récupérer les positions géographiques depuis le formulaire.
        final CoordinateReferenceSystem crs = uiCRSs.getSelectionModel().getSelectedItem();
        if(crs==null) return;

        Point startPoint = null;
        Point endPoint = null;
        if(uiLongitudeStart.getValue()!=null && uiLatitudeStart.getValue()!=null){
            startPoint = GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(
                    uiLongitudeStart.getValue(), uiLatitudeStart.getValue()));
            JTS.setCRS(startPoint, crs);
        }

        if(uiLongitudeEnd.getValue()!=null && uiLatitudeEnd.getValue()!=null){
            endPoint = GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(
                    uiLongitudeEnd.getValue(), uiLatitudeEnd.getValue()));
            JTS.setCRS(endPoint, crs);
        }

        if(startPoint==null && endPoint==null) return;
        if(startPoint==null) startPoint = endPoint;
        if(endPoint==null) endPoint = startPoint;

        final TronconDigue troncon = FXPositionableMode.getTronconFromPositionable(positionable);
        final LineString geometry = LinearReferencingUtilities.buildGeometryFromGeo(troncon.getGeometry(), startPoint, endPoint);

        //on sauvegarde les points dans le crs de la base
        positionable.setGeometry(geometry);
        if(!Utilities.equalsIgnoreMetadata(crs, Injector.getSession().getProjection())){
            try{
                final MathTransform trs = CRS.findOperation(crs, Injector.getSession().getProjection(), null).getMathTransform();
                startPoint = (Point) JTS.transform(startPoint, trs);
                endPoint = (Point) JTS.transform(endPoint, trs);
            }catch(FactoryException | MismatchedDimensionException | TransformException ex){
                GeotkFX.newExceptionDialog("La conversion des positions a échouée.", ex).show();
                throw new RuntimeException("La conversion des positions a échouée.", ex);
            }
        }
        positionable.setPositionDebut(startPoint);
        positionable.setPositionFin(endPoint);
        positionable.geometryModeProperty().set(getID());
        positionable.geometryProperty().set(geometry);
    }

    protected void coordChange(){
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
                final MathTransform conversion = CRS.findOperation(oldValue, newValue, null).getMathTransform();

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
