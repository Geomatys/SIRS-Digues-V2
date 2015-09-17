package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefRive;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.references.GenericTypeReferenceImporter;

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
    protected Class<RefRive> getDocumentClass() {
	return RefRive.class;
    }
}
