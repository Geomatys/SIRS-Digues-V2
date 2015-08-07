package fr.sirs.theme.ui;

import fr.sirs.core.model.Element;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.stage.Stage;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public abstract class PojoTableChoiceStage extends Stage {

    protected ObjectProperty<Element> retrievedElement = new SimpleObjectProperty<>();
    
    public ObjectProperty<Element> getRetrievedElement(){
        return retrievedElement;
    }
}
