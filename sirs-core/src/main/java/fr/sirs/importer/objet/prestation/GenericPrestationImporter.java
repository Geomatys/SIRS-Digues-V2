package fr.sirs.importer.objet.prestation;

import fr.sirs.importer.v2.references.TypeCoteImporter;
import com.healthmarketscience.jackcess.Database;
import fr.sirs.core.model.Prestation;
import fr.sirs.importer.v2.linear.BorneDigueImporter;
import fr.sirs.importer.v2.linear.SystemeReperageImporter;
import fr.sirs.importer.objet.*;
import fr.sirs.importer.documentTroncon.document.marche.MarcheImporter;
import fr.sirs.importer.v2.linear.TronconGestionDigueImporter;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T>
 */
abstract class GenericPrestationImporter extends GenericObjetImporter<Prestation> {
    
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
