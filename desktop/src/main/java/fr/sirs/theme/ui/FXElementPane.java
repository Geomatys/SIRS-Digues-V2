package fr.sirs.theme.ui;

import fr.sirs.core.model.Element;
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
     * Record unbound field changes before saving.
     */
    void preSave();
}
