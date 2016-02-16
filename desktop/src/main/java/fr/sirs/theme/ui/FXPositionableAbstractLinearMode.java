
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
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
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
import javafx.util.StringConverter;
import org.geotoolkit.gui.javafx.util.ComboBoxCompletion;
import org.geotoolkit.gui.javafx.util.FXNumberCell;
import org.geotoolkit.referencing.LinearReferencing;

/**
 * Edition des bornes d'un {@link Positionable}.
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class FXPositionableAbstractLinearMode extends BorderPane implements FXPositionableMode {

    private final ObjectProperty<Positionable> posProperty = new SimpleObjectProperty<>();
    protected final BooleanProperty disableProperty = new SimpleBooleanProperty(true);
    protected LinearReferencing.SegmentInfo[] tronconSegments;

    @FXML protected ComboBox<SystemeReperage> uiSRs;
    @FXML protected ComboBox<BorneDigue> uiBorneStart;
    @FXML protected ComboBox<BorneDigue> uiBorneEnd;
    @FXML protected RadioButton uiAvalStart;
    @FXML protected RadioButton uiAvalEnd;
    @FXML protected RadioButton uiAmontStart;
    @FXML protected RadioButton uiAmontEnd;
    @FXML protected Spinner<Double> uiDistanceStart;
    @FXML protected Spinner<Double> uiDistanceEnd;

    private boolean reseting = false;
    public boolean isReseting(){return reseting;}
    public void setReseting(final boolean res){reseting = res;}

    public FXPositionableAbstractLinearMode() {
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

        final StringConverter conv= new ThreeDecimalsConverter();
        SpinnerValueFactory.DoubleSpinnerValueFactory valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE, 0,1);
        valueFactory.setConverter(conv);
        uiDistanceStart.setValueFactory(valueFactory);

        valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE, 0,1);
        valueFactory.setConverter(conv);
        uiDistanceEnd.setValueFactory(valueFactory);


        final ChangeListener<Geometry> geomListener = new ChangeListener<Geometry>() {
            @Override
            public void changed(ObservableValue<? extends Geometry> observable, Geometry oldValue, Geometry newValue) {
                if(reseting) return;
                if(newValue==null){
                    throw new IllegalArgumentException("New geometry is null");
                }
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
    public Node getFXNode() {
        return this;
    }

    @Override
    public String getTitle() {
        return "Borne";
    }

    @Override
    public ObjectProperty<Positionable> positionableProperty() {
        return posProperty;
    }

    @Override
    public BooleanProperty disablingProperty() {
        return disableProperty;
    }

    /**
     * Cette méthode ne doit s'occuper que de mettre à jour les champs et non
     * de la mise à jour de l'information géométrique du positionable.
     */
    @Override
    public void updateFields(){
        reseting = true;

        final Positionable pos = posProperty.get();
        final String mode = pos.getGeometryMode();

        final TronconDigue t = FXPositionableMode.getTronconFromPositionable(pos);
        final SystemeReperageRepository srRepo = (SystemeReperageRepository) Injector.getSession().getRepositoryForClass(SystemeReperage.class);
        final SystemeReperage defaultSR;
        if (pos.getSystemeRepId() != null) {
            defaultSR = srRepo.get(pos.getSystemeRepId());
        } else if (t.getSystemeRepDefautId() != null) {
            defaultSR = srRepo.get(t.getSystemeRepDefautId());
        } else {
            defaultSR = null;
        }
        uiSRs.setValue(defaultSR);

        /*
        Init list of bornes and SRs : must be done all the time to allow the user
        to change/choose the positionable SR and bornes among list elements.
        */
        final Map<String, BorneDigue> borneMap = initSRBorneLists(t, defaultSR);

        if(mode == null || getID().equals(mode)){
            //on assigne les valeurs sans changement
            uiAmontStart.setSelected(pos.getBorne_debut_aval());
            uiAvalStart.setSelected(!pos.getBorne_debut_aval());
            uiAmontEnd.setSelected(pos.getBorne_fin_aval());
            uiAvalEnd.setSelected(!pos.getBorne_fin_aval());

            uiDistanceStart.getValueFactory().setValue(pos.getBorne_debut_distance());
            uiDistanceEnd.getValueFactory().setValue(pos.getBorne_fin_distance());

            uiBorneStart.valueProperty().set(borneMap.get(pos.borneDebutIdProperty().get()));
            uiBorneEnd.valueProperty().set(borneMap.get(pos.borneFinIdProperty().get()));

        }else if(pos.getGeometry()!=null){
            //on calcule les valeurs en fonction des points de debut et fin
            final TronconUtils.PosInfo ps = new TronconUtils.PosInfo(pos, t);
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

    /**
     * Init SRs, borneStart and borneEnd UI lists.
     *
     * Return a map of BorneDigue accessible by their id.
     *
     * @param t
     * @param defaultSR
     * @return
     */
    protected Map<String,BorneDigue> initSRBorneLists(final TronconDigue t, final SystemeReperage defaultSR){
        final List<SystemeReperage> srs = ((SystemeReperageRepository) Injector.getSession().getRepositoryForClass(SystemeReperage.class)).getByLinear(t);
        uiSRs.setItems(SIRS.observableList(srs));
        uiSRs.getSelectionModel().select(defaultSR);

        // Init list of bornes
        final Map<String,BorneDigue> borneMap = new HashMap<>();
        ObservableList<BorneDigue> bornes = FXCollections.observableArrayList();
        if (defaultSR != null) {
            final AbstractSIRSRepository<BorneDigue> borneRepo = Injector.getSession().getRepositoryForClass(BorneDigue.class);
            for(SystemeReperageBorne srb : defaultSR.systemeReperageBornes){
                borneMap.put(srb.getBorneId(), borneRepo.get(srb.getBorneId()));
            }
            bornes.addAll(borneMap.values());
        }

        bornes = bornes.sorted(new BorneComparator());
        uiBorneStart.setItems(bornes);
        uiBorneEnd.setItems(bornes);
        return borneMap;
    }

    /**
     * Cette méthode ne doit s'occuper que de la mise à jour de l'information
     * géométrique du positionable et non de la mise à jour des champs.
     */
    @Override
    public void buildGeometry(){


        //sauvegarde des propriétés
        final Positionable positionable = posProperty.get();

        // On ne met la géométrie à jour depuis ce panneau que si on est dans son mode.
        if(!getID().equals(positionable.getGeometryMode())) return;

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
        positionable.geometryModeProperty().set(getID());
        positionable.geometryProperty().set(geometry);
        positionable.setPositionDebut(TronconUtils.getPointFromGeometry(positionable.getGeometry(), getSourceLinear(sr), Injector.getSession().getProjection(), false));
        positionable.setPositionFin(TronconUtils.getPointFromGeometry(positionable.getGeometry(), getSourceLinear(sr), Injector.getSession().getProjection(), true));
    }

    protected void coordChange(){
        if(reseting) return;
        reseting = true;
        buildGeometry();
        reseting = false;
    }

    protected void srsChange(ObservableValue<? extends SystemeReperage> observable,
            SystemeReperage oldValue, SystemeReperage newSR) {
        if(reseting) return;

        reseting = true;

        final Positionable positionable = posProperty.get();

        // Mise à jour de la liste des bornes
        final ArrayList<BorneDigue> bornes = new ArrayList<>();
        final AbstractSIRSRepository<BorneDigue> borneRepo = Injector.getSession().getRepositoryForClass(BorneDigue.class);
//        BorneDigue defaultBorneStart = null;
//        BorneDigue defaultBorneEnd = null;
        for (final SystemeReperageBorne srb : newSR.systemeReperageBornes) {
            final BorneDigue bd = borneRepo.get(srb.getBorneId());
            if (bd != null) {
                bornes.add(bd);
//                if(bd.getId().equals(positionable.getBorneDebutId())){
//                    defaultBorneStart = bd;
//                }
//                if(bd.getId().equals(positionable.getBorneFinId())){
//                    defaultBorneEnd = bd;
//                }
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
    protected LinearReferencing.SegmentInfo[] getSourceLinear(final SystemeReperage source) {
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
     * @param distance
     * @param borneProperty
     * @param amont
     * @return The point computed from starting borne. If we cannot, we return null.
     */
    protected Point computeGeoFromLinear(final Number distance,
            final BorneDigue borneProperty, final boolean amont) {

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

    /**
     * A converter displaying 3 decimals for numbers. Hack because of hard-coded
     * and unmodifiable decimal formats in both {@link Spinner} and {@link FXNumberCell}.
     *
     * Jira task : SYM-1133
     */
    private static class ThreeDecimalsConverter extends StringConverter<Double> {

        private final DecimalFormat df = new DecimalFormat("#.###");

        @Override
        public String toString(Double value) {
            // If the specified value is null, return a zero-length String
            if (value == null) {
                return "";
            }

            return df.format(value);
        }

        @Override
        public Double fromString(String value) {
            try {
                // If the specified value is null or zero-length, return null
                if (value == null) {
                    return null;
                }

                value = value.trim();

                if (value.length() < 1) {
                    return null;
                }

                // Perform the requested parsing
                return df.parse(value).doubleValue();
            } catch (ParseException ex) {
                try {
                    return Double.valueOf(value);
                } catch (NumberFormatException e1) {
                    ex.addSuppressed(e1);
                }
                throw new RuntimeException(ex);
            }
        }
    }

    private static class BorneComparator implements Comparator<BorneDigue> {

        @Override
        public int compare(BorneDigue o1, BorneDigue o2) {
            if (o1 == null) {
                return o2 == null? 0 : 1;
            }

            if (o1.getLibelle() == null) {
                if (o2.getLibelle() == null) {
                    if (o1.getDesignation() == null) {
                        if (o2.getDesignation() == null) {
                            return 0;
                        } else return 1;
                    } else if (o2.getDesignation() == null) {
                        return -1;
                    } else return o1.getDesignation().compareTo(o2.getDesignation());
                } else return 1;
            }

            return o1.getLibelle().compareTo(o2.getLibelle());
        }

    }
}
