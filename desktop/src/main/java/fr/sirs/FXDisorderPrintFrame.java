package fr.sirs;

import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.RefTypeDesordre;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.ui.PojoTable;
import java.util.ArrayList;
import java.util.List;
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
    
    final TronconChoicePojoTable tronconsTable = new TronconChoicePojoTable();
    final DisorderTypeChoicePojoTable disordreTypesTable = new DisorderTypeChoicePojoTable();
    
    public FXDisorderPrintFrame(){
        SIRS.loadFXML(this, FXDisorderPrintFrame.class);
        tronconsTable.setTableItems(()-> (ObservableList) FXCollections.observableList(Injector.getSession().getRepositoryForClass(TronconDigue.class).getAll()));
        uiTronconChoice.setContent(tronconsTable);
        disordreTypesTable.setTableItems(()-> (ObservableList) FXCollections.observableList(Injector.getSession().getRepositoryForClass(RefTypeDesordre.class).getAll()));
        uiDisorderTypeChoice.setContent(disordreTypesTable);
    }
    
    
    @FXML 
    private void print(){
        
        final List<String> tronconIds = new ArrayList<>();
        for(final Element element : tronconsTable.getSelectedItems()){
            tronconIds.add(element.getId());
        }
        final List<String> typeDesordresIds = new ArrayList<>();
        for(final Element element : disordreTypesTable.getSelectedItems()){
            typeDesordresIds.add(element.getId());
        }
        final List<Desordre> desordres = Injector.getSession().getRepositoryForClass(Desordre.class).getAll();
        desordres.removeIf(
                (Desordre desordre) -> 
                        !tronconIds.contains(desordre.getForeignParentId())
                        || !typeDesordresIds.contains(desordre.getTypeDesordreId())
        );
        
        new Thread(() -> {
            try {
                Injector.getSession().getPrintManager().printDesordres(desordres);
            } catch (Exception ex) {
                Logger.getLogger(FXDisorderPrintFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }).run();
    }
    
    private class TronconChoicePojoTable extends PojoTable {
        
        ObservableList<Element> getSelectedItems(){
            return uiTable.getSelectionModel().getSelectedItems();
        }

        public TronconChoicePojoTable() {
            super(TronconDigue.class, "Tronçons");
            uiTable.getColumns().remove(editCol);
            editableProperty.set(false);
        }
    }
    
    private class DisorderTypeChoicePojoTable extends PojoTable {
        
        ObservableList<Element> getSelectedItems(){
            return uiTable.getSelectionModel().getSelectedItems();
        }

        public DisorderTypeChoicePojoTable() {
            super(RefTypeDesordre.class, "Types de désordres");
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
