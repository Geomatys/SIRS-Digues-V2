
package fr.sirs.map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.InjectorCore;
import fr.sirs.core.LinearReferencingUtilities;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.BorneDigueRepository;
import fr.sirs.core.component.SystemeReperageRepository;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.SystemeReperageBorne;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.util.SirsStringConverter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import static javafx.beans.binding.Bindings.*;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.referencing.LinearReferencing;
import org.geotoolkit.referencing.LinearReferencing.SegmentInfo;

/**
 * Outil de calcule de position.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class FXPRPane extends VBox {

    private final NumberFormat DF = new DecimalFormat("0.###");

    //source
    @FXML private GridPane uiGrid;
    @FXML private ComboBox<TronconDigue> uiSourceTroncon;
    @FXML private ComboBox<SystemeReperage> uiSourceSR;
    @FXML private ComboBox<SystemeReperageBorne> uiSourceBorne;
    @FXML private RadioButton uiChoosePR;
    @FXML private RadioButton uiChooseCoord;
    @FXML private RadioButton uiChooseBorne;
    @FXML private ToggleButton uiPickCoord;
    private final Spinner<Double> uiSourcePR = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE,0));
    private final Spinner<Double> uiSourceX = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE,0));
    private final Spinner<Double> uiSourceY = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE,0));
    private final Spinner<Double> uiSourceDist = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE,0));

    @FXML private Button uiCalculate;

    
    //target
    @FXML private ComboBox<SystemeReperage> uiTargetSR;
    @FXML private TextField uiTargetPR;
    @FXML private CheckBox uiTargetView;
    @FXML private TextField uiTargetBorneAmont;
    @FXML private TextField uiTagetBorneAmontDist;
    @FXML private TextField uiTargetBorneAval;
    @FXML private TextField uiTargetBorneAvalDist;
    @FXML private TextField uiTargetX;
    @FXML private TextField uiTargetY;

    private final ObjectProperty<Geometry> targetPoint = new SimpleObjectProperty<>();
    private final PointCalculatorHandler handler;

    public FXPRPane(PointCalculatorHandler handler) {
        SIRS.loadFXML(this, Positionable.class);

        this.handler = handler;
        uiGrid.add(uiSourcePR, 1, 4);
        uiGrid.add(uiSourceX, 1, 5);
        uiGrid.add(uiSourceY, 2, 5);
        uiGrid.add(uiSourceDist, 2, 6);

        uiSourcePR.setEditable(true);
        uiSourceX.setEditable(true);
        uiSourceY.setEditable(true);
        uiSourceDist.setEditable(true);
        uiSourcePR.setMaxWidth(Double.MAX_VALUE);
        uiSourceX.setMaxWidth(Double.MAX_VALUE);
        uiSourceY.setMaxWidth(Double.MAX_VALUE);
        uiSourceDist.setMaxWidth(Double.MAX_VALUE);

        uiSourceTroncon.setConverter(new SirsStringConverter());
        uiSourceSR.setConverter(new SirsStringConverter());
        uiSourceBorne.setConverter(new SirsStringConverter());
        uiTargetSR.setConverter(new SirsStringConverter());

        final ToggleGroup group = new ToggleGroup();
        uiChoosePR.setToggleGroup(group);
        uiChooseCoord.setToggleGroup(group);
        uiChooseBorne.setToggleGroup(group);

        uiSourcePR.disableProperty().bind(uiChoosePR.selectedProperty().not());
        uiSourceX.disableProperty().bind(uiChooseCoord.selectedProperty().not());
        uiSourceY.disableProperty().bind(uiChooseCoord.selectedProperty().not());
        uiPickCoord.disableProperty().bind(uiChooseCoord.selectedProperty().not());
        uiSourceBorne.disableProperty().bind(uiChooseBorne.selectedProperty().not());
        uiSourceDist.disableProperty().bind(uiChooseBorne.selectedProperty().not());

        final BooleanBinding canCalculate = and(
                        and(uiSourceTroncon.valueProperty().isNotNull(),
                            and(uiSourceSR.valueProperty().isNotNull(),
                                uiTargetSR.valueProperty().isNotNull())),
                        or(
                            or( uiChoosePR.selectedProperty(),
                                uiChooseCoord.selectedProperty()),
                            and(uiChooseBorne.selectedProperty(),
                                uiSourceBorne.valueProperty().isNotNull())
                            )
                        );
        uiCalculate.disableProperty().bind(canCalculate.not());

        uiChoosePR.setSelected(true);

        uiSourceTroncon.valueProperty().addListener(this::tronconChange);
        uiSourceSR.valueProperty().addListener(this::sourceSRChange);


        //on remplit la liste des troncons
        final AbstractSIRSRepository<TronconDigue> tronconRepo = Injector.getSession().getRepositoryForClass(TronconDigue.class);
        uiSourceTroncon.setItems(FXCollections.observableArrayList(tronconRepo.getAll()));
        uiSourceTroncon.getSelectionModel().selectFirst();

        //on met a jour la decoration si le point cible change
        targetPoint.addListener((ObservableValue<? extends Geometry> observable, Geometry oldValue, Geometry newValue) -> updateMap());
        uiTargetView.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> updateMap());

        
    }


    private void tronconChange(ObservableValue<? extends TronconDigue> observable, TronconDigue oldValue, TronconDigue newValue){
        if(newValue!=null){
            final Session session = Injector.getSession();
            final List<SystemeReperage> srs = ((SystemeReperageRepository) session.getRepositoryForClass(SystemeReperage.class)).getByLinear(newValue);
            uiSourceSR.setItems(FXCollections.observableArrayList(srs));
            uiTargetSR.setItems(FXCollections.observableArrayList(srs));
        }else{
            uiSourceSR.setItems(FXCollections.emptyObservableList());
            uiTargetSR.setItems(FXCollections.emptyObservableList());
        }

        uiSourceSR.getSelectionModel().selectFirst();
        uiTargetSR.getSelectionModel().selectFirst();
    }

    private void sourceSRChange(ObservableValue<? extends SystemeReperage> observable, SystemeReperage oldValue, SystemeReperage newValue){
        if(newValue!=null){
            final ObservableList bornes = FXCollections.observableList(newValue.getSystemeReperageBornes());
            uiSourceBorne.setItems(bornes);
        }else{
            uiSourceBorne.setItems(FXCollections.emptyObservableList());
        }
        uiSourceBorne.getSelectionModel().selectFirst();
    }

    private void updateMap(){
        handler.getDecoration().getGeometries().clear();
        if(uiTargetView.isSelected() && targetPoint.get()!=null){
            handler.getDecoration().getGeometries().add(targetPoint.get());
        }
    }

    @FXML
    void calculate(ActionEvent event) {
        final Session session = Injector.getSession();
        final BorneDigueRepository borneRepo = InjectorCore.getBean(BorneDigueRepository.class);

        //calcule de la position geographique dans le systeme source
        final Point pt;
        if(uiChoosePR.isSelected()){
            pt = TronconUtils.computeCoordinate(uiSourceSR.getValue(), uiSourcePR.getValue());
        }else if(uiChooseCoord.isSelected()){
            pt = GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(uiSourceX.getValue(), uiSourceY.getValue()));
            JTS.setCRS(pt, session.getProjection());
        }else if(uiChooseBorne.isSelected()){
            pt = TronconUtils.computeCoordinate(uiSourceSR.getValue(), uiSourceBorne.getValue(), uiSourceDist.getValue());
        }else{
            pt = GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(0, 0));
            JTS.setCRS(pt, session.getProjection());
        }
        targetPoint.set(pt);

        //calcule de la position dans le systeme cible
        final SegmentInfo[] segments = LinearReferencingUtilities.buildSegments(
                LinearReferencing.asLineString(uiSourceTroncon.getValue().getGeometry()));
        final LinearReferencing.ProjectedPoint pos = LinearReferencingUtilities.projectReference(segments, pt);

        uiTargetX.setText(DF.format(pos.projected.x));
        uiTargetY.setText(DF.format(pos.projected.y));

        //calcule du PR cible
        final float targetPR = TronconUtils.computePR(segments, uiTargetSR.getValue(), pt, borneRepo);
        uiTargetPR.setText(DF.format(targetPR));

        //calcule de la position par rapport aux bornes
        final StringConverter strCvt = new SirsStringConverter();
        final Map.Entry<Double, SystemeReperageBorne>[] nearest = TronconUtils.findNearest(segments, uiTargetSR.getValue(), pt, borneRepo);
        if(nearest[0]!=null){
            uiTargetBorneAmont.setText(strCvt.toString(nearest[0].getValue()));
            uiTagetBorneAmontDist.setText(DF.format(nearest[0].getKey()));
        }else{
            uiTargetBorneAmont.setText("");
            uiTagetBorneAmontDist.setText("");
        }
        if(nearest[1]!=null){
            uiTargetBorneAval.setText(strCvt.toString(nearest[1].getValue()));
            uiTargetBorneAvalDist.setText(DF.format(nearest[1].getKey()));
        }else{
            uiTargetBorneAval.setText("");
            uiTargetBorneAvalDist.setText("");
        }


    }

    @FXML
    void pickCoord(ActionEvent event) {

    }

    @FXML
    void pickTroncon(ActionEvent event) {

    }

}
