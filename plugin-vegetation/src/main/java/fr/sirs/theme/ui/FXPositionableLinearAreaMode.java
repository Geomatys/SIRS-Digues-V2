package fr.sirs.theme.ui;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import fr.sirs.Injector;
import fr.sirs.core.LinearReferencingUtilities;
import static fr.sirs.core.LinearReferencingUtilities.buildSegmentFromBorne;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.component.SystemeReperageRepository;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.GeometryType;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.PositionableVegetation;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.core.model.ZoneVegetation;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.display2d.primitive.jts.JTSLineIterator;
import org.geotoolkit.displayt2d.style.j2d.DoublePathWalker;
import static org.geotoolkit.referencing.LinearReferencing.asLineString;

/**
 * Edition des bornes d'un {@link Positionable}.
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXPositionableLinearAreaMode extends FXPositionableAbstractLinearMode {

    private static final String MODE = "LINEAR_AREA";

    //area
    @FXML private Spinner<Double> uiStartNear;
    @FXML private Spinner<Double> uiStartFar;
    @FXML private Spinner<Double> uiEndNear;
    @FXML private Spinner<Double> uiEndFar;

    // Libellés à cacher si c'est un ponctuel
    @FXML private Label lblFin;
    @FXML private Label lblStartNear;
    @FXML private Label lblStartFar;
    @FXML private Label lblEndNear;
    @FXML private Label lblEndFar;

    private final String originalLblStartNear;

    private final BooleanProperty pctProp = new SimpleBooleanProperty(false);


    final ChangeListener<String> typeCoteChangeListener = (ObservableValue<? extends String> observable, String oldValue, String newValue) -> buildGeometry();

    public FXPositionableLinearAreaMode() {
        super();

        uiStartNear.disableProperty().bind(disableProperty);
        uiStartFar.disableProperty().bind(disableProperty);
        uiEndNear.disableProperty().bind(disableProperty);
        uiEndFar.disableProperty().bind(disableProperty);
        uiStartNear.setEditable(true);
        uiStartFar.setEditable(true);
        uiEndNear.setEditable(true);
        uiEndFar.setEditable(true);

        uiStartNear.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, 0,1));
        uiStartFar.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, 0,1));
        uiEndNear.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, 0,1));
        uiEndFar.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, 0,1));

        final ChangeListener chgListener = (ChangeListener) (ObservableValue observable, Object oldValue, Object newValue) -> coordChange();
        uiStartNear.valueProperty().addListener(chgListener);
        uiStartFar.valueProperty().addListener(chgListener);
        uiEndNear.valueProperty().addListener(chgListener);
        uiEndFar.valueProperty().addListener(chgListener);

        //on cache certain elements quand c'est un ponctuel
        lblFin.visibleProperty().bind(pctProp);
        uiBorneEnd.visibleProperty().bind(pctProp);
        uiAvalEnd.visibleProperty().bind(pctProp);
        uiDistanceEnd.visibleProperty().bind(pctProp);
        uiAmontEnd.visibleProperty().bind(pctProp);
        uiEndNear.visibleProperty().bind(pctProp);
        uiEndFar.visibleProperty().bind(pctProp);
        lblStartFar.visibleProperty().bind(pctProp);
        uiStartFar.visibleProperty().bind(pctProp);
        lblEndNear.visibleProperty().bind(pctProp);
        lblEndFar.visibleProperty().bind(pctProp);
        lblFin.managedProperty().bind(pctProp);
        uiBorneEnd.managedProperty().bind(pctProp);
        uiAvalEnd.managedProperty().bind(pctProp);
        uiDistanceEnd.managedProperty().bind(pctProp);
        uiAmontEnd.managedProperty().bind(pctProp);
        uiEndNear.managedProperty().bind(pctProp);
        uiEndFar.managedProperty().bind(pctProp);
        lblStartFar.managedProperty().bind(pctProp);
        uiStartFar.managedProperty().bind(pctProp);
        lblEndNear.managedProperty().bind(pctProp);
        lblEndFar.managedProperty().bind(pctProp);

        originalLblStartNear = lblStartNear.getText();
        lblStartNear.textProperty().bind(new StringBinding() {
            {bind(pctProp);}
            @Override
            protected String computeValue() {
                if(pctProp.get()){
                    return originalLblStartNear;
                }
                else return "Point unique";
            }
        });

        positionableProperty().addListener(new ChangeListener<Positionable>() {

            @Override
            public void changed(ObservableValue<? extends Positionable> observable, Positionable oldValue, Positionable newValue) {
                if(newValue instanceof ZoneVegetation){
                    ((ZoneVegetation) newValue).typeCoteIdProperty().addListener(typeCoteChangeListener);
                }
                if(oldValue instanceof ZoneVegetation){
                    ((ZoneVegetation) newValue).typeCoteIdProperty().removeListener(typeCoteChangeListener);
                }
            }
        });
    }

    @Override
    public String getID() {
        return MODE;
    }

    @Override
    public void updateFields(){
        setReseting(true);

        final PositionableVegetation pos = (PositionableVegetation) positionableProperty().get();
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

        /*
        Init list of bornes and SRs : must be done all the time to allow the user
        to change/choose the positionable SR and bornes among list elements.
        */
        final Map<String, BorneDigue> borneMap = initSRBorneLists(t, defaultSR);

        if(MODE.equals(mode)){
            //on assigne les valeurs sans changement
            uiAmontStart.setSelected(pos.getBorne_debut_aval());
            uiAvalStart.setSelected(!pos.getBorne_debut_aval());
            uiAmontEnd.setSelected(pos.getBorne_fin_aval());
            uiAvalEnd.setSelected(!pos.getBorne_fin_aval());

            uiDistanceStart.getValueFactory().setValue(pos.getBorne_debut_distance());
            uiDistanceEnd.getValueFactory().setValue(pos.getBorne_fin_distance());
            uiStartNear.getValueFactory().setValue(pos.getDistanceDebutMin());
            uiStartFar.getValueFactory().setValue(pos.getDistanceDebutMax());
            uiEndNear.getValueFactory().setValue(pos.getDistanceFinMin());
            uiEndFar.getValueFactory().setValue(pos.getDistanceFinMax());

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

            uiStartNear.getValueFactory().setValue(0.0);
            uiStartFar.getValueFactory().setValue(0.0);
            uiEndNear.getValueFactory().setValue(0.0);
            uiEndFar.getValueFactory().setValue(0.0);
        }else{
            uiAvalStart.setSelected(true);
            uiAmontStart.setSelected(false);
            uiDistanceStart.getValueFactory().setValue(0.0);
            uiBorneStart.getSelectionModel().selectFirst();

            uiAvalEnd.setSelected(true);
            uiAmontEnd.setSelected(false);
            uiDistanceEnd.getValueFactory().setValue(0.0);
            uiBorneEnd.getSelectionModel().selectFirst();

            uiStartNear.getValueFactory().setValue(0.0);
            uiStartFar.getValueFactory().setValue(0.0);
            uiEndNear.getValueFactory().setValue(0.0);
            uiEndFar.getValueFactory().setValue(0.0);
        }

        //on cache certains champs si c'est un ponctuel
        pctProp.unbind();
        pctProp.bind(pos.geometryTypeProperty().isNotEqualTo(GeometryType.PONCTUAL));

        setReseting(false);
    }

    @Override
    public void buildGeometry(){


        //sauvegarde des propriétés
        final ZoneVegetation positionable = (ZoneVegetation) positionableProperty().get();

        // On ne met la géométrie à jour depuis ce panneau que si on est dans son mode.
        if(!MODE.equals(positionable.getGeometryMode())) return;

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
        positionable.setDistanceDebutMin(uiStartNear.getValue());
        positionable.setDistanceDebutMax(uiStartFar.getValue());
        positionable.setDistanceFinMin(uiEndNear.getValue());
        positionable.setDistanceFinMax(uiEndFar.getValue());

        //on recalculate la geometrie linear
        final TronconDigue troncon = FXPositionableMode.getTronconFromPositionable(positionable);


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
        final LineString linear;

        if(GeometryType.PONCTUAL.equals(positionable.getGeometryType())){
            /*
            Pour un point, il faut récupérer à partir de la géométrie du tronçon
            le segment sur lequel se trouve le point, car pour mesurer la
            direction du décalage perpendiculaire au tronçon, un point seul ne
            suffit pas.
            */
            final Entry<LineString, Double> pointAndSegment = buildSegmentFromBorne(asLineString(troncon.getGeometry()),
                    positionable.getBorneDebutId(),
                    positionable.getBorne_debut_aval(),
                    positionable.getBorne_debut_distance(),
                    Injector.getSession().getRepositoryForClass(BorneDigue.class));
            linear = pointAndSegment.getKey();
            if(ratio==0.) ratio=1.;// On ne met pas un arbre des deux côtés.
            geometry = toPoint(linear,
                positionable.getDistanceDebutMin() * ratio,
                pointAndSegment.getValue());
        }
        else {
            /*
            Si on n'a pas à faire à un ponctuel, on peut utiliser la géométrie
            de la structure plutôt que celle du tronçon.
            */
            linear = LinearReferencingUtilities.buildGeometryFromBorne(troncon.getGeometry(), positionable, Injector.getSession().getRepositoryForClass(BorneDigue.class));
            if(ratio==0){
                //des 2 cotés
                ratio = 1;
                final Polygon left = toPolygon(linear,
                    positionable.getDistanceDebutMin() * ratio,
                    positionable.getDistanceDebutMax() * ratio,
                    positionable.getDistanceFinMin() * ratio,
                    positionable.getDistanceFinMax() * ratio);
                ratio = -1;
                final Polygon right = toPolygon(linear,
                    positionable.getDistanceDebutMin() * ratio,
                    positionable.getDistanceDebutMax() * ratio,
                    positionable.getDistanceFinMin() * ratio,
                    positionable.getDistanceFinMax() * ratio);
                geometry = GO2Utilities.JTS_FACTORY.createMultiPolygon(new Polygon[]{left,right});
                geometry.setSRID(linear.getSRID());
                geometry.setUserData(linear.getUserData());

            }else{
                //1 coté
                geometry = toPolygon(linear,
                    positionable.getDistanceDebutMin() * ratio,
                    positionable.getDistanceDebutMax() * ratio,
                    positionable.getDistanceFinMin() * ratio,
                    positionable.getDistanceFinMax() * ratio);
            }
        }


        //sauvegarde de la geometrie
        positionable.geometryModeProperty().set(MODE);
        positionable.geometryProperty().set(geometry);
        positionable.setPositionDebut(TronconUtils.getPointFromGeometry(positionable.getGeometry(), getSourceLinear(sr), Injector.getSession().getProjection(), false));
        positionable.setPositionFin(TronconUtils.getPointFromGeometry(positionable.getGeometry(), getSourceLinear(sr), Injector.getSession().getProjection(), true));
    }

    /**
     * Builds an area from a linear.
     *
     *        ||
     *        ||
     *        ||
     *        ||----------->   --------------(startFar)
     *        ||---->          --------------(startNear)
     *        ||     *******
     *        ||     *\\\\\*
     *        ||     *\\\\\*
     *        \\    *\\\\\\\*
     *         \\    *\\\\\\\*
     *          \\    *\\\\\\\*     (SURFACE)
     *           \\   *\\\\\\\\\*
     *            ||   *\\\\\\\\\*
     *            ||   *\\\\\\\\\*
     *            ||  *\\\\\\\\\\\*
     *            ||  *\\\\\\\\\\\*
     *            ||  *************
     *            ||->                  --------(endNear)
     *            ||-------------->     --------(endFar)
     *            ||
     *            ||
     *
     *
     *
     *
     *
     * Utilisé dans le plugin vegetation.
     *
     */
    private static Polygon toPolygon(LineString linear, final double startNear,
            final double startFar, final double endNear, final double endFar) {

        final PathIterator ite = new JTSLineIterator(linear, null);
        final DoublePathWalker walker = new DoublePathWalker(ite);
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
            final double d = walker.getSegmentLengthRemaining();
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

        //on ferme le polygone
        coords.add(new Coordinate(coords.get(0)));
        while(coords.size()<4){
            coords.add(new Coordinate(coords.get(0)));
        }

        final Polygon polygon = GO2Utilities.JTS_FACTORY.createPolygon(coords.toArray(new Coordinate[0]));
        polygon.setSRID(linear.getSRID());
        polygon.setUserData(linear.getUserData());
        return polygon;
    }

    /**
     * Calcule les coordonnées d'un point à partir d'un segment de référence,
     * d'une distance au point de départ du segment et d'une distance au segment
     * lui-même.
     *
     *
     *                         X Point computed
     *                         |
     *                         | perpendicular distance
     *        linear distance  |
     *     x---------------------------------------------x
     *     start             linear (segment)           end
     *
     * @param segment Te reference segment
     * @param perpendicularDistance
     * @param linearDistance
     * @return
     */
    private static Point toPoint(final LineString segment, final double perpendicularDistance, final double linearDistance) {

        final double segmentLength = segment.getLength();
        /*
        La suite calcule les coordonnées du point définitif à la distance
        "perpendicularDistance" du segment.
         */
        final PathIterator ite = new JTSLineIterator(segment, null);
        final DoublePathWalker walker = new DoublePathWalker(ite);
        final Point2D.Double pt = new Point2D.Double();
        final Coordinate coord = new Coordinate(0,0);

        /*
        On va jusqu'au point intermédiaire, on récupère la position et la
        direction perpendiculaire.
        */
//        final float d = walker.getSegmentLengthRemaining();
        final double distanceToWalk;
        // On sécurise la distance à parcourir pour que celle-ci soit bien incluse dans le segment.
        if(linearDistance>segmentLength) distanceToWalk=segmentLength;
        else if(linearDistance<0.) distanceToWalk=0.;
        else distanceToWalk = linearDistance;

        walker.walk(distanceToWalk);
        walker.getPosition(pt);
        double angle = Math.PI/2 + walker.getRotation();

        coord.x = pt.x + Math.cos(angle)*perpendicularDistance;
        coord.y = pt.y + Math.sin(angle)*perpendicularDistance;

        /**
         * Les quatre points retournés par "toPolygon()" sont identiques. On prend le premier.
         */
        final Point polygon = GO2Utilities.JTS_FACTORY.createPoint(coord);
        polygon.setSRID(segment.getSRID());
        polygon.setUserData(segment.getUserData());
        return polygon;
    }
}
