package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefSource;
import static fr.sirs.importer.DbImporter.TableName.*;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class TypeSourceImporter extends GenericTypeReferenceImporter<RefSource> {

    @Override
    protected Class<RefSource> getElementClass() {
	return RefSource.class;
    }

    @Override
    public String getTableName() {
        return SOURCE_INFO.toString();
    }


}
