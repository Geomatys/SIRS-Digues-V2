package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefMoyenManipBatardeaux;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.references.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeMoyenManipBatardeauxImporter extends GenericTypeReferenceImporter<RefMoyenManipBatardeaux> {

    @Override
    public String getTableName() {
        return TYPE_MOYEN_MANIP_BATARDEAUX.toString();
    }

    @Override
    protected Class<RefMoyenManipBatardeaux> getElementClass() {
	return RefMoyenManipBatardeaux.class;
    }
}
