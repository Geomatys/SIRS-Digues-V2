package fr.sirs.importer.objet;

import com.healthmarketscience.jackcess.Database;
import fr.sirs.core.model.Objet;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.GenericImporter;
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
public abstract class GenericStructureImporter<T extends Objet> extends GenericImporter {

    protected TronconGestionDigueImporter tronconGestionDigueImporter;
    protected SystemeReperageImporter systemeReperageImporter;
    protected BorneDigueImporter borneDigueImporter;
    protected OrganismeImporter organismeImporter;
    protected TypeSourceImporter typeSourceImporter;
    protected TypeCoteImporter typeCoteImporter;
    protected TypePositionImporter typePositionImporter;
    protected TypeMateriauImporter typeMateriauImporter;
    protected TypeNatureImporter typeNatureImporter;
    protected TypeFonctionImporter typeFonctionImporter;
    
    private GenericStructureImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    public GenericStructureImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final TronconGestionDigueImporter tronconGestionDigueImporter, 
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter, 
            final OrganismeImporter organismeImporter,
            final TypeSourceImporter typeSourceImporter,
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
        this.typeSourceImporter = typeSourceImporter;
        this.typeCoteImporter = typeCoteImporter;
        this.typePositionImporter = typePositionImporter;
        this.typeMateriauImporter = typeMateriauImporter;
        this.typeNatureImporter = typeNatureImporter;
        this.typeFonctionImporter = typeFonctionImporter;
    }
    
    public abstract Map<Integer, T> getStructures() throws IOException, AccessDbImporterException;
    public abstract Map<Integer, List<T>> getStructuresByTronconId() throws IOException, AccessDbImporterException;
}
