package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefReseauTelecomEnergie;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.references.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
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
