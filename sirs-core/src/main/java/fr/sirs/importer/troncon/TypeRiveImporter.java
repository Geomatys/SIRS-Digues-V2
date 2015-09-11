package fr.sirs.importer.troncon;

import fr.sirs.core.model.RefRive;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeRiveImporter extends GenericTypeReferenceImporter<RefRive> {

    @Override
    public String getTableName() {
        return TYPE_RIVE.toString();
    }

    @Override
    protected Class<RefRive> getOutputClass() {
	return RefRive.class;
    }
}
