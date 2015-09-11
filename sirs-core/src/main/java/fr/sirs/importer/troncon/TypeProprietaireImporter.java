package fr.sirs.importer.troncon;

import fr.sirs.core.model.RefProprietaire;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericTypeReferenceImporter;

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
    protected Class<RefProprietaire> getOutputClass() {
	return RefProprietaire.class;
    }
}
