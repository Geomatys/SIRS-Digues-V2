
package fr.sirs.plugin.vegetation;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.model.PlanVegetation;
import fr.sirs.util.SirsStringConverter;
import java.util.Collections;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXPlanDeGestionPane extends BorderPane {

    @FXML private Tab tabPlanification;
    @FXML private Tab tabParametrage;

    private final BorderPane tablePane = new BorderPane();

    public FXPlanDeGestionPane() {
        SIRS.loadFXML(this, FXParametragePane.class);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    }

    public void initialize() {
        tablePane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        final GridPane pane = new GridPane();
        pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        pane.setHgap(10);
        pane.setVgap(10);
        pane.setPadding(new Insets(10, 10, 10, 10));
        pane.getColumnConstraints().add(new ColumnConstraints(USE_PREF_SIZE, USE_COMPUTED_SIZE, USE_PREF_SIZE, Priority.NEVER, HPos.LEFT, true));
        pane.getColumnConstraints().add(new ColumnConstraints(USE_PREF_SIZE, USE_COMPUTED_SIZE, USE_PREF_SIZE, Priority.NEVER, HPos.LEFT, true));
        pane.getColumnConstraints().add(new ColumnConstraints(USE_PREF_SIZE, USE_COMPUTED_SIZE, Double.MAX_VALUE, Priority.ALWAYS, HPos.LEFT, true));
        pane.getRowConstraints().add(new RowConstraints(USE_PREF_SIZE, USE_COMPUTED_SIZE, USE_PREF_SIZE, Priority.NEVER, VPos.CENTER, true));
        pane.getRowConstraints().add(new RowConstraints(USE_PREF_SIZE, USE_COMPUTED_SIZE, Double.MAX_VALUE, Priority.ALWAYS, VPos.CENTER, true));
        pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        final ChoiceBox<PlanVegetation> cb = new ChoiceBox<>();
        cb.setConverter(new SirsStringConverter());

        final Session session = Injector.getSession();
        final List<PlanVegetation> all = session.getRepositoryForClass(PlanVegetation.class).getAll();
        cb.setItems(FXCollections.observableArrayList(all));

        pane.add(new Label("Plan"), 0, 0);
        pane.add(cb, 1, 0);
        pane.add(tablePane, 0, 1, 3, 1);

        tabPlanification.setContent(pane);
        tabParametrage.setContent(new FXParametragePane());

        cb.valueProperty().addListener(new ChangeListener<PlanVegetation>() {
            @Override
            public void changed(ObservableValue<? extends PlanVegetation> observable, PlanVegetation oldValue, PlanVegetation newValue) {
                tablePane.setCenter(new FXPlanTable(newValue, false));
            }
        });

    }


}
