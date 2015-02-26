package fr.sirs.importer.objet.reseau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.ObjetReseau;
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
abstract class GenericReseauImporter<T extends ObjetReseau> extends GenericObjetImporter<T> {

    public GenericReseauImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector, 
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter, 
            final SourceInfoImporter typeSourceImporter, 
            final TypeCoteImporter typeCoteImporter, 
            final TypePositionImporter typePositionImporter, 
            final TypeNatureImporter typeNatureImporter) {
        super(accessDatabase, couchDbConnector, 
                systemeReperageImporter, borneDigueImporter,
                typeSourceImporter, typeCoteImporter, 
                typePositionImporter, null, typeNatureImporter, 
                null);
    }
    
    /**
     * 
     * @param row
     * @return The POJO mapping the row.
     * @throws IOException
     * @throws AccessDbImporterException 
     */
    public abstract T importRow(final Row row) throws IOException, AccessDbImporterException;
}
