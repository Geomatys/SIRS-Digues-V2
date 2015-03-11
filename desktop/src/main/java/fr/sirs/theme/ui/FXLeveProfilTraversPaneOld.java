
package fr.sirs.theme.ui;

import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import fr.sirs.core.component.EvenementHydrauliqueRepository;
import fr.sirs.core.component.ProfilTraversRepository;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.DocumentTroncon;
import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.core.model.LeveProfilTravers;
import fr.sirs.core.model.PreviewLabel;
import fr.sirs.core.model.ProfilTravers;
import fr.sirs.core.model.ProfilTraversEvenementHydraulique;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.util.StringConverter;
import org.geotoolkit.gui.javafx.util.FXDateField;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXLeveProfilTraversPaneOld extends AbstractFXElementPane<LeveProfilTravers> {
    
    private LeveProfilTravers leveProfilTravers;
    private final Session session;
    private final BooleanProperty disableFields = new SimpleBooleanProperty();
    
    private boolean initializing = false;
    
    @FXML VBox uiLeves;
    @FXML HTMLEditor uiComment;
    @FXML FXDateField uiDateMiseAJour;
    @FXML FXDateField uiDateLeve;
    
    @FXML private ChoiceBox<PreviewLabel>  uiTypeOrigine;
    @FXML private ChoiceBox<RefTypeProfilTravers> uiTypeProfil;
    @FXML private ChoiceBox<PreviewLabel> uiTypeSystemeReleve;
    @FXML private TextField uiReferencePapier;
    @FXML private TextField uiReferenceCalque;
    @FXML private TextField uiReferenceNumerique;
    
    @FXML private ComboBox<PreviewLabel> uiEvenementHydro;
    @FXML private ComboBox<PreviewLabel> uiTroncon;
    @FXML private ComboBox<DocumentTroncon> uiDocumentTroncon;
    
    private TronconDigueRepository tronconDigueRepository;
    private EvenementHydrauliqueRepository evenementHydrauliqueRepository;
    
    
    private FXLeveProfilTraversPaneOld(){
        SIRS.loadFXML(this);
        session = Injector.getBean(Session.class);
    }
    
    public FXLeveProfilTraversPaneOld(final LeveProfilTravers leveProfilTravers){
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
        uiDateLeve.disableProperty().bind(disableFieldsProperty());
        
        uiDateMiseAJour.valueProperty().bindBidirectional(leveProfilTravers.dateMajProperty());
        uiDateMiseAJour.setDisable(true);
        
        uiComment.setHtmlText(leveProfilTravers.getCommentaire());
        uiComment.disableProperty().bind(disableFieldsProperty());
        
        
        // ORIGINES
        final ObservableList<PreviewLabel> allOrigines = FXCollections.observableList(session.getPreviewLabelRepository().getPreviewLabels(RefOrigineProfilTravers.class));
        PreviewLabel origine = null;
        for(final PreviewLabel ropt : allOrigines){
            if(ropt.getObjectId().equals(leveProfilTravers.getOriginesProfil())){
                origine = ropt;
                break;
            }
        }
                
        this.uiTypeOrigine.setItems(allOrigines);
        final StringConverter<PreviewLabel> originesConverter = new StringConverter<PreviewLabel>() {
            @Override
            public String toString(PreviewLabel or) {return or.getLabel();}
            @Override
            public PreviewLabel fromString(String string) {return null;}
        };
        
        uiTypeOrigine.setConverter(originesConverter);
        uiTypeOrigine.setValue(origine);
        uiTypeOrigine.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<PreviewLabel>() {
            
            @Override
            public void changed(ObservableValue<? extends PreviewLabel> observable, PreviewLabel oldValue, PreviewLabel newValue) {
                if(initializing) return;
                // Do not open dialog if the levee list is reset to the old value.
                if (!newValue.equals(oldValue)){
                    
                    final Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                            "Le changement d'origine est enregistré d'office.",
                            ButtonType.OK,ButtonType.CANCEL
                            );
                    final ButtonType res = alert.showAndWait().get();
                    if(res==ButtonType.OK){
                        leveProfilTravers.setOriginesProfil(newValue.getObjectId());
                        preSave();
                    }
                }
            }
        });
        uiTypeOrigine.disableProperty().bind(disableFieldsProperty());
        
        
        
        
        
        
        // TYPES DE PROFILS EN TRAVERS
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
        final ObservableList<PreviewLabel> allSystemesReleves = FXCollections.observableList(session.getPreviewLabelRepository().getPreviewLabels(RefSystemeReleveProfil.class));
        uiTypeSystemeReleve.setItems(allSystemesReleves);
        uiTypeSystemeReleve.setConverter(new PreviewLabelStringConverter());
        uiTypeSystemeReleve.setValue(findPreviewLabel(allSystemesReleves, leveProfilTravers.getTypeSystemesReleveId()));
        uiTypeSystemeReleve.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<PreviewLabel>() {
            
            @Override
            public void changed(ObservableValue<? extends PreviewLabel> observable, PreviewLabel oldValue, PreviewLabel newValue) {
                if(initializing) return;
                // Do not open dialog if the levee list is reset to the old value.
                if (!newValue.equals(oldValue)){
                    
                    final Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                            "Le changement de système de relevé est enregistré d'office.",
                            ButtonType.OK,ButtonType.CANCEL
                            );
                    final ButtonType res = alert.showAndWait().get();
                    if(res==ButtonType.OK){
                        leveProfilTravers.setTypeSystemesReleveId(newValue.getObjectId());
                        preSave();
                    }
                }
            }
        });
        uiTypeSystemeReleve.disableProperty().bind(disableFields);
        
        
        uiReferenceCalque.textProperty().bindBidirectional(leveProfilTravers.referenceCalqueProperty());
        uiReferenceCalque.disableProperty().bind(disableFields);
        uiReferenceNumerique.textProperty().bindBidirectional(leveProfilTravers.referenceNumeriqueProperty());
        uiReferenceNumerique.disableProperty().bind(disableFields);
        uiReferencePapier.textProperty().bindBidirectional(leveProfilTravers.referencePapierProperty());
        uiReferencePapier.disableProperty().bind(disableFields);
        
        
        
        //EVENEMENTS HYDRAULIQUES : il faut mettre une table view et non pas une combobox car il ne s'agit pas d'ID vers des événements hydrauliques directement, mais des
//        final ObservableList<PreviewLabel> allEvenementsHydro = FXCollections.observableArrayList(session.getPreviewLabelRepository().getPreviewLabels(EvenementHydraulique.class));
//        uiEvenementHydro.setItems(allEvenementsHydro);
//        uiEvenementHydro.setConverter(new PreviewLabelStringConverter());
//        uiEvenementHydro.setValue(findPreviewLabel(allEvenementsHydro, null));
//        uiEvenementHydro.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<PreviewLabel>() {
//            
//            @Override
//            public void changed(ObservableValue<? extends PreviewLabel> observable, PreviewLabel oldValue, PreviewLabel newValue) {
//                if(initializing) return;
//                // Do not open dialog if the levee list is reset to the old value.
//                if (!newValue.equals(oldValue)){
//                    
//                    final Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
//                            "Le changement de système de relevé est enregistré d'office.",
//                            ButtonType.OK,ButtonType.CANCEL
//                            );
//                    final ButtonType res = alert.showAndWait().get();
//                    if(res==ButtonType.OK){
//                        leveProfilTravers.setProfilTraversEvenementHydraulique(newValue.getObjectId());
//                        preSave();
//                        ProfilTraversEvenementHydraulique pe;
//                    }
//                }
//            }
//        });
        
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
    
    private PreviewLabel findPreviewLabel(final ObservableList<PreviewLabel> labels, final String id){
        for(final PreviewLabel label : labels){
            if(label.getObjectId().equals(id)){
                return label;
            }
        }
        return null;
    }
    
    private class PreviewLabelStringConverter extends StringConverter<PreviewLabel>{

        @Override
        public String toString(PreviewLabel object) {
            return object.getLabel();
        }

        @Override
        public PreviewLabel fromString(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
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
