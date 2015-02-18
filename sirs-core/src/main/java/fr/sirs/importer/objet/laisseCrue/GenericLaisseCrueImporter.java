package fr.sirs.importer.objet.laisseCrue;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.LaisseCrue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.IntervenantImporter;
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
abstract class GenericLaisseCrueImporter extends GenericObjetImporter<LaisseCrue> {

    protected final IntervenantImporter intervenantImporter;
    protected final EvenementHydrauliqueImporter evenementHydrauliqueImporter;
    protected final TypeRefHeauImporter typeRefHeauImporter;

    public GenericLaisseCrueImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector, 
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter, 
            final IntervenantImporter intervenantImporter, 
            final EvenementHydrauliqueImporter evenementHydrauliqueImporter,
            final SourceInfoImporter typeSourceImporter,
            final TypeRefHeauImporter typeRefHeauImporter) {
        super(accessDatabase, couchDbConnector, 
                systemeReperageImporter, borneDigueImporter,
                typeSourceImporter, null, null, null, null,
                null);
        this.evenementHydrauliqueImporter = evenementHydrauliqueImporter;
        this.typeRefHeauImporter = typeRefHeauImporter;
        this.intervenantImporter = intervenantImporter;
    }
    
    /**
     * 
     * @param row
     * @return The POJO mapping the row.
     * @throws IOException
     * @throws AccessDbImporterException 
     */
    public abstract LaisseCrue importRow(final Row row) throws IOException, AccessDbImporterException;
}
