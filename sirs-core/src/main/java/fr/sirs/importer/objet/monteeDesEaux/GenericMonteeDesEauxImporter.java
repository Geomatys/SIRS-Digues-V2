package fr.sirs.importer.objet.monteeDesEaux;

import com.healthmarketscience.jackcess.Database;
import fr.sirs.core.model.MonteeEaux;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.TronconGestionDigueImporter;
import fr.sirs.importer.evenementHydraulique.EvenementHydrauliqueImporter;
import fr.sirs.importer.objet.*;
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
            final TronconGestionDigueImporter tronconGestionDigueImporter, 
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter, 
            final EvenementHydrauliqueImporter evenementHydrauliqueImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter,
                null, null, null, null, null, null);
        this.evenementHydrauliqueImporter = evenementHydrauliqueImporter;
    }
}
