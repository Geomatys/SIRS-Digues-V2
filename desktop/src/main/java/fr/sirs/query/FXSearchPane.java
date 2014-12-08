
package fr.sirs.query;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.index.ElasticSearchEngine;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.util.SirsTableCell;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
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

    @FXML private BorderPane uiTopPane;
    @FXML private TableView<ElementHit> uiTable;
    
    //Recherche plein texte
    @FXML private ToggleButton uiTogglePlainText;
    @FXML private GridPane uiPlainTextPane;
    @FXML private TextField uiElasticKeywords;
    
    //recherche simple
    @FXML private ToggleButton uiToggleSimple;
    @FXML private GridPane uiSimplePane;
    @FXML private ChoiceBox<?> uiDBTable;
    @FXML private BorderPane uiFilterPane;
    private FXSQLFilterEditor uiFilterEditor;
    
    //recherche SQL
    @FXML private ToggleButton uiToggleSQL;
    @FXML private GridPane uiSQLPane;
    @FXML private TextArea uiSQL;
    
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
        
        uiFilterEditor = new FXSQLFilterEditor();
        uiFilterPane.setCenter(uiFilterEditor);
        
        uiTopPane.setCenter(null);
        
        //affichage des panneaux coord/borne
        final ToggleGroup group = new ToggleGroup();
        uiTogglePlainText.setToggleGroup(group);
        uiToggleSimple.setToggleGroup(group);
        uiToggleSQL.setToggleGroup(group);
        group.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
            if(newValue==null){
                group.selectToggle(uiTogglePlainText);
            }
            if(newValue==uiTogglePlainText){
                uiTopPane.setCenter(uiPlainTextPane);
            }else if(newValue==uiToggleSimple){
                uiTopPane.setCenter(uiSimplePane);
            }else if(newValue==uiToggleSQL){
                uiTopPane.setCenter(uiSQLPane);
            }else{
                uiTopPane.setCenter(null);
            }
        });
        
        uiTogglePlainText.setSelected(true);

    }

    @FXML
    private void viewDBModel(ActionEvent event) {
        
    }
        
    @FXML
    private void search(ActionEvent event) {
        
        final ElasticSearchEngine engine = Injector.getElasticEngine();        
        final Client client = engine.getClient();
        final QueryBuilder qb = QueryBuilders.queryString(uiElasticKeywords.getText());
        
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
