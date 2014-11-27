
package fr.sirs.query;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.model.Element;
import fr.sirs.index.ElasticSearchEngine;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.util.SirsTableCell;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXSearchPane extends BorderPane {

    @FXML private TableView<ElementHit> uiTable;
    @FXML private TextField uiSearchField;
    
    private final Session session;

    public FXSearchPane() {
        SIRS.loadFXML(this);
        session = Injector.getSession();
        
        
        final TableColumn editCol = new PojoTable.EditColumn(PojoTable::editElement);
        
        final TableColumn<ElementHit,String> typeCol = new TableColumn<>();
        typeCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ElementHit, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ElementHit, String> param) {
                String str = param.getValue().geteElementClassName();
                String[] split = str.split("\\.");
                str = split[split.length-1];
                return new SimpleObjectProperty(str);
            }
        });
        
        final TableColumn<ElementHit,Object> libelleCol = new TableColumn<>();
        libelleCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ElementHit, Object>, ObservableValue<Object>>() {
            @Override
            public ObservableValue<Object> call(TableColumn.CellDataFeatures<ElementHit, Object> param) {
                return new SimpleObjectProperty(param.getValue());
            }
        });
        libelleCol.setCellFactory(new Callback<TableColumn<ElementHit, Object>, TableCell<ElementHit, Object>>() {
            @Override
            public TableCell<ElementHit, Object> call(TableColumn<ElementHit, Object> param) {
                return new SirsTableCell<>();
            }
        });
        
        uiTable.getColumns().add(editCol);
        uiTable.getColumns().add(typeCol);
        uiTable.getColumns().add(libelleCol);
    }

    @FXML
    private void search(ActionEvent event) {
        
        final ElasticSearchEngine engine = Injector.getElasticEngine();        
        final Client client = engine.getClient();
        final QueryBuilder qb = QueryBuilders.queryString(uiSearchField.getText());
        
        final SearchResponse response = client.prepareSearch("sirs")
                .setQuery(qb)
                .setSize(10000)
                .addFields("@class","libelle")
                .execute()
                .actionGet();
        
        
        final List<ElementHit> results = new ArrayList<>();
        
        final SearchHits hits = response.getHits();
        final Iterator<SearchHit> ite = hits.iterator();
        while(ite.hasNext()){
            final SearchHit hit = ite.next();     
            results.add(new ElementHit(hit));
        }
        
        uiTable.setItems(FXCollections.observableList(results));
        
    }

    
}
