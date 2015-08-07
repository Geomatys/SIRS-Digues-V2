
package fr.sirs.plugin.vegetation;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.PlanVegetation;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;


/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXParametragePane extends BorderPane {
    
    @FXML private ListView<PlanVegetation> uiPlanList;

    @FXML
    private SplitPane uisplit;

    private final Session session;
    private final AbstractSIRSRepository<PlanVegetation> planRepo;

    public FXParametragePane() {
        session = Injector.getSession();
        planRepo = session.getRepositoryForClass(PlanVegetation.class);
        SIRS.loadFXML(this, FXParametragePane.class);
    }

    public void initialize() {
        final BorderPane pane = new BorderPane();
        uisplit.getItems().add(pane);

        uiPlanList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        uiPlanList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<PlanVegetation>() {
            @Override
            public void changed(ObservableValue<? extends PlanVegetation> observable, PlanVegetation oldValue, PlanVegetation newValue) {
                if(newValue!=null){
                    pane.setCenter(new FXPlanVegetationPane(newValue));
                }else{
                    pane.setCenter(null);
                }
            }
        });
    }

    private void setPlan(){

    }
    
    @FXML
    void planAdd(ActionEvent event) {

    }

    @FXML
    void planDelete(ActionEvent event) {

    }

}
