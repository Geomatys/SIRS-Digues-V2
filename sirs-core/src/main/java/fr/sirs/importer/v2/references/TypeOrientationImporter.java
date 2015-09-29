package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefOrientationPhoto;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class TypeOrientationImporter extends GenericTypeReferenceImporter<RefOrientationPhoto> {

    @Override
    public String getTableName() {
        return ORIENTATION.toString();
    }

    @Override
    protected Class<RefOrientationPhoto> getElementClass() {
        return RefOrientationPhoto.class;
    }

}
