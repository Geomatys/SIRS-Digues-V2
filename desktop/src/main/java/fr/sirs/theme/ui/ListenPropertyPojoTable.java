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
 * This is a special kind of PojoTable that listen one specific property of its
 * items in order to kwon if they had to be removed from the table view.
 * 
 * This table is usefull for links without opposite in order to detect an object 
 * that were associated to the "virtual container" of the table list is no 
 * longer associated, or, may be, associated again.
 * 
 * For instance, let us consider two classes A and B, linked by an 
 * unidirectional association : A -> B.
 * 
 * So, A has a lis of B ids and can observe it in order to update UIs when the 
 * contend of the list changes. On the contrary, B has not its own list of A ids
 * If a new link is added from an instance of A to an instance of B, this last 
 * one cannot know the updates of this link because it doesn't handle it.
 * 
 * This table provides some mechanisms of listening between entities that are 
 * known to have been associated.
 * 
 * 1- It adds a listener to the objects it is initially linked with, or that are 
 * added to the table list.
 * 
 * 2- It continues to listen the objects that have been removed from the table 
 * in order to detect if they are associated again.
 * 
 * 3- But it does not listen other objects, and so, it cannot know if they are 
 * associated for the first time to the "virtual container".
 * 
 * @author Samuel Andrés (Geomatys)
 */
public class ListenPropertyPojoTable extends PojoTable {

    private final Map<Element, ChangeListener> listeners = new HashMap<>();
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
    protected Element createPojo() {
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
                    listeners.put(element, changeListener);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(ListenPropertyPojoTable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
