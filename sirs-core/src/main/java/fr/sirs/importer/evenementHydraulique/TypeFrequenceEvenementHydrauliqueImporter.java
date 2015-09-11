package fr.sirs.importer.evenementHydraulique;

import fr.sirs.core.model.RefFrequenceEvenementHydraulique;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeFrequenceEvenementHydrauliqueImporter extends GenericTypeReferenceImporter<RefFrequenceEvenementHydraulique> {

    @Override
    public String getTableName() {
        return TYPE_FREQUENCE_EVENEMENT_HYDRAU.toString();
    }

    @Override
    protected Class<RefFrequenceEvenementHydraulique> getOutputClass() {
	return RefFrequenceEvenementHydraulique.class;
    }
}
