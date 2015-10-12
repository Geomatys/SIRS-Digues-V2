package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefOuvrageHydrauliqueAssocie;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
class TypeOuvrageHydrauAssocieImporter extends GenericTypeReferenceImporter<RefOuvrageHydrauliqueAssocie> {

    @Override
    public String getTableName() {
        return TYPE_OUVRAGE_HYDRAU_ASSOCIE.toString();
    }

    @Override
    public Class<RefOuvrageHydrauliqueAssocie> getElementClass() {
        return RefOuvrageHydrauliqueAssocie.class;
    }
}
