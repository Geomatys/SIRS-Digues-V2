package fr.sirs.importer.v2.document;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.DocumentGrandeEchelle;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.v2.AbstractImporter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class MarcheImporter extends AbstractImporter<DocumentGrandeEchelle> {

    @Autowired
    DocTypeRegistry registry;

    @Override
    protected Class<DocumentGrandeEchelle> getElementClass() {
        return DocumentGrandeEchelle.class;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.DOCUMENT.name();
    }

    @Override
    public String getRowIdFieldName() {
        return "ID_DOC";
    }

    @Override
    protected DocumentGrandeEchelle getOrCreateElement(Row input) {
        final Class docType = registry.getDocType(input);
        if (docType != null && DocumentGrandeEchelle.class.isAssignableFrom(docType)) {
            return super.getOrCreateElement(input);
        } else {
            return null;
        }
    }
}
