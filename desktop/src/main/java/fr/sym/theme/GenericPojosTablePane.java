
package fr.sym.theme;

import fr.symadrem.sirs.core.Repository;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class GenericPojosTablePane extends BorderPane{
    
    private final TableView uiTable = new TableView();
    private final ScrollPane uiScroll = new ScrollPane(uiTable);
    private final Repository repository;

    public GenericPojosTablePane(Repository repository) {
        this.repository = repository;
        
        uiScroll.setFitToHeight(true);
        uiScroll.setFitToWidth(true);
        uiTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        uiTable.setEditable(true);
        
        setCenter(uiScroll);
        
        uiTable.setItems(FXCollections.observableList(repository.getAll()));
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
