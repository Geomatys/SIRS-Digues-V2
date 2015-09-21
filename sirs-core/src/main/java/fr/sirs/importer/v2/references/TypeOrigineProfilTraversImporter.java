package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefOrigineProfilTravers;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.references.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeOrigineProfilTraversImporter extends GenericTypeReferenceImporter<RefOrigineProfilTravers> {

    @Override
    public String getTableName() {
        return TYPE_ORIGINE_PROFIL_EN_TRAVERS.toString();
    }

    @Override
    protected Class<RefOrigineProfilTravers> getElementClass() {
	return RefOrigineProfilTravers.class;
    }
}
