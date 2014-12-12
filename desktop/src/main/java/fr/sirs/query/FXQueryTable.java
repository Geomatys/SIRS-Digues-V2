
package fr.sirs.query;

import fr.sirs.SIRS;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.logging.Level;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import org.geotoolkit.gui.javafx.util.ButtonTableCell;
import org.geotoolkit.gui.javafx.util.FXTableView;
import org.geotoolkit.gui.javafx.util.FXUtilities;
import org.geotoolkit.internal.GeotkFX;

/**
 *
 * @author Johann Sorel
 */
public class FXQueryTable extends BorderPane{

    private final TableView<SQLQuery> table = new FXTableView<>();
    private final ScrollPane scroll = new ScrollPane(table);
    
    public FXQueryTable(List<SQLQuery> queries) {
        scroll.setFitToHeight(true);
        scroll.setFitToWidth(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.columnResizePolicyProperty().set(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefSize(200, 400);
        FXUtilities.hideTableColumn(table);
        setLeft(scroll);
        
        table.setItems(FXCollections.observableList(queries));
        
        final TableColumn<SQLQuery,String> nameCol = new TableColumn<>();
        nameCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SQLQuery, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<SQLQuery, String> param) {
                return param.getValue().name;
            }
        });
        
        table.getColumns().add(nameCol);
        table.getColumns().add(new DeleteColumn());
        
        table.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<SQLQuery>() {

            @Override
            public void onChanged(ListChangeListener.Change<? extends SQLQuery> c) {
                final SQLQuery sqlq = table.getSelectionModel().getSelectedItem();
                if(sqlq !=null){
                    setCenter(new FXQueryPane(sqlq));
                }else{
                    setCenter(null);
                }
            }
        });
        
    }
    
    public void save(){
        try {
            SQLQueries.saveQueries(table.getItems());
        } catch (IOException ex) {
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    final SQLQuery getSelection() {
        return table.getSelectionModel().getSelectedItem();
    }
    
    public class DeleteColumn extends TableColumn{

        public DeleteColumn() {
            super();            
            setSortable(false);
            setResizable(false);
            setPrefWidth(24);
            setMinWidth(24);
            setMaxWidth(24);
            setGraphic(new ImageView(GeotkFX.ICON_DELETE));
            
            setCellValueFactory(new Callback<TableColumn.CellDataFeatures, ObservableValue>() {
                @Override
                public ObservableValue call(TableColumn.CellDataFeatures param) {
                    return new SimpleObjectProperty<>(param.getValue());
                }
            });
            setCellFactory(new Callback<TableColumn, TableCell>() {

                public TableCell call(TableColumn param) {
                    return new ButtonTableCell(
                            false,new ImageView(GeotkFX.ICON_DELETE), (Object t) -> true, new Function() {
                                @Override
                                public Object apply(Object t) {
                                    final ButtonType res = new Alert(Alert.AlertType.CONFIRMATION,"Confirmer la suppression ?",
                                            ButtonType.NO, ButtonType.YES).showAndWait().get();
                                    if(ButtonType.YES == res){
                                        table.getItems().remove(t);
                                        save();
                                    }
                                    return null;
                                }
                            }); }
            });
        }  
    }
    
    
}
