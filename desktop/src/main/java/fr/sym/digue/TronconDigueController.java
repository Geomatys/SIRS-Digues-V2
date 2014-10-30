

package fr.sym.digue;

import fr.sym.Session;
import fr.sym.Symadrem;
import fr.symadrem.sirs.core.model.Digue;
import fr.symadrem.sirs.core.model.TronconDigue;
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
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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
    
    @FXML private TextField section_name;
    @FXML private WebView commentaire;
    @FXML private ChoiceBox<Digue> digues;
    @FXML private FXDateField date_debut;
    @FXML private FXDateField date_fin;
    @FXML private ToggleButton editionButton;
    @FXML private Button saveButton;
    @FXML private Label mode;
    @FXML private ChoiceBox<String> typeRiveChoiceBox;

    //flag afin de ne pas faire de traitement lors de l'initialisation
    private boolean initializing = false;
    
    public TronconDigueController() {
        Symadrem.loadFXML(this);
        Injector.injectDependencies(this);
        
        tronconProperty.addListener((ObservableValue<? extends TronconDigue> observable, TronconDigue oldValue, TronconDigue newValue) -> {
            initFields();
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
        initFields();
    }
    
    @FXML
    private void enableFields(final ActionEvent event){
        if (this.editionButton.isSelected()) {
            this.section_name.setEditable(true);
            this.commentaire.setOnMouseClicked(new TronconDigueController.OpenHtmlEditorEventHandler());
            this.digues.setDisable(false);
            this.date_debut.setDisable(false);
            this.date_fin.setDisable(false);
            this.editionButton.setText("Passer en consultation");
            this.mode.setText("Mode saisie");
            this.mode.setTextFill(Color.RED);
            this.saveButton.setDisable(false);
        } else {
            this.section_name.setEditable(false);
            this.commentaire.setOnMouseClicked((MouseEvent event1) -> {});
            this.digues.setDisable(true);
            this.date_debut.setDisable(true);
            this.date_fin.setDisable(true);
            this.editionButton.setText("Passer en saisie");
            this.mode.setText("Mode consultation");
            this.mode.setTextFill(Color.WHITE);
            this.saveButton.setDisable(true);
        }
    }
    
    @FXML
    private void save(final ActionEvent event){
        this.session.update(getTroncon());
        
        // Set the fields no longer editable.-----------------------------------
        this.editionButton.setSelected(false);
        this.enableFields(event);
    }
    
    private void initFields(){
        initializing = true;
        
        final TronconDigue troncon = tronconProperty.get();
        final ObservableList<Digue> allDigues = FXCollections.observableList(session.getDigueRepository().getAll());
        final Digue digue = session.getDigueById(troncon.getDigueId());
        
        this.section_name.setEditable(false);
        this.section_name.textProperty().bindBidirectional(troncon.nomProperty());
        this.commentaire.getEngine().loadContent(troncon.getCommentaire());
                
        this.digues.setItems(allDigues);
        final StringConverter<Digue> digueStringConverter = new StringConverter<Digue>() {
            @Override
            public String toString(Digue digue) {return digue.getLibelle();}
            @Override
            public Digue fromString(String string) {return null;}
        };
        
        this.digues.setConverter(digueStringConverter);
        this.digues.setValue(digue);
        this.digues.setDisable(true);
        this.digues.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Digue>() {

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
        this.digues.getValue().getId();
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
        this.typeRiveChoiceBox.setValue(troncon.getTypeRive());
                
        this.date_debut.valueProperty().bindBidirectional(troncon.date_debutProperty());
        this.date_debut.setDisable(true);
        this.date_fin.valueProperty().bindBidirectional(troncon.date_finProperty());
        this.date_fin.setDisable(true);
        
        // Disable the save button.---------------------------------------------
        this.saveButton.setDisable(true);
        
        initializing = false;
    }
        
    /**
     * Defines the OpenHtmlEditorEventHandler for editing comment field.
     */
    private class OpenHtmlEditorEventHandler implements EventHandler<MouseEvent> {

        @Override
        public void handle(MouseEvent event) {
            
            if(editionButton.isSelected()){
                final Stage dialog = new Stage();
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initOwner(editionButton.getScene().getWindow());

                final HTMLEditor htmlEditor = new HTMLEditor();
                htmlEditor.setHtmlText(tronconProperty.get().getCommentaire());

                final Button valider = new Button("Valider");
                valider.setOnAction((ActionEvent event1) -> {
                    tronconProperty.get().setCommentaire(htmlEditor.getHtmlText());
                    commentaire.getEngine().loadContent(htmlEditor.getHtmlText());
                    dialog.hide();
                });

                final Button annuler = new Button("Annuler");
                annuler.setOnAction((ActionEvent event1) -> {
                    dialog.hide();
                });

                final HBox hBox = new HBox();
                hBox.getChildren().add(valider);
                hBox.getChildren().add(annuler);

                final VBox vBox = new VBox();
                vBox.getChildren().add(htmlEditor);
                vBox.getChildren().add(hBox);

                final Scene dialogScene = new Scene(vBox);
                dialog.setScene(dialogScene);
                dialog.show();
            }
        }
    } 
}
