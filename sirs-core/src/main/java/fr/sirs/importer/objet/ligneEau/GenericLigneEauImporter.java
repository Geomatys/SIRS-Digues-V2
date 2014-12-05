package fr.sirs.importer.objet.ligneEau;

import com.healthmarketscience.jackcess.Database;
import fr.sirs.core.model.LigneEau;
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
abstract class GenericLigneEauImporter extends GenericObjetImporter<LigneEau> {
    
    protected final EvenementHydrauliqueImporter evenementHydrauliqueImporter;
    protected final TypeRefHeauImporter typeRefHeauImporter;

    public GenericLigneEauImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector, 
            final TronconGestionDigueImporter tronconGestionDigueImporter, 
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter, 
            final EvenementHydrauliqueImporter evenementHydrauliqueImporter,
            final TypeRefHeauImporter typeRefHeauImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter, null, 
                null, null, null, null, null, null, null);
        this.evenementHydrauliqueImporter = evenementHydrauliqueImporter;
        this.typeRefHeauImporter = typeRefHeauImporter;
    }
}
