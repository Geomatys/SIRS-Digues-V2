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
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.ContactOrganisme;
import fr.sirs.core.model.ContactTroncon;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.LeveeProfilTravers;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.Organisme;
import fr.sirs.core.model.ProfilTravers;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.digue.FXDiguePane;
import fr.sirs.digue.FXTronconDiguePane;
import fr.sirs.index.SearchEngine;
import fr.sirs.other.FXContactOrganismePane;
import fr.sirs.other.FXContactPane;
import fr.sirs.other.FXOrganismePane;
import fr.sirs.query.ElementHit;
import fr.sirs.util.FXFreeTab;
import fr.sirs.util.SirsTableCell;
import fr.sirs.util.property.Reference;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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
    
    // Editabilité du tableau
    protected final BooleanProperty editableProperty = new SimpleBooleanProperty(true);
    
    // Mode tableau / parcours de fiches
    protected Mode mode = Mode.TABLE;
    protected enum Mode{TABLE, FICHE};
    
    // Icônes de la barre d'action
    // Barre de droite : manipulation du tableau et passage en mode parcours de fiche
    private final ImageView searchNone = new ImageView(SIRS.ICON_SEARCH);
    private final Button uiSearch;
    protected final Button uiAdd = new Button(null, new ImageView(SIRS.ICON_ADD_WHITE));
    protected final Button uiDelete = new Button(null, new ImageView(SIRS.ICON_TRASH));
    protected final ImageView playIcon = new ImageView(SIRS.ICON_FILE);
    private final ImageView stopIcon = new ImageView(SIRS.ICON_TABLE);
    protected final Button uiPlay = new Button();
    protected final HBox searchEditionToolbar = new HBox();
    
    // Barre de gauche : navigation dans le parcours de fiches
    protected FXElementPane elementPane = null;
    private int currentFiche = 0;
    private final ImageView previousIcon = new ImageView(SIRS.ICON_CARET_LEFT);
    private final Button uiPrevious = new Button();
    private final ImageView nextIcon = new ImageView(SIRS.ICON_CARET_RIGHT);
    private final Button uiNext = new Button();
    private final Button uiCurrent = new Button();
    protected final HBox navigationToolbar = new HBox();
    
    
    private final ProgressIndicator searchRunning = new ProgressIndicator();
    private ObservableList<Element> allValues;
    private ObservableList<Element> filteredValues;
    
    
    
    private final StringProperty currentSearch = new SimpleStringProperty("");
    protected final BorderPane topPane;
    
    public PojoTable(Class pojoClass, String title) {
        this(pojoClass, title, null, false, true);
    }
        
    public PojoTable(Class pojoClass, String title, boolean isEditable) {
        this(pojoClass, title, null, isEditable, true);
    }
    
    public PojoTable(Repository repo, String title) {
        this(repo.getModelClass(), title, repo, true, true);
    }
    
    private PojoTable(Class pojoClass, String title, Repository repo, boolean isEditable, boolean isFichable) {
        getStylesheets().add(SIRS.CSS_PATH);
        this.pojoClass = pojoClass;
        this.labelMapper = new LabelMapper(pojoClass);
        this.repo = repo;
        
        searchRunning.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        searchRunning.setPrefSize(22, 22);
        searchRunning.setStyle("-fx-progress-color: white;");
//        uiScroll.setFitToHeight(true);
//        uiScroll.setFitToWidth(true);
        uiTable.setEditable(isEditable);
        uiTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        //contruction des colonnes editable
        final List<PropertyDescriptor> properties = Session.listSimpleProperties(pojoClass);
        uiTable.getColumns().add(new DeleteColumn());
        
        final EditColumn editCol = new EditColumn(this::editPojo);
        editCol.editableProperty().bind(editableProperty);
        editCol.visibleProperty().bind(editableProperty);
        
        uiTable.getColumns().add((TableColumn)editCol);
        for(PropertyDescriptor desc : properties){
            final PropertyColumn col = new PropertyColumn(desc); 
             uiTable.getColumns().add(col);
             //sauvegarde sur chaque changement dans la table
             col.addEventHandler(TableColumn.editCommitEvent(), (TableColumn.CellEditEvent<Element, Object> event) -> {
                 elementEdited(event);
             });
        }
        
        
        /* barre d'outils. Si on a un accesseur sur la base, on affiche des
         * boutons de création / suppression.
         */
        uiSearch = new Button(null, searchNone);
        uiSearch.textProperty().bind(currentSearch);
        uiSearch.getStyleClass().add("btn-without-style");        
        uiSearch.setOnAction((ActionEvent event) -> {search();});
        uiSearch.getStyleClass().add("label-header");
        uiSearch.setTooltip(new Tooltip("Rechercher un terme dans la table"));
        
        final Label uiTitle = new Label(title);
        uiTitle.getStyleClass().add("pojotable-header");   
        uiTitle.setAlignment(Pos.CENTER);
        
        uiTable.editableProperty().bind(editableProperty);
        
        searchEditionToolbar.getChildren().add(uiSearch);
        searchEditionToolbar.getStyleClass().add("buttonbar");
        
        if (isEditable) {
            
            uiAdd.getStyleClass().add("btn-without-style");
            uiAdd.setOnAction((ActionEvent event) -> {
                editPojo(createPojo());
            });

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
            uiAdd.visibleProperty().bind(editableProperty);
            uiDelete.visibleProperty().bind(editableProperty);

            searchEditionToolbar.getChildren().addAll(uiAdd, uiDelete);
        }
        
        topPane = new BorderPane(uiTitle,null,searchEditionToolbar,null,null);
        setTop(topPane);
        uiTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        uiTable.setPlaceholder(new Label(""));
        uiTable.setTableMenuButtonVisible(true);        
        if(repo!=null){
            updateTable();
        }
        
        final FXCommentPhotoView commentPhotoView = new FXCommentPhotoView();
        commentPhotoView.valueProperty().bind(uiTable.getSelectionModel().selectedItemProperty());
        
        final SplitPane sPane = new SplitPane();
        sPane.setOrientation(Orientation.VERTICAL);
        sPane.getItems().addAll(uiTable, commentPhotoView);
        sPane.setDividerPositions(0.8);
        setCenter(sPane);
        
        
        
        navigationToolbar.setVisible(false);
        navigationToolbar.getStyleClass().add("buttonbarleft");
        
        if(isFichable){
            uiCurrent.setFont(Font.font(20));
            uiCurrent.getStyleClass().add("btn-without-style"); 
            uiCurrent.setAlignment(Pos.CENTER);
            uiCurrent.setTextFill(Color.WHITE);
            uiCurrent.setOnAction((ActionEvent event) -> {goTo();});

            uiPrevious.setGraphic(previousIcon);
            uiPrevious.getStyleClass().add("btn-without-style"); 
            uiPrevious.setTooltip(new Tooltip("Fiche précédente."));
            uiPrevious.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    if(uiTable.getItems().size()>0){
                        if(currentFiche>0)
                            currentFiche--;
                        else
                            currentFiche=uiTable.getItems().size()-1;
                        elementPane.setElement(uiTable.getItems().get(currentFiche));
                        uiCurrent.setText((currentFiche+1)+" / "+uiTable.getItems().size());
                    }
                }
            });

            uiNext.setGraphic(nextIcon);
            uiNext.getStyleClass().add("btn-without-style"); 
            uiNext.setTooltip(new Tooltip("Fiche suivante."));
            uiNext.setOnAction(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    if(uiTable.getItems().size()>0){
                        if(currentFiche<uiTable.getItems().size()-1)
                            currentFiche++;
                        else
                            currentFiche=0;
                        elementPane.setElement(uiTable.getItems().get(currentFiche));
                        uiCurrent.setText((currentFiche+1)+" / "+uiTable.getItems().size());
                    }
                }
            });
            navigationToolbar.getChildren().addAll(uiPrevious, uiCurrent, uiNext);

            uiPlay.setGraphic(playIcon);
            uiPlay.getStyleClass().add("btn-without-style"); 
            uiPlay.setTooltip(new Tooltip("Passer en mode de parcours des fiches."));
            uiPlay.setOnAction(new  EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent event) {
                    if(mode==Mode.FICHE){
                        mode = Mode.TABLE;
                        navigationToolbar.setVisible(false);
                        setCenter(uiTable);
                        if(!editableProperty.isBound())
                            editableProperty.setValue(true);
                        uiPlay.setGraphic(playIcon);
                        uiPlay.setTooltip(new Tooltip("Passer en mode de parcours des fiches."));
                    }
                    else{
                        mode = Mode.FICHE;
                        navigationToolbar.setVisible(true);
                        if(uiTable.getItems().size()>0){
                            currentFiche=0;
                            elementPane = generateEditionPane(uiTable.getItems().get(currentFiche));
                            setCenter((Node) elementPane);
                            uiCurrent.setText((currentFiche+1)+" / "+uiTable.getItems().size());
                        }
                        else{
                            uiCurrent.setText(0+" / "+0);
                        }
                        if(!editableProperty.isBound())
                            editableProperty.setValue(false);
                        uiPlay.setGraphic(stopIcon);
                        uiPlay.setTooltip(new Tooltip("Passer en mode de tableau synoptique."));
                    }
                }
            });

            searchEditionToolbar.getChildren().add(0, uiPlay);
        }
        
        topPane.setLeft(navigationToolbar);
    }
    
    
    private static FXElementPane generateEditionPane(final Object pojo) {
        System.out.println("GENERATION DU PANNEAU");
        final FXElementPane content;
        if (pojo instanceof Objet) {
            content = new FXObjetPane((Objet) pojo);
        } else if (pojo instanceof Contact) {
            content = new FXContactPane((Contact) pojo);
        } else if (pojo instanceof Organisme) {
            content = new FXOrganismePane((Organisme) pojo);
        } else if (pojo instanceof ContactOrganisme) {
            content = new FXContactOrganismePane((ContactOrganisme) pojo);
        } else if (pojo instanceof ContactTroncon) {
            System.out.println("Pas de panneau pour les contact/tronçons.");
            content = null;
        } else if (pojo instanceof ProfilTravers) {
            content = new FXThemePane((ProfilTravers) pojo);
            ((FXThemePane) content).setShowOnMapButton(false);
        } else if (pojo instanceof LeveeProfilTravers){
            content = new FXThemePane((LeveeProfilTravers) pojo);
            ((FXThemePane) content).setShowOnMapButton(false);
        } else if (pojo instanceof TronconDigue) {
            final FXTronconDiguePane ctrl = new FXTronconDiguePane();
            ctrl.setElement((TronconDigue) pojo);
            content = ctrl;
        } else if (pojo instanceof Digue) {
            final FXDiguePane ctrl = new FXDiguePane();
            ctrl.setElement((Digue) pojo);
            content = ctrl;
        } else {
            content = null;
        }
        System.out.println("On en est là !!!!");
        return content;
    }
    
    protected ObservableList<Element> getAllValues(){return allValues;}

    public BooleanProperty editableProperty(){
        return editableProperty;
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
    
    protected void goTo(){
        
        final Popup popup = new Popup();
        final TextField textField = new TextField();
        popup.getContent().add(textField);
        
        textField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int oldCurrentFiche = currentFiche;
                try{
                    currentFiche = Integer.valueOf(textField.getText()).intValue()-1;
                }
                catch(Exception e){
                    System.out.println("Message : "+e.getMessage());
                }
                finally{
                    if(currentFiche<0 || currentFiche>uiTable.getItems().size()-1)
                        currentFiche=oldCurrentFiche;
                    
                    elementPane.setElement(uiTable.getItems().get(currentFiche));
                    uiCurrent.setText((currentFiche+1)+" / "+uiTable.getItems().size());
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
        
    
    protected void deletePojos(Element ... pojos){
        if (repo!=null) {
            for(Element pojo : pojos){
                repo.remove(pojo);
            }
            updateTable(); 
        } else {
            new Alert(Alert.AlertType.INFORMATION, "L'entrée ne peut pas être supprimée.").showAndWait();
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
            updateTable();
        } else {
            new Alert(Alert.AlertType.INFORMATION, "Aucune entrée ne peut être créée.").showAndWait();
        }
        return result;
    }
        
    private void updateTable(){
        if(repo!=null){
            setTableItems(()-> FXCollections.observableList(repo.getAll()));
        }
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

        final Tab tab = new FXFreeTab();

        try {
            Node content = (Node) generateEditionPane(pojo);
            if (content==null) content = new BorderPane(new Label("Pas d'éditeur pour le type : " + pojo.getClass().getSimpleName()));

            tab.setContent(content);
            final Session session = Injector.getSession();
            final Element ele = (Element) pojo;
            tab.setText(session.getPreviewLabelRepository().getPreview(ele.getId()));
            tab.setOnSelectionChanged((Event event) -> {
                if (tab.isSelected()) {
                    session.prepareToPrint(ele);
                }
            });
            session.getFrame().addTab(tab);
        } catch (Exception ex) {
            Dialog d = new Alert(Alert.AlertType.ERROR, "Impossible d'afficher un éditeur", ButtonType.OK);
            d.showAndWait();
            throw new UnsupportedOperationException("Failed to load panel : " + ex.getMessage(), ex);
            
        }
    }
    
    
    public class PropertyColumn extends TableColumn<Element,Object>{

        private final PropertyDescriptor desc;

        public PropertyColumn(final PropertyDescriptor desc) {
            super(labelMapper.mapPropertyName(desc.getDisplayName()));
            this.desc = desc;
            setEditable(true);
            
            final Reference ref = desc.getReadMethod().getAnnotation(Reference.class);
                        
            addEventHandler(TableColumn.editCommitEvent(), (CellEditEvent<Object, Object> event) -> {
                final Object rowElement = event.getRowValue();
                new PropertyReference<>(rowElement.getClass(), desc.getName()).set(rowElement, event.getNewValue());
            });
            
            //choix de l'editeur en fonction du type de données          
            if(ref!=null){
                //reference vers un autre objet
                setEditable(false);
                setCellValueFactory(new CellLinkValueFactory(desc, ref.ref()));
                setCellFactory((TableColumn<Element, Object> param) -> new SirsTableCell());
                
            }else{
                final Class type = desc.getReadMethod().getReturnType();  
                
                setCellValueFactory(new PropertyValueFactory<>(desc.getName()));
                if(Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type)){
                    setCellFactory((TableColumn<Element, Object> param) -> new FXBooleanCell());
                }else if(String.class.isAssignableFrom(type)){
                    setCellFactory((TableColumn<Element, Object> param) -> new FXStringCell());
                }else if(Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type)){
                    setCellFactory((TableColumn<Element, Object> param) -> new FXNumberCell(NumberField.NumberType.Integer));
                }else if(Float.class.isAssignableFrom(type) || float.class.isAssignableFrom(type)){
                    setCellFactory((TableColumn<Element, Object> param) -> new FXNumberCell(NumberField.NumberType.Normal));
                }else if(Double.class.isAssignableFrom(type) || double.class.isAssignableFrom(type)){
                    setCellFactory((TableColumn<Element, Object> param) -> new FXNumberCell(NumberField.NumberType.Normal));
                }else if(LocalDateTime.class.isAssignableFrom(type)){
                    setCellFactory((TableColumn<Element, Object> param) -> new FXLocalDateTimeCell());
                }
            }
            
        }  
    }
    
    public class CellLinkValueFactory implements Callback<TableColumn.CellDataFeatures<Element, Object>, ObservableValue<Object>>{

        private final PropertyDescriptor desc;
        private final Class refClass;

        public CellLinkValueFactory(PropertyDescriptor desc, Class refClass) {
            this.desc = desc;
            this.refClass = refClass;
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
            super();            
            setSortable(false);
            setResizable(false);
            setPrefWidth(24);
            setMinWidth(24);
            setMaxWidth(24);
            setGraphic(new ImageView(GeotkFX.ICON_DELETE));
            DeleteColumn.this.editableProperty().bind(editableProperty);
            DeleteColumn.this.visibleProperty().bind(editableProperty);
            
            setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Element, Element>, ObservableValue<Element>>() {
                @Override
                public ObservableValue<Element> call(TableColumn.CellDataFeatures<Element, Element> param) {
                    return new SimpleObjectProperty<>(param.getValue());
                }
            });
            setCellFactory((TableColumn<Element, Element> param) -> new ButtonTableCell<>(
                    false,new ImageView(GeotkFX.ICON_DELETE), (Element t) -> true, new Function<Element, Element>() {
                @Override
                public Element apply(Element t) {
                    final ButtonType res = new Alert(Alert.AlertType.CONFIRMATION,"Confirmer la suppression ?", 
                            ButtonType.NO, ButtonType.YES).showAndWait().get();
                    if(ButtonType.YES == res){
                        deletePojos(t);
                    }
                    return null;
                }
            }));
        }  
    }
    
    public static class EditColumn extends TableColumn<Object,Object>{

        public EditColumn(Consumer editFct) {
            super();            
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
