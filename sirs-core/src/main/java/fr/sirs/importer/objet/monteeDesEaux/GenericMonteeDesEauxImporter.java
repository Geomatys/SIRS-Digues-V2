package fr.sirs.importer.objet.monteeDesEaux;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.MonteeEaux;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.troncon.TronconGestionDigueImporter;
import fr.sirs.importer.evenementHydraulique.EvenementHydrauliqueImporter;
import fr.sirs.importer.objet.*;
import java.io.IOException;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T>
 */
abstract class GenericMonteeDesEauxImporter extends GenericObjetImporter<MonteeEaux> {
    
    protected final EvenementHydrauliqueImporter evenementHydrauliqueImporter;

    public GenericMonteeDesEauxImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector, 
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter, 
            final EvenementHydrauliqueImporter evenementHydrauliqueImporter) {
        super(accessDatabase, couchDbConnector, 
                systemeReperageImporter, borneDigueImporter,
                null, null, null, null, null, null);
        this.evenementHydrauliqueImporter = evenementHydrauliqueImporter;
    }
    
    /**
     * 
     * @param row
     * @return The POJO mapping the row.
     * @throws IOException
     * @throws AccessDbImporterException 
     */
    public abstract MonteeEaux importRow(final Row row) throws IOException, AccessDbImporterException;
}
