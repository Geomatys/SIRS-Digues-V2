package fr.sirs.theme.ui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.stage.Stage;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * 
 * @param <T> The type of the retrieved element.
 */
public abstract class PojoTableChoiceStage<T> extends Stage {

    protected final ObjectProperty<T> retrievedElement = new SimpleObjectProperty<>();
    
    public ObjectProperty<T> getRetrievedElement(){
        return retrievedElement;
    }
}
