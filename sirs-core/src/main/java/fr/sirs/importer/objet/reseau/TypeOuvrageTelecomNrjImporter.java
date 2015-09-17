package fr.sirs.importer.objet.reseau;

import fr.sirs.core.model.RefOuvrageTelecomEnergie;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.references.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeOuvrageTelecomNrjImporter extends GenericTypeReferenceImporter<RefOuvrageTelecomEnergie> {

    @Override
    public String getTableName() {
        return TYPE_OUVRAGE_TELECOM_NRJ.toString();
    }

    @Override
    protected Class<RefOuvrageTelecomEnergie> getDocumentClass() {
	return RefOuvrageTelecomEnergie.class;
    }
}
