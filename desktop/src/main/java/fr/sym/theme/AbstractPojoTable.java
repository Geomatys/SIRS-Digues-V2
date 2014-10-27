
package fr.sym.theme;

import org.geotoolkit.gui.javafx.util.FXNumberCell;
import org.geotoolkit.gui.javafx.util.FXStringCell;
import org.geotoolkit.gui.javafx.util.FXLocalDateTimeCell;
import org.geotoolkit.gui.javafx.util.FXBooleanCell;
import com.geomatys.property.Internal;
import com.sun.javafx.property.PropertyReference;
import fr.sym.Session;
import fr.sym.digue.Injector;
import fr.symadrem.sirs.core.model.Element;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
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
    
    private static final Class[] SUPPORTED_TYPES = new Class[]{
        Boolean.class,
        String.class,
        Integer.class,
        Float.class,
        LocalDateTime.class
    };
    
    protected final TableView uiTable = new FXTableView();
    protected final ScrollPane uiScroll = new ScrollPane(uiTable);
    protected final Class pojoClass;
    protected final Session session = Injector.getBean(Session.class);
    
    public AbstractPojoTable(Class pojoClass) {
        this.pojoClass = pojoClass;
        
        uiScroll.setFitToHeight(true);
        uiScroll.setFitToWidth(true);
        uiTable.setEditable(true);
        
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
        
    }
        
    protected abstract void deletePojo(Element pojo);
    
    protected abstract void editPojo(Element pojo);
    
    protected abstract void elementEdited(TableColumn.CellEditEvent<Element, Object> event);
    
    public static class PropertyColumn extends TableColumn<Element,Object>{

        private final PropertyDescriptor desc;

        public PropertyColumn(final PropertyDescriptor desc) {
            super(desc.getDisplayName());
            this.desc = desc;
            setEditable(true);
            setSortable(true);
            
            setCellValueFactory(new PropertyValueFactory<>(desc.getName()));
            
            addEventHandler(TableColumn.editCommitEvent(), (CellEditEvent<Object, Object> event) -> {
                final Object rowElement = event.getRowValue();
                new PropertyReference<>(rowElement.getClass(), desc.getName()).set(rowElement, event.getNewValue());
            });
            
            //choix de l'editeur en fonction du type de données
            final Class type = desc.getReadMethod().getReturnType();            
            if(Boolean.class.isAssignableFrom(type)){
                setCellFactory((TableColumn<Element, Object> param) -> new FXBooleanCell());
            }else if(String.class.isAssignableFrom(type)){
                setCellFactory((TableColumn<Element, Object> param) -> new FXStringCell());
            }else if(Integer.class.isAssignableFrom(type)){
                setCellFactory((TableColumn<Element, Object> param) -> new FXNumberCell(NumberField.NumberType.Integer));
            }else if(Float.class.isAssignableFrom(type)){
                setCellFactory((TableColumn<Element, Object> param) -> new FXNumberCell(NumberField.NumberType.Normal));
            }else if(LocalDateTime.class.isAssignableFrom(type)){
                setCellFactory((TableColumn<Element, Object> param) -> new FXLocalDateTimeCell());
            }
            
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
                        deletePojo(t);
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
