package fr.sirs.importer.objet.reseau;

import fr.sirs.core.model.RefConduiteFermee;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.references.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeConduiteFermeeImporter extends GenericTypeReferenceImporter<RefConduiteFermee> {

    @Override
    public String getTableName() {
        return TYPE_CONDUITE_FERMEE.toString();
    }

    @Override
    protected Class<RefConduiteFermee> getDocumentClass() {
	return RefConduiteFermee.class;
    }
}
