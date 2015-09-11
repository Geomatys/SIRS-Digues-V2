package fr.sirs.importer.evenementHydraulique;

import fr.sirs.core.model.RefEvenementHydraulique;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeEvenementHydrauliqueImporter extends GenericTypeReferenceImporter<RefEvenementHydraulique> {

    @Override
    public String getTableName() {
        return TYPE_EVENEMENT_HYDRAU.toString();
    }

    @Override
    protected Class<RefEvenementHydraulique> getOutputClass() {
	return RefEvenementHydraulique.class;
    }
}
