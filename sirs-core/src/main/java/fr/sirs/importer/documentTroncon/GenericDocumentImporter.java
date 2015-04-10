package fr.sirs.importer.documentTroncon;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.AbstractPositionDocument;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.GenericImporter;
import fr.sirs.importer.SystemeReperageImporter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
abstract class GenericDocumentImporter<T extends AbstractPositionDocument> extends GenericImporter {
    
    protected Map<Integer, T> documentTroncons = null;
    protected Map<Integer, List<T>> documentTronconByTronconId = null;
    
    protected BorneDigueImporter borneDigueImporter;
    protected SystemeReperageImporter systemeReperageImporter;
    
    protected boolean computed=false;

    private GenericDocumentImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    public GenericDocumentImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final BorneDigueImporter borneDigueImporter,
            final SystemeReperageImporter systemeReperageImporter){
        this(accessDatabase, couchDbConnector);
        this.borneDigueImporter = borneDigueImporter;
        this.systemeReperageImporter = systemeReperageImporter;
    }
    
    /**
     *
     * @return A map containing all Document instances accessibles from the
     * internal database identifier (documents may be empty).
     * @throws IOException
     * @throws AccessDbImporterException
     */
    public Map<Integer, T> getPrecomputedDocuments() throws IOException, AccessDbImporterException {
        if (documentTroncons == null)  preCompute();
        return documentTroncons;
    }
    
    /**
     * 
     * @return
     * @throws IOException
     * @throws AccessDbImporterException 
     */
    public Map<Integer, List<T>> getPrecomputedDocumentsByTronconId() throws IOException, AccessDbImporterException {
        if (documentTronconByTronconId == null)  preCompute();
        return documentTronconByTronconId;
    }
    
    /**
     *
     * @return A map containing all Document instances accessibles from the
     * internal database identifier (documents are mapped).
     * @throws IOException
     * @throws AccessDbImporterException
     */
    public Map<Integer, T> getDocuments() throws IOException, AccessDbImporterException {
        if (documentTroncons == null)  preCompute();
        if (!computed)  compute();
        return documentTroncons;
    }
    
    /**
     * 
     * @return
     * @throws IOException
     * @throws AccessDbImporterException 
     */
    public Map<Integer, List<T>> getDocumentsByTronconId() throws IOException, AccessDbImporterException {
        if (documentTronconByTronconId == null)  preCompute();
        if (!computed)  compute();
        return documentTronconByTronconId;
    }
    
    /**
     * Registers documents into CouchDb and feed Document list, but do not map
     * database fields
     */
    protected abstract void preCompute() throws IOException, AccessDbImporterException;
    
    /**
     * 
     * @param row
     * @param documentTroncon
     * @return
     * @throws IOException 
     * @throws AccessDbImporterException
     */
    abstract T importRow(final Row row) 
            throws IOException, AccessDbImporterException;
}
