package fr.sirs.importer.objet.prestation;

import com.healthmarketscience.jackcess.Database;
import fr.sirs.core.model.Objet;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.troncon.TronconGestionDigueImporter;
import fr.sirs.importer.objet.*;
import fr.sirs.importer.theme.document.related.marche.MarcheImporter;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T>
 */
abstract class GenericPrestationImporter<T extends Objet> extends GenericObjetImporter<T> {
    
    protected final MarcheImporter marcheImporter;
    
    public GenericPrestationImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector, 
            final TronconGestionDigueImporter tronconGestionDigueImporter, 
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter,
            final MarcheImporter marcheImporter,
            final SourceInfoImporter sourceInfoImporter,
            final TypePositionImporter typePositionImporter,
            final TypeCoteImporter typeCoteImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter,
                sourceInfoImporter, typeCoteImporter, typePositionImporter, 
                null, null, null);
        this.marcheImporter = marcheImporter;
    }
}
