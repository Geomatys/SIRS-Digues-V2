
package fr.sirs.plugin.vegetation;

import fr.sirs.theme.ui.FXPlanVegetationPane;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.AbstractZoneVegetationRepository;
import fr.sirs.core.component.ParcelleVegetationRepository;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.PlanVegetation;
import fr.sirs.core.model.ZoneVegetation;
import fr.sirs.util.SirsStringConverter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;


/**
 *
 * @author Johann Sorel (Geomatys)
 * @author Samuel Andrés (Geomatys)
 */
public class FXParametragePane extends BorderPane {
    

    @FXML private SplitPane uisplit;
    @FXML private ListView<PlanVegetation> uiPlanList;
    @FXML private Button uiAdd;
    @FXML private Button uiDuplicate;
    @FXML private Button uiDelete;

    private final Session session = Injector.getSession();
    private final AbstractSIRSRepository<PlanVegetation> planRepo = session.getRepositoryForClass(PlanVegetation.class);
    private final ParcelleVegetationRepository parcelleRepo = (ParcelleVegetationRepository) session.getRepositoryForClass(ParcelleVegetation.class);
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
            } else {
                setText("");
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
        
        uiAdd.setOnAction(this::planAdd);
        uiAdd.setGraphic(new ImageView(SIRS.ICON_ADD_WHITE));
        
        uiDuplicate.disableProperty().bind(uiPlanList.getSelectionModel().selectedItemProperty().isNull());
        uiDuplicate.setOnAction(this::planDuplicate);
        uiDuplicate.setGraphic(new ImageView(SIRS.ICON_COPY_WHITE));
        
        uiDelete.setOnAction(this::planDelete);
        uiDelete.setGraphic(new ImageView(SIRS.ICON_TRASH_WHITE));
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
    
    void planDuplicate(ActionEvent event) {
        final PlanVegetation toDuplicate = uiPlanList.getSelectionModel().getSelectedItem();
        if(toDuplicate!=null){
        
            // Vérification des intentions de l'utilisateur.
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment dupliquer le plan "+converter.toString(toDuplicate)+" ?\n"
                    + "Cette opération dupliquera les parcelles de ce plan, leurs zones de végétation, ainsi que tous les paramétrages liés aux plans et aux parcelles.", ButtonType.YES, ButtonType.NO);
            alert.setResizable(true);
            
            final Optional<ButtonType> result = alert.showAndWait();
            if(result.isPresent() && result.get()==ButtonType.YES){
                
                // Duplication du plan.
                final PlanVegetation newPlan = toDuplicate.copy();
                planRepo.add(newPlan);
                
                
                // Récupération des parcelles de l'ancien plan.
                final List<ParcelleVegetation> oldParcelles = parcelleRepo.getByPlanId(toDuplicate.getId());
                
                final List<String> oldParcellesIds = new ArrayList<>(); 
                for(final ParcelleVegetation oldParcelle : oldParcelles) oldParcellesIds.add(oldParcelle.getId());
                
                // Duplication des parcelles.
                final Map<String, ParcelleVegetation> newParcelles = new HashMap<>();
                for(final ParcelleVegetation oldParcelle : oldParcelles){
                    final ParcelleVegetation newParcelle = oldParcelle.copy();
                    newParcelle.setPlanId(newPlan.getId());
                    newParcelles.put(oldParcelle.getId(), newParcelle);
                }
                
                // Enregistrement des parcelles (nécessaire pour les doter d'identifiants)
                parcelleRepo.executeBulk(newParcelles.values());
                    
                // Récupération des zones de végétation des parcelles de l'ancien plan, pour chaque sorte de zone.
                final Collection<AbstractSIRSRepository> zonesVegetationRepos = session.getRepositoriesForClass(ZoneVegetation.class);
                final List<ZoneVegetation> oldZonesVegetation = new ArrayList<>();
                for(final AbstractSIRSRepository zoneVegetationRepo : zonesVegetationRepos){
                    if(zoneVegetationRepo instanceof AbstractZoneVegetationRepository){
                        final AbstractZoneVegetationRepository zoneRepo = (AbstractZoneVegetationRepository) zoneVegetationRepo;
                        final List retrievedZones = zoneRepo.getByParcelleIds(oldParcellesIds);
                        if(retrievedZones!=null && !retrievedZones.isEmpty()) oldZonesVegetation.addAll(retrievedZones);

                    }
                }
                    
                
                final Map<Class<? extends ZoneVegetation>, List<ZoneVegetation>> zonesByClass = new HashMap<>();
                
                // Duplication des zones et affectation aux nouvelles parcelles (on a besoin de leur identifiant.
                for(final ZoneVegetation oldZone : oldZonesVegetation){
                    final ZoneVegetation newZone = oldZone.copy();
                    
                    final ParcelleVegetation newParcelle = newParcelles.get(oldZone.getParcelleId());
                    if(newParcelle!=null) newZone.setParcelleId(newParcelle.getId());
                    
                    if(zonesByClass.get(newZone.getClass())==null) zonesByClass.put(newZone.getClass(), new ArrayList<>());
                    
                    zonesByClass.get(newZone.getClass()).add(newZone);
                }
                
                // Enregistrement des zones de végétation
                for(final Class clazz : zonesByClass.keySet()){
                    session.getRepositoryForClass(clazz).executeBulk(zonesByClass.get(clazz));
                }

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
            
            // Vérification des intentions de l'utilisateur
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer le plan "+converter.toString(toDelete)+" ?\n"
                    + "Cette opération supprimera les parcelles de ce plan, leurs zones de végétation, ainsi que tous les paramétrages liés aux plans et aux parcelles.", ButtonType.YES, ButtonType.NO);
            alert.setResizable(true);
            final Optional<ButtonType> result = alert.showAndWait();
            
            if(result.isPresent() && result.get()==ButtonType.YES){
                
                // Récupération des parcelles à supprimer
                final List<ParcelleVegetation> parcellesToDelete = parcelleRepo.getByPlan(toDelete);
                
                if(parcellesToDelete!=null && !parcellesToDelete.isEmpty()){
                
                    final List<String> parcellesIdsToDelete = new ArrayList();
                    for(final ParcelleVegetation parcelle : parcellesToDelete) parcellesIdsToDelete.add(parcelle.getId());
                    
                    // Récupération des zones à supprimer et suppression
                    final Collection<AbstractSIRSRepository> zoneVegetationRepos = session.getRepositoriesForClass(ZoneVegetation.class);
                    
                    for(final AbstractSIRSRepository zoneVegetationRepo : zoneVegetationRepos){
                        if(zoneVegetationRepo instanceof AbstractZoneVegetationRepository){
                            final AbstractZoneVegetationRepository repo = (AbstractZoneVegetationRepository) zoneVegetationRepo;
                            final List<ZoneVegetation> zones = repo.getByParcelleIds(parcellesIdsToDelete);
                            repo.executeBulkDelete(zones);
                        }
                    }

                    // Suppression des parcelles
                    parcelleRepo.executeBulkDelete(parcellesToDelete);
                }
                
                // Suppression du plan
                planRepo.remove(toDelete);
                refreshPlanList();
            }
        }
    }

}
