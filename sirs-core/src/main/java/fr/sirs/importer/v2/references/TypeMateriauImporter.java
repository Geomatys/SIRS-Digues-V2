package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefMateriau;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class TypeMateriauImporter extends GenericTypeReferenceImporter<RefMateriau> {

    @Override
    public String getTableName() {
        return TYPE_MATERIAU.toString();
    }

    @Override
    public Class<RefMateriau> getElementClass() {
        return RefMateriau.class;
    }

}
