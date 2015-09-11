package fr.sirs.importer.objet;

import fr.sirs.core.model.RefMateriau;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericTypeReferenceImporter;

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
    protected Class<RefMateriau> getOutputClass() {
	return RefMateriau.class;
    }

}
