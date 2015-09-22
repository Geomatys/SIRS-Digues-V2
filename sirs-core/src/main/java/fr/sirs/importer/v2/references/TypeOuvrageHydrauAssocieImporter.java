package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefOuvrageHydrauliqueAssocie;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.references.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeOuvrageHydrauAssocieImporter extends GenericTypeReferenceImporter<RefOuvrageHydrauliqueAssocie> {

    @Override
    public String getTableName() {
        return TYPE_OUVRAGE_HYDRAU_ASSOCIE.toString();
    }

    @Override
    protected Class<RefOuvrageHydrauliqueAssocie> getElementClass() {
	return RefOuvrageHydrauliqueAssocie.class;
    }
}
