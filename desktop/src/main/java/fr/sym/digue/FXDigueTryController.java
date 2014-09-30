

package fr.sym.digue;

import fr.sym.Session;
import fr.sym.Symadrem;
import fr.symadrem.sirs.core.model.Digue;
import fr.symadrem.sirs.core.model.Troncon;
import java.io.IOException;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXDigueTryController {
    
    public Parent root;
    public Digue digue;
    
    
    @FXML
    private TextField libelleDigueTextField;
    
    @FXML
    private TextArea commentaireDigueTextField;
    
    @FXML
    private TableView<Troncon> tronconsTable;
    
    @FXML
    private ToggleButton editionButton;
    
    public void init(Digue digue){
        
        // Set the levee for the controller.------------------------------------
        this.digue = digue;
        
        // Binding levee's name.------------------------------------------------
        this.libelleDigueTextField.textProperty().bindBidirectional(digue.label);
        this.libelleDigueTextField.setEditable(true);
        
        // Binding levee's comment.---------------------------------------------
        this.commentaireDigueTextField.textProperty().bindBidirectional(digue.comment);
        this.commentaireDigueTextField.setWrapText(true);
        this.commentaireDigueTextField.setEditable(true);
        
        // Configuring table for levee's sections.------------------------------
        final TableColumn colName = this.tronconsTable.getColumns().get(1);
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colName.setEditable(true);
        
        colName.setCellFactory(TextFieldTableCell.forTableColumn());
        colName.setOnEditCommit(
            new EventHandler<TableColumn.CellEditEvent<Troncon, String>>() {
        
            @Override
            public void handle(TableColumn.CellEditEvent<Troncon, String> event) {
            ((Troncon) event.getTableView().getItems().get(
                event.getTablePosition().getRow())).setName(event.getNewValue());
            }
        }
        );
        /*
        final TableColumn colJojo = this.tronconsTable.getColumns().get(0);
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
        final List<Troncon> troncons = Session.getInstance().getTronconGestionDigueTrysByDigueTry(this.digue);
        final ObservableList<Troncon> tronconsObservables = FXCollections.observableArrayList();
        troncons.stream().forEach((troncon) -> {
            tronconsObservables.add(troncon);
        });
        this.tronconsTable.setItems(tronconsObservables);
        this.tronconsTable.setEditable(true);
        
        PropertyValueFactory<Troncon, String> pvf = new PropertyValueFactory<>("name");
        TableColumn.CellDataFeatures<Troncon, String> cdf = new TableColumn.CellDataFeatures<Troncon, String>(tronconsTable, colName, null);
    }
    
    @FXML
    public void change(ActionEvent event){
        System.out.println(digue.label);
    }
    
    @FXML
    public void enableFields(ActionEvent event){
        if (this.editionButton.isSelected()) {
            this.libelleDigueTextField.setEditable(false);
            this.commentaireDigueTextField.setEditable(false);
            this.tronconsTable.setEditable(false);
        } else {
            this.libelleDigueTextField.setEditable(true);
            this.commentaireDigueTextField.setEditable(true);
            this.tronconsTable.setEditable(true);
        }
    }
    
    public static FXDigueTryController create(Digue digue) {
        
        final FXMLLoader loader = new FXMLLoader(Symadrem.class.getResource("/fr/sym/digue/digueTryDisplay.fxml"));
        final Parent root;
        
        try {
            root = loader.load();
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
        
        final FXDigueTryController controller = loader.getController();
        controller.root = root;
        controller.init(digue);
        return controller;
    }
    
}
