package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefPosition;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class TypePositionImporter extends GenericTypeReferenceImporter<RefPosition> {

    @Override
    public String getTableName() {
        return TYPE_POSITION.toString();
    }

    @Override
    protected Class<RefPosition> getElementClass() {
        return RefPosition.class;
    }
}
