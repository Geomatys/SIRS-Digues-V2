package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefMateriau;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.references.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class TypeMateriauImporter extends GenericTypeReferenceImporter<RefMateriau> {

    @Override
    public String getTableName() {
        return TYPE_MATERIAU.toString();
    }

    @Override
    protected Class<RefMateriau> getElementClass() {
	return RefMateriau.class;
    }

}
