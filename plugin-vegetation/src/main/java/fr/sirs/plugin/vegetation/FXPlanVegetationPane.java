
package fr.sirs.plugin.vegetation;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.PlanVegetation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;


/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXPlanVegetationPane extends BorderPane {
    
    @FXML private TextField uiPlanName;
    @FXML private TextField uiPlanFin;
    @FXML private TextField uiPlanDebut;
    @FXML private TableView<?> uiCoutTable;
    @FXML private TableView<?> uiTraitementTable;

    private final Session session;
    private final AbstractSIRSRepository<PlanVegetation> planRepo;
    private final PlanVegetation plan;

    public FXPlanVegetationPane(PlanVegetation plan) {
        session = Injector.getSession();
        planRepo = session.getRepositoryForClass(PlanVegetation.class);
        this.plan = plan;
        SIRS.loadFXML(this, FXPlanVegetationPane.class);
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
