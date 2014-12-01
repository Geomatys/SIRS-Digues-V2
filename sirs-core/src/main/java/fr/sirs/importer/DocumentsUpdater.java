package fr.sirs.importer;

import java.io.IOException;

/**
 * In case an importer serializes the data it imports into a CouchDbRepository, 
 * the objects it serializes may be modified after it send them to couchDb.
 * 
 * @author Samuel Andrés
 */
public interface DocumentsUpdater {
    
    /**
     * This method purpose is to allow modifications to be registered into 
     * couchDb after their modification.
     * @throws java.io.IOException
     * @throws fr.sirs.importer.AccessDbImporterException
     */
    void update() throws IOException, AccessDbImporterException;
}
