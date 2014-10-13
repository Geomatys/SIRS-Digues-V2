package fr.sym.digue;

import com.vividsolutions.jts.geom.Geometry;
import fr.sym.Session;
import fr.sym.Symadrem;
import fr.symadrem.sirs.core.model.Digue;
import fr.symadrem.sirs.core.model.TronconDigue;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
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
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import jfxtras.scene.control.LocalDateTimeTextField;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class DigueController {
    
    public Parent root;
    private TreeView uiTree;
    private Digue digue;
    private ObservableList<TronconDigue> troncons;
    //private BooleanProperty editionMode;
    
    @Autowired
    private Session session;

    @FXML private TextField libelle;
    @FXML private Label id;
    @FXML private Label mode;
    @FXML private LocalDateTimeTextField date_maj;
    @FXML private WebView commentaire;
    @FXML private TableView<TronconDigue> tronconsTable;
    @FXML private ToggleButton editionButton;
    @FXML private Button saveButton;
    @FXML private Button addTroncon;
    @FXML private Button deleteTroncon;

    
    private static enum Field {
        DATE_DEBUT("date_debut"), DATE_FIN("date_fin");
        private final String field;
        private Field(final String field){this.field=field;}
        
        @Override
        public String toString(){return this.field;}
    };
      
    /*@Override
    public void initialize(URL location, ResourceBundle resources) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }*/

    @FXML
    public void enableFields(ActionEvent event) {
        
        if (this.editionButton.isSelected()) {
            this.editionButton.setText("Passer en consultation");
            this.mode.setText("Mode saisie");
            this.mode.setTextFill(Color.RED);
            this.addTroncon.setGraphic(new ImageView("fr/sym/images/add-icon.png"));
            this.deleteTroncon.setGraphic(new ImageView("fr/sym/images/delete-icon.png"));
            this.saveButton.setDisable(false);
        } else {
            this.editionButton.setText("Passer en saisie");
            this.mode.setText("Mode consultation");
            this.mode.setTextFill(Color.WHITE);
            this.addTroncon.setGraphic(new ImageView("fr/sym/images/add-icon-inactif.png"));
            this.deleteTroncon.setGraphic(new ImageView("fr/sym/images/delete-icon-inactif.png"));
            this.saveButton.setDisable(true);
        }
    }
    
    @FXML
    private void save(ActionEvent event){
        this.session.update(this.digue);
//        this.session.update(this.troncons);
        this.editionButton.setSelected(false);
        this.enableFields(event);
    }
    
    @FXML
    private void addTroncon(){
        
        if (editionButton.isSelected()){
            
            final Stage dialog = new Stage();
            final Label libelle = new Label("Libellé : ");
            final TextField libelleInput = new TextField();
            final Button ok = new Button("Ok");
            ok.setOnAction((ActionEvent event1) -> {
                TronconDigue tronconDigue = new TronconDigue();
                tronconDigue.libelleProperty().bindBidirectional(libelleInput.textProperty());
                tronconDigue.setDigueId(digue.getId());
                this.loadTronconUI(tronconDigue);
                this.session.add(tronconDigue);
                dialog.hide();
            });

            final VBox vBox = new VBox();
            vBox.setPadding(new Insets(20));
            vBox.setAlignment(Pos.CENTER);
            vBox.getChildren().add(libelle);
            vBox.getChildren().add(libelleInput);
            vBox.getChildren().add(ok);

            final Scene dialogScene = new Scene(vBox);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(root.getScene().getWindow());
            dialog.setScene(dialogScene);
            dialog.setTitle("Nouveau tronçon de digue.");
            dialog.show();
        }
    }
    
    @FXML
    private void deleteTroncon(){
        
        if (editionButton.isSelected()){

            final TronconDigue tronconDigue = this.tronconsTable.getSelectionModel().getSelectedItem();
            final Stage dialog = new Stage();
            final Label libelle = new Label(tronconDigue.getLibelle());
            final Label id = new Label(tronconDigue.getId());
            final Label confirmationMsg = new Label("Voulez-vous vraiment supprimer ce tronçon ?");
            final Button annuler = new Button("Annuler");
            annuler.setOnAction((ActionEvent event1) -> {
                dialog.hide();
            });
            final Button ok = new Button("Ok");
            ok.setOnAction((ActionEvent event1) -> {
                this.session.delete(tronconDigue);
                this.discardTronconUI(tronconDigue);
                dialog.hide();
            });
            
            final HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER);
            hBox.getChildren().add(annuler);
            hBox.getChildren().add(ok);

            final VBox vBox = new VBox();
            vBox.setPadding(new Insets(20));
            vBox.setAlignment(Pos.CENTER);
            vBox.getChildren().add(libelle);
            vBox.getChildren().add(id);
            vBox.getChildren().add(confirmationMsg);
            vBox.getChildren().add(hBox);

            final Scene dialogScene = new Scene(vBox);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(root.getScene().getWindow());
            dialog.setScene(dialogScene);
            dialog.setTitle("Suppression d'un tronçon de digue.");
            dialog.show();
        
        }
    }
    
    private void loadTroncons(){
        
        final List<TreeItem> items = ((TreeItem) uiTree.getSelectionModel().getSelectedItem()).getChildren();
        this.troncons = FXCollections.observableArrayList();
        items.stream().forEach((item) -> {
            this.troncons.add((TronconDigue) item.getValue());
        });
        this.tronconsTable.setItems(this.troncons);
    }
    
    /**
     * Update the TreeView and TableView with the new section.
     * @param tronconDigue 
     */
    private void loadTronconUI(final TronconDigue tronconDigue){
        ((TreeItem) this.uiTree.getSelectionModel().getSelectedItem()).getChildren().add(new TreeItem(tronconDigue));
        this.troncons.add(tronconDigue);
    }
    
    /**
     * Remove the section from the TreeView and the tableView.
     * @param tronconDigue 
     */
    private void discardTronconUI(final TronconDigue tronconDigue){
        for (final Object treeItem : ((TreeItem) this.uiTree.getSelectionModel().getSelectedItem()).getChildren()){
            if(tronconDigue.equals(((TreeItem) treeItem).getValue())){
                ((TreeItem) this.uiTree.getSelectionModel().getSelectedItem()).getChildren().remove(treeItem);
                this.troncons.remove(tronconDigue);
                break;
            }
        }
    }

    /**
     * 
     * @param uiTree 
     */
    public void init(final TreeView uiTree) {
        
        // Keep the TreeView reference.
        this.uiTree = uiTree;
        
        // Set the levee for the controller.------------------------------------
        this.digue = (Digue) ((TreeItem) uiTree.getSelectionModel().getSelectedItem()).getValue();
        
        // Binding levee's name.------------------------------------------------
        this.libelle.textProperty().bindBidirectional(digue.libelleProperty());
        this.libelle.editableProperty().bindBidirectional(this.editionButton.selectedProperty());
        
        // Display levee's id.--------------------------------------------------
        this.id.setText(this.digue.getId());
        
        // Display levee's update date.-----------------------------------------
        this.date_maj.setLocalDateTime(this.digue.getDate_maj());
        this.date_maj.setDisable(true);
        this.date_maj.localDateTimeProperty().bindBidirectional(this.digue.date_majProperty());

        // Binding levee's comment.---------------------------------------------
        this.commentaire.getEngine().loadContent(digue.getCommentaire());
        this.commentaire.setOnMouseClicked(new OpenHtmlEditorEventHandler());
        
        // Configuring table for levee's sections.------------------------------
        final TableColumn idCol = this.tronconsTable.getColumns().get(0);
        idCol.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        idCol.setEditable(false);
        idCol.setCellFactory(new Callback<TableColumn<TronconDigue, String>, CustomizedIdTableCell>() {
            @Override
            public CustomizedIdTableCell call(TableColumn<TronconDigue, String> param) {
                return new CustomizedIdTableCell();
            }
        });

        final TableColumn colName = this.tronconsTable.getColumns().get(1);
        colName.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        colName.setEditable(false);
        colName.setCellFactory(TextFieldTableCell.forTableColumn());

        final TableColumn colDateDebut = this.tronconsTable.getColumns().get(2);
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("date_debut"));
        colDateDebut.setEditable(false);
        colDateDebut.setCellFactory(new Callback<TableColumn<TronconDigue, LocalDateTime>, CustomizedLocalDateTimeTableCell>() {
            @Override
            public CustomizedLocalDateTimeTableCell call(TableColumn<TronconDigue, LocalDateTime> param) {
                return new CustomizedLocalDateTimeTableCell(Field.DATE_DEBUT);
            }
        });
        
        final TableColumn colDateFin = this.tronconsTable.getColumns().get(3);
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("date_fin"));
        colDateFin.setEditable(false);
        colDateFin.setCellFactory(new Callback<TableColumn<TronconDigue, LocalDateTime>, CustomizedLocalDateTimeTableCell>() {
            @Override
            public CustomizedLocalDateTimeTableCell call(TableColumn<TronconDigue, LocalDateTime> param) {
                return new CustomizedLocalDateTimeTableCell(Field.DATE_FIN);
            }
        });
        
        final TableColumn colSR = this.tronconsTable.getColumns().get(4);
        colSR.setCellValueFactory(new PropertyValueFactory<>("systeme_reperage_defaut"));
        colSR.setEditable(false);
        colSR.setCellFactory(TextFieldTableCell.forTableColumn());
        
        final TableColumn colGeom = this.tronconsTable.getColumns().get(5);
        colGeom.setCellValueFactory(new PropertyValueFactory<>("geometry"));
        colGeom.setEditable(false);
        colGeom.setCellFactory(new Callback<TableColumn<TronconDigue, Geometry>, CustomizedGeometryTableCell>() {
            @Override
            public CustomizedGeometryTableCell call(TableColumn<TronconDigue, Geometry> param) {
                return new CustomizedGeometryTableCell();
            }
        });
        

        /*colJojo.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Troncon, Troncon.jojoenum>>(){

         @Override
         public void handle(TableColumn.CellEditEvent<Troncon, Troncon.jojoenum> event) {
         ((Troncon) event.getTableView().getItems().get(
         event.getTablePosition().getRow())).setJojo(Troncon.jojoenum.non);  
         }
         }
        
         );*/
        
        this.loadTroncons();
        this.tronconsTable.setEditable(false);
        this.saveButton.setDisable(true);
    }

    public static DigueController create(final TreeView uiTree) {

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
        controller.init(uiTree);
        return controller;
    }

    // FocusTransverse ?
    /**
     * Defines the customized table cell for displaying id of each levee's section.
     */
    private class CustomizedIdTableCell extends TableCell<TronconDigue, String> {
        
        private Button button;
        
        @Override
        protected void updateItem(String item, boolean empty) {
            
            System.out.println("CELL : "+CustomizedIdTableCell.this);
            System.out.println("ITEM : "+item);
            System.out.println("EMPTY : "+empty);
            
            super.updateItem(item, empty);
            
            if(item != null && !empty) {
                button = new Button();
                button.setText("ID");
                setGraphic(button);
                button.setBackground(new Background(new BackgroundFill(Color.ALICEBLUE, new CornerRadii(20), Insets.EMPTY)));
                button.setBorder(new Border(new BorderStroke(Color.ROYALBLUE, BorderStrokeStyle.SOLID, new CornerRadii(20), BorderWidths.DEFAULT)));
                button.setOnAction((ActionEvent event) -> {
                    final TronconDigue troncon = (TronconDigue) ((TableRow) CustomizedIdTableCell.this.getParent()).getItem();
                    final Stage dialog = new Stage();
                    final Label libelle = new Label(troncon.getLibelle());
                    final Label id = new Label(troncon.getId());
                    final Button ok = new Button("Ok");
                    ok.setOnAction((ActionEvent event1) -> {
                        dialog.hide();
                    });

                    final VBox vBox = new VBox();
                    vBox.setAlignment(Pos.CENTER);
                    vBox.setPadding(new Insets(20));
                    vBox.getChildren().add(libelle);
                    vBox.getChildren().add(id);
                    vBox.getChildren().add(ok);

                    final Scene dialogScene = new Scene(vBox);
                    dialog.initModality(Modality.APPLICATION_MODAL);
                    dialog.initOwner(root.getScene().getWindow());
                    dialog.setScene(dialogScene);
                    dialog.setTitle("Identifiant de tronçon de digue.");
                    dialog.show();
                });
            }
        }
    }
    
    /**
     * Defines the customized table cell for displaying geometry of each levee's section.
     */
    private class CustomizedGeometryTableCell extends TableCell<TronconDigue, Geometry> {
        
        private Button button = new Button();
                
        @Override
        protected void updateItem(Geometry item, boolean empty) {
            
            super.updateItem(item, empty);
            
            if(item != null) {
                button.setText(item.getGeometryType());
                setGraphic(button);
                button.setBackground(new Background(new BackgroundFill(Color.LIGHTPINK, new CornerRadii(20), Insets.EMPTY)));
                button.setBorder(new Border(new BorderStroke(Color.DARKMAGENTA, BorderStrokeStyle.SOLID, new CornerRadii(20), BorderWidths.DEFAULT)));
                button.setOnAction((ActionEvent event) -> {
                    final TronconDigue troncon = (TronconDigue) ((TableRow) CustomizedGeometryTableCell.this.getParent()).getItem();
                    final Stage dialog = new Stage();
                    final Label libelle = new Label(troncon.getLibelle());
                    final Label wkt = new Label(troncon.getGeometry().toText());
                    final Button ok = new Button("Ok");
                    ok.setOnAction((ActionEvent event1) -> {
                        dialog.hide();
                    });

                    final VBox vBox = new VBox();
                    vBox.setAlignment(Pos.CENTER);
                    vBox.setPadding(new Insets(20));
                    vBox.getChildren().add(libelle);
                    vBox.getChildren().add(wkt);
                    vBox.getChildren().add(ok);

                    final Scene dialogScene = new Scene(vBox);
                    dialog.initModality(Modality.APPLICATION_MODAL);
                    dialog.initOwner(root.getScene().getWindow());
                    dialog.setScene(dialogScene);
                    dialog.setTitle("Géométrie de tronçon de digue.");
                    dialog.show();
                });
            }
        }
    }
    
    /**
     * Defines the customized table cell for displaying geometry of each levee's section.
     */
    private class CustomizedLocalDateTimeTableCell extends TableCell<TronconDigue, LocalDateTime> {
        
        
        private final LocalDateTimeTextField localDateTimeTextField;
        private final Field field;

        public CustomizedLocalDateTimeTableCell(final Field field) {
            super();
            this.localDateTimeTextField = new LocalDateTimeTextField();
            this.localDateTimeTextField.setDisable(true);
            this.field = field;
        }
        
        @Override
        protected void updateItem(LocalDateTime item, boolean empty) {
            
            super.updateItem(item, empty);
            
            if(item != null) {
                this.localDateTimeTextField.setLocalDateTime(item);
                switch(this.field){
                    case DATE_DEBUT: 
                        this.localDateTimeTextField.localDateTimeProperty().bindBidirectional(
                                ((TronconDigue) CustomizedLocalDateTimeTableCell.this.getTableRow().getItem()).date_debutProperty());
                        break;
                    case DATE_FIN: 
                        this.localDateTimeTextField.localDateTimeProperty().bindBidirectional(
                                ((TronconDigue) CustomizedLocalDateTimeTableCell.this.getTableRow().getItem()).date_finProperty());
                        break;
                }
                setGraphic(this.localDateTimeTextField);
            }
        }
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
                dialog.initOwner(root.getScene().getWindow());
                
                final VBox vbox = new VBox();

                final HTMLEditor htmlEditor = new HTMLEditor();
                htmlEditor.setHtmlText(digue.getCommentaire());
                
                final HBox hbox = new HBox();
                hbox.setPadding(new Insets(20));
                hbox.setAlignment(Pos.CENTER);
                
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
}
