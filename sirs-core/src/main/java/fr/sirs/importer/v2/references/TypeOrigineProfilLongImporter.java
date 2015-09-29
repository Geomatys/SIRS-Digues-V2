package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefOrigineProfilLong;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
class TypeOrigineProfilLongImporter extends GenericTypeReferenceImporter<RefOrigineProfilLong> {

    @Override
    public String getTableName() {
        return TYPE_ORIGINE_PROFIL_EN_LONG.toString();
    }

    @Override
    protected Class<RefOrigineProfilLong> getElementClass() {
        return RefOrigineProfilLong.class;
    }
}
