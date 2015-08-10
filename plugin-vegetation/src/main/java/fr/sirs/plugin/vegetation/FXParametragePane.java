
package fr.sirs.plugin.vegetation;

import fr.sirs.theme.ui.FXPlanVegetationPane;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.PlanVegetation;
import fr.sirs.core.model.PlanifParcelleVegetation;
import fr.sirs.util.SirsStringConverter;
import java.util.List;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.layout.BorderPane;


/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXParametragePane extends BorderPane {
    

    @FXML private SplitPane uisplit;
    @FXML private ListView<PlanVegetation> uiPlanList;

    private final Session session = Injector.getSession();
    private final AbstractSIRSRepository<PlanVegetation> planRepo = session.getRepositoryForClass(PlanVegetation.class);
    private final AbstractSIRSRepository<ParcelleVegetation> parcelleRepo = session.getRepositoryForClass(ParcelleVegetation.class);
    

    public FXParametragePane() {
        SIRS.loadFXML(this, FXParametragePane.class);
        initialize();
    }
    
    private class UpdatableListCell extends ListCell<PlanVegetation>{
        
        private final SirsStringConverter cvt = new SirsStringConverter();
        
        @Override
        protected void updateItem(final PlanVegetation item, boolean empty) {
            super.updateItem(item, empty);
            
            textProperty().unbind();
            if(item!=null){
                textProperty().bind(new ObjectBinding<String>() {

                    {
                        bind(item.libelleProperty(),item.designationProperty());
                    }

                    @Override
                    protected String computeValue() {
                        return cvt.toString(item);
                    }
                });
            }
        }
    }
    

    private void initialize() {
        final BorderPane pane = new BorderPane();
        uisplit.getItems().add(pane);

        refreshPlanList();
        uiPlanList.setCellFactory(ComboBoxListCell.forListView(new SirsStringConverter()));
        
        uiPlanList.setCellFactory((ListView<PlanVegetation> param)-> new UpdatableListCell());
        
        uiPlanList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        uiPlanList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<PlanVegetation>() {
            @Override
            public void changed(ObservableValue<? extends PlanVegetation> observable, PlanVegetation oldValue, PlanVegetation newValue) {
                if(newValue!=null){
                    pane.setCenter(new FXPlanVegetationPane(newValue));
                }
            }
        });
    }

    private void setPlan(){

    }
    
    void refreshPlanList() {
        uiPlanList.setItems(FXCollections.observableList(planRepo.getAll()));
    }
    
    @FXML
    void planAdd(ActionEvent event) {
        final PlanVegetation newPlan = planRepo.create();
        final List<ParcelleVegetation> parcelles = parcelleRepo.getAll();
        
        for(final ParcelleVegetation parcelle : parcelles){
            final PlanifParcelleVegetation planif = session.getElementCreator().createElement(PlanifParcelleVegetation.class);
            planif.setParcelleId(parcelle.getId());
            newPlan.getPlanifParcelle().add(planif);
        }
        
        planRepo.add(newPlan);
        refreshPlanList();
        uiPlanList.getSelectionModel().select(newPlan);
    }

    @FXML
    void planDelete(ActionEvent event) {
        final PlanVegetation toDelete = uiPlanList.getSelectionModel().getSelectedItem();
        if(toDelete!=null){
            planRepo.remove(toDelete);
            refreshPlanList();
        }
    }

}
