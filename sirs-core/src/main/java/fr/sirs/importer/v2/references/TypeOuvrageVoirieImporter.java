package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefOuvrageVoirie;
import static fr.sirs.importer.DbImporter.TableName.TYPE_OUVRAGE_VOIRIE;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
class TypeOuvrageVoirieImporter extends GenericTypeReferenceImporter<RefOuvrageVoirie> {

    @Override
    public String getTableName() {
        return TYPE_OUVRAGE_VOIRIE.toString();
    }

    @Override
    public Class<RefOuvrageVoirie> getElementClass() {
        return RefOuvrageVoirie.class;
    }
}
