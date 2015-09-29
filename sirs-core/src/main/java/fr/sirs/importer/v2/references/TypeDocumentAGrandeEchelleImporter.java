package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefDocumentGrandeEchelle;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
class TypeDocumentAGrandeEchelleImporter extends GenericTypeReferenceImporter<RefDocumentGrandeEchelle> {

    @Override
    public String getTableName() {
        return TYPE_DOCUMENT_A_GRANDE_ECHELLE.toString();
    }

    @Override
    protected Class<RefDocumentGrandeEchelle> getElementClass() {
        return RefDocumentGrandeEchelle.class;
    }
}
