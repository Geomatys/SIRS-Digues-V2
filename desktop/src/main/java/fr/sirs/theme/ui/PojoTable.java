
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
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Element;
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
import fr.sirs.util.SirsTableCell;
import fr.sirs.util.property.Reference;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Constructor;
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
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
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
    
    protected final TableView<Element> uiTable = new FXTableView<>();
    protected final ScrollPane uiScroll = new ScrollPane(uiTable);
    protected final Class pojoClass;
    private final Repository repo;
    protected final Session session = Injector.getBean(Session.class);
    
    //valeurs affichées
    private final BooleanProperty editableProperty = new SimpleBooleanProperty(true);
    private final ImageView searchNone = new ImageView(SIRS.ICON_SEARCH);
    private final ProgressIndicator searchRunning = new ProgressIndicator();
    private ObservableList<Element> allValues;
    private ObservableList<Element> filteredValues;
    private final Button uiSearch;
    private final StringProperty currentSearch = new SimpleStringProperty("");
    
    public PojoTable(Class pojoClass, String title) {
        this(pojoClass, title, null, false);
    }
        
    public PojoTable(Class pojoClass, String title, boolean isEditable) {
        this(pojoClass, title, null, isEditable);
    }
    
    public PojoTable(Repository repo, String title) {
        this(repo.getModelClass(), title, repo, true);
    }
    
    private PojoTable(Class pojoClass, String title, Repository repo, boolean isEditable) {
        getStylesheets().add(SIRS.CSS_PATH);
        this.pojoClass = pojoClass;
        this.repo = repo;
        
        searchRunning.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        searchRunning.setPrefSize(22, 22);
        searchRunning.setStyle("-fx-progress-color: white;");
        uiScroll.setFitToHeight(true);
        uiScroll.setFitToWidth(true);
        uiTable.setEditable(true);
        uiTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                
        setCenter(uiScroll);
        
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
        
        final HBox toolbar = new HBox(uiSearch);
        toolbar.getStyleClass().add("buttonbar");
        
        if (isEditable) {
            final Button uiAdd = new Button(null, new ImageView(SIRS.ICON_ADD_WHITE));
            uiAdd.getStyleClass().add("btn-without-style");
            uiAdd.setOnAction((ActionEvent event) -> {
                editPojo(createPojo());
            });

            final Button uiDelete = new Button(null, new ImageView(SIRS.ICON_TRASH));
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

            toolbar.getChildren().addAll(uiAdd, uiDelete);
        }
        
        final BorderPane top = new BorderPane(uiTitle,null,toolbar,null,null);
        setTop(top);
        
        if(repo!=null){
            updateTable();
        }
        
    }

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

        final Tab tab = new Tab();

        try {
            Node content = new BorderPane(new Label("Pas d'éditeur pour le type : " + pojo.getClass().getSimpleName()));
            if (pojo instanceof Objet) {
                content = new FXStructurePane((Objet) pojo);
            } else if (pojo instanceof Contact) {
                content = new FXContactPane((Contact) pojo);
            } else if (pojo instanceof Organisme) {
                content = new FXOrganismePane((Organisme) pojo);
            } else if (pojo instanceof ContactOrganisme) {
                content = new FXContactOrganismePane((ContactOrganisme)pojo);
            } else if (pojo instanceof ProfilTravers) {
                content = new FXThemePane((ProfilTravers) pojo);
            } else if (pojo instanceof TronconDigue) {
                final FXTronconDiguePane ctrl = new FXTronconDiguePane();
                ctrl.setTroncon((TronconDigue) pojo);
                content = ctrl;
            } else if (pojo instanceof Digue) {
                final FXDiguePane ctrl = new FXDiguePane();
                ctrl.setDigue((Digue) pojo);
                content = ctrl;
            } else if (pojo instanceof Objet) {
                // Choose the pane adapted to the specific structure.
                final String className = "fr.sirs.theme.ui.FX" + pojo.getClass().getSimpleName() + "Pane";
                final Class controllerClass = Class.forName(className);
                final Constructor cstr = controllerClass.getConstructor(pojo.getClass());
                content = (Node) cstr.newInstance(pojo);
            }

            tab.setContent(content);
            final Session session = Injector.getSession();
            final Element ele = (Element) pojo;
            tab.setText(pojo.getClass().getSimpleName());
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
            super(desc.getDisplayName());
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
