package fr.sirs.importer.objet.reseau;

import fr.sirs.core.model.RefRevetement;
import static fr.sirs.importer.DbImporter.TableName.TYPE_REVETEMENT;
import fr.sirs.importer.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeRevetementImporter extends GenericTypeReferenceImporter<RefRevetement> {

    @Override
    public String getTableName() {
        return TYPE_REVETEMENT.toString();
    }

    @Override
    protected Class<RefRevetement> getOutputClass() {
	return RefRevetement.class;
    }
}
