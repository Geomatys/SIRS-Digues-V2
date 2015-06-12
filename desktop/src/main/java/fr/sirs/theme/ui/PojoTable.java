package fr.sirs.theme.ui;

import org.geotoolkit.gui.javafx.util.FXNumberCell;
import org.geotoolkit.gui.javafx.util.FXStringCell;
import org.geotoolkit.gui.javafx.util.FXLocalDateTimeCell;
import org.geotoolkit.gui.javafx.util.FXBooleanCell;
import com.sun.javafx.property.PropertyReference;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import fr.sirs.CorePlugin;
import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import fr.sirs.StructBeanSupplier;
import static fr.sirs.SIRS.AUTHOR_FIELD;
import static fr.sirs.SIRS.BUNDLE_KEY_CLASS_ABREGE;
import static fr.sirs.SIRS.COMMENTAIRE_FIELD;
import static fr.sirs.SIRS.DESIGNATION_FIELD;
import static fr.sirs.SIRS.FOREIGN_PARENT_ID_FIELD;
import static fr.sirs.SIRS.PR_DEBUT_FIELD;
import static fr.sirs.SIRS.PR_FIN_FIELD;
import static fr.sirs.SIRS.VALID_FIELD;
import fr.sirs.core.Repository;
import fr.sirs.core.SirsCore;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.Crete;
import org.geotoolkit.gui.javafx.util.TaskManager;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.LigneEau;
import fr.sirs.core.model.PointZ;
import fr.sirs.core.model.PointDZ;
import fr.sirs.core.model.PointXYZ;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.PrZPointImporter;
import fr.sirs.core.model.ProfilLong;
import fr.sirs.core.model.Role;
import fr.sirs.core.model.Preview;
import fr.sirs.map.ExportMenu;
import fr.sirs.map.ExportTask;
import fr.sirs.util.SirsStringConverter;
import fr.sirs.util.SirsTableCell;
import fr.sirs.util.property.Reference;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import jidefx.scene.control.field.NumberField;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.geotoolkit.data.FeatureStoreFinder;
import org.geotoolkit.data.FileFeatureStoreFactory;
import org.geotoolkit.data.bean.BeanFeatureSupplier;
import org.geotoolkit.data.bean.BeanStore;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.gui.javafx.util.ButtonTableCell;
import org.geotoolkit.gui.javafx.util.FXEnumTableCell;
import org.geotoolkit.gui.javafx.util.FXTableView;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapBuilder;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @author Alexis Manin (Geomatys)
 * @author Samuel Andrés (Geomatys)
 */
public class PojoTable extends BorderPane {
    
    private static final String BUTTON_STYLE = "buttonbar-button";
    
    private static final String[] COLUMNS_TO_IGNORE = new String[] {AUTHOR_FIELD, VALID_FIELD, FOREIGN_PARENT_ID_FIELD};    
    private static final String[] COLUMNS_TO_PRIORIZE = new String[] {DESIGNATION_FIELD, PR_DEBUT_FIELD, PR_FIN_FIELD};
    
    protected final Class pojoClass;
    protected final AbstractSIRSRepository repo;
    protected final Session session = Injector.getBean(Session.class);
    protected final TableView<Element> uiTable = new FXTableView<>();
    private final LabelMapper labelMapper;
    
    // Editabilité du tableau (possibilité d'ajout et de suppression des éléments
    protected final BooleanProperty editableProperty = new SimpleBooleanProperty(true);
    // Editabilité des cellules tableau (possibilité d'ajout et de suppression des éléments
    protected final BooleanProperty cellEditableProperty = new SimpleBooleanProperty();
    // Parcours fiche par fiche
    protected final BooleanProperty fichableProperty = new SimpleBooleanProperty(true);
    // Accès à la fiche détaillée d'un élément particulier
    protected final BooleanProperty detaillableProperty = new SimpleBooleanProperty(true);
    // Possibilité de faire une recherche sur le contenu de la table
    protected final BooleanProperty searchableProperty = new SimpleBooleanProperty(true);
    // Ouvrir l'editeur sur creation d'un nouvel objet
    protected final BooleanProperty openEditorOnNewProperty = new SimpleBooleanProperty(true);
    // Créer un nouvel objet à l'ajout
    protected final BooleanProperty createNewProperty = new SimpleBooleanProperty(true);
    /* Importer des points. Default : false */
    protected final BooleanProperty importPointProperty = new SimpleBooleanProperty(false);
    
        
    // Icônes de la barre d'action
    
    // Barre de droite : manipulation du tableau et passage en mode parcours de fiche
    protected final ToggleButton uiFicheMode = new ToggleButton(null, new ImageView(SIRS.ICON_FILE_WHITE));
    protected final ImageView searchNone = new ImageView(SIRS.ICON_SEARCH_WHITE);
    protected final Button uiSearch = new Button(null, searchNone);;
    protected final Button uiAdd = new Button(null, new ImageView(SIRS.ICON_ADD_WHITE));
    protected final Button uiDelete = new Button(null, new ImageView(SIRS.ICON_TRASH_WHITE));
    protected final Button uiImport = new Button(null, new ImageView(SIRS.ICON_IMPORT_WHITE));
    protected final Button uiExport = new Button(null, new ImageView(SIRS.ICON_EXPORT_WHITE));
    protected final HBox searchEditionToolbar = new HBox(uiFicheMode, uiImport, uiExport, uiSearch, uiAdd, uiDelete);
    
    // Barre de gauche : navigation dans le parcours de fiches
    protected FXElementPane elementPane = null;
    private final Button uiPrevious = new Button("",new ImageView(SIRS.ICON_CARET_LEFT));
    private final Button uiNext = new Button("",new ImageView(SIRS.ICON_CARET_RIGHT));
    private final Button uiCurrent = new Button();
    protected final HBox navigationToolbar = new HBox(uiPrevious, uiCurrent, uiNext);    
    
    protected final ProgressIndicator searchRunning = new ProgressIndicator();
    protected ObservableList<Element> allValues;
    protected ObservableList<Element> filteredValues;
    
    protected final StringProperty currentSearch = new SimpleStringProperty("");
    protected final BorderPane topPane;
    
    /** The element to set as parent for any created element using {@linkplain #createPojo() }. */
    protected final ObjectProperty<Element> parentElementProperty = new SimpleObjectProperty<>();
    /** The element to set as owner for any created element using {@linkplain #createPojo() }. 
     On the contrary to the parent, the owner purpose is not to contain the created pojo, but to reference it.*/
    protected final ObjectProperty<Element> ownerElementProperty = new SimpleObjectProperty<>();
    
    /** Task object designed for asynchronous update of the elements contained in the table. */
    protected Task tableUpdater;
    
    public PojoTable(final Class pojoClass, final String title) {
        this(pojoClass, title, null);
    }
    
    public PojoTable(final AbstractSIRSRepository repo, final String title) {
        this(repo.getModelClass(), title, repo);
    }
    
    private PojoTable(final Class pojoClass, final String title, final AbstractSIRSRepository repo) {
        if (pojoClass == null && repo == null) {
            throw new IllegalArgumentException("Pojo class to expose and Repository parameter are both null. At least one of them must be valid.");
        }
        if (pojoClass == null) {
            this.pojoClass = repo.getModelClass();
        } else {
            this.pojoClass = pojoClass;
        }
        getStylesheets().add(SIRS.CSS_PATH);
        this.labelMapper = new LabelMapper(this.pojoClass);
        if (repo == null) {
            AbstractSIRSRepository tmpRepo;
            try {
                tmpRepo = session.getRepositoryForClass(pojoClass);
            } catch (IllegalArgumentException e) {
                SIRS.LOGGER.log(Level.FINE, e.getMessage());
                tmpRepo = null;
            }
            this.repo = tmpRepo;
        } else {
            this.repo = repo;
        }
        
        searchRunning.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        searchRunning.setPrefSize(22, 22);
        searchRunning.setStyle("-fx-progress-color: white;");
        
        uiTable.setRowFactory((TableView<Element> param) ->  new TableRow<Element>() {

            @Override
            protected void updateItem(Element item, boolean empty) {
                super.updateItem(item, empty);

                if(item!=null && !item.getValid()){
                    getStyleClass().add("invalidRow");
                }
                else{
                    getStyleClass().removeAll("invalidRow");
                }
            }
        });
        uiTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        uiTable.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Element> observable, Element oldValue, Element newValue) -> {
            session.prepareToPrint(uiTable.getSelectionModel().getSelectedItems());
        });
                
        // Colonnes de suppression et d'ouverture d'éditeur.
        final DeleteColumn deleteColumn = new DeleteColumn();
        final EditColumn editCol = new EditColumn(this::editPojo);
                
        /* We cannot bind visible properties of those columns, because TableView 
         * will set their value when user will request to hide them.
         */
        editableProperty.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            deleteColumn.setVisible(newValue);
            //editCol.setVisible(newValue && detaillableProperty.get());
        });
        cellEditableProperty.bind(editableProperty);
        detaillableProperty.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            editCol.setVisible(newValue && detaillableProperty.get());
        });
        
        uiTable.getColumns().add(deleteColumn);
        uiTable.getColumns().add((TableColumn)editCol);
        
        try {
            //contruction des colonnes editable
            final HashMap<String, PropertyDescriptor> properties = SIRS.listSimpleProperties(this.pojoClass);
            
            // On enlève les propriétés inutiles pour l'utilisateur
            for (final String key : COLUMNS_TO_IGNORE) {
                properties.remove(key);
            }
            
            // Ensuite on ajoute les champs à prioriser
            for (final String toPriorize : COLUMNS_TO_PRIORIZE) {
                getPropertyColumn(properties.remove(toPriorize))
                        .ifPresent(column -> uiTable.getColumns().add(column));                
            }
            
            // On donne toutes les informations de position.
            if (Positionable.class.isAssignableFrom(this.pojoClass)) {
                final Set<String> positionableKeys = SIRS.listSimpleProperties(Positionable.class).keySet();
                final ArrayList<TableColumn> positionColumns = new ArrayList<>();
                for (final String key : positionableKeys) {
                    getPropertyColumn(properties.remove(key)).ifPresent(column -> {
                        uiTable.getColumns().add(column);
                        positionColumns.add(column);
                    });
                }
                
                // On permet de cacher toutes les infos de position d'un coup.
                final ImageView viewOn = new ImageView(SIRS.ICON_COMPASS_WHITE);
                final ToggleButton uiPositionVisibility = new ToggleButton(null, viewOn);
                uiPositionVisibility.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    if (newValue == null) return;
                    for (final TableColumn col : positionColumns) {
                        col.setVisible(newValue);
                    }
                    if (newValue) {
                        uiPositionVisibility.setTooltip(new Tooltip("Cacher les informations de position"));
                    } else {
                        uiPositionVisibility.setTooltip(new Tooltip("Afficher les informations de position"));                        
                    }
                });
                uiPositionVisibility.visibleProperty().bind(uiFicheMode.selectedProperty().not());
                uiPositionVisibility.managedProperty().bind(uiPositionVisibility.visibleProperty());
                uiPositionVisibility.getStyleClass().add(BUTTON_STYLE);
                uiPositionVisibility.setSelected(true);
                searchEditionToolbar.getChildren().add(uiPositionVisibility);
            }

            for (final PropertyDescriptor desc : properties.values()) {
                getPropertyColumn(desc)
                        .ifPresent(column -> uiTable.getColumns().add(column));  
            }
        } catch (IntrospectionException ex) {
            SIRS.LOGGER.log(Level.WARNING, "property columns cannot be created.", ex);
        }
        
        uiTable.editableProperty().bind(editableProperty);
        
        /* barre d'outils. Si on a un accesseur sur la base, on affiche des
         * boutons de création / suppression.
         */
        uiSearch.textProperty().bind(currentSearch);
        uiSearch.getStyleClass().add(BUTTON_STYLE);
        uiSearch.setOnAction((ActionEvent event) -> {search();});
        uiSearch.getStyleClass().add("label-header");
        uiSearch.setTooltip(new Tooltip("Rechercher un terme dans la table"));
        uiSearch.disableProperty().bind(searchableProperty.not());
        
        final Label uiTitle = new Label(title==null? labelMapper.mapClassName() : title);
        uiTitle.getStyleClass().add("pojotable-header");
        uiTitle.setAlignment(Pos.CENTER);
        
        searchEditionToolbar.getStyleClass().add("buttonbar");
                
        uiAdd.getStyleClass().add(BUTTON_STYLE);
        uiAdd.setOnAction((ActionEvent event) -> {
            final Element p;
            if(createNewProperty.get()){
                p = createPojo();
                if(this.repo!=null){
                    this.repo.add(p);
                }
                if (p != null && openEditorOnNewProperty.get()) {
                    editPojo(p);
                }
            }
            else{
                final ChoiceStage stage = new ChoiceStage();
                stage.showAndWait();
                p = stage.getRetrievedElement().get();
            }
        });
        uiAdd.disableProperty().bind(editableProperty.not());

        uiDelete.getStyleClass().add(BUTTON_STYLE);
        uiDelete.setOnAction((ActionEvent event) -> {
            final Element[] elements = ((List<Element>) uiTable.getSelectionModel().getSelectedItems()).toArray(new Element[0]);
            if (elements.length > 0) {
                final Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Confirmer la suppression ?",
                        ButtonType.NO, ButtonType.YES);
                alert.setResizable(true);
                final Optional<ButtonType> res = alert.showAndWait();
                if (res.isPresent() && ButtonType.YES.equals(res.get())) {
                        deletePojos(elements);
                }
            } else {
                final Alert alert = new Alert(Alert.AlertType.INFORMATION, "Aucune entrée sélectionnée. Pas de suppression possible.");
                alert.setResizable(true);
                alert.showAndWait();
            }
        });
        uiDelete.disableProperty().bind(editableProperty.not());
        
        topPane = new BorderPane(uiTitle,null,searchEditionToolbar,null,null);
        setTop(topPane);
        uiTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        uiTable.setMaxWidth(Double.MAX_VALUE);
        uiTable.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        uiTable.setPlaceholder(new Label(""));
        uiTable.setTableMenuButtonVisible(true);
        // Load all elements only if the user gave us the repository.
        if (repo != null) {
            setTableItems(()-> FXCollections.observableList(repo.getAll()));
        }
        
        final FXCommentPhotoView commentPhotoView = new FXCommentPhotoView();
        commentPhotoView.valueProperty().bind(uiTable.getSelectionModel().selectedItemProperty());
                
        final SplitPane sPane = new SplitPane();
        sPane.setOrientation(Orientation.VERTICAL);
        sPane.getItems().addAll(uiTable, commentPhotoView);
        sPane.setDividerPositions(0.9);
        setCenter(sPane);
        
        //
        // NAVIGATION FICHE PAR FICHE
        //
        navigationToolbar.getStyleClass().add("buttonbarleft");
        
        uiCurrent.setFont(Font.font(16));
        uiCurrent.getStyleClass().add(BUTTON_STYLE); 
        uiCurrent.setAlignment(Pos.CENTER);
        uiCurrent.setTextFill(Color.WHITE);
        uiCurrent.setOnAction(this::goTo);
        
        uiPrevious.getStyleClass().add(BUTTON_STYLE); 
        uiPrevious.setTooltip(new Tooltip("Fiche précédente."));
        uiPrevious.setOnAction((ActionEvent event) -> {
            uiTable.getSelectionModel().selectPrevious();
        });        

        uiNext.getStyleClass().add(BUTTON_STYLE); 
        uiNext.setTooltip(new Tooltip("Fiche suivante."));
        uiNext.setOnAction((ActionEvent event) -> {
            uiTable.getSelectionModel().selectNext();
        });
        navigationToolbar.visibleProperty().bind(uiFicheMode.selectedProperty());

        uiFicheMode.getStyleClass().add(BUTTON_STYLE); 
        uiFicheMode.setTooltip(new Tooltip("Passer en mode de parcours des fiches."));
        
        // Update counter when we change selected element.
        final ChangeListener<Number> selectedIndexListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            uiCurrent.setText(""+(newValue.intValue()+1) + " / " + uiTable.getItems().size());
        };
        uiFicheMode.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    // If there's no selection, initialize on first element.
                    if (uiTable.getSelectionModel().getSelectedIndex() < 0) {
                        uiTable.getSelectionModel().select(0);
                    }
                    
                    // Prepare editor and bind its content to selection model. 
                    if (elementPane == null) {
                        elementPane = SIRS.generateEditionPane(uiTable.getSelectionModel().getSelectedItem());
                    }
                    elementPane.elementProperty().bind(uiTable.getSelectionModel().selectedItemProperty());
                    
                    uiFicheMode.setTooltip(new Tooltip("Passer en mode de tableau synoptique."));
                    
                    uiCurrent.setText("" + (uiTable.getSelectionModel().getSelectedIndex()+1) + " / " + uiTable.getItems().size());
                    uiTable.getSelectionModel().selectedIndexProperty().addListener(selectedIndexListener);
                    setCenter((Node) elementPane);
                    
                } else {
                    // Deconnect editor.
                    if (elementPane != null) {
                        elementPane.elementProperty().unbind();
                    }
                    
                    // Update display
                    uiTable.getSelectionModel().selectedIndexProperty().removeListener(selectedIndexListener);
                    setCenter(uiTable);
                    
                    uiFicheMode.setTooltip(new Tooltip("Passer en mode de parcours des fiches."));
                }
            }
        });
        uiFicheMode.disableProperty().bind(fichableProperty.not());
        
        uiImport.getStyleClass().add(BUTTON_STYLE);
        uiImport.disableProperty().bind(editableProperty.not());
        uiImport.visibleProperty().bind(importPointProperty);
        uiImport.managedProperty().bind(importPointProperty);
        uiImport.setTooltip(new Tooltip("Importer des points"));
        uiImport.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                final FXAbstractImportPointLeve importCoord;
                if(PointXYZ.class.isAssignableFrom(pojoClass)) importCoord = new FXImportXYZ(PojoTable.this);
                else importCoord = new FXImportDZ(PojoTable.this);
                
                final Dialog dialog = new Dialog();
                final DialogPane pane = new DialogPane();
                pane.getButtonTypes().add(ButtonType.CLOSE);
                pane.setContent(importCoord);
                dialog.setDialogPane(pane);
                dialog.setResizable(true);
                dialog.setTitle("Import de points");
                dialog.setOnCloseRequest((Event event1) -> {dialog.hide();});
                dialog.show();
            }
        });

        uiExport.getStyleClass().add(BUTTON_STYLE);
        uiExport.setTooltip(new Tooltip("Sauvegarder en CSV"));
        uiExport.disableProperty().bind(Bindings.isNull(uiTable.getSelectionModel().selectedItemProperty()));
        uiExport.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                final DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle(GeotkFX.getString(org.geotoolkit.gui.javafx.contexttree.menu.ExportItem.class, "folder"));
                final File folder = chooser.showDialog(null);

                if(folder!=null){
                    try{
                        final BeanFeatureSupplier sup = new StructBeanSupplier(pojoClass, () -> new ArrayList(uiTable.getSelectionModel().getSelectedItems()));
                        final BeanStore store = new BeanStore(sup);
                        final FeatureMapLayer layer = MapBuilder.createFeatureLayer(store.createSession(false)
                                .getFeatureCollection(QueryBuilder.all(store.getNames().iterator().next())));
                        layer.setName(store.getNames().iterator().next().getLocalPart());

                        FileFeatureStoreFactory factory = (FileFeatureStoreFactory) FeatureStoreFinder.getFactoryById("csv");
                        TaskManager.INSTANCE.submit(new ExportTask(layer, folder, factory));
                    } catch (Exception ex) {
                        Dialog d = new Alert(Alert.AlertType.ERROR, "Impossible de créer le fichier CSV", ButtonType.OK);
                        d.setResizable(true);
                        d.showAndWait();
                        throw new UnsupportedOperationException("Failed to create csv store : " + ex.getMessage(), ex);
                    }
                }
            }
        });

        
        if(PointZ.class.isAssignableFrom(pojoClass)){
            uiTable.getColumns().add(new DistanceComputedPropertyColumn());
        }
        
        topPane.setLeft(navigationToolbar);
    }
    
    /**
     * Définit l'élément en paramètre comme parent de tout élément créé via cette table.
     * 
     * Note : Ineffectif dans le cas où les éléments de la PojoTable sont créés 
     * et listés directement depuis un repository couchDB, ou que l'élément créé  
     * est déjà un CouchDB document. 
     * @param parentElement L'élément qui doit devenir le parent de tout objet créé via 
     * la PojoTable.
     */
    public void setParentElement(final Element parentElement) {
        parentElementProperty.set(parentElement);
    }
    
    /**
     * 
     * @return L'élément à affecter en tant que parent de tout élément créé via 
     * cette table. Peut être nul.
     */
    public Element getParentElement() {
        return parentElementProperty.get();
    }
        
    /**
     * 
     * @return La propriété contenant l'élément à affecter en tant que parent de
     *  tout élément créé via cette table. Jamais nulle, mais peut-être vide.
     */
    public ObjectProperty<Element> parentElementProperty() {
        return parentElementProperty;
    }
    
    /**
     * Définit l'élément en paramètre comme principal référent de tout élément créé via cette table.
     * 
     * @param parentElement L'élément qui doit devenir le principal référent de tout objet créé via 
     * la PojoTable.
     */
    public void setOwnerElement(final Element parentElement) {
        ownerElementProperty.set(parentElement);
    }
    
    /**
     * 
     * @return L'élément principal référent de tout élément créé via 
     * cette table. Peut être nul.
     */
    public Element getOwnerElement() {
        return ownerElementProperty.get();
    }
        
    /**
     * 
     * @return La propriété contenant l'élément à affecter en tant que principal référent de
     *  tout élément créé via cette table. Jamais nulle, mais peut-être vide.
     */
    public ObjectProperty<Element> ownerElementProperty() {
        return ownerElementProperty;
    }
    
    protected ObservableList<Element> getAllValues(){return allValues;}

    public BooleanProperty editableProperty(){
        return editableProperty;
    }
    public BooleanProperty cellEditableProperty(){
        return cellEditableProperty;
    }
    public BooleanProperty detaillableProperty(){
        return detaillableProperty;
    }
    public BooleanProperty fichableProperty(){
        return fichableProperty;
    }
    public BooleanProperty searchableProperty(){
        return searchableProperty;
    }
    public BooleanProperty openEditorOnNewProperty() {
        return openEditorOnNewProperty;
    }
    public BooleanProperty createNewProperty() {
        return createNewProperty;
    }
    public BooleanProperty importPointProperty() {
        return importPointProperty;
    }
    
    /**
     * Called when user click on the search icon. Prepare the popup with the textfield
     * to type research into. 
     */
    protected void search(){
        if(uiSearch.getGraphic()!= searchNone){
            //une recherche est deja en cours
            return;
        }
        
        final Popup popup = new Popup();
        final TextField textField = new TextField(currentSearch.get());
        popup.getContent().add(textField);
        popup.setAutoHide(true);
        textField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                currentSearch.set(textField.getText());
                popup.hide();
                setTableItems(() -> allValues);
            }
        });
        final Point2D sc = uiSearch.localToScreen(0, 0);
        popup.show(uiSearch, sc.getX(), sc.getY());
        
    }
    
    protected void goTo(ActionEvent event){
        final Popup popup = new Popup();
        NumberField indexEditor = new NumberField(NumberField.NumberType.Integer);
        
        popup.getContent().add(indexEditor);
        popup.setAutoHide(false);

        indexEditor.setOnAction((ActionEvent event1) -> {
            final Number indexToSelect = indexEditor.valueProperty().get();
            if (indexToSelect != null) {
                final int index = indexToSelect.intValue();
                if (index >= 0 && index < uiTable.getItems().size()) {
                    uiTable.getSelectionModel().select(index);
                }
            }
            popup.hide();
        });
        final Point2D sc = uiCurrent.localToScreen(0, 0);
        popup.show(uiSearch, sc.getX(), sc.getY());
    }
    
    /**
     * @return {@link TableView} used for element display.
     */
    public TableView getUiTable() {
        return uiTable;
    }
    
    /**
     * Start an asynchronous task which will update table content with the elements
     * provided by input supplier.
     * @param producer Data provider.
     */
    public void setTableItems(Supplier<ObservableList<Element>> producer) {        
        if (tableUpdater != null && !tableUpdater.isDone()) {
            tableUpdater.cancel();
        }
        
        tableUpdater = new TaskManager.MockTask("Recherche...", () -> {

            allValues = producer.get();

            final Thread currentThread = Thread.currentThread();
            if (currentThread.isInterrupted()) {
                return;
            }
            final String str = currentSearch.get();
            if (str == null || str.isEmpty() || allValues == null || allValues.isEmpty()) {
                filteredValues = allValues;
            } else {
                final Set<String> result = new HashSet<>();
                SearchResponse search = Injector.getElasticSearchEngine().search(QueryBuilders.queryString(str));
                Iterator<SearchHit> iterator = search.getHits().iterator();
                while (iterator.hasNext() && !currentThread.isInterrupted()) {
                    result.add(iterator.next().getId());
                }

                if (currentThread.isInterrupted()) {
                    return;
                }
                filteredValues = allValues.filtered((Element t) -> {
                    return result.contains(t.getDocumentId());
                });
            }
        });
        
        tableUpdater.stateProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if (Worker.State.SUCCEEDED.equals(newValue)) {
                    Platform.runLater(() -> {
                        uiTable.setItems(filteredValues);
                        uiSearch.setGraphic(searchNone);
                    });
                } else if (Worker.State.FAILED.equals(newValue) || Worker.State.CANCELLED.equals(newValue)) {
                    Platform.runLater(() -> {
                        uiSearch.setGraphic(searchNone);
                    });
                } else if (Worker.State.RUNNING.equals(newValue)) {
                    Platform.runLater(() -> uiSearch.setGraphic(searchRunning));
                }
            }
        });
        tableUpdater = TaskManager.INSTANCE.submit("Recherche...", tableUpdater);
    }
    
    /**
     * Check if the input element can be deleted by current user. If not, an 
     * alert is displyed on screen.
     * @param pojo The element we want to delete.
     * @return True if we can delete the element in parameter, false otherwise.
     */
    protected boolean authoriseElementDeletion(final Element pojo) {
        if (Boolean.TRUE.equals(session.needValidationProperty().get())) {
            if (session.getUtilisateur() == null || session.getUtilisateur().getId() == null || !session.getUtilisateur().getId().equals(pojo.getAuthor())
                    || pojo.getValid()) {
                final Alert alert = new Alert(Alert.AlertType.INFORMATION, "En tant qu'utilisateur externe, vous ne pouvez supprimer que des éléments invalidés dont vous êtes l'auteur.", ButtonType.OK);
                alert.setResizable(true);
                alert.showAndWait();
                return false;
            }
        }
        return true;
    }
    
    /**
     * Delete the elements given in parameter. They are suppressed from the table
     * list, and if a {@link Repository} exists for the current table, elements 
     * are also suppressed from database. If a parent element has been set using
     * {@linkplain #setParentElement(fr.sirs.core.model.Element) } method, we will 
     * try to remove them from the parent as well.
     * @param pojos The {@link Element}s to delete.
     */
    protected void deletePojos(Element... pojos) {
        ObservableList<Element> items = uiTable.getItems();
        for (Element pojo : pojos) {
            // Si l'utilisateur est un externe, il faut qu'il soit l'auteur de 
            // l'élément et que celui-ci soit invalide, sinon, on court-circuite
            // la suppression.
            if(!authoriseElementDeletion(pojo)) continue;
            deletor.accept(pojo);
            items.remove(pojo);
        }
    }
    
    // Default deletor
    private Consumer deletor = new Consumer<Element>() {

        @Override
        public void accept(Element pojo) {
            if (repo != null && createNewProperty.get()) {
                repo.remove(pojo);
            }

            if (parentElementProperty.get() != null) {
                parentElementProperty.get().removeChild(pojo);
            }else if(ownerElementProperty.get() != null){
                ownerElementProperty.get().removeChild(pojo);
            }
        }
    };
    
    // Change the default deletor
    public void setDeletor(final Consumer deletor){
        this.deletor = deletor;
    }
    
    /**
     * Try to find and display a form to edit input object.
     * @param pojo The object we want to edit.
     */
    protected void editPojo(Object pojo){
        editElement(pojo);
    }
    
    /**
     * A method called when an element displayed in the table has been modified 
     * in the table.
     * @param event The table event refering to the edition action.
     */
    protected void elementEdited(TableColumn.CellEditEvent<Element, Object> event){
        if(repo!=null){
            final Element obj = event.getRowValue();
            if(obj == null) return;
            repo.update(obj);
        }
    }
    
    
    /**
     * Create a new element and add it to table items. If the table {@link Repository}
     * is not null, we also add the element to the database. We also set its parent
     * if it's not a contained element and the table {@linkplain #parentElementProperty}
     * is set.
     * @return The newly created object. 
     */
    protected Element createPojo() {
        Object result = null;
        
        if (repo != null) {
            result = repo.create();
        } 
        else if (pojoClass != null) {
            try {
                result = session.getElementCreator().createElement(pojoClass);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        // TODO : check and set date début
        if (result instanceof Element) {
            final Element newlyCreated = (Element) result;
            
            /* Dans le cas où on a un parent, il n'est pas nécessaire de faire
            addChild(), car la liste des éléments de la table est directement 
            cette liste d'éléments enfants, sur laquelle on fait un add().*/
            if (parentElementProperty.get() != null) {
                // this should do nothing for new 
                newlyCreated.setParent(parentElementProperty.get());
            }
            
            /* Mais dans le cas où on a un référant principal, il faut faire un
            addChild(), car la liste des éléments de la table n'est pas une 
            liste d'éléments enfants. Le référant principal n'a qu'une liste 
            d'identifiants qu'il convient de mettre à jour avec addChild().*/
            else if(ownerElementProperty.get() != null){
                ownerElementProperty.get().addChild(newlyCreated);
            }
            
            uiTable.getItems().add(newlyCreated);
        } else {
            final Alert alert = new Alert(Alert.AlertType.INFORMATION, "Aucune entrée ne peut être créée.");
            alert.setResizable(true);
            alert.showAndWait();
        }
        return (Element) result;
    }
        
    public static void editElement(Object pojo) {
        try {
            Injector.getSession().showEditionTab(pojo);
        } catch (Exception ex) {
            Dialog d = new Alert(Alert.AlertType.ERROR, "Impossible d'afficher un éditeur", ButtonType.OK);
            d.setResizable(true);
            d.showAndWait();
            throw new UnsupportedOperationException("Failed to load panel : " + ex.getMessage(), ex);
        }
    }
    
    protected Optional<TableColumn> getPropertyColumn(final PropertyDescriptor desc) {
        if (desc != null) {
            final TableColumn col;
            if (desc.getReadMethod().getReturnType().isEnum()) {
                col = new EnumColumn(desc);
            } else {
                col = new PropertyColumn(desc);
                col.sortableProperty().bind(importPointProperty.not());
            }
            return Optional.of(col);
        }
        return Optional.empty();
    }
    
////////////////////////////////////////////////////////////////////////////////
//
// INTERNAL CLASSES
//
////////////////////////////////////////////////////////////////////////////////
    
    private class EnumColumn extends TableColumn<Element, Role>{
        private EnumColumn(PropertyDescriptor desc){
            super(labelMapper.mapPropertyName(desc.getDisplayName()));
            setEditable(false);
            setCellValueFactory(new PropertyValueFactory<>(desc.getName()));
            setCellFactory(new Callback<TableColumn<Element, Role>, TableCell<Element, Role>>() {

                @Override
                public TableCell<Element, Role> call(TableColumn<Element, Role> param) {
                    final TableCell<Element, Role> cell = new FXEnumTableCell<>(Role.class, new SirsStringConverter());
                    cell.setEditable(false);
                    return cell;
                }

            });     
            addEventHandler(TableColumn.editCommitEvent(), new EventHandler<CellEditEvent<Element, Object>>() {

                @Override
                public void handle(CellEditEvent<Element, Object> event) {
                    final Object rowElement = event.getRowValue();
                    new PropertyReference<>(rowElement.getClass(), desc.getName()).set(rowElement, event.getNewValue());
                    elementEdited(event);
                }
            });
        }
    }
    
    private static abstract class PointBinding<T extends PointZ> extends DoubleBinding {
        
        protected final T point;
        
        public PointBinding(final T point){
            this.point = point;
        }
    }
    
    private static class DXYZBinding extends PointBinding<PointXYZ> {
        
        protected final PointXYZ origine;
        
        public DXYZBinding(final PointXYZ point, final PointXYZ origine){
            super(point);
            this.origine = origine;
            super.bind(point.xProperty(), point.yProperty(), origine.xProperty(), origine.yProperty());
        }

        @Override
        protected double computeValue() {
            return Math.sqrt(Math.pow(point.getX() - origine.getX(), 2) + Math.pow(point.getY() - origine.getY(), 2));
        }
    }
    
    private static class PRXYZBinding extends PointBinding<PointXYZ> {
        
        protected final TronconUtils.PosInfo posInfo;
        
        public PRXYZBinding(final PointXYZ pointLeve, final Positionable positionable){
            super(pointLeve);
            posInfo = new TronconUtils.PosInfo(positionable, Injector.getSession());
            super.bind(pointLeve.xProperty(), pointLeve.yProperty());
        }

        @Override
        protected double computeValue() {
            if(posInfo.getTroncon()==null) throw new IllegalStateException("L'élément positionable doit être associé à un linéaire.");
            else if(posInfo.getTroncon().getSystemeRepDefautId()==null) throw new IllegalStateException("Le linéaire associé doit avoir un système de repérage par défaut.");
            else{
                final double result = (double) TronconUtils.computePR(
                        posInfo.getTronconSegments(false), 
                        Injector.getSession().getSystemeReperageRepository().get(posInfo.getTroncon().getSystemeRepDefautId()), 
                        new GeometryFactory().createPoint(new Coordinate(point.getX(),point.getY())), 
                        Injector.getSession().getBorneDigueRepository());
                return result;
            }
        }
    }
    
    
    private static class PRZBinding extends PointBinding<PointDZ> {
        
        protected final TronconUtils.PosInfo posInfo;
        protected final PrZPointImporter pointImporter;
        
        public PRZBinding(final PointDZ point, final PrZPointImporter pointImporter){
            super(point);
            if(pointImporter instanceof Positionable){
                this.pointImporter = pointImporter;
                posInfo = new TronconUtils.PosInfo((Positionable) pointImporter, Injector.getSession());
                super.bind(point.dProperty(), pointImporter.systemeRepDzIdProperty());
            } 
            else throw new UnsupportedOperationException(pointImporter.toString()+"("+pointImporter.getClass().getName()+") must be "+Positionable.class.getName());
        }

        @Override
        protected double computeValue() {
            
            final double result = TronconUtils.switchSRForPR(posInfo.getTronconSegments(false), 
                    point.getD(),
                    Injector.getSession().getSystemeReperageRepository().get(pointImporter.getSystemeRepDzId()),
                    Injector.getSession().getSystemeReperageRepository().get(posInfo.getTroncon().getSystemeRepDefautId()),
                    Injector.getSession().getBorneDigueRepository());
            return result;
        }
    }
        
    private static abstract class ComputedPropertyColumn extends TableColumn<Element, Double>{
        public ComputedPropertyColumn(){
            setCellFactory((TableColumn<Element, Double> param) -> new FXNumberCell(NumberField.NumberType.Normal));
            setEditable(false);
        }
    }
    
    private class DistanceComputedPropertyColumn extends ComputedPropertyColumn{
        
        private boolean titleSet = false;
        
        public DistanceComputedPropertyColumn(){
            super();
            setCellValueFactory(new Callback<CellDataFeatures<Element, Double>, ObservableValue<Double>>() {

                @Override
                public ObservableValue<Double> call(CellDataFeatures<Element, Double> param) {
                    
                    if(param.getValue() instanceof PointXYZ){
                        // Cas des XYZ de profils en long avec PR calculé
                        if(parentElementProperty().get() instanceof ProfilLong){
                            if(!titleSet){setText("PR calculé");titleSet=true;}
                            return new PRXYZBinding((PointXYZ) param.getValue(), (ProfilLong) parentElementProperty().get()).asObject();
                        } 
                        // Cas des XYZ de lignes d'eau avec PR calculé
                        else if(parentElementProperty().get() instanceof LigneEau){
                            if(!titleSet){setText("PR calculé");titleSet=true;}
                            return new PRXYZBinding((PointXYZ) param.getValue(), (LigneEau) parentElementProperty().get()).asObject();
                        } 
                        // Cas des XYZ de levés de profils en travers avec distance calculée
                        else {
                            final Element origine = getTableView().getItems().get(0);
                            if(origine instanceof PointXYZ){
                                if(!titleSet){setText("Distance calculée");titleSet=true;}
                                return new DXYZBinding((PointXYZ) param.getValue(), (PointXYZ) origine).asObject();
                            }
                            else return null;
                        }
                    } 
                    
                    // Das des PrZ de profils en long ou lignes d'eau avec PR saisi converti en PR calculé dans le SR par défaut
                    else if(param.getValue() instanceof PointDZ 
                            && parentElementProperty().get() instanceof PrZPointImporter){
                        if(!titleSet){setText("PR calculé");titleSet=true;}
                        return new PRZBinding((PointDZ) param.getValue(), (PrZPointImporter) parentElementProperty().get()).asObject();
                    }
                    else {
                        return null;
                    }
                }
            });
        }
    }
        
    public class PropertyColumn extends TableColumn<Element, Object>{
        
        public PropertyColumn(final PropertyDescriptor desc) {
            super(labelMapper.mapPropertyName(desc.getDisplayName()));
            
            final Reference ref = desc.getReadMethod().getAnnotation(Reference.class);
                        
            addEventHandler(TableColumn.editCommitEvent(), new EventHandler<CellEditEvent<Element, Object>>() {

                @Override
                public void handle(CellEditEvent<Element, Object> event) {
                    final Object rowElement = event.getRowValue();
                    new PropertyReference<>(rowElement.getClass(), desc.getName()).set(rowElement, event.getNewValue());
                    elementEdited(event);
                }
            });
            
            //choix de l'editeur en fonction du type de données          
            if (ref != null) {
                //reference vers un autre objet
                setEditable(false);
                setCellFactory((TableColumn<Element, Object> param) -> new SirsTableCell());            
                try {
                    final Method propertyAccessor = pojoClass.getMethod(desc.getName()+"Property");
                    setCellValueFactory((CellDataFeatures<Element, Object> param) -> {
                        try {
                            return (ObservableValue) propertyAccessor.invoke(param.getValue());
                        } catch (Exception ex) {
                            SirsCore.LOGGER.log(Level.WARNING, null, ex);
                            return null;
                        }
                    });
                } catch (Exception ex) {
                    setCellValueFactory(new PropertyValueFactory<>(desc.getName()));
                } 

            } else {
                setCellValueFactory(new PropertyValueFactory<>(desc.getName()));
                final Class type = desc.getReadMethod().getReturnType();
                boolean isEditable = true;
                if (Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type)) {
                    setCellFactory((TableColumn<Element, Object> param) -> new FXBooleanCell());
                } else if (String.class.isAssignableFrom(type)) {
                    setCellFactory((TableColumn<Element, Object> param) -> new FXStringCell());
                } else if (Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type)) {
                    setCellFactory((TableColumn<Element, Object> param) -> new FXNumberCell(NumberField.NumberType.Integer));
                } else if (Float.class.isAssignableFrom(type) || float.class.isAssignableFrom(type)) {
                    setCellFactory((TableColumn<Element, Object> param) -> new FXNumberCell.Float(NumberField.NumberType.Normal));
                } else if (Double.class.isAssignableFrom(type) || double.class.isAssignableFrom(type)) {
                    setCellFactory((TableColumn<Element, Object> param) -> new FXNumberCell(NumberField.NumberType.Normal));
                } else if (LocalDateTime.class.isAssignableFrom(type)) {
                    setCellFactory((TableColumn<Element, Object> param) -> new FXLocalDateTimeCell());
                }else {
                    isEditable = false;
                }
                
                setEditable(isEditable);
                if (isEditable) {
                    editableProperty().bind(cellEditableProperty);
                }
            }
        }  
    }

    /**
     * A column allowing to delete the {@link Element} of a row. Two modes possible :
     * - Concrete deletion, which remove the element from database
     * - unlink mode, which dereference element from current list and parent element.
     */
    public class DeleteColumn extends TableColumn<Element,Element>{
        
        public DeleteColumn() {
            super("Suppression");            
            setSortable(false);
            setResizable(false);
            setPrefWidth(24);
            setMinWidth(24);
            setMaxWidth(24);
            setGraphic(new ImageView(GeotkFX.ICON_DELETE));
            
            setCellValueFactory((TableColumn.CellDataFeatures<Element, Element> param) -> new SimpleObjectProperty<>(param.getValue()));
            setCellFactory((TableColumn<Element, Element> param) -> {
                final boolean realDelete = createNewProperty.get();
                return new ButtonTableCell<>(
                        false, realDelete? new ImageView(GeotkFX.ICON_DELETE) : new ImageView(GeotkFX.ICON_UNLINK), (Element t) -> true, (Element t) -> {
                            final Alert confirm;
                            if (realDelete) {
                                confirm = new Alert(Alert.AlertType.WARNING, "Vous allez supprimer DEFINITIVEMENT l'entrée de la base de données. Êtes-vous sûr ?", ButtonType.NO, ButtonType.YES);
                            } else {
                                confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer le lien ?", ButtonType.NO, ButtonType.YES);
                            }
                            confirm.setResizable(true);
                            final Optional<ButtonType> res = confirm.showAndWait();
                            if (res.isPresent() && ButtonType.YES.equals(res.get())) {
                                deletePojos(t);
                            }
                            return null;
                        });
            });
        }  
    }
    
    public static class EditColumn extends TableColumn {

        public EditColumn(Consumer editFct) {
            super("Edition");
            setSortable(false);
            setResizable(false);
            setPrefWidth(24);
            setMinWidth(24);
            setMaxWidth(24);
            setGraphic(new ImageView(SIRS.ICON_EDIT_BLACK));

            setCellValueFactory(new Callback<TableColumn.CellDataFeatures, ObservableValue>() {

                @Override
                public ObservableValue call(TableColumn.CellDataFeatures param) {
                    return new SimpleObjectProperty<>(param.getValue());
                }
            });

            setCellFactory(new Callback<TableColumn, TableCell>() {

                public TableCell call(TableColumn param) {
                    return new ButtonTableCell(
                            false, new ImageView(SIRS.ICON_EDIT_BLACK),
                            (Object t) -> true, (Object t) -> {
                                editFct.accept(t);
                                return t;
                            });
                }
            });
        }
    }
    
    
    /* Rechercher les objets dans un tronçon donné (sert uniquement si on ne 
    crée pas les objets à l'ajout, mais si on cherche des objets préexisants.
    Cette propriété sert alors à limiter la recherche à un tronçon donné (de 
    manière à ne relier entre eux que des "objets" du même tronçon.*/
    protected final StringProperty tronconSourceProperty = new SimpleStringProperty(null);
    
    public StringProperty tronconSourceProperty() {
        return tronconSourceProperty;
    }
    
    private class ChoiceStage extends Stage {
        
        private ObjectProperty<Element> retrievedElement = new SimpleObjectProperty<>();
        
        private ChoiceStage(){
            super();
            setTitle("Choix de l'élément");
            
            final ResourceBundle bundle = ResourceBundle.getBundle(pojoClass.getName(), Locale.getDefault(),
                    Thread.currentThread().getContextClassLoader());
            final String prefix = bundle.getString(BUNDLE_KEY_CLASS_ABREGE)+" : ";
            final ComboBox<Preview> comboBox;
            if(tronconSourceProperty.get()==null){
                comboBox = new ComboBox<Preview>(FXCollections.observableArrayList(Injector.getSession().getPreviews().getByClass(pojoClass)));
            }
            else{
                
                comboBox = new ComboBox<Preview>(FXCollections.observableArrayList(Injector.getSession().getPreviews().getByClass(pojoClass)).filtered(new Predicate<Preview>() {

                    @Override
                    public boolean test(Preview t) {
                        return tronconSourceProperty.get().equals(t.getDocId());
                    }
                }));
            }
            comboBox.setConverter(new StringConverter<Preview>() {

                @Override
                public String toString(Preview object) {
                    return prefix+object.getDesignation() + ((object.getLibelle()==null) ? "" : " - "+object.getLibelle());
                }

                @Override
                public Preview fromString(String string) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            });
            
            final Button cancel = new Button("Annuler");
            cancel.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    retrievedElement.set(null);
                    hide();
                }
            });
            final Button add = new Button("Ajouter");
            add.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    retrievedElement.set(addExistingPojo(comboBox.valueProperty().get()));
                    hide();
                }
            });
            final HBox hBox = new HBox(cancel, add);
            hBox.setAlignment(Pos.CENTER);
            hBox.setPadding(new Insets(20));
            
            final VBox vBox = new VBox(comboBox, hBox);
            vBox.setAlignment(Pos.CENTER);
            vBox.setPadding(new Insets(20));
            setScene(new Scene(vBox));
        }
        
        private ObjectProperty<Element> getRetrievedElement(){
            return retrievedElement;
        }
        
        protected Element addExistingPojo(final Preview summary) {
            Object result = null;
            if (repo != null) {
                result = repo.get(summary.getDocId());
            } 
//            else {
//                final Set<String> id = new HashSet<>();
//                id.add(summary.getElementId());
//                result = SIRS.getStructures(id, pojoClass).get(0);
//            }


            if (result!=null && result instanceof Element) {
                uiTable.getItems().add((Element) result);
            } else {
                final Alert alert = new Alert(Alert.AlertType.INFORMATION, "Aucune entrée ne peut être créée.");
                alert.setResizable(true);
                alert.showAndWait();
            }
            return (Element) result;
        }
    }
}
