package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefTypeGlissiere;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
class TypeGlissiereImporter extends GenericTypeReferenceImporter<RefTypeGlissiere> {

    @Override
    public String getTableName() {
        return TYPE_GLISSIERE.toString();
    }

    @Override
    public Class<RefTypeGlissiere> getElementClass() {
        return RefTypeGlissiere.class;
    }
}
