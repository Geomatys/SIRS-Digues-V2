
package fr.sirs.theme.ui;

import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import fr.sirs.core.component.ProfilTraversRepository;
import fr.sirs.core.model.LeveeProfilTravers;
import fr.sirs.core.model.ProfilTravers;
import fr.sirs.core.model.RefOrigineProfilTravers;
import fr.sirs.core.model.RefSystemeReleveProfil;
import fr.sirs.core.model.RefTypeProfilTravers;
import java.time.LocalDateTime;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.util.StringConverter;
import org.geotoolkit.gui.javafx.util.FXDateField;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXLeveeProfilTraversPane extends AbstractFXElementPane<LeveeProfilTravers> {
    
    private LeveeProfilTravers leveProfilTravers;
    private final Session session;
    private final BooleanProperty disableFields = new SimpleBooleanProperty();
    
    private boolean initializing = false;
    
    @FXML VBox uiLeves;
    @FXML HTMLEditor uiComment;
    @FXML FXDateField uiDateMiseAJour;
    @FXML FXDateField uiDateLeve;
    
    @FXML private ChoiceBox<RefOrigineProfilTravers>  uiTypeOrigine;
    @FXML private ChoiceBox<RefTypeProfilTravers> uiTypeProfil;
    @FXML private ChoiceBox<RefSystemeReleveProfil> uiTypeSystemeReleve;
    @FXML private TextField uiReferencePapier;
    @FXML private TextField uiReferenceCalque;
    @FXML private TextField uiReferenceNumerique;
    
    private FXLeveeProfilTraversPane(){
        SIRS.loadFXML(this);
        session = Injector.getBean(Session.class);
    }
    
    public FXLeveeProfilTraversPane(final LeveeProfilTravers leveProfilTravers){
//            final Map<String, Object> resources){
        this();
        this.leveProfilTravers = leveProfilTravers;
//        this.resources = resources;
        initFields();
    }       
            
    private void initFields(){
        initializing = true;
        
        // Propriétés propres à la Crête
        uiDateLeve.valueProperty().bindBidirectional(leveProfilTravers.dateLeveeProperty());
        uiDateLeve.disableProperty().bind(disableFields);
        
        uiDateMiseAJour.valueProperty().bindBidirectional(leveProfilTravers.dateMajProperty());
        uiDateMiseAJour.setDisable(true);
        
        uiComment.setHtmlText(leveProfilTravers.getCommentaire());
        uiComment.disableProperty().bind(disableFields);
        
        
        // ORIGINES
        final ObservableList<RefOrigineProfilTravers> allOrigines = FXCollections.observableList(session.getRefOrigineProfilTraversRepository().getAll());
        RefOrigineProfilTravers origine = null;
        for(final RefOrigineProfilTravers ropt : allOrigines){
            if(ropt.getId().equals(leveProfilTravers.getOrigineProfil())){
                origine = ropt;
                break;
            }
        }
                
        this.uiTypeOrigine.setItems(allOrigines);
        final StringConverter<RefOrigineProfilTravers> originesConverter = new StringConverter<RefOrigineProfilTravers>() {
            @Override
            public String toString(RefOrigineProfilTravers or) {return or.getLibelle();}
            @Override
            public RefOrigineProfilTravers fromString(String string) {return null;}
        };
        
        uiTypeOrigine.setConverter(originesConverter);
        uiTypeOrigine.setValue(origine);
        uiTypeOrigine.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<RefOrigineProfilTravers>() {
            
            @Override
            public void changed(ObservableValue<? extends RefOrigineProfilTravers> observable, RefOrigineProfilTravers oldValue, RefOrigineProfilTravers newValue) {
                if(initializing) return;
                // Do not open dialog if the levee list is reset to the old value.
                if (!newValue.equals(oldValue)){
                    
                    final Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                            "Le changement d'origine est enregistré d'office.",
                            ButtonType.OK,ButtonType.CANCEL
                            );
                    final ButtonType res = alert.showAndWait().get();
                    if(res==ButtonType.OK){
                        leveProfilTravers.setOrigineProfil(newValue.getId());
                        preSave();
                    }
                }
            }
        });
        uiTypeOrigine.disableProperty().bind(disableFields);
        
        
        
        
        
        
        // TYPES DE PROFILS DEN ETRAVERS
        final ObservableList<RefTypeProfilTravers> allProfilTypes = FXCollections.observableList(session.getRefTypeProfilTraversRepository().getAll());
        RefTypeProfilTravers profilType = null;
        for(final RefTypeProfilTravers rtpt : allProfilTypes){
            if(rtpt.getId().equals(leveProfilTravers.getTypeProfilId())){
                profilType = rtpt;
                break;
            }
        }
                
        this.uiTypeProfil.setItems(allProfilTypes);
        final StringConverter<RefTypeProfilTravers> typesConverter = new StringConverter<RefTypeProfilTravers>() {
            @Override
            public String toString(RefTypeProfilTravers type) {return type.getLibelle();}
            @Override
            public RefTypeProfilTravers fromString(String string) {return null;}
        };
        
        uiTypeProfil.setConverter(typesConverter);
        uiTypeProfil.setValue(profilType);
        uiTypeProfil.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<RefTypeProfilTravers>() {
            
            @Override
            public void changed(ObservableValue<? extends RefTypeProfilTravers> observable, RefTypeProfilTravers oldValue, RefTypeProfilTravers newValue) {
                if(initializing) return;
                // Do not open dialog if the levee list is reset to the old value.
                if (!newValue.equals(oldValue)){
                    
                    final Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                            "Le changement de type est enregistré d'office.",
                            ButtonType.OK,ButtonType.CANCEL
                            );
                    final ButtonType res = alert.showAndWait().get();
                    if(res==ButtonType.OK){
                        leveProfilTravers.setTypeProfilId(newValue.getId());
                        preSave();
                    }
                }
            }
        });
//        uiTypeProfil.textProperty().bindBidirectional(leveProfilTravers.typeProfilIdProperty());
        uiTypeProfil.disableProperty().bind(disableFields);
        
        
        // TYPES DE SYSTEMES DE RELEVES
        final ObservableList<RefSystemeReleveProfil> allSystemesReleves = FXCollections.observableList(session.getRefSystemeReleveProfilRepository().getAll());
        RefSystemeReleveProfil systemeReleve = null;
        for(final RefSystemeReleveProfil rsrp : allSystemesReleves){
            if(rsrp.getId().equals(leveProfilTravers.getTypeSystemesReleveId())){
                systemeReleve = rsrp;
                break;
            }
        }
                
        this.uiTypeSystemeReleve.setItems(allSystemesReleves);
        final StringConverter<RefSystemeReleveProfil> systemesRelevesConverter = new StringConverter<RefSystemeReleveProfil>() {
            @Override
            public String toString(RefSystemeReleveProfil srp) {return srp.getLibelle();}
            @Override
            public RefSystemeReleveProfil fromString(String string) {return null;}
        };
        
        uiTypeSystemeReleve.setConverter(systemesRelevesConverter);
        uiTypeSystemeReleve.setValue(systemeReleve);
        uiTypeSystemeReleve.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<RefSystemeReleveProfil>() {
            
            @Override
            public void changed(ObservableValue<? extends RefSystemeReleveProfil> observable, RefSystemeReleveProfil oldValue, RefSystemeReleveProfil newValue) {
                if(initializing) return;
                // Do not open dialog if the levee list is reset to the old value.
                if (!newValue.equals(oldValue)){
                    
                    final Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                            "Le changement de système de relevé est enregistré d'office.",
                            ButtonType.OK,ButtonType.CANCEL
                            );
                    final ButtonType res = alert.showAndWait().get();
                    if(res==ButtonType.OK){
                        leveProfilTravers.setTypeSystemesReleveId(newValue.getId());
                        preSave();
                    }
                }
            }
        });
//        uiTypeSystemeReleve.textProperty().bindBidirectional(leveProfilTravers.typeSystemesReleveIdProperty());
        uiTypeSystemeReleve.disableProperty().bind(disableFields);
        
        
        uiReferenceCalque.textProperty().bindBidirectional(leveProfilTravers.reference_calqueProperty());
        uiReferenceCalque.disableProperty().bind(disableFields);
        uiReferenceNumerique.textProperty().bindBidirectional(leveProfilTravers.reference_numeriqueProperty());
        uiReferenceNumerique.disableProperty().bind(disableFields);
        uiReferencePapier.textProperty().bindBidirectional(leveProfilTravers.reference_papierProperty());
        uiReferencePapier.disableProperty().bind(disableFields);
        
//        leveProfilTravers.getProfilTraversEvenementHydraulique();
//        leveProfilTravers.getProfilTraversTroncon();
//        uiEpaisseur.valueProperty().bindBidirectional(crete.epaisseurProperty());
//        uiEpaisseur.disableProperty().bind(disableFields);
//        
//        uiCouches.numberTypeProperty().set(NumberField.NumberType.Integer);
//        uiCouches.minValueProperty().set(0);
//        uiCouches.valueProperty().bindBidirectional(crete.num_coucheProperty());
//        uiCouches.disableProperty().bind(disableFields);
        initializing = false;
    }

    @Override
    public void preSave() {
        this.leveProfilTravers.setDateMaj(LocalDateTime.now());
        leveProfilTravers.setCommentaire(uiComment.getHtmlText());
        final ProfilTravers profil = (ProfilTravers) leveProfilTravers.getParent();//resources.get("profilTravers");
        final Session session = Injector.getBean(Session.class);
        final ProfilTraversRepository repo = session.getProfilTraversRepository();
        repo.update(profil);
    }
}
