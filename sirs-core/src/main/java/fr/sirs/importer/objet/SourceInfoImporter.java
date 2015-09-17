package fr.sirs.importer.objet;

import fr.sirs.core.model.RefSource;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.references.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class SourceInfoImporter extends GenericTypeReferenceImporter<RefSource> {

    @Override
    protected Class<RefSource> getDocumentClass() {
	return RefSource.class;
    }

    @Override
    public String getTableName() {
        return SOURCE_INFO.toString();
    }


}
