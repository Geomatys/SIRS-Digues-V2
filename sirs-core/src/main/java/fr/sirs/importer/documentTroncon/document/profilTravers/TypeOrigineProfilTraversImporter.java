package fr.sirs.importer.documentTroncon.document.profilTravers;

import fr.sirs.core.model.RefOrigineProfilTravers;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericTypeReferenceImporter;

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
    protected Class<RefOrigineProfilTravers> getOutputClass() {
	return RefOrigineProfilTravers.class;
    }
}
