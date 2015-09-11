package fr.sirs.importer.objet.reseau;

import fr.sirs.core.model.RefOuvrageFranchissement;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeOuvrageFranchissementImporter extends GenericTypeReferenceImporter<RefOuvrageFranchissement> {

    @Override
    public String getTableName() {
        return TYPE_OUVRAGE_FRANCHISSEMENT.toString();
    }

    @Override
    protected Class<RefOuvrageFranchissement> getOutputClass() {
	return RefOuvrageFranchissement.class;
    }


}
