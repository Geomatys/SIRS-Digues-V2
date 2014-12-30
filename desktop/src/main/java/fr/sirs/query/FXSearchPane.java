
package fr.sirs.query;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.h2.H2Helper;
import fr.sirs.index.ElasticSearchEngine;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.util.SirsTableCell;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
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
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.sis.storage.DataStoreException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.db.FilterToSQL;
import org.geotoolkit.db.JDBCFeatureStore;
import org.geotoolkit.db.h2.H2FeatureStore;
import org.geotoolkit.feature.type.DefaultName;
import org.geotoolkit.feature.type.FeatureType;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.gui.javafx.layer.FXFeatureTable;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.style.RandomStyleBuilder;
import org.opengis.filter.Filter;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXSearchPane extends BorderPane {

    public static final Image ICON_SAVE    = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_SAVE_ALIAS,22,Color.WHITE),null);
    public static final Image ICON_OPEN    = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_FOLDER_OPEN,22,Color.WHITE),null);
    public static final Image ICON_EXPORT  = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_UPLOAD,22,Color.WHITE),null);
    public static final Image ICON_MODEL   = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_SHARE_ALT_SQUARE,22,Color.WHITE),null);
    public static final Image ICON_CARTO   = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_COMPASS,22,Color.WHITE),null);
    
    @FXML private BorderPane uiTopPane;
    @FXML private Button uiSave;
    @FXML private Button uiOpen;
    @FXML private Button uiExport;
    @FXML private Button uiViewModel;
    @FXML private Button uiCarto;
    
    //Recherche plein texte
    @FXML private ToggleButton uiTogglePlainText;
    @FXML private GridPane uiPlainTextPane;
    @FXML private TextField uiElasticKeywords;
    
    //recherche simple
    @FXML private ToggleButton uiToggleSimple;
    @FXML private GridPane uiSimplePane;
    @FXML private ChoiceBox<String> uiDBTable;
    @FXML private BorderPane uiFilterPane;
    @FXML private TextArea uiSimpleSQL;
    private FXSQLFilterEditor uiFilterEditor;
    
    //recherche SQL
    @FXML private ToggleButton uiToggleSQL;
    @FXML private GridPane uiSQLPane;
    @FXML private TextArea uiAdvSQL;
    
    private final Session session;

    private H2FeatureStore h2Store;
    
    public FXSearchPane() {
        SIRS.loadFXML(this);
        session = Injector.getSession();
               
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
            h2Store = (H2FeatureStore) H2Helper.getStore(session.getConnector());
            
            final Set<Name> names = h2Store.getNames();
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
                        FeatureType type = h2Store.getFeatureType(newValue);
                        uiFilterEditor.setType(type);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        
        uiSave.setGraphic(new ImageView(ICON_SAVE));
        uiOpen.setGraphic(new ImageView(ICON_OPEN));
        uiExport.setGraphic(new ImageView(ICON_EXPORT));
        uiViewModel.setGraphic(new ImageView(ICON_MODEL));
        
        uiViewModel.visibleProperty().bind(uiToggleSimple.selectedProperty().or(uiToggleSQL.selectedProperty()));
        uiCarto.visibleProperty().bind(uiToggleSimple.selectedProperty().or(uiToggleSQL.selectedProperty()));
        uiSave.visibleProperty().bind(uiToggleSQL.selectedProperty());
        uiOpen.visibleProperty().bind(uiToggleSQL.selectedProperty());
        uiExport.visibleProperty().bind(uiToggleSQL.selectedProperty());
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
        stage.setWidth(800);
        stage.setHeight(600);
        stage.show();
    }
        
    @FXML
    private void saveSQL(ActionEvent event){
        final Dialog dialog = new Dialog();
        final DialogPane pane = new DialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK,ButtonType.CANCEL);        
        final SQLQuery query = new SQLQuery();
        query.sql.set(uiAdvSQL.getText());
        pane.setContent(new FXQueryPane(query));
        
        dialog.setDialogPane(pane);
        dialog.setTitle("Information sur la requête");
        dialog.getDialogPane().setHeader(null);
        final Optional<String> name = dialog.showAndWait();
        if(name.isPresent()){
            try{
                final List<SQLQuery> queries = SQLQueries.getQueries();
                queries.add(query);
                SQLQueries.saveQueries(queries);
            }catch(IOException ex){
                SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
    
    @FXML
    private void openSQL(ActionEvent event){
        try{
            final List<SQLQuery> queries = SQLQueries.getQueries();
            if(queries.isEmpty()){
                new Alert(Alert.AlertType.INFORMATION,"Aucune requête disponible.",ButtonType.OK).showAndWait();
            }else{
                final Dialog dia = new Dialog();
                final FXQueryTable table = new FXQueryTable(queries);
                final DialogPane pane = new DialogPane();
                pane.setPrefSize(700, 400);
                pane.getButtonTypes().addAll(ButtonType.OK,ButtonType.CANCEL);
                pane.setContent(table);
                dia.setDialogPane(pane);
                dia.setTitle("Liste des requêtes");
                
                final Optional res = dia.showAndWait();
                //sauvegarde s'il y a eu des changements
                table.save();
                if(res.isPresent() && ButtonType.OK.equals(res.get())){
                    final SQLQuery selected = table.getSelection();
                    if(selected!=null){
                        uiAdvSQL.setText(selected.sql.get());
                    }
                }
            }
        }catch(IOException ex){
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
    }
            
    @FXML
    private void exportSQL(ActionEvent event) {
        
        final DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Dossier d'export");
        final File file = chooser.showDialog(null);
        
        if(file!=null){
            try{
                final Connection cnx = h2Store.getDataSource().getConnection();
                H2Helper.dumbSchema(cnx, file.toPath());
                cnx.close();
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }
    
    @FXML
    private void exportMap(ActionEvent event) throws DataStoreException {
        final String query = getCurrentSQLQuery();
        if(query==null) return;
        
        final FeatureMapLayer layer = searchSQLLayer(query);
        if(layer==null) return;
        
        final MapContext context = session.getMapContext();
        
        MapItem querygroup = null;
        for(MapItem item : context.items()){
            if("Requêtes".equalsIgnoreCase(item.getName())){
                querygroup = item;
            }
        }
        if(querygroup==null){
            querygroup = MapBuilder.createItem();
            querygroup.setName("Requêtes");
            context.items().add(0,querygroup);
        }
        
        querygroup.items().add(layer);
        session.getFrame().getMapTab().show();
    }
    
    private String getCurrentSQLQuery() throws DataStoreException{
        if(uiToggleSimple.isSelected()){
            final String tableName = uiDBTable.getValue();
            if(tableName==null) return null;
            final Filter filter = uiFilterEditor.toFilter();

            final FeatureType ft = h2Store.getFeatureType(tableName);
            final FilterToSQL filterToSQL = new SirsFilterToSQL(ft);
            final StringBuilder sb = new StringBuilder();
            filter.accept(filterToSQL, sb);
            final String condition = sb.toString();

            return "SELECT * FROM \""+tableName+"\" WHERE "+condition;

        }else if(uiToggleSQL.isSelected()){
            return uiAdvSQL.getText().trim();
        }
        return null;
    }
    
    @FXML
    private void search(ActionEvent event) {
        if(h2Store==null) return;
        
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

                final TableView<ElementHit> uiTable = new TableView<>();
                uiTable.setPlaceholder(new Label(""));
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
                
                final ScrollPane scroll = new ScrollPane(uiTable);
                scroll.setFitToHeight(true);
                setCenter(scroll);
                
            }else if(uiToggleSimple.isSelected()){                
                final String query = getCurrentSQLQuery();
                uiSimpleSQL.setText(query);
                searchSQL(query);
                
            }else if(uiToggleSQL.isSelected()){
                final String query = getCurrentSQLQuery();
                searchSQL(query);
            }
        }catch(Exception ex){
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
        
    }

    private void searchSQL(String query){
        final FeatureMapLayer layer = searchSQLLayer(query);
        if(layer==null) return;
        
        final FXFeatureTable table = new FXFeatureTable();
        table.setLoadAll(true);
        table.init(layer);
        setCenter(table);
    }
    
    private FeatureMapLayer searchSQLLayer(String query){
        if(!query.toLowerCase().startsWith("select")){
            final Alert alert = new Alert(Alert.AlertType.WARNING,"Uniquement les reqêtes SELECT sont possibles.",ButtonType.CLOSE);
            alert.showAndWait();
            return null;
        }
        
        final Query fsquery = org.geotoolkit.data.query.QueryBuilder.language(
                JDBCFeatureStore.CUSTOM_SQL, query, new DefaultName("requete"));
        final FeatureCollection col = h2Store.createSession(false).getFeatureCollection(fsquery);
        final FeatureMapLayer layer = MapBuilder.createFeatureLayer(col, RandomStyleBuilder.createDefaultVectorStyle(col.getFeatureType()));
        layer.setName(query);
        return layer;
    }
    
}
