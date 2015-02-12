package fr.sirs.theme.ui;

import fr.sirs.core.model.Element;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;

/**
 *
 * @author Samuel Andrés
 * @author Alexis Manin
 * @param <T> Modèle à éditer
 */
public interface FXElementPane<T extends Element> {
    
    /**
     * Change the element displayed by the pane.
     * @param element 
     */
    void setElement(final T element);
    
    /**
     * The element of the panel, as a java-fx property.
     * @return 
     */
    ObjectProperty<T> elementProperty();
    
    /**
     * Set the pane fields editable or not.
     * @return 
     */
    BooleanProperty disableFieldsProperty();
    
    /**
     * Record unbound field changes before saving.
     * @throws java.lang.Exception If an error happened while updating element attributes.
     */
    void preSave() throws Exception;
}
