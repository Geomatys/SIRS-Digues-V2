package fr.sirs.importer.objet;

import com.healthmarketscience.jackcess.Database;
import fr.sirs.core.model.Objet;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.GenericImporter;
import fr.sirs.importer.IntervenantImporter;
import fr.sirs.importer.OrganismeImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.TronconGestionDigueImporter;
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

    protected Map<Integer, T> structures = null;
    protected Map<Integer, List<T>> structuresByTronconId = null;
    
    protected TronconGestionDigueImporter tronconGestionDigueImporter;
    protected SystemeReperageImporter systemeReperageImporter;
    protected BorneDigueImporter borneDigueImporter;
    protected OrganismeImporter organismeImporter;
    protected IntervenantImporter intervenantImporter;
    
    protected SourceInfoImporter typeSourceImporter;
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
            final OrganismeImporter organismeImporter,
            final IntervenantImporter intervenantImporter,
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
        this.organismeImporter = organismeImporter;
        this.intervenantImporter = intervenantImporter;
        this.typeSourceImporter = typeSourceImporter;
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
    public Map<Integer, T> getStructures() throws IOException, AccessDbImporterException {
        if (this.structures == null) {
            compute();
        }
        return structures;
    }

    /**
     *
     * @return A map containing all T instances accessibles from the
     * internal database <em>TronconDigue</em> identifier.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    public Map<Integer, List<T>> getStructuresByTronconId() throws IOException, AccessDbImporterException {
        if (this.structuresByTronconId == null) {
            compute();
        }
        return this.structuresByTronconId;
    }
}
