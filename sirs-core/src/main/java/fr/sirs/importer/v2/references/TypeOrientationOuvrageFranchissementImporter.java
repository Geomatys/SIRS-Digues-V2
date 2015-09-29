package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefOrientationOuvrage;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
class TypeOrientationOuvrageFranchissementImporter extends GenericTypeReferenceImporter<RefOrientationOuvrage> {

    @Override
    public String getTableName() {
        return TYPE_ORIENTATION_OUVRAGE_FRANCHISSEMENT.toString();
    }

    @Override
    protected Class<RefOrientationOuvrage> getElementClass() {
        return RefOrientationOuvrage.class;
    }
}
