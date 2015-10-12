package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefMoyenManipBatardeaux;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
class TypeMoyenManipBatardeauxImporter extends GenericTypeReferenceImporter<RefMoyenManipBatardeaux> {

    @Override
    public String getTableName() {
        return TYPE_MOYEN_MANIP_BATARDEAUX.toString();
    }

    @Override
    public Class<RefMoyenManipBatardeaux> getElementClass() {
        return RefMoyenManipBatardeaux.class;
    }
}
