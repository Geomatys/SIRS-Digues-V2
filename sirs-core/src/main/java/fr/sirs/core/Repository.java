package fr.sirs.core;

/**
 * An interface which defines interactions with CouchDB database for objects of a 
 * specific type.
 * @author geomatys
 * @param <T> The type of objects managed by this repository.
 */
public interface Repository<T> {
    
    /**
     * @return the class of the managed object type.
     */
    Class<T> getModelClass();
    
    /**
     * Create a new instance of Pojo in memory. No creation in database.
     * @return A new, empty object.
     */
    T create();
}
