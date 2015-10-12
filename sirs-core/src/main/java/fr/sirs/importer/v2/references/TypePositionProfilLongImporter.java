package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefPositionProfilLongSurDigue;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
class TypePositionProfilLongImporter extends GenericTypeReferenceImporter<RefPositionProfilLongSurDigue> {

    @Override
    public String getTableName() {
        return TYPE_POSITION_PROFIL_EN_LONG_SUR_DIGUE.toString();
    }

    @Override
    public Class<RefPositionProfilLongSurDigue> getElementClass() {
        return RefPositionProfilLongSurDigue.class;
    }
}
