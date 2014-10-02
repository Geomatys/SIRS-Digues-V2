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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
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
    private TextArea commentaire;

    @FXML
    private TableView<TronconDigue> tronconsTable;

    @FXML
    private ToggleButton editionButton;

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
        this.commentaire.textProperty().bindBidirectional(digue.commentaireProperty());
        this.commentaire.setWrapText(true);
        this.commentaire.setEditable(false);

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
        
        StringConverter<Instant> instantStringConverter = new StringConverter<Instant>() {
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
         final TableColumn<FieldValue, Field> valueColumn = new TableColumn<>("Value");*/
        

        /*final TableColumn colJojo = this.tronconsTable.getColumns().get(0);
         colJojo.setCellValueFactory(new PropertyValueFactory<>("jojo"));
         colJojo.setEditable(true);
         StringConverter<Troncon.jojoenum> sc = new StringConverter<Troncon.jojoenum>() {
            
         @Override
         public String toString(Troncon.jojoenum object) {
            
         String result;
         switch(object){
         case oui: result = "je vaux oui"; break;
         case non: result = "je vaux non"; break;
         case bof:
         default: result = "je vaux bof";
         }
         return result;
            
         }

         @Override
         public Troncon.jojoenum fromString(String string) {
                
         return Troncon.jojoenum.bof;
         }
         };
         colJojo.setCellFactory(TextFieldTableCell.forTableColumn(sc));
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

    /*@Override
    public void initialize(URL location, ResourceBundle resources) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }*/

    // FocusTransverse ?
    class CustomizedTableCell extends TableCell<TronconDigue, String> {

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            final Button button = new Button();
            button.setText("ID");
            setGraphic(button);
            button.setBackground(new Background(new BackgroundFill(Color.ALICEBLUE, new CornerRadii(20), Insets.EMPTY)));
            button.setBorder(new Border(new BorderStroke(Color.ROYALBLUE, BorderStrokeStyle.SOLID, new CornerRadii(20), BorderWidths.DEFAULT)));
            button.setOnAction((ActionEvent event) -> {
                final TronconDigue troncon = (TronconDigue) ((TableRow) this.getParent()).getItem();
                
                final Stage dialog = new Stage();
                
                final Label libelle = new Label(troncon.getLibelle());
                final Label id = new Label(troncon.getId());
                final Button ok = new Button("Ok");
                ok.setOnAction((ActionEvent event1) -> {
                    dialog.hide();
                });
                final VBox popUpVBox = new VBox();
                popUpVBox.getChildren().add(libelle);
                popUpVBox.getChildren().add(id);
                popUpVBox.getChildren().add(ok);

                final Scene dialogScene = new Scene(popUpVBox, 300, 200);

                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initOwner(root.getScene().getWindow());
                dialog.setScene(dialogScene);
                dialog.show();
            });
                
        }
    }

    @FXML
    public void change(ActionEvent event) {
        System.out.println(digue.libelleProperty());
    }

    @FXML
    public void enableFields(ActionEvent event) {
        if (this.editionButton.isSelected()) {
            this.libelle.setEditable(true);
            this.commentaire.setEditable(true);
            this.tronconsTable.setEditable(true);
        } else {
            this.libelle.setEditable(false);
            this.commentaire.setEditable(false);
            this.tronconsTable.setEditable(false);
        }
        
        // Switch Label's text color.-------------------------------------------
        final Paint paint = this.mode_label_consultation.getTextFill();
        this.mode_label_consultation.setTextFill(this.mode_label_saisie.getTextFill());
        this.mode_label_saisie.setTextFill(paint);
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

}
