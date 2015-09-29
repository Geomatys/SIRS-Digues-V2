package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefProprietaire;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
class TypeProprietaireImporter extends GenericTypeReferenceImporter<RefProprietaire> {

    @Override
    public String getTableName() {
        return TYPE_PROPRIETAIRE.toString();
    }

    @Override
    protected Class<RefProprietaire> getElementClass() {
        return RefProprietaire.class;
    }
}
