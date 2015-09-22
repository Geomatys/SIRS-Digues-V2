package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefNature;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.references.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class TypeNatureImporter extends GenericTypeReferenceImporter<RefNature> {

    @Override
    public String getTableName() {
        return TYPE_NATURE.toString();
    }

    @Override
    protected Class<RefNature> getElementClass() {
	return RefNature.class;
    }

}
