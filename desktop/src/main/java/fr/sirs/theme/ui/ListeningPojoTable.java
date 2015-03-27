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
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.geotoolkit.gui.javafx.util.TaskManager;

/**
 *
 * This is a special kind of PojoTable that listen the list property of the 
 * container of the list displayed on the table.
 * 
 * This table is usefull for bidirectionnal links based on ids instead of 
 * containment.
 * 
 * For instance, let us consider two classes A and B, linked by a bidirectional 
 * association : A <-> B.
 * 
 * So, A has a lis of B ids and can observe it in order to update UIs when the 
 * contend of the list changes. So can B. If a new link is added from an 
 * instance of A with an instance of B, this last one has to be able to warn the
 * ui that a new association exists.
 * 
 * This table provides some mechanisms of listening the list of the container in 
 * order to refresh the tableview.
 * 
 * @author Samuel Andrés (Geomatys)
 */
public class ListeningPojoTable<T> extends PojoTable {

    private Supplier<ObservableList<Element>> producer;
    private ObservableList<T> observableListToListen;
    private final ListChangeListener<T> listener = new ListChangeListener<T>() {

            @Override
            public void onChanged(ListChangeListener.Change<? extends T> c) {
                if(producer!=null){
                    setTableItems(producer);
                }
            }
        }; 
    
    public ListeningPojoTable(Class pojoClass, String title) {
        super(pojoClass, title);
    }
    
    public void setObservableListToListen(final ObservableList<T> observableList){
        if(observableListToListen!=null) observableListToListen.removeListener(listener);
        observableListToListen = observableList;
        observableListToListen.addListener(listener);
    }
    
    @Override
    public void setTableItems(Supplier<ObservableList<Element>> producer) {
        this.producer = producer;
        super.setTableItems(producer);
    }
    
}
