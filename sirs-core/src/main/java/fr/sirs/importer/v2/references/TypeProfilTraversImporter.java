package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefTypeProfilTravers;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.references.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeProfilTraversImporter extends GenericTypeReferenceImporter<RefTypeProfilTravers> {

    @Override
    public String getTableName() {
        return TYPE_PROFIL_EN_TRAVERS.toString();
    }

    @Override
    protected Class<RefTypeProfilTravers> getDocumentClass() {
	return RefTypeProfilTravers.class;
    }
}
