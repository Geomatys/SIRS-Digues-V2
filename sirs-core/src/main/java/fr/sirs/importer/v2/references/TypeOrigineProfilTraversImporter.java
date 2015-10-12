package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefOrigineProfilTravers;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
class TypeOrigineProfilTraversImporter extends GenericTypeReferenceImporter<RefOrigineProfilTravers> {

    @Override
    public String getTableName() {
        return TYPE_ORIGINE_PROFIL_EN_TRAVERS.toString();
    }

    @Override
    public Class<RefOrigineProfilTravers> getElementClass() {
        return RefOrigineProfilTravers.class;
    }
}
