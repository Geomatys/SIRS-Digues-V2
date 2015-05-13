package fr.sirs.importer.objet.desordre;

import com.healthmarketscience.jackcess.Database;
import fr.sirs.core.model.Desordre;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.objet.*;
import fr.sirs.importer.troncon.TronconGestionDigueImporter;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T>
 */
abstract class GenericDesordreImporter extends GenericObjetImporter<Desordre> {

    GenericDesordreImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector, 
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter,
            final SourceInfoImporter typeSourceImporter, 
            final TypeCoteImporter typeCoteImporter, 
            final TypePositionImporter typePositionImporter) {
        super(accessDatabase, couchDbConnector, 
                tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter, 
                typeSourceImporter, typeCoteImporter, typePositionImporter, 
                null, null, null);
    }
}
