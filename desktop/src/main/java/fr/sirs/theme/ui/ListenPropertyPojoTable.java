package fr.sirs.theme.ui;

import fr.sirs.core.model.Element;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Condition;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;

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
 * 
 * @param <T> The type of the listen property.
 */
public class ListenPropertyPojoTable<T> extends PojoTable {

    private final WeakHashMap<Element, ChangeListener<T>> listeners = new WeakHashMap<>();
    protected Method propertyMethodToListen;
    protected T propertyReference;
    
    public ListenPropertyPojoTable(Class pojoClass, String title) {
        super(pojoClass, title);
    }
    
    @Override
    public synchronized void setTableItems(Supplier<ObservableList<Element>> producer) {
        try {
            super.setTableItems(producer);
            
            this.wait();
            for(Element element : getAllValues()){
                addListener(element);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(ListenPropertyPojoTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void setPropertyToListen(String propertyToListen, T propertyReference){
        try {
            propertyMethodToListen = pojoClass.getMethod(propertyToListen);
        } catch (NoSuchMethodException | SecurityException ex) {
            Logger.getLogger(ListenPropertyPojoTable.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.propertyReference = propertyReference;
    }
    
    private void addListener(final Element element){
        if(propertyMethodToListen==null) return;
        
        try {
            final Property<T> property = (Property<T>) propertyMethodToListen.invoke(element);
            if(listeners.get(element)==null){
                final ChangeListener<T> changeListener = new ChangeListener<T>() {
                    @Override
                    public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
                        if(newValue.equals(propertyReference)){
                            if(!getAllValues().contains(element)){
                                getAllValues().add(element);
                            }
                        }else{
                            if(getAllValues().contains(element)){
                                getAllValues().remove(element);
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
