package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.core.model.Element;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.geotoolkit.gui.javafx.util.TaskManager;

/**
 *
 * This is a special king of PojoTable that listen one specific property of its
 * items in order to kwon if they had to be removed from the table view.
 * 
 * The purpose of this component is to be able to display elements separately 
 * from their parent element.
 * 
 * @author Samuel Andrés (Geomatys)
 */
public class ListenPropertyPojoTable extends PojoTable {

    private Map<Element, ChangeListener> listeners = new HashMap<>();
    private Method propertyMethodToListen;
    private Object propertyReference;
    
    public ListenPropertyPojoTable(Class pojoClass, String title) {
        super(pojoClass, title);
    }
    
    @Override
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
                        for(final Element element : uiTable.getItems()){
                            addListener(element);
                        }
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
    
    public void setPropertyToListen(String propertyToListen, Object propertyReference){
        try {
            propertyMethodToListen = pojoClass.getMethod(propertyToListen);
        } catch (NoSuchMethodException | SecurityException ex) {
            Logger.getLogger(ListenPropertyPojoTable.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.propertyReference = propertyReference;
    }
    
    @Override
    protected Object createPojo() {
        final Element element = (Element) super.createPojo();
        addListener(element);
        return element;
    }
    
    private void addListener(final Element element){
        if(propertyMethodToListen!=null){
            try {
                final Property property = (Property) propertyMethodToListen.invoke(element);
                if(listeners.get(element)==null){
                    final ChangeListener changeListener = new ChangeListener() {

                        @Override
                        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                            if(newValue.equals(propertyReference)){
                                if(!uiTable.getItems().contains(element)){
                                    uiTable.getItems().add(element);
                                }
                            }
                            else{
                                if(uiTable.getItems().contains(element)){
                                    uiTable.getItems().remove(element);
                                }
                            }
                        }
                    };
                    property.addListener(changeListener);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(ListenPropertyPojoTable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
