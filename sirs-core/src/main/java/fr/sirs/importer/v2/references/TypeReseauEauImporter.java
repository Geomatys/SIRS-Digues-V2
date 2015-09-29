package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefReseauHydroCielOuvert;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
class TypeReseauEauImporter extends GenericTypeReferenceImporter<RefReseauHydroCielOuvert> {

    @Override
    public String getTableName() {
        return TYPE_RESEAU_EAU.toString();
    }

    @Override
    protected Class<RefReseauHydroCielOuvert> getElementClass() {
        return RefReseauHydroCielOuvert.class;
    }
}
