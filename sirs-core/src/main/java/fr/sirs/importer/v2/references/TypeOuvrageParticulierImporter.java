package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefOuvrageParticulier;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.references.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeOuvrageParticulierImporter extends GenericTypeReferenceImporter<RefOuvrageParticulier> {

    // Identifiant des échelles limnimetriques
    public static int ECHELLE_LIMNIMETRIQUE = 5;

    @Override
    public String getTableName() {
        return TYPE_OUVRAGE_PARTICULIER.toString();
    }

    @Override
    protected Class<RefOuvrageParticulier> getElementClass() {
	return RefOuvrageParticulier.class;
    }
}
