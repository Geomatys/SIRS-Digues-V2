
package fr.sym.theme;

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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import org.geotoolkit.gui.javafx.util.ButtonTableCell;
import org.geotoolkit.gui.javafx.util.FXUtilities;
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
    
    protected final TableView uiTable = new TableView();
    protected final ScrollPane uiScroll = new ScrollPane(uiTable);
    protected final Class pojoClass;

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
             uiTable.getColumns().add(new PropertyColumn(desc));
        }
        
    }
        
    protected abstract void deletePojo(Element pojo);
    
    protected abstract void editPojo(Element pojo);
    
    public static class PropertyColumn extends TableColumn<Element,Object>{

        private final PropertyDescriptor desc;

        public PropertyColumn(PropertyDescriptor desc) {
            super(desc.getDisplayName());
            this.desc = desc;
            
            setCellValueFactory(new Callback<CellDataFeatures<Element, Object>, ObservableValue<Object>>() {
                @Override
                public ObservableValue<Object> call(CellDataFeatures<Element, Object> param) {
                    final Element ele = param.getValue();
                    return (ObservableValue)FXUtilities.beanProperty(param, desc.getName(), desc.getReadMethod().getReturnType());
                }
            });
        }  
    }
    
    public class DeleteColumn extends TableColumn<Element,Element>{

        public DeleteColumn() {
            super();            
            setSortable(false);
            setResizable(false);
            setPrefWidth(18);
            setMinWidth(18);
            setMaxWidth(18);
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
                    deletePojo(t);
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
            setPrefWidth(18);
            setMinWidth(18);
            setMaxWidth(18);
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
                if(m==null) continue;
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
