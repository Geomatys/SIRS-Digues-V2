package fr.sirs.importer.link.photo;

import fr.sirs.core.model.RefOrientationPhoto;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class OrientationImporter extends GenericTypeReferenceImporter<RefOrientationPhoto> {


    @Override
    public String getTableName() {
        return ORIENTATION.toString();
    }

    @Override
    protected Class<RefOrientationPhoto> getOutputClass() {
	return RefOrientationPhoto.class;
    }

}
