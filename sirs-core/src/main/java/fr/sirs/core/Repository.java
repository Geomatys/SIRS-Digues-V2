package fr.sirs.core;

/**
 * An interface which defines interactions with CouchDB database for objects of a 
 * specific type.
 * @author geomatys
 * @param <T> The type of objects managed by this repository.
 */
public interface Repository<T> {
    
    /**
     * Return the class of the managed object type.
     * @return 
     */
    Class<T> getModelClass();
    
    /**
     * Create a new instance of Pojo in memory. No creation in database.
     * @return 
     */
    T create();
}
