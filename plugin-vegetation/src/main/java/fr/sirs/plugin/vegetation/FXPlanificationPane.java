package fr.sirs.plugin.vegetation;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.model.PlanVegetation;
import fr.sirs.core.model.TronconDigue;
import static fr.sirs.plugin.vegetation.FXPlanTable.Mode.PLANIFICATION;
import fr.sirs.util.SirsStringConverter;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;
import static javafx.scene.layout.Region.USE_PREF_SIZE;
import javafx.scene.layout.RowConstraints;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @author Samuel Andrés (Geomatys)
 */
public class FXPlanificationPane extends GridPane {

    private final BorderPane tablePane = new BorderPane();
    private final Session session = Injector.getSession();
    private final ChoiceBox<PlanVegetation> planChoiceBox = new ChoiceBox<>();
    private final ChoiceBox<TronconDigue> tronconChoiceBox = new ChoiceBox<>();
    
    public FXPlanificationPane() {
        SIRS.loadFXML(this, FXPlanificationPane.class);
        initialize();
    }

    private void initialize() {

        tablePane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        //plan de gestion actif
        planChoiceBox.setMaxWidth(400);
        planChoiceBox.setConverter(new SirsStringConverter());
        final List<PlanVegetation> allPlan = session.getRepositoryForClass(PlanVegetation.class).getAll();
        planChoiceBox.setItems(FXCollections.observableArrayList(allPlan));
        planChoiceBox.valueProperty().bindBidirectional(VegetationSession.INSTANCE.planProperty());
        final Label lblPlan = new Label("Plan de gestion actif : ");
        lblPlan.getStyleClass().add("label-header");
        final HBox planBox = new HBox(10, lblPlan, planChoiceBox);
        planBox.getStyleClass().add("blue-light");
        planBox.setPadding(new Insets(10));

        //troncon actif
        tronconChoiceBox.setMaxWidth(400);
        tronconChoiceBox.setConverter(new SirsStringConverter());
        final List<TronconDigue> allTroncon = session.getRepositoryForClass(TronconDigue.class).getAll();
        allTroncon.add(0, null);
        tronconChoiceBox.setItems(FXCollections.observableArrayList(allTroncon));
        final Label lblTroncon = new Label("Tronçon : ");
        lblTroncon.getStyleClass().add("label-header");
        final HBox tronconBox = new HBox(10, lblTroncon, tronconChoiceBox);
        tronconBox.getStyleClass().add("blue-light");
        tronconBox.setPadding(new Insets(10));

        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        setHgap(10);
        setVgap(10);
        setPadding(new Insets(10, 10, 10, 10));
        getColumnConstraints().add(new ColumnConstraints(USE_PREF_SIZE, USE_COMPUTED_SIZE, USE_PREF_SIZE, Priority.NEVER, HPos.LEFT, true));
        getColumnConstraints().add(new ColumnConstraints(USE_PREF_SIZE, USE_COMPUTED_SIZE, USE_PREF_SIZE, Priority.NEVER, HPos.LEFT, true));
        getColumnConstraints().add(new ColumnConstraints(USE_PREF_SIZE, USE_COMPUTED_SIZE, Double.MAX_VALUE, Priority.ALWAYS, HPos.LEFT, true));
        getRowConstraints().add(new RowConstraints(USE_PREF_SIZE, USE_COMPUTED_SIZE, USE_PREF_SIZE, Priority.NEVER, VPos.CENTER, true));
        getRowConstraints().add(new RowConstraints(USE_PREF_SIZE, USE_COMPUTED_SIZE, Double.MAX_VALUE, Priority.ALWAYS, VPos.CENTER, true));
        add(planBox, 0, 0);
        add(tronconBox, 1, 0);
        add(tablePane, 0, 1, 3, 1);

        if(planChoiceBox.getValue()!=null){
            tablePane.setCenter(new FXPlanTable(planChoiceBox.getValue(), tronconChoiceBox.getValue(), PLANIFICATION));
        }

        //on ecoute les changements de troncon et de plan
        final ChangeListener chgListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                tablePane.setCenter(new FXPlanTable(planChoiceBox.getValue(), tronconChoiceBox.getValue(), PLANIFICATION));
            }
        };

        planChoiceBox.valueProperty().addListener(chgListener);
        tronconChoiceBox.valueProperty().addListener(chgListener);
    }
}
