package fr.sirs.importer.documentTroncon.document.profilLong;

import fr.sirs.core.model.RefPositionProfilLongSurDigue;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypePositionProfilLongImporter extends GenericTypeReferenceImporter<RefPositionProfilLongSurDigue> {

    @Override
    public String getTableName() {
        return TYPE_POSITION_PROFIL_EN_LONG_SUR_DIGUE.toString();
    }

    @Override
    protected Class<RefPositionProfilLongSurDigue> getOutputClass() {
	return RefPositionProfilLongSurDigue.class;
    }
}
