package fr.sirs.importer.v2;

import fr.sirs.core.model.Element;

/**
 * Modify an element after it's been filled with source ms-access row data, but
 * before it's updated on CouchDB.
 *
 * @author Alexis Manin (Geomatys)
 * @param <T> Type of the document to modify.
 */
public interface ElementModifier<T extends Element> {

    /**
     * @return type for the object to create at import.
     */
    Class<T> getDocumentClass();

    /**
     *
     * @param outputData Object to alter.
     */
    void modify(final T outputData);
}
