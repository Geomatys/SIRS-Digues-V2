package fr.sirs.importer;

import com.healthmarketscience.jackcess.Database;
import java.io.IOException;
import java.util.Map;
import org.ektorp.CouchDbConnector;

/**
 * 
 * @author Samuel Andrés (Geomatys)
 * @param <T>
 */
public abstract class GenericTypeImporter<T> extends GenericImporter {
    
    protected Map<Integer, T> types = null;
    
    public GenericTypeImporter(Database accessDatabase, CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    public Map<Integer, T> getTypes() throws IOException, AccessDbImporterException {
        if(types == null) compute();
        return types;
    }
}
