package fr.sirs.importer.theme.document;

import com.healthmarketscience.jackcess.Database;
import fr.sirs.core.model.DocumentTroncon;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.GenericImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.troncon.TronconGestionDigueImporter;
import java.io.IOException;
import java.util.Map;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
abstract class GenericDocumentImporter extends GenericImporter {
    
    protected Map<Integer, DocumentTroncon> documentTronconAssociations = null;
    
//    protected DocumentRepository documentRepository;
    
    protected BorneDigueImporter borneDigueImporter;
    protected SystemeReperageImporter systemeReperageImporter;
    protected TronconGestionDigueImporter tronconGestionDigueImporter;
    
    protected boolean computed=false;

    private GenericDocumentImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    public GenericDocumentImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final BorneDigueImporter borneDigueImporter,
            final SystemeReperageImporter systemeReperageImporter,
            final TronconGestionDigueImporter tronconGestionDigueImporter){
        this(accessDatabase, couchDbConnector);
        this.borneDigueImporter = borneDigueImporter;
        this.systemeReperageImporter = systemeReperageImporter;
        this.tronconGestionDigueImporter = tronconGestionDigueImporter;
    }
    
    /**
     *
     * @return A map containing all Document instances accessibles from the
     * internal database identifier (documents may be empty).
     * @throws IOException
     * @throws AccessDbImporterException
     */
    public Map<Integer, DocumentTroncon> getPrecomputedDocuments() throws IOException, AccessDbImporterException {
        if (documentTronconAssociations == null)  preCompute();
        return documentTronconAssociations;
    }
    
    /**
     *
     * @return A map containing all Document instances accessibles from the
     * internal database identifier (documents are mapped).
     * @throws IOException
     * @throws AccessDbImporterException
     */
    public Map<Integer, DocumentTroncon> getDocuments() throws IOException, AccessDbImporterException {
        if (documentTronconAssociations == null)  preCompute();
        if (!computed)  compute();
        return documentTronconAssociations;
    }
    
    /**
     * Registers documents into CouchDb and feed Document list, but do not map
     * database fields
     */
    protected abstract void preCompute() throws IOException, AccessDbImporterException;
}
