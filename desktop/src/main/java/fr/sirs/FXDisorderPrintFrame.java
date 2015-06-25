package fr.sirs;

import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.ui.PojoTable;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    
    
    @FXML 
    private void print(){
        
        final Desordre desordre = Injector.getSession().getRepositoryForClass(Desordre.class).getOne();
        
        try {
            Injector.getSession().getPrintManager().printDesordre(desordre);
        } catch (Exception ex) {
            Logger.getLogger(FXDisorderPrintFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private class TronconChoicePojoTable extends PojoTable {

        public TronconChoicePojoTable() {
            super(TronconDigue.class, "Troncons");
            uiTable.getColumns().remove(editCol);
            editableProperty.set(false);
        }
    }
    
    
    
//    
//    public static class SelectColumn extends TableColumn {
//        
//        
//        private ObservableList selected;
//
//        public SelectColumn() {
//            super("Sélection");
//            setSortable(false);
//            setResizable(false);
//            setPrefWidth(24);
//            setMinWidth(24);
//            setMaxWidth(24);
//            setGraphic(new ImageView(SIRS.ICON_EDIT_BLACK));
//
//            setCellValueFactory(new Callback<TableColumn.CellDataFeatures, ObservableValue>() {
//
//                @Override
//                public ObservableValue call(TableColumn.CellDataFeatures param) {
//                    return new SimpleObjectProperty<>(param.getValue());
//                }
//            });
//
//            setCellFactory(new Callback<TableColumn, TableCell>() {
//
//                @Override
//                public TableCell call(TableColumn param) {
//                    return new FXBooleanCell();
//                }
//            });
//        }
//    }
    
    
    
}
