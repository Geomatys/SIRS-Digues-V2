package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefCote;
import static fr.sirs.importer.DbImporter.TableName.*;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class TypeCoteImporter extends GenericTypeReferenceImporter<RefCote> {

    @Override
    public String getTableName() {
        return TYPE_COTE.toString();
    }

    @Override
    protected Class<RefCote> getDocumentClass() {
        return RefCote.class;
    }
}
