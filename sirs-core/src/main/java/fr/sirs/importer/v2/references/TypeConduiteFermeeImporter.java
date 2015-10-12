package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefConduiteFermee;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
class TypeConduiteFermeeImporter extends GenericTypeReferenceImporter<RefConduiteFermee> {

    @Override
    public String getTableName() {
        return TYPE_CONDUITE_FERMEE.toString();
    }

    @Override
    public Class<RefConduiteFermee> getElementClass() {
        return RefConduiteFermee.class;
    }
}
