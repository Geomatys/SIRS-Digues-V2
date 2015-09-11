package fr.sirs.importer;

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
    protected Class<RefCote> getOutputClass() {
        return RefCote.class;
    }
}
