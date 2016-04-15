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
package fr.sirs.core.component;

import fr.sirs.core.model.Element;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Component receiving notifications for events sent by {@link DocumentChangeEmiter}.
 *
 * @author Alexis Manin (Geomatys)
 */
public interface DocumentListener {

    /**
     * Called when a set of elements have been added in database.
     * @param added Set of added elements, sorted by type.
     */
    void documentCreated(Map<Class, List<Element>> added);

    /**
     * Called when a set of elements have been updated in database.
     * @param changed Set of updated elements, sorted by type.
     */
    void documentChanged(Map<Class, List<Element>> changed);

    /**
     * Called when a set of elements have been deleted from database.
     * @param deleted Ids of removed elements.
     */
    void documentDeleted(Set<String> deleted);
}
