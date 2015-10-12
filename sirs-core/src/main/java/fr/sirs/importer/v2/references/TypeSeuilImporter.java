package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefSeuil;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
class TypeSeuilImporter extends GenericTypeReferenceImporter<RefSeuil> {

    @Override
    public String getTableName() {
        return TYPE_SEUIL.toString();
    }

    @Override
    public Class<RefSeuil> getElementClass() {
        return RefSeuil.class;
    }
}
