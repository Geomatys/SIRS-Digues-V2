package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefOuvrageFranchissement;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
class TypeOuvrageFranchissementImporter extends GenericTypeReferenceImporter<RefOuvrageFranchissement> {

    @Override
    public String getTableName() {
        return TYPE_OUVRAGE_FRANCHISSEMENT.toString();
    }

    @Override
    public Class<RefOuvrageFranchissement> getElementClass() {
        return RefOuvrageFranchissement.class;
    }

}
