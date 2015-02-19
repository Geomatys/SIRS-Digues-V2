package fr.sirs.importer.objet.desordre;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Desordre;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.objet.*;
import java.io.IOException;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T>
 */
abstract class GenericDesordreImporter extends GenericObjetImporter<Desordre> {

    GenericDesordreImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector, 
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter,
            final SourceInfoImporter typeSourceImporter, 
            final TypeCoteImporter typeCoteImporter, 
            final TypePositionImporter typePositionImporter) {
        super(accessDatabase, couchDbConnector, 
                systemeReperageImporter, borneDigueImporter, 
                typeSourceImporter, typeCoteImporter, typePositionImporter, 
                null, null, null);
    }
    
    /**
     * 
     * @param row
     * @return The POJO mapping the row.
     * @throws IOException
     * @throws AccessDbImporterException 
     */
    public abstract Desordre importRow(final Row row) throws IOException, AccessDbImporterException;
}
