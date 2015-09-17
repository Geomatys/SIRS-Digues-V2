package fr.sirs.importer.objet.reseau;

import fr.sirs.importer.v2.references.TypeCoteImporter;
import com.healthmarketscience.jackcess.Database;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.importer.v2.linear.BorneDigueImporter;
import fr.sirs.importer.v2.linear.SystemeReperageImporter;
import fr.sirs.importer.objet.*;
import fr.sirs.importer.v2.linear.TronconGestionDigueImporter;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T>
 */
abstract class GenericReseauImporter<T extends ObjetReseau> extends GenericObjetImporter<T> {

    public GenericReseauImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector, 
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter, 
            final SourceInfoImporter typeSourceImporter, 
            final TypeCoteImporter typeCoteImporter, 
            final TypePositionImporter typePositionImporter, 
            final TypeMateriauImporter typeMateriauImporter,
            final TypeNatureImporter typeNatureImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter,
                typeSourceImporter, typeCoteImporter, 
                typePositionImporter, typeMateriauImporter, typeNatureImporter, 
                null);
    }
}
