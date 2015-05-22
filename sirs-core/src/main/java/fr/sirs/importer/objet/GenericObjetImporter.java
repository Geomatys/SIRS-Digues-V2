package fr.sirs.importer.objet;

import fr.sirs.importer.TypeCoteImporter;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Objet;
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
 * @param <T>
 */
public abstract class GenericObjetImporter<T extends Objet> extends GenericImporter {

    protected Map<Integer, T> objets = null;
    protected Map<Integer, List<T>> objetsByTronconId = null;
    
    protected TronconGestionDigueImporter tronconGestionDigueImporter;
    protected SystemeReperageImporter systemeReperageImporter;
    protected BorneDigueImporter borneDigueImporter;
    
    protected SourceInfoImporter sourceInfoImporter;
    protected TypeCoteImporter typeCoteImporter;
    protected TypePositionImporter typePositionImporter;
    protected TypeMateriauImporter typeMateriauImporter;
    protected TypeNatureImporter typeNatureImporter;
    protected TypeFonctionImporter typeFonctionImporter;
    
    private GenericObjetImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    public GenericObjetImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter, 
            final SourceInfoImporter typeSourceImporter,
            final TypeCoteImporter typeCoteImporter,
            final TypePositionImporter typePositionImporter,
            final TypeMateriauImporter typeMateriauImporter,
            final TypeNatureImporter typeNatureImporter,
            final TypeFonctionImporter typeFonctionImporter) {
        this(accessDatabase, couchDbConnector);
        this.tronconGestionDigueImporter = tronconGestionDigueImporter;
        this.systemeReperageImporter = systemeReperageImporter;
        this.borneDigueImporter = borneDigueImporter;
        this.sourceInfoImporter = typeSourceImporter;
        this.typeCoteImporter = typeCoteImporter;
        this.typePositionImporter = typePositionImporter;
        this.typeMateriauImporter = typeMateriauImporter;
        this.typeNatureImporter = typeNatureImporter;
        this.typeFonctionImporter = typeFonctionImporter;
    }

    /**
     *
     * @return A map containing all T instances accessibles from the
     * internal database identifier.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    public Map<Integer, T> getById() throws IOException, AccessDbImporterException {
        if (objets == null) {
            compute();
        }
        return objets;
    }

    /**
     *
     * @return A map containing all T instances accessibles from the
     * internal database <em>TronconDigue</em> identifier.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    public Map<Integer, List<T>> getByTronconId() throws IOException, AccessDbImporterException {
        if (objetsByTronconId == null) {
            compute();
        }
        return objetsByTronconId;
    }
    
    /**
     * 
     * @param row
     * @return The POJO mapping the row.
     * @throws IOException
     * @throws AccessDbImporterException 
     */
    public abstract T importRow(final Row row) throws IOException, AccessDbImporterException;

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        throw new UnsupportedOperationException("Do not use system table importers.");
    }
    
    public void update() throws IOException, AccessDbImporterException{
        couchDbConnector.executeBulk(getById().values());
    }
}
