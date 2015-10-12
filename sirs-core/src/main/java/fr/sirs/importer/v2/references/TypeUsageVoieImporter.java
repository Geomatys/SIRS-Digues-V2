package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefUsageVoie;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
class TypeUsageVoieImporter extends GenericTypeReferenceImporter<RefUsageVoie> {

    @Override
    public String getTableName() {
        return TYPE_USAGE_VOIE.toString();
    }

    @Override
    public Class<RefUsageVoie> getElementClass() {
        return RefUsageVoie.class;
    }
}
