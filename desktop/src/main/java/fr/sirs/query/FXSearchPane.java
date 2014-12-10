
package fr.sirs.query;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.h2.H2Helper;
import fr.sirs.index.ElasticSearchEngine;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.util.SirsTableCell;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.sis.storage.DataStoreException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import static org.elasticsearch.search.suggest.context.CategoryContextMapping.query;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureIterator;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.db.JDBCFeatureStore;
import org.geotoolkit.db.h2.H2FeatureStore;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.feature.type.DefaultName;
import org.geotoolkit.feature.type.FeatureType;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.feature.type.PropertyDescriptor;
import org.geotoolkit.gui.javafx.layer.FXFeatureTable;
import org.geotoolkit.gui.javafx.util.FXDialog;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.style.RandomStyleBuilder;

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
    @FXML private ChoiceBox<String> uiDBTable;
    @FXML private BorderPane uiFilterPane;
    private FXSQLFilterEditor uiFilterEditor;
    
    //recherche SQL
    @FXML private ToggleButton uiToggleSQL;
    @FXML private GridPane uiSQLPane;
    @FXML private TextArea uiSQL;
    
    private final Session session;

    private H2FeatureStore h2Connection;
    
    public FXSearchPane() {
        SIRS.loadFXML(this);
        session = Injector.getSession();
                
        uiTable.setPlaceholder(new Label(""));
        
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

        try {
            //h2 connection
            h2Connection = (H2FeatureStore) H2Helper.createStore(session.getConnector());
            
            final Set<Name> names = h2Connection.getNames();
            final ObservableList lst = FXCollections.observableArrayList();
            for(Name n : names) lst.add(n.getLocalPart());
            Collections.sort(lst);
            uiDBTable.setItems(lst);
            
        } catch (Exception ex) {
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
        
        
        uiDBTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(newValue!=null){
                    try {
                        FeatureType type = h2Connection.getFeatureType(newValue);
                        final ObservableList<String> choices = FXCollections.observableArrayList();
                        for(PropertyDescriptor desc : type.getDescriptors()){
                            final String propName = desc.getName().getLocalPart();
                            choices.add(propName);
                        }
                        Collections.sort(choices);
                        uiFilterEditor.setChoices(choices);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        
    }

    @FXML
    private void viewDBModel(ActionEvent event) {
        
        final Image image = new Image("/fr/sirs/diagram.png");
        final ImageView view = new ImageView(image);
        
        final Stage stage = new Stage();
        final ScrollPane scroll = new ScrollPane(view);
        final Scene scene = new Scene(scroll);
        
        stage.setScene(scene);
        stage.setTitle("Modèle");
        stage.show();
    }
        
    @FXML
    private void exportSQL(ActionEvent event) {
        
        DirectoryChooser chooser = new DirectoryChooser();
        File file = chooser.showDialog(null);
        
        if(file!=null){
            try{
                final Connection cnx = h2Connection.getDataSource().getConnection();
                H2Helper.dumbSchema(cnx, file.toPath());
                cnx.close();
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
        
    }
    
    @FXML
    private void search(ActionEvent event) {
        
        try{
            if(uiTogglePlainText.isSelected()){
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
            }else if(uiToggleSimple.isSelected()){
                if(h2Connection==null) return;
                
            }else if(uiToggleSQL.isSelected()){
                if(h2Connection==null) return;
                
                
                final String query = uiSQL.getText().trim();
                if(!query.toLowerCase().startsWith("select")){
                    final Alert alert = new Alert(Alert.AlertType.WARNING,"Uniquement les reqêtes SELECT sont possibles.",ButtonType.CLOSE);
                    alert.showAndWait();
                    return;
                }
                
                final Query fsquery = org.geotoolkit.data.query.QueryBuilder.language(
                        JDBCFeatureStore.CUSTOM_SQL, query, new DefaultName("search"));
                
                final FeatureCollection col = h2Connection.createSession(false).getFeatureCollection(fsquery);
                
                final FeatureMapLayer layer = MapBuilder.createFeatureLayer(col, RandomStyleBuilder.createDefaultRasterStyle());
                
                
                final FXFeatureTable table = new FXFeatureTable();
                table.setLoadAll(true);
                table.init(layer);
                setCenter(table);

            }
        }catch(Exception ex){
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
        
    }

    
}
