package fr.sirs.util;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public interface SaveableConfiguration {

    /** Action which persists edited confirguration.
     * @throws java.lang.Exception
     */
    void save() throws Exception;

    /**
     *
     * @return A title to display to user to identify current configuration.
     */
    String getTitle();
}
