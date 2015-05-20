package fr.sirs.theme.ui;

import fr.sirs.core.model.Element;
import java.util.function.Supplier;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

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
 * 
 * @param <T> The type of the elements in the list to listen. For instance, the
 * type is String in the case of a list of ids.
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
        // The producer has to be memorized to be used by the listener later.
        this.producer = producer;
        super.setTableItems(producer);
    }
    
}
