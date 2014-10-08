

package fr.sym.digue;

import fr.sym.Session;
import fr.sym.Symadrem;
import fr.symadrem.sirs.core.model.Digue;
import fr.symadrem.sirs.core.model.TronconDigue;
import java.io.IOException;
import java.util.List;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import jfxtras.scene.control.LocalDateTimeTextField;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class TronconDigueController {
    
    public Parent root;
    private TronconDigue troncon;
    
    @Autowired
    private Session session;
    
    @FXML
    private TextField section_name;
    
    @FXML
    private WebView commentaire;
    
    @FXML
    private ChoiceBox<Digue> digues;
    
    @FXML
    private LocalDateTimeTextField date_debut;
    
    @FXML
    private LocalDateTimeTextField date_fin;
    
    @FXML
    private ToggleButton editionButton;
    
    @FXML
    private Label mode;
    
    @FXML
    private void enableFields(ActionEvent event){
        if (this.editionButton.isSelected()) {
            this.section_name.setEditable(true);
            this.commentaire.setOnMouseClicked(new TronconDigueController.OpenHtmlEditorEventHandler());
            this.digues.setDisable(false);
            this.date_debut.setDisable(false);
            this.date_fin.setDisable(false);
            this.editionButton.setText("Passer en consultation");
            this.mode.setText("Mode saisie");
            this.mode.setTextFill(Color.RED);
        } else {
            this.section_name.setEditable(false);
            this.commentaire.setOnMouseClicked((MouseEvent event1) -> {});
            this.digues.setDisable(true);
            this.date_debut.setDisable(true);
            this.date_fin.setDisable(true);
            this.editionButton.setText("Passer en saisie");
            this.mode.setText("Mode consultation");
            this.mode.setTextFill(Color.WHITE);
        }
    }
    
    public void init(TronconDigue troncon){
        
        this.troncon = troncon;
        
        this.section_name.setEditable(false);
        this.section_name.textProperty().bindBidirectional(this.troncon.libelleProperty());
        
        this.commentaire.getEngine().loadContent(this.troncon.getCommentaire());
        
        final List<Digue> digs = session.getDigues();
        Digue diguePropre = null;
        final ObservableList<Digue> diguesObservables = FXCollections.observableArrayList();
        for (final Digue dig : digs) {
            diguesObservables.add(dig);
            if(dig.getId().equals(this.troncon.getDigueId())) diguePropre=dig;
        }
        
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
                
        this.date_debut.localDateTimeProperty().bindBidirectional(this.troncon.date_debutProperty());
        this.date_debut.setDisable(true);
        this.date_fin.localDateTimeProperty().bindBidirectional(this.troncon.date_finProperty());
        this.date_fin.setDisable(true);
    }
    
    public static TronconDigueController create(TronconDigue troncon) {
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
        controller.root = root;
        controller.init(troncon);
        return controller;
    }
    
    /**
     * Defines the OpenHtmlEditorEventHandler for editing comment field.
     */
    private class OpenHtmlEditorEventHandler implements EventHandler<MouseEvent> {

        @Override
        public void handle(MouseEvent event) {
            
            final Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(root.getScene().getWindow());

            final VBox vbox = new VBox();

            final HTMLEditor htmlEditor = new HTMLEditor();
            htmlEditor.setHtmlText(troncon.getCommentaire());

            final HBox hbox = new HBox();

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

            hbox.getChildren().add(valider);
            hbox.getChildren().add(annuler);

            vbox.getChildren().add(htmlEditor);
            vbox.getChildren().add(hbox);

            final Scene dialogScene = new Scene(vbox);
            dialog.setScene(dialogScene);
            dialog.show();
        }
    }
    
}
