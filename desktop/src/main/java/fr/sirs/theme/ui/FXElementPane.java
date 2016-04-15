/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 * 
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
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
     * @param element The new element to edit.
     */
    void setElement(final T element);
    
    /**
     * The element of the panel, as a java-fx property.
     * @return Property containing currently edited bean.
     */
    ObjectProperty<T> elementProperty();
    
    /**
     * Set the pane fields editable or not.
     * @return Property managing input disabling property.
     */
    BooleanProperty disableFieldsProperty();
    
    /**
     * Record unbound field changes before saving.
     * @throws java.lang.Exception If an error happened while updating element attributes.
     */
    void preSave() throws Exception;
}
