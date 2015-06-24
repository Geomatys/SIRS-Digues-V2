package fr.sirs;

import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.ui.PojoTable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXDisorderPrintFrame extends BorderPane {
    
    @FXML Tab uiTronconChoice;
    @FXML Tab uiDisorderTypeChoice;
    @FXML Tab uiOptionChoice;
    
    public FXDisorderPrintFrame(){
        SIRS.loadFXML(this, FXDisorderPrintFrame.class);
        final PojoTable tronconsTable = new TronconChoicePojoTable();
        tronconsTable.setTableItems(()-> (ObservableList) FXCollections.observableList(Injector.getSession().getRepositoryForClass(TronconDigue.class).getAll()));
        uiTronconChoice.setContent(tronconsTable);
    }
    
    
    private class TronconChoicePojoTable extends PojoTable {

        public TronconChoicePojoTable() {
            super(TronconDigue.class, "Troncons");
            uiTable.getColumns();
        }
        
    }
    
}
