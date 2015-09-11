package fr.sirs.importer;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class AccessDbImporterException extends Exception {

    public AccessDbImporterException(final String message) {
        super(message);
    }

    public AccessDbImporterException(final String message, Exception cause) {
        super(message, cause);
    }
}
