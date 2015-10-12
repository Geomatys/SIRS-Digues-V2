package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefCote;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class TypeCoteImporter extends GenericTypeReferenceImporter<RefCote> {

    @Override
    public String getTableName() {
        return TYPE_COTE.toString();
    }

    @Override
    public Class<RefCote> getElementClass() {
        return RefCote.class;
    }
}
