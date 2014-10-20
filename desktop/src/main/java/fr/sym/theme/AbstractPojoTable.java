
package fr.sym.theme;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class AbstractPojoTable extends BorderPane{
    
    protected final TableView uiTable = new TableView();
    protected final ScrollPane uiScroll = new ScrollPane(uiTable);
    protected final Class pojoClass;

    public AbstractPojoTable(Class pojoClass) {
        this.pojoClass = pojoClass;
        
        uiScroll.setFitToHeight(true);
        uiScroll.setFitToWidth(true);
        uiTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        uiTable.setEditable(true);
        
        setCenter(uiScroll);
        
        //TODO build columns
        uiTable.getColumns().add(new PropertyColumn());
        
    }
        
    public static class PropertyColumn extends TableColumn<Object,Object>{

        public PropertyColumn() {
            super("nom");
            setCellValueFactory(new Callback<CellDataFeatures<Object, Object>, ObservableValue<Object>>() {
                @Override
                public ObservableValue<Object> call(CellDataFeatures<Object, Object> param) {
                    return new SimpleObjectProperty<>(param.getValue().toString());
                }
            });
        }
        
    }
    
}
