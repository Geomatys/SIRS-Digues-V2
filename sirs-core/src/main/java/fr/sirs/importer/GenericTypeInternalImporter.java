package fr.sirs.importer;

import com.healthmarketscience.jackcess.Database;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés
 * @param <T>
 */
public abstract class GenericTypeInternalImporter<T extends Class> extends GenericTypeImporter<T> {

    protected GenericTypeInternalImporter(Database accessDatabase, CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
}
