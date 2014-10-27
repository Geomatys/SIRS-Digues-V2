

package fr.sym.digue;

import fr.sym.Session;
import fr.sym.Symadrem;
import fr.symadrem.sirs.core.model.Digue;
import fr.symadrem.sirs.core.model.TronconDigue;
import java.io.IOException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.geotoolkit.gui.javafx.util.FXDateField;
//import jfxtras.scene.control.LocalDateTimeTextField;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class TronconDigueController {
    
    private TreeView uiTree;
    private TronconDigue troncon;
    
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
        this.session.update(this.troncon);
        
        // Set the fields no longer editable.-----------------------------------
        this.editionButton.setSelected(false);
        this.enableFields(event);
    }
    
    private ObservableList<Digue> getDigues(){
        ObservableList<Digue> digues = FXCollections.observableArrayList();
        this.uiTree.getRoot().getChildren().stream().forEach((item) -> {
            if(((TreeItem) item).getValue() instanceof Digue)
                digues.add((Digue) ((TreeItem) item).getValue());
        }
        );
        return digues;
    }
    
    private ObservableList<TreeItem> getDigueItems(){
        ObservableList<TreeItem> digueItems = FXCollections.observableArrayList();
        this.uiTree.getRoot().getChildren().stream().forEach((item) -> {
            if(((TreeItem) item).getValue() instanceof Digue)
                digueItems.add((TreeItem) item);
        }
        );
        return digueItems;
    }
    
    public void init(final TreeView uiTree){
        
        // Keep the TreeView reference.
        this.uiTree = uiTree;
        
        TreeItem tronconItem = (TreeItem) uiTree.getSelectionModel().getSelectedItem();
        this.troncon = (TronconDigue) (tronconItem).getValue();
        
        this.section_name.setEditable(false);
        this.section_name.textProperty().bindBidirectional(this.troncon.nomProperty());
        
        this.commentaire.getEngine().loadContent(this.troncon.getCommentaire());
        
        final Digue diguePropre = (Digue) ((TreeItem) uiTree.getSelectionModel().getSelectedItem()).getParent().getValue();
        final ObservableList<Digue> diguesObservables = this.getDigues();
        
        this.digues.setItems(diguesObservables);
        final StringConverter<Digue> digueStringConverter = new StringConverter<Digue>() {
        
            @Override
            public String toString(Digue digue) {return digue.getLibelle();}

            // TODO ?
            @Override
            public Digue fromString(String string) {return null;}
        };
        
        this.digues.setConverter(digueStringConverter);
        this.digues.setValue(diguePropre);
        this.digues.setDisable(true);
        this.digues.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Digue>() {

            @Override
            public void changed(ObservableValue<? extends Digue> observable, Digue oldValue, Digue newValue) {
                
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
                if (!newValue.equals(diguePropre)){
                    
                    final Stage dialog = new Stage();
                    dialog.initModality(Modality.APPLICATION_MODAL);
                    dialog.initOwner(digues.getScene().getWindow());

                    final Label label = new Label("Le changement de digue est enregistré d'office.");
                    final Button ok = new Button("Continuer");
                    ok.setOnAction((ActionEvent event) -> {
                        getDigueItems().stream().forEach((item) -> {
                            if(((Digue) item.getValue()).equals(oldValue)){
                                item.getChildren().remove(tronconItem);
                            }
                        });

                        getDigueItems().stream().forEach((item) -> {
                            if(((Digue) item.getValue()).equals(newValue)){
                                item.getChildren().add(tronconItem);
                            }
                        });
                        troncon.setDigueId(newValue.getId());
                        save(null);
                        dialog.hide();
                    });
                    final Button annuler = new Button("Annuler");
                    annuler.setOnAction((ActionEvent event) -> {
                        digues.setValue(diguePropre);
                        dialog.hide();
                    });

                    final HBox hBox = new HBox();
                    hBox.getChildren().add(ok);
                    hBox.getChildren().add(annuler);

                    final VBox vBox = new VBox();
                    vBox.getChildren().add(label);
                    vBox.getChildren().add(hBox);

                    final Scene dialogScene = new Scene(vBox);
                    dialog.setScene(dialogScene);
                    dialog.show();
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
        this.typeRiveChoiceBox.setValue(this.troncon.getTypeRive());
                
        this.date_debut.valueProperty().bindBidirectional(this.troncon.date_debutProperty());
        this.date_debut.setDisable(true);
        this.date_fin.valueProperty().bindBidirectional(this.troncon.date_finProperty());
        this.date_fin.setDisable(true);
        
        // Disable the save button.---------------------------------------------
        this.saveButton.setDisable(true);
    }
    
    public static Parent create(TreeView uiTree) {
        final FXMLLoader loader = new FXMLLoader(Symadrem.class.getResource(
                "/fr/sym/digue/tronconDigueDisplay.fxml"));
        final Parent root;
        try {
            root = loader.load();
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
        
        final TronconDigueController controller = loader.getController();
        Injector.injectDependencies(controller);
        controller.init(uiTree);
        return root;
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
                htmlEditor.setHtmlText(troncon.getCommentaire());

                final Button valider = new Button("Valider");
                valider.setOnAction((ActionEvent event1) -> {
                    troncon.setCommentaire(htmlEditor.getHtmlText());
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
