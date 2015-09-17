package fr.sirs.importer.objet.desordre;

import fr.sirs.core.model.RefUrgence;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.references.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeUrgenceImporter extends GenericTypeReferenceImporter<RefUrgence> {

    @Override
    public String getTableName() {
        return TYPE_URGENCE.toString();
    }

    @Override
    protected Class<RefUrgence> getDocumentClass() {
	return RefUrgence.class;
    }
}
