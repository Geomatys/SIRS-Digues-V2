package fr.sirs.importer.v2;

import fr.sirs.core.model.Element;
import fr.sirs.importer.AccessDbImporterException;

/**
 * A class whose aim is to update an already existing document by putting id of
 * another already existing document into it.
 *
 * @author Alexis Manin (Geomatys)
 * @param <U> Type of object which will contain the reference.
 */
public interface Linker<U extends Element> {

    /**
     * @return Type of the object which will contain the link.
     */
    Class<U> getHolderClass();

    /**
     *
     * @param accessHolderId Id of the holder object in source MS-access database.
     * @param holder the object (holder) which will be modified to contain link.
     */
    void link(final Integer accessHolderId, final U holder) throws AccessDbImporterException;

}
