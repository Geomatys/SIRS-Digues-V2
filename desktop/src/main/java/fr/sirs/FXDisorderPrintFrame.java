package fr.sirs;

import com.vividsolutions.jts.geom.Point;
import fr.sirs.core.LinearReferencingUtilities;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.RefTypeDesordre;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.theme.ui.PojoTable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.gui.javafx.util.FXNumberCell;
import org.geotoolkit.referencing.LinearReferencing;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXDisorderPrintFrame extends BorderPane {
    
    @FXML Tab uiTronconChoice;
    @FXML Tab uiDisorderTypeChoice;
    @FXML Tab uiOptionChoice;
    
    private final Map<String, ObjectProperty<Number>[]> prsByTronconId = new HashMap<>();
    private final TronconChoicePojoTable tronconsTable = new TronconChoicePojoTable();
    private final DisorderTypeChoicePojoTable disordreTypesTable = new DisorderTypeChoicePojoTable();
    
    public FXDisorderPrintFrame(){
        SIRS.loadFXML(this, FXDisorderPrintFrame.class);
        tronconsTable.setTableItems(()-> (ObservableList) FXCollections.observableList(Injector.getSession().getRepositoryForClass(TronconDigue.class).getAll()));
        uiTronconChoice.setContent(tronconsTable);
        disordreTypesTable.setTableItems(()-> (ObservableList) FXCollections.observableList(Injector.getSession().getRepositoryForClass(RefTypeDesordre.class).getAll()));
        uiDisorderTypeChoice.setContent(disordreTypesTable);
    }
    
    
    @FXML 
    private void print(){
        Injector.getSession().getTaskManager().submit("Génération de fiches détaillées de désordres",
        new Thread(() -> {
            
            final List<String> tronconIds = new ArrayList<>();
            for(final Element element : tronconsTable.getSelectedItems()){
                tronconIds.add(element.getId());
            }
            final List<String> typeDesordresIds = new ArrayList<>();
            for(final Element element : disordreTypesTable.getSelectedItems()){
                typeDesordresIds.add(element.getId());
            }
            final List<Desordre> desordres = Injector.getSession().getRepositoryForClass(Desordre.class).getAll();
            
            // On retire les désordred de la liste dans les cas suivants :
            desordres.removeIf(
                    (Desordre desordre) -> {
                        
                        System.out.println(desordre);
                        System.out.println(desordre.getForeignParentId());
                        System.out.println(prsByTronconId.keySet().size());
                        System.out.println(prsByTronconId.get(desordre.getForeignParentId()));
                        System.out.println(prsByTronconId.get(desordre.getForeignParentId())[0]);
                        System.out.println(prsByTronconId.get(desordre.getForeignParentId())[1]);
                        System.out.println(prsByTronconId.get(desordre.getForeignParentId())[0].get());
                        System.out.println(prsByTronconId.get(desordre.getForeignParentId())[1].get());
                        
                            final boolean remove = !tronconIds.contains(desordre.getForeignParentId()) // Si le tronçon ne figure pas parmi les tronçons sélectionnés.
                            || !typeDesordresIds.contains(desordre.getTypeDesordreId()) // Si le type de figure pas parmi les types de désordres sélectionnés.
                            || (desordre.getPrFin() < prsByTronconId.get(desordre.getForeignParentId())[0].get().floatValue()) // Si le désordre s'achève avant le début de la zone du tronçon que l'on souhaite.
                            || (desordre.getPrDebut() > prsByTronconId.get(desordre.getForeignParentId())[1].get().floatValue()); // Si le désordre débute après la fin de la zone du tronçon que l'on souhaite.
                    
                            
                        if(!remove)System.out.println(desordre);
                            return remove;
                    } 
            );
            
            try {
                Injector.getSession().getPrintManager().printDesordres(desordres);
            } catch (Exception ex) {
                Logger.getLogger(FXDisorderPrintFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }));
    }
    
    
    private class TronconChoicePojoTable extends PojoTable {
        
        public TronconChoicePojoTable() {
            super(TronconDigue.class, "Tronçons");
            getColumns().remove(editCol);
            editableProperty.set(false);
            getColumns().add(new SelectPRColumn("PR début", true));
            getColumns().add(new SelectPRColumn("PR fin", false));
//            TableView table = getTable();
//            ObservableList selectedIndices = table.getSelectionModel().getSelectedIndices();
//            table.getColumns().get(0).
        }
    }
    
    private class DisorderTypeChoicePojoTable extends PojoTable {
        
        public DisorderTypeChoicePojoTable() {
            super(RefTypeDesordre.class, "Types de désordres");
            getColumns().remove(editCol);
            editableProperty.set(false);
        }
    }
    
    private class SelectPRColumn extends TableColumn {
        
        
        
        
        public SelectPRColumn(final String text, final boolean begin){
            super(text);
            
//            prProperty.addListener(new ChangeListener<Number>() {
//
//                @Override
//                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                    System.out.println("nouvelle valeur de pr : "+newValue);
//                }
//            });
            
            setCellFactory(new Callback<TableColumn<TronconDigue, Number>, TableCell<TronconDigue, Number>>() {

                @Override
                public TableCell<TronconDigue, Number> call(TableColumn<TronconDigue, Number> param) {
                    return new FXNumberCell(Float.class);
                }
            });
            setCellValueFactory(new Callback<TableColumn.CellDataFeatures<TronconDigue, Number>, ObservableValue<Number>>() {

                
                @Override
                public ObservableValue<Number> call(CellDataFeatures<TronconDigue, Number> param) {
                    
                    if(param!=null && param.getValue()!=null){
                        final TronconDigue troncon = param.getValue();
                        if(troncon.getSystemeRepDefautId()!=null && troncon.getGeometry()!=null){
                            final int index = begin ? 0 : 1;
                            final ObjectProperty<Number> prProperty;
                            if(prsByTronconId.get(troncon.getId())==null) prsByTronconId.put(troncon.getId(), new ObjectProperty[2]);
                            if(prsByTronconId.get(troncon.getId())[index]==null){
                                prProperty = new SimpleObjectProperty<>();
                                final SystemeReperage sr = Injector.getSession().getRepositoryForClass(SystemeReperage.class).get(troncon.getSystemeRepDefautId());
                                final LinearReferencing.SegmentInfo[] tronconSegments = LinearReferencingUtilities.buildSegments(LinearReferencing.asLineString(troncon.getGeometry()));

                                final Point point;
                                if(begin) point = GO2Utilities.JTS_FACTORY.createPoint(tronconSegments[0].getPoint(0, 0));
                                else{
                                    final LinearReferencing.SegmentInfo lastSegment = tronconSegments[tronconSegments.length-1];
                                    point = GO2Utilities.JTS_FACTORY.createPoint(lastSegment.getPoint(lastSegment.length, 0));
                                }
                                prProperty.set(TronconUtils.computePR(tronconSegments, sr, point, Injector.getSession().getRepositoryForClass(BorneDigue.class)));
                                prsByTronconId.get(troncon.getId())[index] = prProperty;
                            }
                            else prProperty = prsByTronconId.get(troncon.getId())[index];
                            
                            return prProperty;
                        }
                    }
                    return null;
                }
            });
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
