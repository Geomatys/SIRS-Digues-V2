package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefRevetement;
import static fr.sirs.importer.DbImporter.TableName.TYPE_REVETEMENT;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
class TypeRevetementImporter extends GenericTypeReferenceImporter<RefRevetement> {

    @Override
    public String getTableName() {
        return TYPE_REVETEMENT.toString();
    }

    @Override
    public Class<RefRevetement> getElementClass() {
        return RefRevetement.class;
    }
}
