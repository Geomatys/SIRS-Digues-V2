package fr.sirs.importer.objet.reseau;

import fr.sirs.core.model.RefOuvrageVoirie;
import static fr.sirs.importer.DbImporter.TableName.TYPE_OUVRAGE_VOIRIE;
import fr.sirs.importer.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeOuvrageVoirieImporter extends GenericTypeReferenceImporter<RefOuvrageVoirie> {

    @Override
    public String getTableName() {
        return TYPE_OUVRAGE_VOIRIE.toString();
    }

    @Override
    protected Class<RefOuvrageVoirie> getOutputClass() {
	return RefOuvrageVoirie.class;
    }
}
