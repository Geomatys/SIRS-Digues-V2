package fr.sirs.theme.ui;

import fr.sirs.core.model.Element;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Alexis Manin (Geomatys)
 * @param <T>
 */
public abstract class AbstractFXElementPane<T extends Element> extends BorderPane implements FXElementPane<T> {

    protected final ObjectProperty<T> elementProperty = new SimpleObjectProperty<>();
    private final BooleanProperty editableProperty = new SimpleBooleanProperty();
    
    @Override
    public void setElement(T element) {
        elementProperty.set(element);
    }

    @Override
    public ObjectProperty<T> elementProperty() {
        return elementProperty;
    }
    
    @Override
    public BooleanProperty disableFieldsProperty() {
        return editableProperty;
    }
    
}
