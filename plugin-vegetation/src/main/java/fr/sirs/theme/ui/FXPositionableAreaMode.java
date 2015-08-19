package fr.sirs.theme.ui;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.LinearReferencingUtilities;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.SystemeReperageRepository;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.GeometryType;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.PositionableVegetation;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.SystemeReperageBorne;
import fr.sirs.core.model.TronconDigue;
import static fr.sirs.theme.ui.FXPositionableMode.computeLinearFromGeo;
import fr.sirs.util.SirsStringConverter;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.binding.BooleanBinding;
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
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.display2d.primitive.jts.JTSLineIterator;
import org.geotoolkit.display2d.style.j2d.PathWalker;
import org.geotoolkit.gui.javafx.util.ComboBoxCompletion;
import org.geotoolkit.referencing.LinearReferencing;

/**
 * Edition des bornes d'un {@link Positionable}.
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXPositionableAreaMode extends BorderPane implements FXPositionableMode {

    private static final String MODE = "AREA";

    private final ObjectProperty<PositionableVegetation> posProperty = new SimpleObjectProperty<>();
    private final BooleanProperty disableProperty = new SimpleBooleanProperty(true);
    private LinearReferencing.SegmentInfo[] tronconSegments;

    @FXML private ComboBox<SystemeReperage> uiSRs;
    @FXML private ComboBox<BorneDigue> uiBorneStart;
    @FXML private ComboBox<BorneDigue> uiBorneEnd;
    @FXML private RadioButton uiAmontStart;
    @FXML private RadioButton uiAmontEnd;
    @FXML private RadioButton uiAvalStart;
    @FXML private RadioButton uiAvalEnd;
    @FXML private Spinner<Double> uiDistanceStart;
    @FXML private Spinner<Double> uiDistanceEnd;
    //area
    @FXML private Spinner<Double> uiStartNear;
    @FXML private Spinner<Double> uiStartFar;
    @FXML private Spinner<Double> uiEndNear;
    @FXML private Spinner<Double> uiEndFar;

    //label a caché si c'est un ponctuel
    @FXML private Label lblFin;
    @FXML private Label lblStartFar;
    @FXML private Label lblEndNear;
    @FXML private Label lblEndFar;

    private final BooleanProperty pctProp = new SimpleBooleanProperty(false);
    private boolean reseting = false;

    public FXPositionableAreaMode() {
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
        uiAmontStart.disableProperty().bind(disableProperty);
        uiAvalStart.disableProperty().bind(disableProperty);
        uiAmontEnd.disableProperty().bind(disableProperty);
        uiAvalEnd.disableProperty().bind(disableProperty);
        uiDistanceStart.disableProperty().bind(disableProperty);
        uiDistanceEnd.disableProperty().bind(disableProperty);
        uiStartNear.disableProperty().bind(disableProperty);
        uiStartFar.disableProperty().bind(disableProperty);
        uiEndNear.disableProperty().bind(disableProperty);
        uiEndFar.disableProperty().bind(disableProperty);
        uiDistanceStart.setEditable(true);
        uiDistanceEnd.setEditable(true);
        uiStartNear.setEditable(true);
        uiStartFar.setEditable(true);
        uiEndNear.setEditable(true);
        uiEndFar.setEditable(true);

        final ToggleGroup groupStart = new ToggleGroup();
        uiAvalStart.setToggleGroup(groupStart);
        uiAmontStart.setToggleGroup(groupStart);
        uiAmontStart.setSelected(true);

        final ToggleGroup groupEnd = new ToggleGroup();
        uiAvalEnd.setToggleGroup(groupEnd);
        uiAmontEnd.setToggleGroup(groupEnd);
        uiAmontEnd.setSelected(true);
        
        uiDistanceStart.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE, 0,1));
        uiDistanceEnd.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE, 0,1));
        uiStartNear.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, 0,1));
        uiStartFar.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, 0,1));
        uiEndNear.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, 0,1));
        uiEndFar.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, 0,1));


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
        
        final ChangeListener chgListener = (ChangeListener) (ObservableValue observable, Object oldValue, Object newValue) -> coordChange();
        groupStart.selectedToggleProperty().addListener(chgListener);
        groupEnd.selectedToggleProperty().addListener(chgListener);
        uiBorneStart.valueProperty().addListener(chgListener);
        uiBorneEnd.valueProperty().addListener(chgListener);
        uiDistanceStart.valueProperty().addListener(chgListener);
        uiDistanceEnd.valueProperty().addListener(chgListener);
        uiStartNear.valueProperty().addListener(chgListener);
        uiStartFar.valueProperty().addListener(chgListener);
        uiEndNear.valueProperty().addListener(chgListener);
        uiEndFar.valueProperty().addListener(chgListener);

        //on cache certain elements quand c'est un ponctuel
        lblFin.visibleProperty().bind(pctProp);
        uiBorneEnd.visibleProperty().bind(pctProp);
        uiAmontEnd.visibleProperty().bind(pctProp);
        uiDistanceEnd.visibleProperty().bind(pctProp);
        uiAvalEnd.visibleProperty().bind(pctProp);
        uiEndNear.visibleProperty().bind(pctProp);
        uiEndFar.visibleProperty().bind(pctProp);
        lblStartFar.visibleProperty().bind(pctProp);
        lblEndNear.visibleProperty().bind(pctProp);
        lblEndFar.visibleProperty().bind(pctProp);

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
    public ObjectProperty positionableProperty() {
        return posProperty;
    }

    @Override
    public BooleanProperty disablingProperty() {
        return disableProperty;
    }

    private void updateFields(){
        reseting = true;

        final PositionableVegetation pos = posProperty.get();
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

        if(MODE.equals(mode)){
            //on assigne les valeurs sans changement
            uiAvalStart.setSelected(pos.getBorne_debut_aval());
            uiAmontStart.setSelected(!pos.getBorne_debut_aval());
            uiAvalEnd.setSelected(pos.getBorne_fin_aval());
            uiAmontEnd.setSelected(!pos.getBorne_fin_aval());

            uiDistanceStart.getValueFactory().setValue(pos.getBorne_debut_distance());
            uiDistanceEnd.getValueFactory().setValue(pos.getBorne_fin_distance());
            uiStartNear.getValueFactory().setValue(pos.getDistanceDebutMin());
            uiStartFar.getValueFactory().setValue(pos.getDistanceDebutMax());
            uiEndNear.getValueFactory().setValue(pos.getDistanceFinMin());
            uiEndFar.getValueFactory().setValue(pos.getDistanceFinMax());


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

        }else{
            //on calcule les valeurs en fonction des points de debut et fin
            final TronconUtils.PosInfo ps = new TronconUtils.PosInfo(pos, t, Injector.getSession());
            final TronconUtils.PosSR rp = ps.getForSR(defaultSR);

            uiAmontStart.setSelected(!rp.startAval);
            uiAvalStart.setSelected(rp.startAval);
            uiDistanceStart.getValueFactory().setValue(rp.distanceStartBorne);
            uiBorneStart.getSelectionModel().select(rp.borneDigueStart);

            uiAmontEnd.setSelected(!rp.endAval);
            uiAvalEnd.setSelected(rp.endAval);
            uiDistanceEnd.getValueFactory().setValue(rp.distanceEndBorne);
            uiBorneEnd.getSelectionModel().select(rp.borneDigueEnd);

            uiStartNear.getValueFactory().setValue(0.0);
            uiStartFar.getValueFactory().setValue(0.0);
            uiEndNear.getValueFactory().setValue(0.0);
            uiEndFar.getValueFactory().setValue(0.0);
        }

        //on cache certains champs si c'est un ponctuel
        pctProp.unbind();
        pctProp.bind(pos.geometryTypeProperty().isNotEqualTo(GeometryType.PONCTUAL));

        reseting = false;
    }

    private void buildGeometry(){

        //sauvegarde des propriétés
        final PositionableVegetation positionable = posProperty.get();

        final SystemeReperage sr = uiSRs.getValue();
        final BorneDigue startBorne = uiBorneStart.getValue();
        final BorneDigue endBorne = uiBorneEnd.getValue();
        positionable.setSystemeRepId(sr==null ? null : sr.getDocumentId());
        positionable.setBorneDebutId(startBorne==null ? null : startBorne.getDocumentId());
        positionable.setBorneFinId(endBorne==null ? null : endBorne.getDocumentId());
        positionable.setBorne_debut_aval(uiAvalStart.isSelected());
        positionable.setBorne_fin_aval(uiAvalEnd.isSelected());
        positionable.setBorne_debut_distance(uiDistanceStart.getValue());
        positionable.setBorne_fin_distance(uiDistanceEnd.getValue());
        positionable.setDistanceDebutMin(uiStartNear.getValue());
        positionable.setDistanceDebutMax(uiStartFar.getValue());
        positionable.setDistanceFinMin(uiEndNear.getValue());
        positionable.setDistanceFinMax(uiEndFar.getValue());

        //on recalculate la geometrie linear
        final TronconDigue troncon = FXPositionableMode.getTronconFromPositionable(positionable);
        final AbstractSIRSRepository<BorneDigue> borneRepo = Injector.getSession().getRepositoryForClass(BorneDigue.class);
        final LineString linear = LinearReferencingUtilities.buildGeometryFromBorne(troncon.getGeometry(), positionable, borneRepo);

        
        //on calcule le ratio on fonction de la rive et du coté
        final String typeRiveId = troncon.getTypeRiveId();
        final String typeCoteId = positionable.getTypeCoteId();
        double ratio = 1.0;
        if("RefRive:1".equals(typeRiveId)){
            //rive gauche
            ratio *= -1.0;
        }

        switch (typeCoteId==null ? "" : typeCoteId) {
            //coté eau
            case "RefCote:1": //riviere
            case "RefCote:3": //etang
            case "RefCote:4": //mer
                ratio *= 1.0;
                break;
            //coté terre
            case "RefCote:2": //terre
            case "RefCote:6": //crete
            case "RefCote:99"://indéfini
            default :
                //Terre, Crete
                ratio *= -1.0;
                break;
            case "RefCote:5": //2 coté
                ratio = 0.0;
                break;
        }


        //on extrude avec la distance
        Geometry geometry;
        if(ratio==0){
            //des 2 cotés
            ratio = 1;
            final Polygon left = extrude(linear,
                positionable.getDistanceDebutMin() * ratio,
                positionable.getDistanceDebutMax() * ratio,
                positionable.getDistanceFinMin() * ratio,
                positionable.getDistanceFinMax() * ratio);
            ratio = -1;
            final Polygon right = extrude(linear,
                positionable.getDistanceDebutMin() * ratio,
                positionable.getDistanceDebutMax() * ratio,
                positionable.getDistanceFinMin() * ratio,
                positionable.getDistanceFinMax() * ratio);
            geometry = GO2Utilities.JTS_FACTORY.createMultiPolygon(new Polygon[]{left,right});
            geometry.setSRID(linear.getSRID());
            geometry.setUserData(linear.getUserData());

        }else{
            //1 coté
            geometry = extrude(linear,
                positionable.getDistanceDebutMin() * ratio,
                positionable.getDistanceDebutMax() * ratio,
                positionable.getDistanceFinMin() * ratio,
                positionable.getDistanceFinMax() * ratio);
        }

        //si c'est un ponctuel on prend le centre
        if(GeometryType.PONCTUAL.equals(positionable.getGeometryType())){
            geometry = geometry.getCentroid();
            geometry.setSRID(linear.getSRID());
            geometry.setUserData(linear.getUserData());
        }


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
        final Point ptStart = computeGeoFromLinear(uiDistanceStart.getValue(), uiBorneStart.getValue(), uiAmontStart.isSelected());
        final Point ptEnd   = computeGeoFromLinear(uiDistanceEnd.getValue(), uiBorneEnd.getValue(), uiAmontEnd.isSelected());
        final LinearReferencing.SegmentInfo[] segments = getSourceLinear(newSR);
        Map.Entry<BorneDigue, Double> relStart = computeLinearFromGeo(segments, newSR, ptStart);
        Map.Entry<BorneDigue, Double> relEnd = computeLinearFromGeo(segments, newSR, ptEnd);

        uiAmontStart.setSelected(relStart.getValue() < 0);
        uiDistanceStart.getValueFactory().setValue(StrictMath.abs(relStart.getValue()));
        uiBorneStart.getSelectionModel().select(relStart.getKey());

        uiAmontEnd.setSelected(relEnd.getValue() < 0);
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


    /**
     * Utilisé dans le plugin vegetation.
     *
     */
    private static Polygon extrude(LineString linear, final double startNear,
            final double startFar, final double endNear, final double endFar) {

        final PathIterator ite = new JTSLineIterator(linear, null);
        final PathWalker walker = new PathWalker(ite);
        final Point2D.Double pt = new Point2D.Double();
        final double totalLength = linear.getLength();
        final Coordinate c0 = new Coordinate(0,0);
        final Coordinate c1 = new Coordinate(0,0);
        final List<Coordinate> coords = new ArrayList<>();

        double distance = 0;
        double distNear = startNear;
        double distFar = startFar;

        //premiers points
        walker.walk(0);
        walker.getPosition(pt);
        double angle = Math.PI/2 + walker.getRotation();
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        c0.x = pt.x + cos*distNear;
        c0.y = pt.y + sin*distNear;
        c1.x = pt.x + cos*distFar;
        c1.y = pt.y + sin*distFar;
        coords.add(0,new Coordinate(c0));
        coords.add(new Coordinate(c1));

        
        while(!walker.isFinished()){
            final float d = walker.getSegmentLengthRemaining();
            distance += d;
            walker.walk(d+0.0001f);
            walker.getPosition(pt);
            angle = Math.PI/2 + walker.getRotation();
            cos = Math.cos(angle);
            sin = Math.sin(angle);

            distNear = startNear + (endNear-startNear)*(distance/totalLength);
            distFar = startFar + (endFar-startFar)*(distance/totalLength);

            c0.x = pt.x + cos*distNear;
            c0.y = pt.y + sin*distNear;
            c1.x = pt.x + cos*distFar;
            c1.y = pt.y + sin*distFar;
            coords.add(0,new Coordinate(c0));
            coords.add(new Coordinate(c1));
        }

        //on ferme le polygon
        coords.add(new Coordinate(coords.get(0)));
        while(coords.size()<4){
            coords.add(new Coordinate(coords.get(0)));
        }


        final Polygon polygon = GO2Utilities.JTS_FACTORY.createPolygon(coords.toArray(new Coordinate[0]));
        polygon.setSRID(linear.getSRID());
        polygon.setUserData(linear.getUserData());
        return polygon;
    }


}
