/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.theme.document;

import com.healthmarketscience.jackcess.Database;
import fr.sirs.core.model.Document;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.GenericImporter;
import java.io.IOException;
import java.util.Map;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
abstract class GenericDocumentImporter extends GenericImporter {
    protected Map<Integer, Document> documents = null;

    GenericDocumentImporter(Database accessDatabase, CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    /**
     *
     * @return A map containing all Document instances accessibles from the
     * internal database identifier.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    public Map<Integer, Document> getDocuments() throws IOException, AccessDbImporterException {
        if (documents == null)  compute();
        return documents;
    }
}
