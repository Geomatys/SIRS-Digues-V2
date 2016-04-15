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
package fr.sirs.importer.v2;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Element;

/**
 * An importer designed for document update. The aim is to create and fill an
 * element (of type T) which is not a CouchDB document, but a sub-structure of a
 * document. Once filled, this element will be added to its parent document.
 * Finally, the parent document will be updated.
 *
 * @author Alexis Manin (Geomatys)
 *
 * @param <T> Type of the element which will be created and filled by this importer
 * @param <U> Type of the document which will be updated with computed element.
 */
public abstract class AbstractUpdater<T extends Element, U extends Element> extends AbstractImporter<T> {

    /**
     * Once a row has been computed and its parent retrieved, we ask implementation
     * to make the binding between the two objects.
     * @param container The parent document to update.
     * @param toPut The computed element to bind with its parent.
     */
    public abstract void put(final U container, final T toPut);

    /**
     * Get document which must be updated with given row data.
     * @param rowId Id of the input row.
     * @param input The row which has been imported.
     * @param output The object in which row has been imported.
     * @return A couchDB document which will contain output element created from row.
     */
    protected abstract U getDocument(final Object rowId, final Row input, T output);

    @Override
    protected U prepareToPost(Object rowId, Row row, T output) {
        // Once a new element is computed, we add it in its parent.
        final U document = getDocument(rowId, row, output);
        if (document != null)
            put(document, output);
        return document;
    }
}
