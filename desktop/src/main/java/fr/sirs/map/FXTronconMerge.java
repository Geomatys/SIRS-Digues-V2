
package fr.sirs.map;

import fr.sirs.SIRS;
import fr.sirs.core.model.TronconDigue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import org.geotoolkit.gui.javafx.util.FXDeleteTableColumn;
import org.geotoolkit.gui.javafx.util.FXMoveDownTableColumn;
import org.geotoolkit.gui.javafx.util.FXMoveUpTableColumn;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXTronconMerge extends VBox{
    
    @FXML private TableView uiTable;

    private final ObservableList<TronconDigue> troncons = FXCollections.observableArrayList();
    
    public FXTronconMerge() {
        SIRS.loadFXML(this);
        
        final TableColumn<TronconDigue,String> col = new TableColumn<>("Nom");
        col.setEditable(false);
        col.setCellValueFactory((TableColumn.CellDataFeatures<TronconDigue, String> param) -> param.getValue().libelleProperty());
        
        uiTable.setItems(troncons);
        uiTable.getColumns().add(new FXMoveUpTableColumn());
        uiTable.getColumns().add(new FXMoveDownTableColumn());
        uiTable.getColumns().add(col);
        uiTable.getColumns().add(new FXDeleteTableColumn(false));
        
    }

    public ObservableList<TronconDigue> getTroncons() {
        return troncons;
    }
 
    public void processMerge(){
        
    }
    
    
}
