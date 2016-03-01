package fr.sirs.importer.v2;

import fr.sirs.core.model.Element;
import fr.sirs.importer.AccessDbImporterException;
import java.io.IOException;

/**
 * A class whose aim is to update an already existing document by putting id of
 * another already existing document into it.
 *
 * @author Alexis Manin (Geomatys)
 * @param <T> Target element type : type pointed by the link.
 * @param <U> Type of object which will contain the reference.
 */
public interface Linker<T extends Element, U extends Element> {

    /**
     * @return Type of the object which will pointed by the link.
     */
    Class<T> getTargetClass();

    /**
     * @return Type of the object which will contain the link.
     */
    Class<U> getHolderClass();

    /**
     * Bind target objects to link holders.
     * @throws java.io.IOException If an error occurs while accessing data.
     * @throws fr.sirs.importer.AccessDbImporterException If an error occurs while processing a link.
     */
    void link() throws IOException, AccessDbImporterException;

}
