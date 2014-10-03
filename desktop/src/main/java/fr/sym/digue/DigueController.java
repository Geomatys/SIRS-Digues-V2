package fr.sym.digue;

import fr.sym.Session;
import fr.sym.Symadrem;
import fr.symadrem.sirs.core.model.Digue;
import fr.symadrem.sirs.core.model.TronconDigue;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class DigueController {
    
    public Parent root;
    private Digue digue;
    
    @Autowired
    private Session session;

    @FXML
    private TextField libelle;
    
    @FXML
    private Label id;
    
    @FXML
    private Label date_maj;
    
    @FXML
    private Label mode_label_consultation;
    
    @FXML
    private Label mode_label_saisie;

    @FXML
    private WebView commentaire;

    @FXML
    private TableView<TronconDigue> tronconsTable;

    @FXML
    private ToggleButton editionButton;

    /*@Override
    public void initialize(URL location, ResourceBundle resources) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }*/

    @FXML
    public void change(ActionEvent event) {
        System.out.println(digue.libelleProperty());
    }

    @FXML
    public void enableFields(ActionEvent event) {
        
        if (this.editionButton.isSelected()) {
            this.libelle.setEditable(true);
            this.tronconsTable.setEditable(true);
            this.commentaire.setOnMouseClicked(new OpenHtmlEditorEventHandler());
        } else {
            this.libelle.setEditable(false);
            this.tronconsTable.setEditable(false);
            this.commentaire.setOnMouseClicked((MouseEvent event1) -> {});
        }
        
        // Switch Label's text color.-------------------------------------------
        final Paint paint = this.mode_label_consultation.getTextFill();
        this.mode_label_consultation.setTextFill(this.mode_label_saisie.getTextFill());
        this.mode_label_saisie.setTextFill(paint);
    }

    public void init(Digue digue) {

        // Set the levee for the controller.------------------------------------
        this.digue = digue;
        
        // Binding levee's name.------------------------------------------------
        this.libelle.textProperty().bindBidirectional(digue.libelleProperty());
        this.libelle.setEditable(false);
        
        // Display levee's id.--------------------------------------------------
        this.id.setText(this.digue.getId());
        
        // Display levee's update date.-----------------------------------------
        this.date_maj.setText(this.digue.getDate_maj().toString());

        // Binding levee's comment.---------------------------------------------
        this.commentaire.getEngine().loadContent(digue.getCommentaire());

        // Configuring table for levee's sections.------------------------------
        final TableColumn idName = this.tronconsTable.getColumns().get(0);
        idName.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        idName.setEditable(true);
        idName.setCellFactory(new Callback<TableColumn<TronconDigue, String>, CustomizedTableCell>() {
            @Override
            public CustomizedTableCell call(TableColumn<TronconDigue, String> param) {
                return new CustomizedTableCell();
            }
        });

        final TableColumn colName = this.tronconsTable.getColumns().get(1);
        colName.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        colName.setEditable(true);
        colName.setCellFactory(TextFieldTableCell.forTableColumn());

        final TableColumn colDateDebut = this.tronconsTable.getColumns().get(2);
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("date_debut"));
        colDateDebut.setEditable(true);
        
        final StringConverter<Instant> instantStringConverter = new StringConverter<Instant>() {
            @Override
            public String toString(Instant object) {return object.toString();}

            @Override
            public Instant fromString(String string) {return Instant.parse(string);}
        };
        colDateDebut.setCellFactory(TextFieldTableCell.forTableColumn(instantStringConverter));
        
        final TableColumn colDateFin = this.tronconsTable.getColumns().get(3);
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("date_fin"));
        colDateFin.setEditable(true);
        colDateFin.setCellFactory(TextFieldTableCell.forTableColumn(instantStringConverter));
        
        
        
        /*colName.setOnEditCommit(
         new EventHandler<TableColumn.CellEditEvent<Troncon, String>>() {
        
         @Override
         public void handle(TableColumn.CellEditEvent<Troncon, String> event) {
         ((Troncon) event.getTableView().getItems().get(
         event.getTablePosition().getRow())).setName(event.getNewValue());
         }
         }
         );*/
        

        /*
         colJojo.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Troncon, Troncon.jojoenum>>(){

         @Override
         public void handle(TableColumn.CellEditEvent<Troncon, Troncon.jojoenum> event) {
         ((Troncon) event.getTableView().getItems().get(
         event.getTablePosition().getRow())).setJojo(Troncon.jojoenum.non);  
         }
         }
        
         );*/
        // Binding levee's section.---------------------------------------------
        final List<TronconDigue> troncons = session.getTronconGestionDigueTrysByDigueTry(this.digue);
        final ObservableList<TronconDigue> tronconsObservables = FXCollections.observableArrayList();
        troncons.stream().forEach((troncon) -> {
            tronconsObservables.add(troncon);
        });
        this.tronconsTable.setItems(tronconsObservables);
        this.tronconsTable.setEditable(false);

        /*
        PropertyValueFactory<TronconDigue, String> pvf = new PropertyValueFactory<>("libelle");
        TableColumn.CellDataFeatures<TronconDigue, String> cdf = 
                new TableColumn.CellDataFeatures<TronconDigue, String>(tronconsTable, colName, null);*/
    }

    public static DigueController create(Digue digue) {

        final FXMLLoader loader = new FXMLLoader(Symadrem.class.getResource(
                "/fr/sym/digue/digueDisplay.fxml"));
        final Parent root;

        try {
            root = loader.load();
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }

        final DigueController controller = loader.getController();
        Injector.injectDependencies(controller);
        controller.root = root;
        controller.init(digue);
        return controller;
    }

    // FocusTransverse ?
    /**
     * Defines the customized table cell for displaying id of each levee's section.
     */
    private class CustomizedTableCell extends TableCell<TronconDigue, String> {
        
        @Override
        protected void updateItem(String item, boolean empty) {
            
            super.updateItem(item, empty);
            
            final Button button = new Button();
            button.setText("ID");
            setGraphic(button);
            button.setBackground(new Background(new BackgroundFill(Color.ALICEBLUE, new CornerRadii(20), Insets.EMPTY)));
            button.setBorder(new Border(new BorderStroke(Color.ROYALBLUE, BorderStrokeStyle.SOLID, new CornerRadii(20), BorderWidths.DEFAULT)));
            button.setOnAction((ActionEvent event) -> {
                final TronconDigue troncon = (TronconDigue) ((TableRow) CustomizedTableCell.this.getParent()).getItem();
                final Stage dialog = new Stage();
                final Label libelle1 = new Label(troncon.getLibelle());
                final Label id1 = new Label(troncon.getId());
                final Button ok = new Button("Ok");
                ok.setOnAction((ActionEvent event1) -> {
                    dialog.hide();
                });
                
                final VBox popUpVBox = new VBox();
                popUpVBox.getChildren().add(libelle1);
                popUpVBox.getChildren().add(id1);
                popUpVBox.getChildren().add(ok);
                
                final Scene dialogScene = new Scene(popUpVBox, 300, 200);
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initOwner(root.getScene().getWindow());
                dialog.setScene(dialogScene);
                dialog.show();
            });
        }
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
                htmlEditor.setHtmlText(digue.getCommentaire());
                
                final HBox hbox = new HBox();
                
                final Button valider = new Button("Valider");
                valider.setOnAction((ActionEvent event1) -> {
                    digue.setCommentaire(htmlEditor.getHtmlText());
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
