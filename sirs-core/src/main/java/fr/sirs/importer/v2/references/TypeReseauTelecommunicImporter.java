package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefReseauTelecomEnergie;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
class TypeReseauTelecommunicImporter extends GenericTypeReferenceImporter<RefReseauTelecomEnergie> {

    @Override
    public String getTableName() {
        return TYPE_RESEAU_TELECOMMUNIC.toString();
    }

    @Override
    protected Class<RefReseauTelecomEnergie> getElementClass() {
        return RefReseauTelecomEnergie.class;
    }
}
