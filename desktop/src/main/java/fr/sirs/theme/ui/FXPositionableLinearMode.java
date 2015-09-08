package fr.sirs.theme.ui;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.LinearReferencingUtilities;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.SystemeReperageRepository;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.SystemeReperageBorne;
import fr.sirs.core.model.TronconDigue;
import static fr.sirs.theme.ui.FXPositionableMode.computeLinearFromGeo;
import fr.sirs.util.SirsStringConverter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import org.geotoolkit.gui.javafx.util.ComboBoxCompletion;
import org.geotoolkit.referencing.LinearReferencing;

/**
 * Edition des bornes d'un {@link Positionable}.
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXPositionableLinearMode extends BorderPane implements FXPositionableMode {

    private static final String MODE = "LINEAR";

    private final ObjectProperty<Positionable> posProperty = new SimpleObjectProperty<>();
    private final BooleanProperty disableProperty = new SimpleBooleanProperty(true);
    private LinearReferencing.SegmentInfo[] tronconSegments;

    @FXML private ComboBox<SystemeReperage> uiSRs;
    @FXML private ComboBox<BorneDigue> uiBorneStart;
    @FXML private ComboBox<BorneDigue> uiBorneEnd;
    @FXML private RadioButton uiAvalStart;
    @FXML private RadioButton uiAvalEnd;
    @FXML private RadioButton uiAmontStart;
    @FXML private RadioButton uiAmontEnd;
    @FXML private Spinner<Double> uiDistanceStart;
    @FXML private Spinner<Double> uiDistanceEnd;

    private boolean reseting = false;

    public FXPositionableLinearMode() {
        SIRS.loadFXML(this, Positionable.class);

        final SirsStringConverter sirsStringConverter = new SirsStringConverter();
        uiSRs.setConverter(sirsStringConverter);
        uiBorneStart.setConverter(sirsStringConverter);
        uiBorneStart.setEditable(true);
        uiBorneEnd.setConverter(sirsStringConverter);
        uiBorneEnd.setEditable(true);

        ComboBoxCompletion.autocomplete(uiBorneStart);
        ComboBoxCompletion.autocomplete(uiBorneEnd);

        uiSRs.disableProperty().bind(disableProperty);
        uiBorneStart.disableProperty().bind(disableProperty);
        uiBorneEnd.disableProperty().bind(disableProperty);
        uiAvalStart.disableProperty().bind(disableProperty);
        uiAmontStart.disableProperty().bind(disableProperty);
        uiAvalEnd.disableProperty().bind(disableProperty);
        uiAmontEnd.disableProperty().bind(disableProperty);
        uiDistanceStart.disableProperty().bind(disableProperty);
        uiDistanceEnd.disableProperty().bind(disableProperty);
        uiDistanceStart.setEditable(true);
        uiDistanceEnd.setEditable(true);

        final ToggleGroup groupStart = new ToggleGroup();
        uiAmontStart.setToggleGroup(groupStart);
        uiAvalStart.setToggleGroup(groupStart);
        uiAvalStart.setSelected(true);

        final ToggleGroup groupEnd = new ToggleGroup();
        uiAmontEnd.setToggleGroup(groupEnd);
        uiAvalEnd.setToggleGroup(groupEnd);
        uiAvalEnd.setSelected(true);
        
        uiDistanceStart.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE, 0,1));
        uiDistanceEnd.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE, 0,1));


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

        uiSRs.getSelectionModel().selectedItemProperty().addListener(this::srsChange);
        
        final ChangeListener chgListener = (ObservableValue observable, Object oldValue, Object newValue) -> coordChange();
        groupStart.selectedToggleProperty().addListener(chgListener);
        groupEnd.selectedToggleProperty().addListener(chgListener);
        uiBorneStart.valueProperty().addListener(chgListener);
        uiBorneEnd.valueProperty().addListener(chgListener);
        uiDistanceStart.valueProperty().addListener(chgListener);
        uiDistanceEnd.valueProperty().addListener(chgListener);


    }

    @Override
    public String getID() {
        return MODE;
    }

    @Override
    public String getTitle() {
        return "Borne";
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

    private void updateFields(){
        reseting = true;

        final Positionable pos = posProperty.get();
        final String mode = pos.getGeometryMode();

        final TronconDigue t = FXPositionableMode.getTronconFromPositionable(pos);
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
        uiSRs.setValue(defaultSR);

        if(mode == null || MODE.equals(mode)){
            //on assigne les valeurs sans changement
            uiAmontStart.setSelected(pos.getBorne_debut_aval());
            uiAvalStart.setSelected(!pos.getBorne_debut_aval());
            uiAmontEnd.setSelected(pos.getBorne_fin_aval());
            uiAvalEnd.setSelected(!pos.getBorne_fin_aval());

            uiDistanceStart.getValueFactory().setValue(pos.getBorne_debut_distance());
            uiDistanceEnd.getValueFactory().setValue(pos.getBorne_fin_distance());


            uiSRs.setItems(FXCollections.observableList(srs));
            uiSRs.getSelectionModel().select(defaultSR);

            // Init list of bornes
            final Map<String,BorneDigue> borneMap = new HashMap<>();
            final ObservableList<BorneDigue> bornes = FXCollections.observableArrayList();
            if (defaultSR != null) {
                final AbstractSIRSRepository<BorneDigue> borneRepo = Injector.getSession().getRepositoryForClass(BorneDigue.class);
                for(SystemeReperageBorne srb : defaultSR.systemeReperageBornes){
                    borneMap.put(srb.getBorneId(), borneRepo.get(srb.getBorneId()));
                }
                bornes.addAll(borneMap.values());
            }
            uiBorneStart.setItems(bornes);
            uiBorneEnd.setItems(bornes);

            uiBorneStart.valueProperty().set(borneMap.get(pos.borneDebutIdProperty().get()));
            uiBorneEnd.valueProperty().set(borneMap.get(pos.borneFinIdProperty().get()));

        }else if(pos.getGeometry()!=null){
            //on calcule les valeurs en fonction des points de debut et fin
            final TronconUtils.PosInfo ps = new TronconUtils.PosInfo(pos, t, Injector.getSession());
            final TronconUtils.PosSR rp = ps.getForSR(defaultSR);

            uiAvalStart.setSelected(!rp.startAval);
            uiAmontStart.setSelected(rp.startAval);
            uiDistanceStart.getValueFactory().setValue(rp.distanceStartBorne);
            uiBorneStart.getSelectionModel().select(rp.borneDigueStart);

            uiAvalEnd.setSelected(!rp.endAval);
            uiAmontEnd.setSelected(rp.endAval);
            uiDistanceEnd.getValueFactory().setValue(rp.distanceEndBorne);
            uiBorneEnd.getSelectionModel().select(rp.borneDigueEnd);

        }else{
            //pas de geometrie
            uiAvalStart.setSelected(true);
            uiAmontStart.setSelected(false);
            uiDistanceStart.getValueFactory().setValue(0.0);
            uiBorneStart.getSelectionModel().selectFirst();

            uiAvalEnd.setSelected(true);
            uiAmontEnd.setSelected(false);
            uiDistanceEnd.getValueFactory().setValue(0.0);
            uiBorneEnd.getSelectionModel().selectFirst();
        }

        reseting = false;
    }

    private void buildGeometry(){

        //sauvegarde des propriétés
        final Positionable positionable = posProperty.get();

        final SystemeReperage sr = uiSRs.getValue();
        final BorneDigue startBorne = uiBorneStart.getValue();
        final BorneDigue endBorne = uiBorneEnd.getValue();
        positionable.setSystemeRepId(sr==null ? null : sr.getDocumentId());
        positionable.setBorneDebutId(startBorne==null ? null : startBorne.getDocumentId());
        positionable.setBorneFinId(endBorne==null ? null : endBorne.getDocumentId());
        positionable.setBorne_debut_aval(uiAmontStart.isSelected());
        positionable.setBorne_fin_aval(uiAmontEnd.isSelected());
        positionable.setBorne_debut_distance(uiDistanceStart.getValue());
        positionable.setBorne_fin_distance(uiDistanceEnd.getValue());

        //on recalculate la geometrie
        final TronconDigue troncon = FXPositionableMode.getTronconFromPositionable(positionable);
        final AbstractSIRSRepository<BorneDigue> borneRepo = Injector.getSession().getRepositoryForClass(BorneDigue.class);
        final LineString geometry = LinearReferencingUtilities.buildGeometryFromBorne(troncon.getGeometry(), positionable, borneRepo);

        //sauvegarde de la geometrie
        positionable.geometryModeProperty().set(MODE);
        positionable.geometryProperty().set(geometry);
    }

    private void coordChange(){
        if(reseting) return;
        reseting = true;
        buildGeometry();
        reseting = false;
    }

    private void srsChange(ObservableValue<? extends SystemeReperage> observable,
            SystemeReperage oldValue, SystemeReperage newSR) {
        if(reseting) return;

        reseting = true;

        final Positionable positionable = posProperty.get();

        // Mise à jour de la liste des bornes
        final ArrayList<BorneDigue> bornes = new ArrayList<>();
        final AbstractSIRSRepository<BorneDigue> borneRepo = Injector.getSession().getRepositoryForClass(BorneDigue.class);
        BorneDigue defaultBorneStart = null;
        BorneDigue defaultBorneEnd = null;
        for (final SystemeReperageBorne srb : newSR.systemeReperageBornes) {
            final BorneDigue bd = borneRepo.get(srb.getBorneId());
            if (bd != null) {
                bornes.add(bd);
                if(bd.getId().equals(positionable.getBorneDebutId())){
                    defaultBorneStart = bd;
                }
                if(bd.getId().equals(positionable.getBorneFinId())){
                    defaultBorneEnd = bd;
                }
            }
        }
        
        uiBorneStart.setItems(FXCollections.observableList(bornes));
        uiBorneEnd.setItems(FXCollections.observableList(bornes));


        //calcul de la position relative dans le nouveau SR
        final Point ptStart = computeGeoFromLinear(uiDistanceStart.getValue(), uiBorneStart.getValue(), uiAvalStart.isSelected());
        final Point ptEnd   = computeGeoFromLinear(uiDistanceEnd.getValue(), uiBorneEnd.getValue(), uiAvalEnd.isSelected());
        final LinearReferencing.SegmentInfo[] segments = getSourceLinear(newSR);
        Map.Entry<BorneDigue, Double> relStart = computeLinearFromGeo(segments, newSR, ptStart);
        Map.Entry<BorneDigue, Double> relEnd = computeLinearFromGeo(segments, newSR, ptEnd);

        uiAvalStart.setSelected(relStart.getValue() < 0);
        uiDistanceStart.getValueFactory().setValue(StrictMath.abs(relStart.getValue()));
        uiBorneStart.getSelectionModel().select(relStart.getKey());

        uiAvalEnd.setSelected(relEnd.getValue() < 0);
        uiDistanceEnd.getValueFactory().setValue(StrictMath.abs(relEnd.getValue()));
        uiBorneEnd.getSelectionModel().select(relEnd.getKey());


        buildGeometry();
        reseting = false;
    }


    /**
     * Return the Linear geometry on which the input {@link SystemeReperage} is based on.
     * @param source The SR to get linear for. If null, we'll try to get tronçon
     * geometry of the currently edited {@link Positionable}.
     * @return The linear associated, or null if we cannot get it.
     */
    private LinearReferencing.SegmentInfo[] getSourceLinear(final SystemeReperage source) {
        if (tronconSegments == null) {
            final Positionable positionable = posProperty.get();
            final TronconDigue t = FXPositionableMode.getTronconFromPositionable(positionable);
            tronconSegments = LinearReferencingUtilities.getSourceLinear(t, source);
        }
        return tronconSegments;
    }

    /**
     * Compute current positionable point using linear referencing information
     * defined in the form. Returned point is expressed with Database CRS.
     *
     * @return The point computed from starting borne. If we cannot, we return null.
     */
    private Point computeGeoFromLinear(Number distance,
            BorneDigue borneProperty, boolean amont) {

        final Positionable positionable = posProperty.get();
        final TronconDigue t = FXPositionableMode.getTronconFromPositionable(positionable);

        if (distance != null && borneProperty != null && t != null) {
            //calcul à partir des bornes
            final Point bornePoint = borneProperty.getGeometry();
            double dist = distance.doubleValue();
            if (amont) {
                dist *= -1;
            }
            return LinearReferencingUtilities.computeCoordinate(t.getGeometry(), bornePoint, dist, 0);
        } else {
            return null;
        }
    }


}
