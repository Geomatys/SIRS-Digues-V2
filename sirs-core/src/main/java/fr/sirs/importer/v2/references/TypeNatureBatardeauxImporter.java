package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefNatureBatardeaux;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
class TypeNatureBatardeauxImporter extends GenericTypeReferenceImporter<RefNatureBatardeaux> {

    @Override
    public String getTableName() {
        return TYPE_NATURE_BATARDEAUX.toString();
    }

    @Override
    protected Class<RefNatureBatardeaux> getElementClass() {
        return RefNatureBatardeaux.class;
    }
}
