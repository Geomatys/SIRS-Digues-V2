

package fr.sym.digue;

import fr.sym.Session;
import fr.sym.Symadrem;
import fr.symadrem.sirs.core.model.Digue;
import fr.symadrem.sirs.core.model.TronconDigue;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.geotoolkit.gui.javafx.util.FXDateField;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class TronconDigueController extends BorderPane{
    
    private final ObjectProperty<TronconDigue> tronconProperty = new SimpleObjectProperty<>();
    
    @Autowired
    private Session session;
    
    @FXML private Label uiId;
    @FXML private TextField uiName;
    @FXML private HTMLEditor uiComment;
    @FXML private ChoiceBox<Digue> uiDigue;
    @FXML private ChoiceBox<Digue> uiSrDefault;
    @FXML private ChoiceBox<String> uiRive;
    @FXML private FXDateField uiDateStart;
    @FXML private FXDateField uiDateEnd;
    @FXML private ToggleButton uiConsult;
    @FXML private ToggleButton uiEdit;
    @FXML private Button uiSave;

    //flag afin de ne pas faire de traitement lors de l'initialisation
    private boolean initializing = false;
    
    public TronconDigueController() {
        Symadrem.loadFXML(this);
        Injector.injectDependencies(this);
        
        //mode edition
        final BooleanBinding editBind = uiEdit.selectedProperty().not();
        uiSave.disableProperty().bind(editBind);
        uiName.disableProperty().bind(editBind);
        uiDigue.disableProperty().bind(editBind);
        uiSrDefault.disableProperty().bind(editBind);
        uiRive.disableProperty().bind(editBind);
        uiDateStart.disableProperty().bind(editBind);
        uiDateEnd.disableProperty().bind(editBind);
        uiComment.disableProperty().bind(editBind);
        
        tronconProperty.addListener((ObservableValue<? extends TronconDigue> observable, TronconDigue oldValue, TronconDigue newValue) -> {
            initFields();
        });
        
        final ToggleGroup group = new ToggleGroup();
        uiConsult.setToggleGroup(group);
        uiEdit.setToggleGroup(group);
        group.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
            if(newValue==null) group.selectToggle(uiConsult);
        });
        
    }
    
    public ObjectProperty<TronconDigue> tronconProperty(){
        return tronconProperty;
    }
    
    public TronconDigue getTroncon(){
        return tronconProperty.get();
    }
    
    public void setTroncon(TronconDigue troncon){
        this.tronconProperty.set(troncon);
    }
        
    @FXML
    private void save(final ActionEvent event){
        tronconProperty.get().setCommentaire(uiComment.getHtmlText());
        this.session.update(getTroncon());
    }
    
    private void initFields(){
        initializing = true;
        
        final TronconDigue troncon = tronconProperty.get();
        final ObservableList<Digue> allDigues = FXCollections.observableList(session.getDigueRepository().getAll());
        final Digue digue = session.getDigueById(troncon.getDigueId());
        
        this.uiId.setText(troncon.getId());
        this.uiName.textProperty().bindBidirectional(troncon.nomProperty());
        this.uiComment.setHtmlText(troncon.getCommentaire());
                
        this.uiDigue.setItems(allDigues);
        final StringConverter<Digue> digueStringConverter = new StringConverter<Digue>() {
            @Override
            public String toString(Digue digue) {return digue.getLibelle();}
            @Override
            public Digue fromString(String string) {return null;}
        };
        
        this.uiDigue.setConverter(digueStringConverter);
        this.uiDigue.setValue(digue);
        this.uiDigue.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Digue>() {

            @Override
            public void changed(ObservableValue<? extends Digue> observable, Digue oldValue, Digue newValue) {
                if(initializing) return;
                /* TODO ?
                Le changement de digue d'un tronçon a des implication immédiates
                sur la mise à jour de l'arbre de navigation des entités. Il 
                entraîne a priori le changement d'élément de l'arbre en cours de
                sélection et donc le chargement de la vue d'un nouvel élément 
                avant d'avoir eu la possibilité d'enregistrer les changements
                apportés au modèle manuellement dans la base.
                Il y a donc deux possibilités :
                1- Ouvrir ici une fenêtre d'avertissement expliquant que cette 
                modification est sauvegardée d'office car elle provoque un 
                changement de vue de digue. (choix pour le moment).
                2- Rester sur la vue du bon tronçon en sélectionnant le bon 
                élément de l'arbre et en contournant le rechargement de la page
                (plus compliqué mais plus léger pour l'utilisateur).
                */
                
                // Do not open dialog if the levee list is reset to the old value.
                if (!newValue.equals(digue)){
                    
                    final Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                            "Le changement de digue est enregistré d'office.",
                            ButtonType.OK,ButtonType.CANCEL
                            );
                    final ButtonType res = alert.showAndWait().get();
                    if(res==ButtonType.OK){
//                        getDigueItems().stream().forEach((item) -> {
//                            if(((Digue) item.getValue()).equals(oldValue)){
//                                item.getChildren().remove(troncon);
//                            }
//                        });
//
//                        getDigueItems().stream().forEach((item) -> {
//                            if(((Digue) item.getValue()).equals(newValue)){
//                                item.getChildren().add(troncon);
//                            }
//                        });
                        tronconProperty.get().setDigueId(newValue.getId());
                        save(null);
                    }
                }
            }
        });
        this.uiDigue.getValue().getId();
//        
//        this.typeRiveChoiceBox.setItems(FXCollections.observableArrayList(TypeRive.getTypes()));
//        final StringConverter<String> typesRivesStringConverter = new StringConverter<TypeRive>() {
//        
//            @Override
//            public String toString(TypeRive type) {return type.toString();}
//
//            // TODO ?
//            @Override
//            public TypeRive fromString(String string) {return null;}
//        };
//        
//        this.typeRiveChoiceBox.setConverter(typesRivesStringConverter);
        this.uiRive.setValue(troncon.getTypeRive());
                
        this.uiDateStart.valueProperty().bindBidirectional(troncon.date_debutProperty());
        this.uiDateEnd.valueProperty().bindBidirectional(troncon.date_finProperty());
                
        initializing = false;
    }
        
    /**
     * Defines the OpenHtmlEditorEventHandler for editing comment field.
     */
//    private class OpenHtmlEditorEventHandler implements EventHandler<MouseEvent> {
//
//        @Override
//        public void handle(MouseEvent event) {
//            
//            if(uiEdit.isSelected()){
//                final Stage dialog = new Stage();
//                dialog.initModality(Modality.APPLICATION_MODAL);
//                dialog.initOwner(uiEdit.getScene().getWindow());
//
//                final HTMLEditor htmlEditor = new HTMLEditor();
//                htmlEditor.setHtmlText(tronconProperty.get().getCommentaire());
//
//                final Button valider = new Button("Valider");
//                valider.setOnAction((ActionEvent event1) -> {
//                    tronconProperty.get().setCommentaire(htmlEditor.getHtmlText());
//                    uiComment.getEngine().loadContent(htmlEditor.getHtmlText());
//                    dialog.hide();
//                });
//
//                final Button annuler = new Button("Annuler");
//                annuler.setOnAction((ActionEvent event1) -> {
//                    dialog.hide();
//                });
//
//                final HBox hBox = new HBox();
//                hBox.getChildren().add(valider);
//                hBox.getChildren().add(annuler);
//
//                final VBox vBox = new VBox();
//                vBox.getChildren().add(htmlEditor);
//                vBox.getChildren().add(hBox);
//
//                final Scene dialogScene = new Scene(vBox);
//                dialog.setScene(dialogScene);
//                dialog.show();
//            }
//        }
//    } 
}
