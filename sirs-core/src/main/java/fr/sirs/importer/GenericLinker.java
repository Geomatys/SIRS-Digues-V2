package fr.sirs.importer;

import com.healthmarketscience.jackcess.Database;
import java.io.IOException;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public abstract class GenericLinker extends GenericImporter {

    public GenericLinker(Database accessDatabase, CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    public abstract void link() throws IOException, AccessDbImporterException;
}