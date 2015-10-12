package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefOuvrageTelecomEnergie;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
class TypeOuvrageTelecomNrjImporter extends GenericTypeReferenceImporter<RefOuvrageTelecomEnergie> {

    @Override
    public String getTableName() {
        return TYPE_OUVRAGE_TELECOM_NRJ.toString();
    }

    @Override
    public Class<RefOuvrageTelecomEnergie> getElementClass() {
        return RefOuvrageTelecomEnergie.class;
    }
}
