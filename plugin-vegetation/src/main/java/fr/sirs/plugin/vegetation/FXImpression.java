
package fr.sirs.plugin.vegetation;

import fr.sirs.SIRS;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.Preview;
import java.time.LocalDate;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.GridPane;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXImpression extends GridPane{

    @FXML private GridPane uiGrid;
    @FXML private ListView<Preview> uiTroncons;
    @FXML private ComboBox<LocalDate> uiDateStart;
    @FXML private ComboBox<LocalDate> uiDateEnd;
    @FXML private CheckBox uiAllTroncon;
    
    @FXML private CheckBox uiTraiteeNonPlanif;
    @FXML private CheckBox uiTraiteePlanif;
    @FXML private CheckBox uiNonTraiteeNonPlanif;
    @FXML private CheckBox uiNonTraiteePlanif;

    private final Spinner<Double> uiPRStart = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MIN_VALUE, Double.MAX_VALUE, 0));
    private final Spinner<Double> uiPREnd = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MIN_VALUE, Double.MAX_VALUE, 0));

    public FXImpression() {
        SIRS.loadFXML(this, Positionable.class);

        uiGrid.add(uiPRStart, 1, 3);
        uiGrid.add(uiPRStart, 3, 3);

        //on liste les troncons du plan actif.



    }

    @FXML
    void print(ActionEvent event) {

        

    }

}
