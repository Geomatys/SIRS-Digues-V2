package fr.sirs.importer.documentTroncon.document.documentAGrandeEchelle;

import fr.sirs.core.model.RefDocumentGrandeEchelle;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeDocumentAGrandeEchelleImporter extends GenericTypeReferenceImporter<RefDocumentGrandeEchelle> {

    @Override
    public String getTableName() {
        return TYPE_DOCUMENT_A_GRANDE_ECHELLE.toString();
    }

    @Override
    protected Class<RefDocumentGrandeEchelle> getOutputClass() {
	return RefDocumentGrandeEchelle.class;
    }
}
