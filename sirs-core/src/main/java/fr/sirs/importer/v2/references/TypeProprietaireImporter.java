package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefProprietaire;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.references.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeProprietaireImporter extends GenericTypeReferenceImporter<RefProprietaire> {

    @Override
    public String getTableName() {
        return TYPE_PROPRIETAIRE.toString();
    }

    @Override
    protected Class<RefProprietaire> getDocumentClass() {
	return RefProprietaire.class;
    }
}
