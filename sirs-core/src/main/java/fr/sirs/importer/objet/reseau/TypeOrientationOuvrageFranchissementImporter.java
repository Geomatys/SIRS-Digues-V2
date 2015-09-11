package fr.sirs.importer.objet.reseau;

import fr.sirs.core.model.RefOrientationOuvrage;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeOrientationOuvrageFranchissementImporter extends GenericTypeReferenceImporter<RefOrientationOuvrage> {

    @Override
    public String getTableName() {
        return TYPE_ORIENTATION_OUVRAGE_FRANCHISSEMENT.toString();
    }

    @Override
    protected Class<RefOrientationOuvrage> getOutputClass() {
	return RefOrientationOuvrage.class;
    }
}
