package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefRive;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
class TypeRiveImporter extends GenericTypeReferenceImporter<RefRive> {

    @Override
    public String getTableName() {
        return TYPE_RIVE.toString();
    }

    @Override
    public Class<RefRive> getElementClass() {
        return RefRive.class;
    }
}
