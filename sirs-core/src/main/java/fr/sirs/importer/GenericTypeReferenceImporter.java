package fr.sirs.importer;

import com.healthmarketscience.jackcess.Database;
import fr.sirs.core.model.ReferenceType;
import org.ektorp.CouchDbConnector;

/**
 * 
 * @author Samuel Andrés (Geomatys)
 * @param <T>
 */
public abstract class GenericTypeReferenceImporter<T extends ReferenceType> extends GenericTypeImporter<T> {

    public GenericTypeReferenceImporter(Database accessDatabase, CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
}
