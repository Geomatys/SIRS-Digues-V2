
package fr.sirs.theme.ui;

import org.geotoolkit.gui.javafx.util.FXNumberCell;
import org.geotoolkit.gui.javafx.util.FXStringCell;
import org.geotoolkit.gui.javafx.util.FXLocalDateTimeCell;
import org.geotoolkit.gui.javafx.util.FXBooleanCell;
import fr.sirs.util.property.Internal;
import com.sun.javafx.property.PropertyReference;
import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import fr.sirs.core.model.Element;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import jidefx.scene.control.field.NumberField;
import org.geotoolkit.gui.javafx.util.ButtonTableCell;
import org.geotoolkit.gui.javafx.util.FXTableView;
import org.geotoolkit.internal.GeotkFX;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractPojoTable extends BorderPane{
    
    private static final Comparator COMPARATOR = new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                if(o1==null && o2==null) return 0;
                if(o1==null) return -1;
                if(o2==null) return +1;

                if(o1 instanceof Boolean){
                    return Boolean.compare((Boolean)o1, (Boolean)o2);
                }else if(o1 instanceof Number){
                    final double d1 = ((Number)o1).doubleValue();
                    final double d2 = ((Number)o2).doubleValue();
                    return Double.compare(d1, d2);
                }else if(o1 instanceof String){
                    return ((String)o1).compareToIgnoreCase((String)o2);
                }else if(o1 instanceof LocalDateTime){
                    return ((LocalDateTime)o1).compareTo((LocalDateTime)o2);
                }else {
                    return 0;
                }
            }
        };
    
    private static final Class[] SUPPORTED_TYPES = new Class[]{
        Boolean.class,
        String.class,
        Integer.class,
        Float.class,
        Double.class,
        boolean.class,
        int.class,
        float.class,
        double.class,
        LocalDateTime.class
    };
    
    protected final TableView uiTable = new FXTableView();
    protected final ScrollPane uiScroll = new ScrollPane(uiTable);
    protected final Class pojoClass;
    protected final Session session = Injector.getBean(Session.class);
    
    public AbstractPojoTable(Class pojoClass, String title) {
        getStylesheets().add(SIRS.CSS_PATH);
        this.pojoClass = pojoClass;
        
        uiScroll.setFitToHeight(true);
        uiScroll.setFitToWidth(true);
        uiTable.setEditable(true);
        uiTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                
        setCenter(uiScroll);
        
        //contruction des colonnes editable
        final List<PropertyDescriptor> properties = listSimpleProperties(pojoClass);
        uiTable.getColumns().add(new DeleteColumn());
        uiTable.getColumns().add(new EditColumn());
        for(PropertyDescriptor desc : properties){
            final PropertyColumn col = new PropertyColumn(desc); 
             uiTable.getColumns().add(col);
             //sauvegarde sur chaque changement dans la table
             col.addEventHandler(TableColumn.editCommitEvent(), (TableColumn.CellEditEvent<Element, Object> event) -> {
                 elementEdited(event);
             });
        }
        
        
        //barre d'outils
        final Button uiAdd = new Button(null, new ImageView(SIRS.ICON_ADD));
        uiAdd.getStyleClass().add("btn-without-style");
        uiAdd.setOnAction((ActionEvent event) -> {createPojo();});
        
        final Button uiDelete = new Button(null, new ImageView(SIRS.ICON_TRASH));
        uiDelete.getStyleClass().add("btn-without-style");
        uiDelete.setOnAction((ActionEvent event) -> {
            final Element[] elements = ((List<Element>)uiTable.getSelectionModel().getSelectedItems()).toArray(new Element[0]);
            if(elements.length>0){
                final ButtonType res = new Alert(Alert.AlertType.CONFIRMATION,"Confirmer la suppression ?", 
                        ButtonType.NO, ButtonType.YES).showAndWait().get();
                if(ButtonType.YES == res){
                    deletePojos(elements);
                }
                
            }
        });
        
        final Button uiSearch = new Button(null, new ImageView(SIRS.ICON_SEARCH));
        uiSearch.getStyleClass().add("btn-without-style");        
        uiSearch.setOnAction((ActionEvent event) -> {/*TODO*/});
        
        final Label uiTitle = new Label(title);
        uiTitle.getStyleClass().add("pojotable-header");   
        uiTitle.setAlignment(Pos.CENTER);
        
        
        uiAdd.visibleProperty().bind(this.disableProperty().not());
        uiDelete.visibleProperty().bind(this.disableProperty().not());
        final HBox toolbar = new HBox(uiAdd,uiDelete,uiSearch);     
        toolbar.getStyleClass().add("buttonbar");
        final BorderPane top = new BorderPane(uiTitle,null,toolbar,null,null);
        setTop(top);
    }

    public TableView getUiTable() {
        return uiTable;
    }
    
    protected abstract void deletePojos(Element ... pojos);
    
    protected abstract void editPojo(Element pojo);
    
    protected abstract void elementEdited(TableColumn.CellEditEvent<Element, Object> event);
    
    protected abstract void createPojo();
    
    
    public static class PropertyColumn extends TableColumn<Element,Object>{

        private final PropertyDescriptor desc;

        public PropertyColumn(final PropertyDescriptor desc) {
            super(desc.getDisplayName());
            this.desc = desc;
            setEditable(true);
            
            setCellValueFactory(new PropertyValueFactory<>(desc.getName()));
            
            addEventHandler(TableColumn.editCommitEvent(), (CellEditEvent<Object, Object> event) -> {
                final Object rowElement = event.getRowValue();
                new PropertyReference<>(rowElement.getClass(), desc.getName()).set(rowElement, event.getNewValue());
            });
            
            //choix de l'editeur en fonction du type de données
            final Class type = desc.getReadMethod().getReturnType();            
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
            setComparator(COMPARATOR);
            
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
    
    public class EditColumn extends TableColumn<Element,Element>{

        public EditColumn() {
            super();            
            setSortable(false);
            setResizable(false);
            setPrefWidth(24);
            setMinWidth(24);
            setMaxWidth(24);
            setGraphic(new ImageView(GeotkFX.ICON_EDIT));
            
            setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Element, Element>, ObservableValue<Element>>() {
                @Override
                public ObservableValue<Element> call(TableColumn.CellDataFeatures<Element, Element> param) {
                    return new SimpleObjectProperty<>(param.getValue());
                }
            });
            setCellFactory((TableColumn<Element, Element> param) -> new ButtonTableCell<>(
                    false,new ImageView(GeotkFX.ICON_EDIT), (Element t) -> true, new Function<Element, Element>() {
                @Override
                public Element apply(Element t) {
                    editPojo(t);
                    return t;
                }
            }));
        }  
    }
    
    private static List<PropertyDescriptor> listSimpleProperties(Class clazz) {
        final List<PropertyDescriptor> properties = new ArrayList<>();
        try {
            for (java.beans.PropertyDescriptor pd : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
                final Method m = pd.getReadMethod();
                if(m==null || m.getAnnotation(Internal.class)!=null) continue;
                final Class propClass = m.getReturnType();
                for(Class c : SUPPORTED_TYPES){
                    if(c.isAssignableFrom(propClass)){
                        properties.add(pd);
                        break;
                    }
                }
            }
        } catch (IntrospectionException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return properties;
    }
    
}
