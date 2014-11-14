package fr.sirs.importer.theme.document;

import com.healthmarketscience.jackcess.Database;
import fr.sirs.core.component.DocumentRepository;
import fr.sirs.core.model.Document;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.GenericImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.TronconGestionDigueImporter;
import java.io.IOException;
import java.util.Map;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
abstract class GenericDocumentImporter extends GenericImporter {
    protected Map<Integer, Document> documents = null;
    protected DocumentRepository documentRepository;
    protected BorneDigueImporter borneDigueImporter;
    protected SystemeReperageImporter systemeReperageImporter;
    protected TronconGestionDigueImporter tronconGestionDigueImporter;

    private GenericDocumentImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    public GenericDocumentImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final DocumentRepository documentRepository, 
            final BorneDigueImporter borneDigueImporter,
            final SystemeReperageImporter systemeReperageImporter,
            final TronconGestionDigueImporter tronconGestionDigueImporter){
        this(accessDatabase, couchDbConnector);
        this.documentRepository = documentRepository;
        this.borneDigueImporter = borneDigueImporter;
        this.systemeReperageImporter = systemeReperageImporter;
        this.tronconGestionDigueImporter = tronconGestionDigueImporter;
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
