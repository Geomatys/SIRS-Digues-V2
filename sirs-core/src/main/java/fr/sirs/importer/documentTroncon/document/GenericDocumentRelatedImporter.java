package fr.sirs.importer.documentTroncon.document;

import fr.sirs.importer.*;
import com.healthmarketscience.jackcess.Database;
import java.io.IOException;
import java.util.Map;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T>
 */
public abstract class GenericDocumentRelatedImporter<T extends Object> extends GenericImporter  implements DocumentsUpdatable {

    protected Map<Integer, T> related = null;
    
    public GenericDocumentRelatedImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    /**
     * A map containing all Document related instances accessibles from their
     * internal database identifier.
     * @return
     * @throws IOException
     * @throws AccessDbImporterException 
     */
    public Map<Integer, T> getRelated() throws IOException, AccessDbImporterException {
        if (related == null)  compute();
        return related;
    }

    @Override
    public void update() throws IOException, AccessDbImporterException {
        if(related==null) compute();
        couchDbConnector.executeBulk(related.values());
    }
}
