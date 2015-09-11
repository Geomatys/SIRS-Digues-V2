package fr.sirs.importer.objet;

import fr.sirs.core.model.RefSource;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class SourceInfoImporter extends GenericTypeReferenceImporter<RefSource> {

    @Override
    protected Class<RefSource> getOutputClass() {
	return RefSource.class;
    }

    @Override
    public String getTableName() {
        return SOURCE_INFO.toString();
    }


}
