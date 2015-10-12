package fr.sirs.importer.v2.document;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.DocumentGrandeEchelle;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.v2.AbstractImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class DocGrandeEchelleImporter extends AbstractImporter<DocumentGrandeEchelle> {

    @Autowired
    DocTypeRegistry registry;

    @Override
    public Class<DocumentGrandeEchelle> getElementClass() {
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
    protected DocumentGrandeEchelle createElement(Row input) {
        final Class docType = registry.getDocType(input);
        if (docType != null && DocumentGrandeEchelle.class.isAssignableFrom(docType)) {
            return super.createElement(input);
        } else {
            return null;
        }
    }
}
