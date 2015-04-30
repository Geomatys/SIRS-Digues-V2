
package fr.sirs.query;

import fr.sirs.index.ElementHit;
import fr.sirs.CorePlugin;
import fr.sirs.core.model.SQLQuery;
import fr.sirs.core.model.SQLQueries;
import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import org.geotoolkit.gui.javafx.util.TaskManager;
import fr.sirs.core.h2.H2Helper;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ReferenceType;
import fr.sirs.core.model.Role;
import fr.sirs.core.model.ValiditySummary;
import fr.sirs.index.ElasticSearchEngine;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.util.FXValiditySummaryToElementTableColumn;
import fr.sirs.util.PrinterUtilities;
import fr.sirs.util.SirsStringConverter;
import fr.sirs.util.SirsTableCell;
import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.sis.storage.DataStoreException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureStoreUtilities;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.db.FilterToSQL;
import org.geotoolkit.db.JDBCFeatureStore;
import org.geotoolkit.db.h2.H2FeatureStore;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.feature.type.DefaultName;
import org.geotoolkit.feature.type.FeatureType;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.gui.javafx.layer.FXFeatureTable;
import org.geotoolkit.gui.javafx.util.ButtonTableCell;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.style.MutableStyle;
import org.opengis.feature.PropertyType;
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
    public static final Image ICON_REFRESH = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_REFRESH,22,Color.WHITE),null);

    @FXML private Button uiSave;
    @FXML private Button uiOpen;
    @FXML private Button uiQueryManagement;
    @FXML private Button uiExportModel;
    @FXML private Button uiRefreshModel;
    @FXML private Button uiViewModel;
    @FXML private Button uiCarto;
    
    // 1- Recherche simple
    @FXML private ToggleButton uiToggleSimple;
    @FXML private ToggleGroup simpleRadio;
    @FXML private GridPane uiSimplePane;
    // a) Recherche texte
    @FXML private RadioButton uiRadioPlainText;
    @FXML private GridPane uiPlainTextPane;
    @FXML private TextField uiElasticKeywords;
    
    // b) Recherche par désignation
    @FXML private RadioButton uiRadioDesignation;
    @FXML private GridPane uiDesignationPane;
    @FXML private ComboBox<Class<? extends Element>> uiDesignationClass;
    @FXML private TextField uiDesignation;
    private TableView<ValiditySummary> designations;
    private List<ValiditySummary> validitySummaries;
    
    // 2- Recherche SQL
    @FXML private ToggleButton uiToggleSQL;
    @FXML private GridPane uiSQLPane;
    @FXML private GridPane uiSQLModelOptions;
    @FXML private GridPane uiSQLQueryOptions;
    @FXML private GridPane uiAdminOptions;
    @FXML private ComboBox<String> uiTableChoice;
    @FXML private BorderPane uiFilterPane;
    @FXML private TextArea uiSQLText;
    @FXML private Button uiExportQueries;
    private FXSQLFilterEditor uiFilterEditor;
    
    
    private final Session session;

    private H2FeatureStore h2Store;
    
    public FXSearchPane() {
        SIRS.loadFXML(this);
        session = Injector.getSession();
               
        uiFilterEditor = new FXSQLFilterEditor();
        uiFilterEditor.filterProperty.addListener(this::setSQLText);
        
        uiFilterPane.setCenter(uiFilterEditor);
        
        //affichage des panneaux coord/borne
        final ToggleGroup group = new ToggleGroup();
        uiToggleSimple.setToggleGroup(group);
        uiToggleSQL.setToggleGroup(group);
        
        final List<Class<? extends Element>> modelClasses = Session.getElements();
        modelClasses.removeIf(new Predicate<Class<? extends Element>>() {

            @Override
            public boolean test(Class<? extends Element> t) {
                return ReferenceType.class.isAssignableFrom(t);
            }
        });
        uiDesignationClass.setItems(FXCollections.observableArrayList(modelClasses));
        uiDesignationClass.setConverter(new SirsStringConverter());
        
        uiToggleSimple.setSelected(true);

        //h2 connection
        connectToH2Store();
        
        uiTableChoice.getSelectionModel().selectedItemProperty().addListener(
            (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if(newValue!=null){
                try {
                    FeatureType type = h2Store.getFeatureType(newValue);
                    uiFilterEditor.setType(type);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        
        uiSave.setGraphic(new ImageView(ICON_SAVE));
        uiOpen.setGraphic(new ImageView(ICON_OPEN));
        uiExportModel.setGraphic(new ImageView(ICON_EXPORT));
        uiRefreshModel.setGraphic(new ImageView(ICON_REFRESH));
        uiExportQueries.setGraphic(new ImageView(ICON_EXPORT));
        uiViewModel.setGraphic(new ImageView(ICON_MODEL));
        
        // VISIBILITY RULES
        uiSimplePane.visibleProperty().bind(uiToggleSimple.selectedProperty());
        uiSimplePane.managedProperty().bind(uiSimplePane.visibleProperty());
        simpleRadio.selectedToggleProperty().addListener(
            (ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
            setCenter(null);
        });
        
        uiSQLPane.visibleProperty().bind(uiToggleSQL.selectedProperty());
        uiSQLPane.managedProperty().bind(uiSQLPane.visibleProperty());
        
        uiSQLModelOptions.visibleProperty().bind(uiToggleSQL.selectedProperty());
        uiSQLModelOptions.managedProperty().bind(uiSQLModelOptions.visibleProperty());
        
        uiSQLQueryOptions.visibleProperty().bind(uiToggleSQL.selectedProperty());
        uiSQLQueryOptions.managedProperty().bind(uiSQLQueryOptions.visibleProperty());
        
        uiPlainTextPane.visibleProperty().bind(new BooleanBinding() {

            {super.bind(uiRadioPlainText.selectedProperty(), uiToggleSimple.selectedProperty());}
            
            @Override
            protected boolean computeValue() {
                return uiRadioPlainText.isSelected() && uiToggleSimple.isSelected();
            }
        });
        uiPlainTextPane.managedProperty().bind(uiPlainTextPane.visibleProperty());
        uiDesignationPane.visibleProperty().bind(new BooleanBinding() {

            {super.bind(uiRadioDesignation.selectedProperty(), uiToggleSimple.selectedProperty());}
            
            @Override
            protected boolean computeValue() {
                return uiRadioDesignation.isSelected() && uiToggleSimple.isSelected();
            }
        });
        uiDesignationPane.managedProperty().bind(uiDesignationPane.visibleProperty());
        
        final SimpleBooleanProperty isAdmin = new ReadOnlyBooleanWrapper(Role.ADMIN.equals(Injector.getSession().getRole()));
        uiAdminOptions.visibleProperty().bind(uiToggleSQL.selectedProperty().and(isAdmin));
        uiAdminOptions.managedProperty().bind(uiAdminOptions.visibleProperty());
        
        // TODO : change binding to make it visible if selected result is positionable or has geometry ?
        uiCarto.visibleProperty().bind(uiToggleSQL.selectedProperty());
        uiCarto.managedProperty().bind(uiCarto.visibleProperty());
        
        // TOOLTIPS
        uiExportModel.setTooltip(new Tooltip("Voir la structure de la base SQL."));
        uiExportModel.setTooltip(new Tooltip("Exporter l'intégralité de la base SQL."));
        uiOpen.setTooltip(new Tooltip("Choisir une requête SQL parmi celles stockées dans le système."));
        uiSave.setTooltip(new Tooltip("Enregistrer la requête actuelle dans le système local."));
        uiSave.setTooltip(new Tooltip("Ajouter / supprimer des requêtes en base de données."));
        
        uiCarto.setTooltip(new Tooltip("Afficher le résultat de la requête sur la carte."));
        
        // Action on admin button
        uiQueryManagement.setOnAction((ActionEvent e)-> FXAdminQueryPane.showAndWait());
    }

    @FXML
    private void viewDBModel(ActionEvent event) {
        
        final Stage stage = new Stage();
        
        final WebView webView = new WebView();
        final String url = this.getClass().getResource("/fr/sirs/model.html").toExternalForm();  
        webView.getEngine().load(url);
        webView.setOnScroll(new EventHandler<ScrollEvent>() {

            @Override
            public void handle(ScrollEvent event) {
                final double zoom = webView.getZoom();
                if(event.getDeltaY()>0)
                    webView.setZoom(zoom * 1.1);
                else if(event.getDeltaY()<0)
                    webView.setZoom(zoom * .9);
            }
        });
        
        stage.setScene(new Scene(webView));
        stage.setTitle("Modèle");
        stage.setWidth(800);
        stage.setHeight(600);
        stage.show();
    }
    
    @FXML
    private void refreshModel(ActionEvent event) {
        uiRefreshModel.setDisable(true);
        Task t = H2Helper.init();
        t.setOnSucceeded(e -> {
            connectToH2Store().setOnSucceeded(e2 -> Platform.runLater(() -> uiRefreshModel.setDisable(false)));
        
        });
        t.setOnFailed(e -> Platform.runLater(() -> uiRefreshModel.setDisable(false)));
    }
    
    private Task connectToH2Store() {
        //h2 connection
        Task<ObservableList> h2Names = TaskManager.INSTANCE.submit("Connexion à la base de données", ()-> {
            h2Store = (H2FeatureStore) H2Helper.getStore(session.getConnector());
            
            final Set<Name> names = h2Store.getNames();
            final ObservableList observableNames = FXCollections.observableArrayList();
            for(Name n : names) observableNames.add(n.getLocalPart());
            Collections.sort(observableNames);
            
            SIRS.LOGGER.fine("RDBMS CONNEXION FINISHED");
            return observableNames;
        });
        
        h2Names.setOnSucceeded((WorkerStateEvent e) -> Platform.runLater(()-> uiTableChoice.setItems(h2Names.getValue())));
        return h2Names;
    }
    
    @FXML
    private void saveSQLQuery(ActionEvent event){
        final Dialog dialog = new Dialog();
        final DialogPane pane = new DialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK,ButtonType.CANCEL);        
        final SQLQuery query = new SQLQuery();
        query.sql.set(uiSQLText.getText());
        pane.setContent(new FXQueryPane(query));
        
        dialog.setDialogPane(pane);
        dialog.setTitle("Information sur la requête");
        dialog.getDialogPane().setHeader(null);
        final Optional name = dialog.showAndWait();
        if (name.isPresent() && ButtonType.OK.equals(name.get())) {
            try{
                final List<SQLQuery> queries = SQLQueries.getLocalQueries();
                queries.add(query);
                SQLQueries.saveQueriesLocally(queries);
            }catch(IOException ex){
                SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
    
    @FXML
    private void openSQLQuery(ActionEvent event){
        final List<SQLQuery> queries;
        try {
            queries = SQLQueries.getLocalQueries();
            queries.addAll(Injector.getSession().getSqlQueryRepository().getAll());
        } catch (IOException ex) {
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            GeotkFX.newExceptionDialog("Une erreur s'est produite pendant la création de la liste des requêtes.", ex).show();
            return;
        }
        
        if (queries.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Aucune requête disponible.", ButtonType.OK).showAndWait();
        } else {
            final Dialog dia = new Dialog();
            final FXQueryTable table = new FXQueryTable(queries);
            final DialogPane pane = new DialogPane();
            pane.setPrefSize(700, 400);
            pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            pane.setContent(table);
            dia.setDialogPane(pane);
            dia.setTitle("Liste des requêtes");

            final Optional res = dia.showAndWait();
            if (res.isPresent() && ButtonType.OK.equals(res.get())) {
                //sauvegarde s'il y a eu des changements
                table.save();
                final SQLQuery selected = table.getSelection();
                if (selected != null) {
                    uiSQLText.setText(selected.sql.get());
                }
            }
        }
    }
            
    @FXML
    private void exportSQLQueries(ActionEvent event) {
        // Load available queries
        final List<SQLQuery> queries;
        try {
            queries = SQLQueries.getLocalQueries();
            queries.addAll(Injector.getSession().getSqlQueryRepository().getAll());
        } catch (IOException ex) {
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            GeotkFX.newExceptionDialog("Une erreur s'est produite pendant la création de la liste des requêtes.", ex).show();
            return;
        }
        if (queries.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Aucune requête disponible.", ButtonType.OK).showAndWait();
        } else {
            final Dialog dia = new Dialog();
            final FXQueryTable table = new FXQueryTable(queries);
            table.table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            final DialogPane pane = new DialogPane();
            pane.setPrefSize(700, 400);
            pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            pane.setContent(table);
            dia.setDialogPane(pane);
            dia.setTitle("Liste des requêtes");

            final Optional res = dia.showAndWait();
            if (res.isPresent() && ButtonType.OK.equals(res.get())) {
                // Save system local if some queries has been deleted.
                table.save();
                final List<SQLQuery> selected = table.table.getSelectionModel().getSelectedItems();
                if (selected == null || selected.isEmpty()) {
                    new Alert(Alert.AlertType.INFORMATION, "Aucune requête sélectionnée pour l'export.", ButtonType.OK).show();
                } else {
                    FileChooser chooser = new FileChooser();
                    chooser.setTitle("Fichier d'export");
                    chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Fichier de propriétés Java", ".properties"));
                    File outputFile = chooser.showSaveDialog(null);
                    if (outputFile != null) {
                        try {
                            SQLQueries.saveQueriesInFile(selected, outputFile.toPath());
                        } catch (IOException ex) {
                            SIRS.LOGGER.log(Level.WARNING, "Impossible de sauvegarder les requêtes sélectionnées.", ex);
                            GeotkFX.newExceptionDialog("Impossible de sauvegarder les requêtes sélectionnées.", ex).show();
                        }
                    }
                }
            }
        }
    }
    
    @FXML
    private void exportModel(ActionEvent event) {
        
        final DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Dossier d'export");
        final File file = chooser.showDialog(null);

        if (file != null) {
            TaskManager.INSTANCE.submit("Export vers un fichier SQL", () -> {
                try (Connection cnx = h2Store.getDataSource().getConnection()) {
                    H2Helper.dumbSchema(cnx, file.toPath());
                }
                return null;
            });
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
            context.items().add(querygroup);
        }
        
        querygroup.items().add(layer);
        session.getFrame().getMapTab().show();
    }
    
    private void setSQLText(final ObservableValue observable, Object oldValue, Object newValue) {
        try {
            uiSQLText.setText(buildSQLQueryFromFilter());
        } catch (DataStoreException ex) {
            SIRS.LOGGER.log(Level.WARNING, "Impossible de construire une requête SQL depuis le panneau de filtres.", ex);
            GeotkFX.newExceptionDialog("Impossible de construire une requête SQL depuis le panneau de filtres.", ex).show();
        }
    }
    
    private String buildSQLQueryFromFilter() throws DataStoreException {
        final String tableName = uiTableChoice.getValue();
        if (tableName == null) {
            return null;
        }
        final Filter filter = uiFilterEditor.toFilter();

        final FeatureType ft = h2Store.getFeatureType(tableName);
        final FilterToSQL filterToSQL = new SirsFilterToSQL(ft);
        final StringBuilder sb = new StringBuilder();
        filter.accept(filterToSQL, sb);
        final String condition = sb.toString();

        return "SELECT * FROM \"" + tableName + "\" WHERE " + condition;
    }
    
    private String getCurrentSQLQuery() throws DataStoreException {
        return uiSQLText.getText().trim();
    }
    
    @FXML
    private void search(ActionEvent event) {
        
        try{
            if(uiToggleSimple.isSelected()){
                if(uiRadioDesignation.isSelected()){
                    searchDesignation();
                } else if (uiRadioPlainText.isSelected()){
                    searchText();
                }
                
            } else if(uiToggleSQL.isSelected()) {
                if(h2Store==null) {
                    new Alert(Alert.AlertType.INFORMATION, "Veuillez attendre que la connexion à la base de donnée SQL soit établie.", ButtonType.OK).show();
                } else {
                    final String query = getCurrentSQLQuery();
                    searchSQL(query);
                }
            } 
        } catch(Exception ex) {
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            
            // Si une erreur s'est produite pendant la requête, on propose un panneau
            // dépliable pour informer l'utilisateur.
            Label errorLabel = new Label("Une erreur est survenue. Assurez-vous que la syntaxe de votre requête est correcte.");            
            errorLabel.setBackground(Background.EMPTY);
                      
            final StringWriter errorStack = new StringWriter();            
            ex.printStackTrace(new PrintWriter(errorStack));
            final TextArea stackArea = new TextArea(errorStack.toString());            
            final TitledPane errorPane = new TitledPane("Trace de l'erreur :", stackArea);
            errorPane.setBackground(Background.EMPTY);
            errorPane.setBorder(Border.EMPTY);
            
            final Accordion accordion = new Accordion();  
            accordion.getPanes().add(errorPane);
            VBox vBox = new VBox(errorLabel, accordion);
            vBox.setSpacing(10);
            vBox.setPadding(new Insets(10));
            setCenter(vBox);
        }
    }
    
    private void searchText(){
        final TableColumn editCol = new PojoTable.EditColumn(PojoTable::editElement);

        final TableColumn<ElementHit,String> typeCol = new TableColumn<>();
        typeCol.setCellValueFactory((TableColumn.CellDataFeatures<ElementHit, String> param) -> {
                String str = param.getValue().geteElementClassName();
                String[] split = str.split("\\.");
                str = split[split.length-1];
                return new SimpleObjectProperty(str);
            });

        final TableColumn<ElementHit,Object> libelleCol = new TableColumn<>();
        libelleCol.setCellValueFactory((TableColumn.CellDataFeatures<ElementHit, Object> param) -> {
                return new SimpleObjectProperty(param.getValue());
            });
        libelleCol.setCellFactory((TableColumn<ElementHit, Object> param) -> {
                return new SirsTableCell<>();
            });

        final TableView<ElementHit> uiTable = new TableView<>();
        uiTable.setPlaceholder(new Label(""));
        uiTable.getColumns().add(editCol);
        uiTable.getColumns().add(typeCol);
        uiTable.getColumns().add(libelleCol);


        final ElasticSearchEngine engine = Injector.getElasticSearchEngine();
        final QueryBuilder qb = QueryBuilders.queryString(uiElasticKeywords.getText());

        final SearchResponse response = engine.search(qb);
        final SearchHits hits = response.getHits();

        final List<ElementHit> results = new ArrayList<>();
        final Iterator<SearchHit> ite = hits.iterator();
        while(ite.hasNext()){
            final SearchHit hit = ite.next();
            results.add(new ElementHit(hit));
        }

        uiTable.setItems(FXCollections.observableList(results));

        final ScrollPane scroll = new ScrollPane(uiTable);
        scroll.setFitToHeight(true);
        scroll.setFitToWidth(true);
        setCenter(scroll);
    }
    
    private void searchDesignation(){
        final ResourceBundle bundle = ResourceBundle.getBundle(ValiditySummary.class.getName());

        validitySummaries = session.getValiditySummaryRepository().getDesignationsForClass(uiDesignationClass.getValue());
        validitySummaries.removeIf((ValiditySummary t) -> {
                return (uiDesignation.getText()==null || "".equals(uiDesignation.getText())) ?
                        (t.getDesignation()!=null || !"".equals(t.getDesignation())) : 
                        !uiDesignation.getText().equals(t.getDesignation());
            });

        designations = new TableView<>(FXCollections.observableArrayList(validitySummaries));
        designations.setEditable(false);

        designations.getColumns().add(new FXValiditySummaryToElementTableColumn());

        final TableColumn<ValiditySummary, String> propertyColumn = new TableColumn<>(bundle.getString("pseudoId"));
        propertyColumn.setCellValueFactory((TableColumn.CellDataFeatures<ValiditySummary, String> param) -> {
                return new SimpleObjectProperty<>(param.getValue().getDesignation());
            });
        designations.getColumns().add(propertyColumn);
        
        final TableColumn<ValiditySummary, String> labelColumn = new TableColumn<>(bundle.getString("label"));
        labelColumn.setCellValueFactory((TableColumn.CellDataFeatures<ValiditySummary, String> param) -> {
                return new SimpleObjectProperty(param.getValue().getLabel());
            });
        designations.getColumns().add(labelColumn);
        setCenter(designations);
    }

    private void searchSQL(String query){
        final FeatureMapLayer layer = searchSQLLayer(query);
        if (layer == null || layer.getCollection().isEmpty()) {
            setCenter(new Label("Pas de résultat pour votre recherche."));
        } else {
            final CustomizedFeatureTable table = new CustomizedFeatureTable("fr.sirs.core.model.");
            table.setLoadAll(true);
            table.init(layer);
            setCenter(table);
        }
    }
    
    private FeatureMapLayer searchSQLLayer(String query){
        if(!query.toLowerCase().startsWith("select")){
            final Alert alert = new Alert(Alert.AlertType.WARNING,"Uniquement les requêtes SELECT sont possibles.",ButtonType.CLOSE);
            alert.showAndWait();
            return null;
        }
        
        final Query fsquery = org.geotoolkit.data.query.QueryBuilder.language(
                JDBCFeatureStore.CUSTOM_SQL, query, new DefaultName("requete"));
        final FeatureCollection col = h2Store.createSession(false).getFeatureCollection(fsquery);
        final FeatureMapLayer layer = MapBuilder.createFeatureLayer(col, getStyleForType(col.getFeatureType()));
        layer.setName(query);
        return layer;
    }
    
    private static MutableStyle getStyleForType(final FeatureType fType) {
        PropertyType geomType;
        try {
            geomType = (fType.getProperty("geometry") != null)? fType.getProperty("geometry") : null;
        } catch (IllegalArgumentException e) {
            geomType = null;
        }
        
        return CorePlugin.createStructureStyle(Color.GRAY, (geomType == null)? null : geomType.getName().toString());
    } 
    
    private static class CustomizedFeatureTable extends FXFeatureTable {
        
        FeatureCollection features;
        
        CustomizedFeatureTable(final String path){
            super(path);
        }
        
        @Override
        public boolean init(Object candidate){
            final  boolean result = super.init(candidate);
            if(result){
            table.getColumns().add(0, new PrintFeatureColumn());
            features = ((FeatureMapLayer) layer).getCollection();
            }
            return result;
        }
        
        private class PrintFeatureColumn extends TableColumn<Feature, Feature>{
            PrintFeatureColumn(){
                Button printAll = new Button("Imprimer", new ImageView(SIRS.ICON_PRINT));
                printAll.setOnAction((ActionEvent event) -> {
                   Injector.getSession().prepareToPrint(features);
                });
                setGraphic(printAll);
                setPrefWidth(50);
                
                setCellValueFactory((CellDataFeatures<Feature, Feature> param) -> {
                    return new SimpleObjectProperty<>(param.getValue());
                });
                
                setCellFactory(new Callback<TableColumn<Feature, Feature>, TableCell<Feature, Feature>>() {

                    @Override
                    public TableCell<Feature, Feature> call(TableColumn<Feature, Feature> param) {
                        return new ButtonTableCell<>(false, new ImageView(SIRS.ICON_PRINT_BLACK), (Feature t)-> {return true;}, 
                            (Feature t) -> {
                                Injector.getSession().prepareToPrint(t);
                                return t;
                            });
                    }
                });
            }
        }
    }
}
