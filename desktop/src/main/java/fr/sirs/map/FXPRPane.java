
package fr.sirs.map;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.SystemeEndiguement;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.util.SirsStringConverter;
import static javafx.beans.binding.Bindings.*;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
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

/**
 * Outil de calcule de position.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class FXPRPane extends VBox {


    //source
    @FXML private GridPane uiGrid;
    @FXML private ComboBox<TronconDigue> uiSourceTroncon;
    @FXML private ComboBox<SystemeEndiguement> uiSourceSR;
    @FXML private ComboBox<BorneDigue> uiSourceBorne;
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
    @FXML private ComboBox<?> uiTargetSR;
    @FXML private TextField uiTargetPR;
    @FXML private CheckBox uiTargetView;
    @FXML private TextField uiTargetBorneAmont;
    @FXML private TextField uiTagetBorneAmontDist;
    @FXML private TextField uiTargetBorneAval;
    @FXML private TextField uiTargetBorneAvalDist;
    @FXML private TextField uiTargetX;
    @FXML private TextField uiTargetY;

    public FXPRPane() {
        SIRS.loadFXML(this, Positionable.class);

        uiGrid.add(uiSourcePR, 1, 4);
        uiGrid.add(uiSourceX, 1, 5);
        uiGrid.add(uiSourceY, 2, 5);
        uiGrid.add(uiSourceDist, 2, 6);

        uiSourceTroncon.setConverter(new SirsStringConverter());
        uiSourceSR.setConverter(new SirsStringConverter());
        uiSourceBorne.setConverter(new SirsStringConverter());

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
                            uiSourceSR.valueProperty().isNotNull()),
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


        //on remplit la liste des troncons
        final AbstractSIRSRepository<TronconDigue> tronconRepo = Injector.getSession().getRepositoryForClass(TronconDigue.class);
        uiSourceTroncon.setItems(FXCollections.observableArrayList(tronconRepo.getAll()));
        uiSourceTroncon.getSelectionModel().selectFirst();

    }

    private void tronconChange(ObservableValue<? extends TronconDigue> observable, TronconDigue oldValue, TronconDigue newValue){
        if(newValue!=null){
            
        }else{
            
        }
    }

    @FXML
    void calculate(ActionEvent event) {
    }

    @FXML
    void pickCoord(ActionEvent event) {

    }

    @FXML
    void pickTroncon(ActionEvent event) {

    }

}
