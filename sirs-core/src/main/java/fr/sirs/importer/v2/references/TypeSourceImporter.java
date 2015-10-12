package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefSource;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class TypeSourceImporter extends GenericTypeReferenceImporter<RefSource> {

    @Override
    public Class<RefSource> getElementClass() {
        return RefSource.class;
    }

    @Override
    public String getTableName() {
        return SOURCE_INFO.toString();
    }

}
