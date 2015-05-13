package fr.sirs.importer.documentTroncon;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.AbstractPositionDocument;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.GenericImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.troncon.TronconGestionDigueImporter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
abstract class GenericPositionDocumentImporter<T extends AbstractPositionDocument> extends GenericImporter {
    
    protected Map<Integer, T> positions = null;
    protected Map<Integer, List<T>> positionsByTronconId = null;
    
    protected TronconGestionDigueImporter tronconGestionDigueImporter;
    protected BorneDigueImporter borneDigueImporter;
    protected SystemeReperageImporter systemeReperageImporter;
    
//    protected boolean computed=false;

    private GenericPositionDocumentImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    public GenericPositionDocumentImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final BorneDigueImporter borneDigueImporter,
            final SystemeReperageImporter systemeReperageImporter){
        this(accessDatabase, couchDbConnector);
        this.tronconGestionDigueImporter = tronconGestionDigueImporter;
        this.borneDigueImporter = borneDigueImporter;
        this.systemeReperageImporter = systemeReperageImporter;
    }
    
//    /**
//     *
//     * @return A map containing all Document instances accessibles from the
//     * internal database identifier (documents may be empty).
//     * @throws IOException
//     * @throws AccessDbImporterException
//     */
//    public Map<Integer, T> getPrecomputedPositions() throws IOException, AccessDbImporterException {
//        if (positions == null)  preCompute();
//        return positions;
//    }
//    
//    /**
//     * 
//     * @return
//     * @throws IOException
//     * @throws AccessDbImporterException 
//     */
//    public Map<Integer, List<T>> getPrecomputedPositionsByTronconId() throws IOException, AccessDbImporterException {
//        if (positionsByTronconId == null)  preCompute();
//        return positionsByTronconId;
//    }
    
    /**
     *
     * @return A map containing all Document instances accessibles from the
     * internal database identifier (documents are mapped).
     * @throws IOException
     * @throws AccessDbImporterException
     */
    public Map<Integer, T> getPositions() throws IOException, AccessDbImporterException {
        if (positions == null)  
//            preCompute();
//        if (!computed)  
            compute();
        return positions;
    }
    
    /**
     * 
     * @return
     * @throws IOException
     * @throws AccessDbImporterException 
     */
    public Map<Integer, List<T>> getPositionsByTronconId() throws IOException, AccessDbImporterException {
        if (positionsByTronconId == null)  
//            preCompute();
//        if (!computed)  
            compute();
        return positionsByTronconId;
    }
    
    /**
     * Registers documents into CouchDb and feed Document list, but do not map
     * database fields
     */
//    protected abstract void preCompute() throws IOException, AccessDbImporterException;
//    protected void preCompute() throws IOException, AccessDbImporterException {
//        throw new UnsupportedOperationException("Do not use system table importers.");
//    }
    
    /**
     * 
     * @param row
     * @return
     * @throws IOException 
     * @throws AccessDbImporterException
     */
    abstract T importRow(final Row row) 
            throws IOException, AccessDbImporterException;
    
    

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        throw new UnsupportedOperationException("Do not use system table importers.");
    }
}
