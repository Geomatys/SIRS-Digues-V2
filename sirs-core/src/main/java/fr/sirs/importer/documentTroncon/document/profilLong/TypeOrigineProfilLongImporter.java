package fr.sirs.importer.documentTroncon.document.profilLong;

import fr.sirs.core.model.RefOrigineProfilLong;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeOrigineProfilLongImporter extends GenericTypeReferenceImporter<RefOrigineProfilLong> {

    @Override
    public String getTableName() {
        return TYPE_ORIGINE_PROFIL_EN_LONG.toString();
    }

    @Override
    protected Class<RefOrigineProfilLong> getOutputClass() {
	return RefOrigineProfilLong.class;
    }
}
