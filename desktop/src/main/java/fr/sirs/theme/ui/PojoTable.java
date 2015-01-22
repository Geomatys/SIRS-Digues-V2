package fr.sirs.theme.ui;

import org.geotoolkit.gui.javafx.util.FXNumberCell;
import org.geotoolkit.gui.javafx.util.FXStringCell;
import org.geotoolkit.gui.javafx.util.FXLocalDateTimeCell;
import org.geotoolkit.gui.javafx.util.FXBooleanCell;
import com.sun.javafx.property.PropertyReference;
import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import fr.sirs.core.Repository;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.index.SearchEngine;
import fr.sirs.query.ElementHit;
import fr.sirs.util.FXFreeTab;
import fr.sirs.util.SirsTableCell;
import fr.sirs.util.property.Reference;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Popup;
import javafx.util.Callback;
import jidefx.scene.control.field.NumberField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.geotoolkit.gui.javafx.util.ButtonTableCell;
import org.geotoolkit.gui.javafx.util.FXPasswordStringCell;
import org.geotoolkit.gui.javafx.util.FXPasswordTableCell;
import org.geotoolkit.gui.javafx.util.FXTableView;
import org.geotoolkit.internal.GeotkFX;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class PojoTable extends BorderPane {
    
    protected final Class pojoClass;
    private final Repository repo;
    protected final Session session = Injector.getBean(Session.class);
    protected final TableView<Element> uiTable = new FXTableView<>();
    private final LabelMapper labelMapper;
    
    // Editabilité du tableau (possibilité d'ajout et de suppression des éléments
    protected final BooleanProperty editableProperty = new SimpleBooleanProperty(true);
    // Parcours fiche par fiche
    protected final BooleanProperty fichableProperty = new SimpleBooleanProperty(true);
    // Accès à la fiche détaillée d'un élément particulier
    protected final BooleanProperty detaillableProperty = new SimpleBooleanProperty(true);
    // Possibilité de faire une recherche sur le contenu de la table
    protected final BooleanProperty searchableProperty = new SimpleBooleanProperty(true);
    // Ouvrir l'editeur sur creation d'un nouvel objet
    protected final BooleanProperty openEditorOnNewProperty = new SimpleBooleanProperty(true);
        
    // Icônes de la barre d'action
    // Barre de droite : manipulation du tableau et passage en mode parcours de fiche
    private final ImageView searchNone = new ImageView(SIRS.ICON_SEARCH);
    private final Button uiSearch;
    protected final Button uiAdd = new Button(null, new ImageView(SIRS.ICON_ADD_WHITE));
    protected final Button uiDelete = new Button(null, new ImageView(SIRS.ICON_TRASH));
    protected final ImageView playIcon = new ImageView(SIRS.ICON_FILE);
    private final ImageView stopIcon = new ImageView(SIRS.ICON_TABLE);
    protected final ToggleButton uiFicheMode = new ToggleButton();
    protected final HBox searchEditionToolbar = new HBox();
    
    // Barre de gauche : navigation dans le parcours de fiches
    protected FXElementPane elementPane = null;
    private int currentFiche = 0;
    private final Button uiPrevious = new Button("",new ImageView(SIRS.ICON_CARET_LEFT));
    private final Button uiNext = new Button("",new ImageView(SIRS.ICON_CARET_RIGHT));
    private final Button uiCurrent = new Button();
    protected final HBox navigationToolbar = new HBox();
    
    
    private final ProgressIndicator searchRunning = new ProgressIndicator();
    private ObservableList<Element> allValues;
    private ObservableList<Element> filteredValues;
    
    private final StringProperty currentSearch = new SimpleStringProperty("");
    protected final BorderPane topPane;
    
    public PojoTable(final Class pojoClass, final String title) {
        this(pojoClass, title, null);
    }
    
    public PojoTable(final Repository repo, final String title) {
        this(repo.getModelClass(), title, repo);
    }
    
    private PojoTable(final Class pojoClass, final String title, final Repository repo) {
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
            Repository tmpRepo;
            try {
                tmpRepo = Injector.getSession().getRepositoryForClass(pojoClass);
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
        uiTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        // Colonnes de suppression et d'ouverture d'éditeur.
        final DeleteColumn deleteColumn = new DeleteColumn();
        final EditColumn editCol = new EditColumn(this::editPojo);
                
        /* We cannot bind visible properties of those columns, because TableView 
         * will set their value when user will request to hide them.
         */
        editableProperty.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            deleteColumn.setVisible(newValue);
            editCol.setVisible(newValue && detaillableProperty.get());
        });
        detaillableProperty.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            editCol.setVisible(newValue && detaillableProperty.get());
        });
        
        uiTable.getColumns().add(deleteColumn);
        uiTable.getColumns().add((TableColumn)editCol);
        
        //contruction des colonnes editable
        final List<PropertyDescriptor> properties = Session.listSimpleProperties(this.pojoClass);
        for(final PropertyDescriptor desc : properties) {
            final TableColumn col;
            // Colonne de mots de passe simplifiée : ne marche pas très bien.
            if("password".equals(desc.getDisplayName())) {
                col = new PasswordColumn();
            }
            else{
                col = new PropertyColumn(desc); 
            }
            uiTable.getColumns().add(col);
        }
        
        uiTable.editableProperty().bind(editableProperty);
        
        /* barre d'outils. Si on a un accesseur sur la base, on affiche des
         * boutons de création / suppression.
         */
        uiSearch = new Button(null, searchNone);
        uiSearch.textProperty().bind(currentSearch);
        uiSearch.getStyleClass().add("btn-without-style");
        uiSearch.setOnAction((ActionEvent event) -> {search();});
        uiSearch.getStyleClass().add("label-header");
        uiSearch.setTooltip(new Tooltip("Rechercher un terme dans la table"));
        uiSearch.disableProperty().bind(searchableProperty.not());
        
        final Label uiTitle = new Label(title==null? labelMapper.mapClassName() : title);
        uiTitle.getStyleClass().add("pojotable-header");
        uiTitle.setAlignment(Pos.CENTER);
        
        searchEditionToolbar.getStyleClass().add("buttonbar");
        searchEditionToolbar.getChildren().add(uiSearch);
            
        uiAdd.getStyleClass().add("btn-without-style");
        uiAdd.setOnAction((ActionEvent event) -> {
            final Object p = createPojo();
            if (p != null && openEditorOnNewProperty.get()) {
                editPojo(p);
            }
        });
        uiAdd.disableProperty().bind(editableProperty.not());

        uiDelete.getStyleClass().add("btn-without-style");
        uiDelete.setOnAction((ActionEvent event) -> {
            final Element[] elements = ((List<Element>) uiTable.getSelectionModel().getSelectedItems()).toArray(new Element[0]);
            if (elements.length > 0) {
                final ButtonType res = new Alert(Alert.AlertType.CONFIRMATION, "Confirmer la suppression ?",
                        ButtonType.NO, ButtonType.YES).showAndWait().get();
                if (ButtonType.YES == res) {
                    deletePojos(elements);
                }
            } else {
                new Alert(Alert.AlertType.INFORMATION, "Aucune entrée sélectionnée. Pas de suppression possible.").showAndWait();
            }
        });
        uiDelete.disableProperty().bind(editableProperty.not());

        searchEditionToolbar.getChildren().addAll(uiAdd, uiDelete);
        
        topPane = new BorderPane(uiTitle,null,searchEditionToolbar,null,null);
        setTop(topPane);
        uiTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        uiTable.setMaxWidth(Double.MAX_VALUE);
        uiTable.setPlaceholder(new Label(""));
        uiTable.setTableMenuButtonVisible(true);        
        if (repo!=null) {
            setTableItems(()-> FXCollections.observableList(repo.getAll()));
        }
        
        final FXCommentPhotoView commentPhotoView = new FXCommentPhotoView();
        commentPhotoView.valueProperty().bind(uiTable.getSelectionModel().selectedItemProperty());
        
        final SplitPane sPane = new SplitPane();
        sPane.setOrientation(Orientation.VERTICAL);
        sPane.getItems().addAll(uiTable, commentPhotoView);
        sPane.setDividerPositions(0.9);
        setCenter(sPane);
        
        // NAVIGABILITE FICHE PAR FICHE
        navigationToolbar.getStyleClass().add("buttonbarleft");
        
        uiCurrent.setFont(Font.font(20));
        uiCurrent.getStyleClass().add("btn-without-style"); 
        uiCurrent.setAlignment(Pos.CENTER);
        uiCurrent.setTextFill(Color.WHITE);
        uiCurrent.setOnAction(this::goTo);

        uiPrevious.getStyleClass().add("btn-without-style"); 
        uiPrevious.setTooltip(new Tooltip("Fiche précédente."));
        uiPrevious.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                final ObservableList<Element> items = uiTable.getItems();
                if(!items.isEmpty()){
                    if(currentFiche>0)
                        currentFiche--;
                    else
                        currentFiche=items.size()-1;
                    elementPane.setElement(items.get(currentFiche));
                    uiCurrent.setText((currentFiche+1)+" / "+items.size());
                }
            }
        });

        uiNext.getStyleClass().add("btn-without-style"); 
        uiNext.setTooltip(new Tooltip("Fiche suivante."));
        uiNext.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                final ObservableList<Element> items = uiTable.getItems();
                if(!items.isEmpty()){
                    if(currentFiche<items.size()-1)
                        currentFiche++;
                    else
                        currentFiche=0;
                    elementPane.setElement(items.get(currentFiche));
                    uiCurrent.setText((currentFiche+1)+" / "+items.size());
                }
            }
        });
        navigationToolbar.getChildren().addAll(uiPrevious, uiCurrent, uiNext);
        navigationToolbar.visibleProperty().bind(uiFicheMode.selectedProperty());

        uiFicheMode.setGraphic(playIcon);
        uiFicheMode.getStyleClass().add("btn-without-style"); 
        uiFicheMode.setTooltip(new Tooltip("Passer en mode de parcours des fiches."));
        uiFicheMode.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(!newValue){
                    setCenter(uiTable);
                    uiFicheMode.setGraphic(playIcon);
                    uiFicheMode.setTooltip(new Tooltip("Passer en mode de parcours des fiches."));
                }else{
                    final ObservableList<Element> items = uiTable.getItems();
                    if(!items.isEmpty()){
                        currentFiche=0;
                        elementPane = SIRS.generateEditionPane(items.get(currentFiche));
                        setCenter((Node) elementPane);
                        uiCurrent.setText((currentFiche+1)+" / "+items.size());
                    }else{
                        uiCurrent.setText(0+" / "+0);
                    }
                    uiFicheMode.setGraphic(stopIcon);
                    uiFicheMode.setTooltip(new Tooltip("Passer en mode de tableau synoptique."));
                }
            }
        });
        uiFicheMode.disableProperty().bind(fichableProperty.not());

        searchEditionToolbar.getChildren().add(0, uiFicheMode);
        
        topPane.setLeft(navigationToolbar);
    }
    
    protected ObservableList<Element> getAllValues(){return allValues;}

    public BooleanProperty editableProperty(){
        return editableProperty;
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
    
    protected void search(){
        if(uiSearch.getGraphic()!= searchNone){
            //une recherche est deja en cours
            return;
        }
        
        final Popup popup = new Popup();
        final TextField textField = new TextField(currentSearch.get());
        popup.getContent().add(textField);
        
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
        final TextField textField = new TextField();
        popup.getContent().add(textField);

        textField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int oldCurrentFiche = currentFiche;
                try {
                    currentFiche = Integer.valueOf(textField.getText()).intValue() - 1;
                } finally {
                    if (currentFiche < 0 || currentFiche > uiTable.getItems().size() - 1) {
                        currentFiche = oldCurrentFiche;
                    }

                    elementPane.setElement(uiTable.getItems().get(currentFiche));
                    uiCurrent.setText((currentFiche + 1) + " / " + uiTable.getItems().size());
                }
                popup.hide();
            }
        });
        final Point2D sc = uiCurrent.localToScreen(0, 0);
        popup.show(uiSearch, sc.getX(), sc.getY());
    }
    
    public TableView getUiTable() {
        return uiTable;
    }
    
    public void setTableItems(Supplier<ObservableList<Element>> producer){
        uiSearch.setGraphic(searchRunning);
        
        new Thread(){
            @Override
            public void run() {
                allValues = producer.get();
                final String str = currentSearch.get();
                if(str == null || str.isEmpty()){
                    filteredValues = allValues;
                }else{
                    final SearchEngine searchEngine = Injector.getSearchEngine();
                    final String type = pojoClass.getSimpleName();
                    final Set<String> result = new HashSet<>();
                    try {
                        result.addAll(searchEngine.search(type, str.split(" ")));
                    } catch (ParseException | IOException ex) {
                        SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                    }

                    filteredValues = allValues.filtered((Element t) -> {
                        return result.contains(t.getDocumentId());
                    });
                }

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        uiTable.setItems(filteredValues);
                        uiSearch.setGraphic(searchNone);
                    }
                });
            }
        }.start();
    }
        
    
    protected void deletePojos(Element... pojos) {
        ObservableList<Element> items = uiTable.getItems();
        for (Element pojo : pojos) {
            if (repo != null) {
                repo.remove(pojo);
            }
            items.remove(pojo);
        }
    }
    
    protected void editPojo(Object pojo){
        editElement(pojo);
    }
    
    protected void elementEdited(TableColumn.CellEditEvent<Element, Object> event){
        if(repo!=null){
            final Element obj = event.getRowValue();
            if(obj == null) return;
            repo.update(obj);
        }
    }
    
    protected Object createPojo() {
        Object result = null;
        if (repo != null) {
            result = repo.create();
            repo.add(result);
        } else if (pojoClass != null) {
            try {
                result = pojoClass.newInstance();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        if (result instanceof Element) {
            uiTable.getItems().add((Element)result);
        } else {
            new Alert(Alert.AlertType.INFORMATION, "Aucune entrée ne peut être créée.").showAndWait();
        }
        return result;
    }
        
    public static void editElement(Object pojo){
        if (pojo instanceof ElementHit) {
            final ElementHit hit = (ElementHit) pojo;
            try {
                pojo = (Element) Injector.getSession().getConnector().get(hit.geteElementClass(), hit.getDocumentId());
            } catch (ClassNotFoundException ex) {
                SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        
        try {
            Injector.getSession().getFrame().addTab(Injector.getSession().getOrCreateElementTab((Element)pojo));
        } catch (Exception ex) {
            Dialog d = new Alert(Alert.AlertType.ERROR, "Impossible d'afficher un éditeur", ButtonType.OK);
            d.showAndWait();
            throw new UnsupportedOperationException("Failed to load panel : " + ex.getMessage(), ex);
            
        }
    }
    
    // Matérieau à utiliser quand le role des utilisateurs sera devenu une énumération
//        public enum role{ADMIN, USER, CONSULTANT, EXTERNE};
//        private class EnumColumn extends TableColumn<Element, role>{
//            private EnumColumn(){
//                setCellValueFactory(new PropertyValueFactory<>("role"));
//                setCellFactory(new Callback<TableColumn<Element, role>, TableCell<Element, role>>() {
//
//                    @Override
//                    public TableCell<Element, role> call(TableColumn<Element, role> param) {
//                        return new FXEnumTableCell<Element, role>();
//                    }
//
//                });
//            }
//        }
    
        private class PasswordColumn extends TableColumn<Element, String>{
            private PasswordColumn(){
                setEditable(true);
                setCellValueFactory(new PropertyValueFactory<>("password"));
                setCellFactory(new Callback<TableColumn<Element, String>, TableCell<Element, String>>() {

                    @Override
                    public TableCell<Element, String> call(TableColumn<Element, String> param) {
                        MessageDigest messageDigest = null;
                        try {
                            messageDigest = MessageDigest.getInstance("MD5");
                        } catch (NoSuchAlgorithmException ex) {
                            Logger.getLogger(PojoTable.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        final TableCell<Element, String> cell = new FXPasswordTableCell<>(messageDigest);
                        cell.setEditable(true);
                        return cell;
                    }
                });
                addEventHandler(TableColumn.editCommitEvent(), new EventHandler<CellEditEvent<Element, Object>>() {

                    @Override
                    public void handle(CellEditEvent<Element, Object> event) {
                        final Element rowElement = event.getRowValue();
                        new PropertyReference<>(rowElement.getClass(), "password").set(rowElement, event.getNewValue());
                        elementEdited(event);
                    }
                });
            }
        }
        
        
    public class PropertyColumn extends TableColumn<Element,Object>{
        
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
            if(ref!=null){
                //reference vers un autre objet
                setEditable(false);
                setCellValueFactory(new CellLinkValueFactory(desc));
                setCellFactory((TableColumn<Element, Object> param) -> new SirsTableCell());
                
            }else{
                editableProperty().bind(editableProperty);
                final Class type = desc.getReadMethod().getReturnType();  
                
                setCellValueFactory(new PropertyValueFactory<>(desc.getName()));
                
                if(Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type)){
                    setCellFactory((TableColumn<Element, Object> param) -> new FXBooleanCell());
                    
                }else if(String.class.isAssignableFrom(type)){
//                    // Cas des cellules de mots de passe.
//                    if(desc.getDisplayName().equals("password")){
//                        setCellFactory(new Callback<TableColumn<Element, Object>, TableCell<Element, Object>>() {
//                        
//                        @Override
//                        public TableCell<Element, Object> call(TableColumn<Element, Object> param) {
//                            MessageDigest messageDigest=null;
//                            try {
//                                messageDigest = MessageDigest.getInstance("MD5");
//                            } catch (NoSuchAlgorithmException ex) {
//                                Logger.getLogger(PojoTable.class.getName()).log(Level.SEVERE, null, ex);
//                            }
//                            return new FXPasswordStringCell(messageDigest);
//                        }
//                    });
//                    } 
//                    
//                    // Cas provisoire des roles
//                    else if(desc.getDisplayName().equals("role")){
//                        setCellFactory(new Callback<TableColumn<Element, Object>, TableCell<Element, Object>>() {
//
//                            @Override
//                            public TableCell<Element, Object> call(TableColumn<Element, Object> param) {
//                                return new FXEnumTableCell<Element, role>();
//                            }
//                            
//                        });
//                    }
                    // Cas des autres chaînes de caractère par défaut.
//                    else {
                        setCellFactory((TableColumn<Element, Object> param) -> new FXStringCell());
//                    }
                }else if(Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type)){
                    setCellFactory((TableColumn<Element, Object> param) -> new FXNumberCell(NumberField.NumberType.Integer));
                }else if(Float.class.isAssignableFrom(type) || float.class.isAssignableFrom(type)){
                    setCellFactory((TableColumn<Element, Object> param) -> new FXNumberCell(NumberField.NumberType.Normal));
                }else if(Double.class.isAssignableFrom(type) || double.class.isAssignableFrom(type)){
                    setCellFactory((TableColumn<Element, Object> param) -> new FXNumberCell(NumberField.NumberType.Normal));
                }else if(LocalDateTime.class.isAssignableFrom(type)){
                    setCellFactory((TableColumn<Element, Object> param) -> new FXLocalDateTimeCell());
                }
                
                
                else if(type.isEnum()){
                    
                }
            }
        }  
    }
    
    public class CellLinkValueFactory implements Callback<TableColumn.CellDataFeatures<Element, Object>, ObservableValue<Object>>{

        private final PropertyDescriptor desc;

        public CellLinkValueFactory(PropertyDescriptor desc) {
            this.desc = desc;
        }
        
        @Override
        public ObservableValue<Object> call(CellDataFeatures<Element, Object> param) {
            Object obj = null;
            try {
                obj = desc.getReadMethod().invoke(param.getValue());
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                SIRS.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            }
            
            final SimpleObjectProperty res = new SimpleObjectProperty();
            
            if(obj instanceof String && !((String)obj).isEmpty()){
                final String id = (String)obj;
                res.set(session.getPreviewLabelRepository().getPreview(id));
            }
            
            return res;
        }
        
    }
    
    public class DeleteColumn extends TableColumn<Element,Element>{

        public DeleteColumn() {
            super("Suppression");            
            setSortable(false);
            setResizable(false);
            setPrefWidth(24);
            setMinWidth(24);
            setMaxWidth(24);
            setGraphic(new ImageView(GeotkFX.ICON_DELETE));
            
            setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Element, Element>, ObservableValue<Element>>() {
                @Override
                public ObservableValue<Element> call(TableColumn.CellDataFeatures<Element, Element> param) {
                    return new SimpleObjectProperty<>(param.getValue());
                }
            });
            setCellFactory(new Callback<TableColumn<Element, Element>, TableCell<Element, Element>>() {

                @Override
                public TableCell<Element, Element> call(TableColumn<Element, Element> param) {
                    return new ButtonTableCell<>(
                            false, new ImageView(GeotkFX.ICON_DELETE), new Function<Element, Boolean>() {

                        public Boolean apply(Element t) {
                            return true;
                        }
                    }, new Function<Element, Element>() {
                                @Override
                                public Element apply(Element t) {
                                    
                                    final ButtonType res = new Alert(Alert.AlertType.CONFIRMATION, "Confirmer la suppression ?",
                                            ButtonType.NO, ButtonType.YES).showAndWait().get();
                                    if (ButtonType.YES == res) {
                                        deletePojos(t);
                                    }
                                    return null;
                                }
                            });
                }
            });
        }  
    }
    
    public static class EditColumn extends TableColumn<Object,Object>{

        public EditColumn(Consumer editFct) {
            super("Edition");        
            setSortable(false);
            setResizable(false);
            setPrefWidth(24);
            setMinWidth(24);
            setMaxWidth(24);
            setGraphic(new ImageView(GeotkFX.ICON_EDIT));
            
            setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Object, Object>, ObservableValue<Object>>() {
                @Override
                public ObservableValue<Object> call(TableColumn.CellDataFeatures<Object, Object> param) {
                    return new SimpleObjectProperty<>(param.getValue());
                }
            });
            setCellFactory((TableColumn<Object,Object> param) -> new ButtonTableCell(
                    false,new ImageView(GeotkFX.ICON_EDIT), (Object t) -> true, new Function<Object, Object>() {
                @Override
                public Object apply(Object t) {
                    editFct.accept(t);
                    return t;
                }
            }));
        }  
    }
    
}
