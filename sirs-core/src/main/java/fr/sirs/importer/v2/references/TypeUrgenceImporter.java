package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefUrgence;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
class TypeUrgenceImporter extends GenericTypeReferenceImporter<RefUrgence> {

    @Override
    public String getTableName() {
        return TYPE_URGENCE.toString();
    }

    @Override
    protected Class<RefUrgence> getElementClass() {
        return RefUrgence.class;
    }
}
