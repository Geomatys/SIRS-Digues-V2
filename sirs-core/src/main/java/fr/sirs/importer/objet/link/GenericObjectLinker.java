package fr.sirs.importer.objet.link;

import com.healthmarketscience.jackcess.Database;
import fr.sirs.importer.AccessDbImporterException;
import java.io.IOException;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
abstract class GenericObjectLinker extends fr.sirs.importer.GenericImporter {

    public GenericObjectLinker(Database accessDatabase, CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    public abstract void link() throws IOException, AccessDbImporterException;
}
