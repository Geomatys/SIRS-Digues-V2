package fr.sirs.importer.v2;

/**
 * If a {@link GenericImporter} implements this interface, the output object on
 * which it works (the pojo to fill) will not be a newly created element, but one
 * already imported and read from CouchDB database. Also, the importer will not be
 * registered in {@link ImportContext#importers}, but in {@link ImportContext#updaters}.
 *
 * @author Alexis Manin (Geomatys)
 */
public interface Updater {

}
