package fr.sirs.importer.v2;

import java.util.Collection;

/**
 *
 * @author Alexis Manin (Geomatys)
 * @param <T> The class inherited by all defined subtypes.
 */
public interface MultipleSubTypes<T> {

    Collection<Class<? extends T>> getSubTypes();
}
