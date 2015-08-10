
package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.PlanVegetation;
import fr.sirs.core.model.ParamCoutTraitementVegetation;
import java.time.LocalDate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;


/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXPlanVegetationPane extends BorderPane {
    
    @FXML private TextField uiPlanName;
    @FXML private TextField uiDesignation;
    @FXML private Spinner uiPlanDebut;
    @FXML private Spinner uiPlanFin;
    @FXML private VBox uiVBox;
    
    private static class TraitementSummary {
        public String typeVegetationId;
        public String typeTraitementId;
        public String typeFrequenceId;
    }
    
    private TableView<TraitementSummary> uiFrequenceTable;
    
    private final PojoTable uiCoutTable;
    @FXML private Button uiSave;

    private final Session session = Injector.getSession();
    private final AbstractSIRSRepository<PlanVegetation> planRepo = session.getRepositoryForClass(PlanVegetation.class);
    private final PlanVegetation plan;

    public FXPlanVegetationPane(PlanVegetation plan) {
        SIRS.loadFXML(this, FXPlanVegetationPane.class);
        this.plan = plan;
        
        uiDesignation.textProperty().bindBidirectional(plan.designationProperty());
        uiPlanName.textProperty().bindBidirectional(plan.libelleProperty());
        uiPlanDebut.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, LocalDate.now().getYear()));
        uiPlanDebut.setEditable(true);
        uiPlanDebut.getValueFactory().valueProperty().bindBidirectional(plan.anneDebutProperty());
        uiPlanFin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, LocalDate.now().getYear()+10));
        uiPlanFin.setEditable(true);
        uiPlanFin.getValueFactory().valueProperty().bindBidirectional(plan.anneFinProperty());
        
        uiSave.setOnAction((ActionEvent event) -> {
            planRepo.update(FXPlanVegetationPane.this.plan);
        });
        
        uiCoutTable = new PojoTable(ParamCoutTraitementVegetation.class, "Coûts des traitements");
        uiCoutTable.setParentElement(plan);
        uiCoutTable.setTableItems(() -> (ObservableList) FXCollections.observableList(plan.paramCout));
        uiVBox.getChildren().add(uiCoutTable);
    }

    public void initialize() {

    }

    @FXML
    void coutAdd(ActionEvent event) {

    }

    @FXML
    void coutDelete(ActionEvent event) {

    }

    @FXML
    void traitementAdd(ActionEvent event) {

    }

    @FXML
    void traitementDelete(ActionEvent event) {

    }

}
