
package fr.sirs.plugin.vegetation;

import static com.sun.tools.internal.xjc.reader.Ring.add;
import fr.sirs.theme.ui.FXPlanVegetationPane;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.ParcelleVegetationRepository;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.PlanVegetation;
import fr.sirs.core.model.ZoneVegetation;
import fr.sirs.util.SirsStringConverter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.layout.BorderPane;


/**
 *
 * @author Johann Sorel (Geomatys)
 * @author Samuel Andrés (Geomatys)
 */
public class FXParametragePane extends BorderPane {
    

    @FXML private SplitPane uisplit;
    @FXML private ListView<PlanVegetation> uiPlanList;

    private final Session session = Injector.getSession();
    private final AbstractSIRSRepository<PlanVegetation> planRepo = session.getRepositoryForClass(PlanVegetation.class);
    private final AbstractSIRSRepository<ParcelleVegetation> parcelleRepo = session.getRepositoryForClass(ParcelleVegetation.class);
    private final SirsStringConverter converter = new SirsStringConverter();
    

    public FXParametragePane() {
        SIRS.loadFXML(this, FXParametragePane.class);
        initialize();
    }
    
    private class UpdatableListCell extends ListCell<PlanVegetation>{
        
        
        @Override
        protected void updateItem(final PlanVegetation item, boolean empty) {
            super.updateItem(item, empty);
            
            textProperty().unbind();
            if(item!=null){
                textProperty().bind(new ObjectBinding<String>() {

                    {
                        bind(item.libelleProperty(), item.designationProperty());
                    }

                    @Override
                    protected String computeValue() {
                        return converter.toString(item);
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
        planRepo.add(newPlan);
        refreshPlanList();
        uiPlanList.getSelectionModel().select(newPlan);
    }
    
    @FXML
    void planDuplicate(ActionEvent event) {
        final PlanVegetation toDuplicate = uiPlanList.getSelectionModel().getSelectedItem();
        if(toDuplicate!=null){
        
            final Alert alert = new Alert(Alert.AlertType.WARNING, "Voulez-vous vraiment dupliquer le plan "+converter.toString(toDuplicate)+" ?\n"
                    + "Cette opération dupliquera les parcelles de ce plan, leurs zones de végétation, ainsi que tous les paramétrages liés aux plans et aux parcelles.", ButtonType.YES, ButtonType.NO);
            alert.setResizable(true);
            final Optional<ButtonType> result = alert.showAndWait();
            
            
            if(result.isPresent() && result.get()==ButtonType.YES){
                
                
                
                // Duplication du plan.
                final PlanVegetation newPlan = toDuplicate.copy();
                planRepo.add(newPlan);
                
                
                // Récupération des parcelles de l'ancien plan.
//                final List<ParcelleVegetation> oldParcelles = parcelleRepo.queryView(ParcelleVegetationRepository.BY_PLAN_ID, toDuplicate.getId());
//                final List<ParcelleVegetation> newParcelles = new ArrayList<>();
//                
//                // Duplication des parcelles.
//                for(final ParcelleVegetation oldParcelle : oldParcelles){
//                    final ParcelleVegetation newParcelle = oldParcelle.copy();
//                    
//                    // Récupération des zones de végétation de la parcelle
//                    final List<ZoneVegetation> oldZones = 
//                    
//                    
//                    
//                    
//                    newParcelles.add(newParcelle);
//                }


                refreshPlanList();
                uiPlanList.getSelectionModel().select(newPlan);
            }
        }
        else {
            final Alert alert = new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner un plan à dupliquer.", ButtonType.CLOSE);
            alert.setResizable(true);
            alert.showAndWait();
        }
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
