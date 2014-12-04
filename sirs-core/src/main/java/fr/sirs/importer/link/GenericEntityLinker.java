package fr.sirs.importer.link;

import com.healthmarketscience.jackcess.Database;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.GenericLinker;
import java.io.IOException;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public abstract class GenericEntityLinker extends GenericLinker {

    public GenericEntityLinker(Database accessDatabase, CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    @Override
    public void link() throws IOException, AccessDbImporterException{
        compute();
    }
}
