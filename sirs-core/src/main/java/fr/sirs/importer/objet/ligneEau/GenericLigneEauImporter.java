package fr.sirs.importer.objet.ligneEau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.LigneEau;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.evenementHydraulique.EvenementHydrauliqueImporter;
import fr.sirs.importer.objet.*;
import java.io.IOException;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T>
 */
abstract class GenericLigneEauImporter extends GenericObjetImporter<LigneEau> {
    
    protected final EvenementHydrauliqueImporter evenementHydrauliqueImporter;
    protected final TypeRefHeauImporter typeRefHeauImporter;

    public GenericLigneEauImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector, 
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter, 
            final EvenementHydrauliqueImporter evenementHydrauliqueImporter,
            final TypeRefHeauImporter typeRefHeauImporter) {
        super(accessDatabase, couchDbConnector, 
                systemeReperageImporter, borneDigueImporter,
                null, null, null, null, null, null);
        this.evenementHydrauliqueImporter = evenementHydrauliqueImporter;
        this.typeRefHeauImporter = typeRefHeauImporter;
    }
    
    /**
     * 
     * @param row
     * @return The POJO mapping the row.
     * @throws IOException
     * @throws AccessDbImporterException 
     */
    public abstract LigneEau importRow(final Row row) throws IOException, AccessDbImporterException;
}
