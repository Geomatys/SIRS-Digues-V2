package fr.sirs.importer.objet.reseau;

import fr.sirs.core.model.RefUsageVoie;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.references.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeUsageVoieImporter extends GenericTypeReferenceImporter<RefUsageVoie> {

    @Override
    public String getTableName() {
        return TYPE_USAGE_VOIE.toString();
    }

    @Override
    protected Class<RefUsageVoie> getDocumentClass() {
	return RefUsageVoie.class;
    }
}
