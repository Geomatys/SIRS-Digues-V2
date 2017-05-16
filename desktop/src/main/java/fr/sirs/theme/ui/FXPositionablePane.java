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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import static fr.sirs.SIRS.CRS_WGS84;
import static fr.sirs.SIRS.ICON_VIEWOTHER_WHITE;
import fr.sirs.core.LinearReferencingUtilities;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.SystemeReperageRepository;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;
import org.apache.sis.measure.Units;
import org.apache.sis.referencing.CRS;
import org.geotoolkit.display.MeasureUtilities;
import org.geotoolkit.geometry.jts.JTS;
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
    private static final NumberFormat PR_FORMAT = new DecimalFormat("0.00");

    private final List<FXPositionableMode> modes = new ArrayList<>();

    @FXML private Button uiView;
    @FXML private HBox uiExtraContainer;
    @FXML private HBox uiModeContainer;

    // PR Information
    @FXML private Label uiSR;
    @FXML private Label uiPRDebut;
    @FXML private Label uiPRFin;
    @FXML private Label uiGeomInfo;

    private final ObjectProperty<Positionable> posProperty = new SimpleObjectProperty<>();
    private final BooleanProperty disableFieldsProperty = new SimpleBooleanProperty(true);
    private final CoordinateReferenceSystem baseCrs = Injector.getSession().getProjection();


    public FXPositionablePane() {
        this(Arrays.asList(new FXPositionableCoordMode(), new FXPositionableLinearMode()));
    }

    public FXPositionablePane(final List<FXPositionableMode> lstModes) {
        this(lstModes, Positionable.class);
    }

    public FXPositionablePane(final List<FXPositionableMode> lstModes, final Class<? extends Positionable> clazz) {
        SIRS.loadFXML(this, clazz);

        uiView.setGraphic(new ImageView(ICON_VIEWOTHER_WHITE));

        modes.addAll(lstModes);

        //pour chaque mode un toggle button
        final ToggleGroup group = new ToggleGroup();
        for(int i=0,n=modes.size();i<n;i++){
            final FXPositionableMode mode = modes.get(i);

            final ToggleButton button = new ToggleButton(mode.getTitle());
            button.setToggleGroup(group);
            button.getStyleClass().add( (i==0) ? "state-button-left" : (i==n-1) ? "state-button-right" : "state-button-center");
            button.setUserData(mode);
            uiModeContainer.getChildren().add(button);
        }

        //on change les panneaux visibles pour le mode actif
        group.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
            if(oldValue!=null){
                final FXPositionableMode mode = (FXPositionableMode) oldValue.getUserData();
                setCenter(null);
                uiExtraContainer.getChildren().clear();
                mode.disablingProperty().unbind();
                mode.positionableProperty().unbind();
            }

            if(newValue==null){
                group.selectToggle(group.getToggles().get(0));
            }else{
                final FXPositionableMode mode = (FXPositionableMode) newValue.getUserData();
                setCenter(mode.getFXNode());
                uiExtraContainer.getChildren().addAll(mode.getExtraButton());
                mode.disablingProperty().bind(disableFieldsProperty);
                mode.positionableProperty().bind(posProperty);
                if(posProperty.get()!=null){
                    posProperty.get().setGeometryMode(mode.getID());
                }

                final Preferences prefNode = Preferences.userNodeForPackage(FXPositionableMode.class);
                prefNode.put("default", mode.getID());
                try {
                    prefNode.flush();
                } catch (Exception e) {
                    SIRS.LOGGER.log(Level.FINE, "Cannot flush user preferences for toggle selection.", e);
                }
            }
        });


        // Update SR-PR information
        final ChangeListener<Geometry> geomListener = new ChangeListener<Geometry>() {
            @Override
            public void changed(ObservableValue<? extends Geometry> observable, Geometry oldValue, Geometry newValue) {
                updateSRAndPRInfo();
            }
        };
        posProperty.addListener(new ChangeListener<Positionable>() {
            @Override
            public void changed(ObservableValue<? extends Positionable> observable, Positionable oldValue, Positionable newValue) {
                if (oldValue != null) {
                    oldValue.geometryProperty().removeListener(geomListener);
                }
                if (newValue != null) {
                    newValue.geometryProperty().addListener(geomListener);

                    //on active le mode dont le type correspond
                    final String modeName = Preferences.userNodeForPackage(FXPositionableMode.class).get("default", null);
                    Toggle active = group.getToggles().get(0);
                    for (Toggle t : group.getToggles()) {
                        final FXPositionableMode mode = (FXPositionableMode) t.getUserData();
                        if (mode != null && mode.getID().equalsIgnoreCase(modeName)) {
                            active = t;
                            break;
                        }
                    }
                    group.selectToggle(active);
                    newValue.setGeometryMode(((FXPositionableMode)active.getUserData()).getID());
                }
                updateSRAndPRInfo();
            }
        });

    }

    private LinearReferencing.SegmentInfo[] getSourceLinear(final SystemeReperage source) {
        final Positionable positionable = posProperty.get();
        final TronconDigue t = FXPositionableMode.getTronconFromPositionable(positionable);
        return LinearReferencingUtilities.getSourceLinear(t, source);
    }

    /**
     * Returns a label specifying the geometry area if the geometry is a Polygon
     * or MultiPolygon, or the geometry length otherwise.
     *
     * @param geometry
     */
    private static String getGeometryInfo(final Geometry geometry) {
        if (geometry != null) {
            if (geometry instanceof Polygon || geometry instanceof MultiPolygon) {
                final String surface = NumberFormat.getNumberInstance().format(
                        MeasureUtilities.calculateArea(geometry, Injector.getSession().getProjection(), Units.SQUARE_METRE)) +" m²";
                return "Surface : "+surface;
            } else {
                final String longueur = NumberFormat.getNumberInstance().format(
                        MeasureUtilities.calculateLenght(geometry,
                                Injector.getSession().getProjection(), Units.METRE)) +" m";
                return "Longueur : "+longueur;
            }
        }
        else {
            return "";
        }
    }

    private void updateSRAndPRInfo() {
        final Positionable pos = getPositionable();

        final SystemeReperageRepository srRepo = (SystemeReperageRepository) Injector.getSession().getRepositoryForClass(SystemeReperage.class);
        final TronconDigue troncon = FXPositionableMode.getTronconFromPositionable(pos);
        final SystemeReperage sr;
        if (pos.getSystemeRepId() != null) {
            sr = srRepo.get(pos.getSystemeRepId());
        } else if (troncon.getSystemeRepDefautId() != null) {
            sr = srRepo.get(troncon.getSystemeRepDefautId());
        } else {
            sr = null;
        }

        if (sr!=null) {
            final LinearReferencing.SegmentInfo[] segments = getSourceLinear(sr);
            final TronconUtils.PosInfo posInfo = new TronconUtils.PosInfo(pos, troncon, segments);

            final Geometry geometry = posInfo.getGeometry();
            final Point startPoint, endPoint;
            if (geometry instanceof LineString) {
                LineString ls = (LineString) geometry;
                startPoint = ls.getStartPoint();
                endPoint = ls.getEndPoint();
            } else {
                startPoint = posInfo.getGeoPointStart();
                endPoint = posInfo.getGeoPointEnd();
            }

            final AbstractSIRSRepository<BorneDigue> borneRepo = Injector.getSession().getRepositoryForClass(BorneDigue.class);
            final float startPr = TronconUtils.computePR(segments, sr, startPoint, borneRepo);
            final float endPr = TronconUtils.computePR(segments, sr, endPoint, borneRepo);

            uiSR.setText(sr.getLibelle());
            uiPRDebut.setText(PR_FORMAT.format(startPr));
            uiPRFin.setText(PR_FORMAT.format(endPr));
            //on sauvegarde les PR dans le positionable.
            pos.setPrDebut(startPr);
            pos.setPrFin(endPr);

        }else{
            uiSR.setText("");
            uiPRDebut.setText("");
            uiPRFin.setText("");
            pos.setPrDebut(0);
            pos.setPrFin(0);
        }

        if(pos.getGeometry()!=null){
            uiGeomInfo.setText(getGeometryInfo(pos.getGeometry()));
        }
    }

    public ObjectProperty<Positionable> positionableProperty() {
        return posProperty;
    }

    public Positionable getPositionable() {
        return posProperty.get();
    }

    public void setPositionable(Positionable positionable) {
        posProperty.set(positionable);
    }

    public BooleanProperty disableFieldsProperty(){
        return disableFieldsProperty;
    }

    public void preSave() {

    }

    /**
     * TODO : check null pointer and computing
     *
     * @param event
     */
    @FXML
    void viewAllSR(ActionEvent event) {
        if (posProperty.get() == null) {
            return;
        }

        final StringBuilder page = new StringBuilder();
        page.append("<html><body>");

        //calcul de la position geographique
        final Positionable pos = getPositionable();
        final TronconDigue troncon = FXPositionableMode.getTronconFromPositionable(getPositionable());
        final SystemeReperageRepository srRepo = (SystemeReperageRepository) Injector.getSession().getRepositoryForClass(SystemeReperage.class);

        final SystemeReperage defaultSr;
        if (pos.getSystemeRepId() != null) {
            defaultSr = srRepo.get(pos.getSystemeRepId());
        } else if (troncon.getSystemeRepDefautId() != null) {
            defaultSr = srRepo.get(troncon.getSystemeRepDefautId());
        } else {
            defaultSr = null;
        }

        final LinearReferencing.SegmentInfo[] defaultSegments = getSourceLinear(defaultSr);
        final TronconUtils.PosInfo posInfo = new TronconUtils.PosInfo(pos, troncon, defaultSegments);

        Point startPoint = posInfo.getGeoPointStart();
        Point endPoint = posInfo.getGeoPointEnd();

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
                final MathTransform trs = CRS.findOperation(baseCrs, CRS_WGS84, null).getMathTransform();
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

            final AbstractSIRSRepository<BorneDigue> borneRepo = Injector.getSession().getRepositoryForClass(BorneDigue.class);
            final List<SystemeReperage> srs = srRepo.getByLinear(troncon);

            //pour chaque systeme de reperage
            for (SystemeReperage sr : srs) {
                final LinearReferencing.SegmentInfo[] segments = getSourceLinear(sr);
                Map.Entry<BorneDigue, Double> computedLinear = FXPositionableMode.computeLinearFromGeo(segments, sr, startPoint);
                boolean aval = true;
                double distanceBorne = computedLinear.getValue();
                if (distanceBorne < 0) {
                    distanceBorne = -distanceBorne;
                    aval = false;
                }
                float computedPR = TronconUtils.computePR(getSourceLinear(sr), sr, startPoint, borneRepo);

                page.append("<h2>SR : ").append(sr.getLibelle()).append("</h2>");
                page.append("<b>Début </b>");
                page.append(computedLinear.getKey().getLibelle()).append(" à ");
                page.append(DISTANCE_FORMAT.format(distanceBorne)).append("m ");
                page.append(aval ? "en aval" : "en amont").append('.');
                page.append(" Valeur du PR : ").append(computedPR).append('.');
                page.append("<br/>");

                if (!startPoint.equals(endPoint)) {
                    computedLinear = FXPositionableMode.computeLinearFromGeo(segments, sr, endPoint);
                    aval = true;
                    distanceBorne = computedLinear.getValue();
                    if (distanceBorne < 0) {
                        distanceBorne = -distanceBorne;
                        aval = false;
                    }
                    computedPR = TronconUtils.computePR(getSourceLinear(sr), sr, endPoint, borneRepo);
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
        dialog.setOnCloseRequest(event1 -> dialog.hide());
        dialog.show();
    }

}
