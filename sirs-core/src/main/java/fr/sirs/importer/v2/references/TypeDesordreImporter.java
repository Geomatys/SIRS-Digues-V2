package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefTypeDesordre;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.references.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeDesordreImporter extends GenericTypeReferenceImporter<RefTypeDesordre> {

    @Override
    public String getTableName() {
        return TYPE_DESORDRE.toString();
    }

    @Override
    protected Class<RefTypeDesordre> getElementClass() {
	return RefTypeDesordre.class;
    }
}
