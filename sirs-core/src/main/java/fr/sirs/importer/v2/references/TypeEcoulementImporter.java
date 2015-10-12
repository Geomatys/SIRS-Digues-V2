package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefEcoulement;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
class TypeEcoulementImporter extends GenericTypeReferenceImporter<RefEcoulement> {

    @Override
    public String getTableName() {
        return ECOULEMENT.toString();
    }

    @Override
    public Class<RefEcoulement> getElementClass() {
        return RefEcoulement.class;
    }
}
