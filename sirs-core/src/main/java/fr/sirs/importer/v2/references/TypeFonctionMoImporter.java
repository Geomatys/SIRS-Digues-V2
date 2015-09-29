package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefFonctionMaitreOeuvre;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
class TypeFonctionMoImporter extends GenericTypeReferenceImporter<RefFonctionMaitreOeuvre> {

    @Override
    public String getTableName() {
        return TYPE_FONCTION_MO.toString();
    }

    @Override
    protected Class<RefFonctionMaitreOeuvre> getElementClass() {
        return RefFonctionMaitreOeuvre.class;
    }
}
