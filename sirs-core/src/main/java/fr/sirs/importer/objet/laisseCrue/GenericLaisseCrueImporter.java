package fr.sirs.importer.objet.laisseCrue;

import com.healthmarketscience.jackcess.Database;
import fr.sirs.core.model.LaisseCrue;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.IntervenantImporter;
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
abstract class GenericLaisseCrueImporter extends GenericObjetImporter<LaisseCrue> {

    protected final IntervenantImporter intervenantImporter;
    protected final EvenementHydrauliqueImporter evenementHydrauliqueImporter;
    protected final TypeRefHeauImporter typeRefHeauImporter;

    public GenericLaisseCrueImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector, 
            final TronconGestionDigueImporter tronconGestionDigueImporter, 
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter, 
            final IntervenantImporter intervenantImporter, 
            final EvenementHydrauliqueImporter evenementHydrauliqueImporter,
            final SourceInfoImporter typeSourceImporter,
            final TypeRefHeauImporter typeRefHeauImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter,
                typeSourceImporter, null, null, null, null,
                null);
        this.evenementHydrauliqueImporter = evenementHydrauliqueImporter;
        this.typeRefHeauImporter = typeRefHeauImporter;
        this.intervenantImporter = intervenantImporter;
    }
}
