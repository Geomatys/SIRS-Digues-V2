package fr.sirs.importer.objet;

import fr.sirs.core.model.RefPosition;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.references.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class TypePositionImporter extends GenericTypeReferenceImporter<RefPosition> {

    @Override
    public String getTableName() {
        return TYPE_POSITION.toString();
    }

    @Override
    protected Class<RefPosition> getDocumentClass() {
	return RefPosition.class;
    }
}
