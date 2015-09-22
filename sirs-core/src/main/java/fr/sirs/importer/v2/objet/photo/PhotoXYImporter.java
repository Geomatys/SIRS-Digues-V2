package fr.sirs.importer.v2.objet.photo;

import static fr.sirs.importer.DbImporter.TableName.PHOTO_LOCALISEE_EN_XY;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class PhotoXYImporter extends AbstractPhotoImporter {

    @Override
    public String getTableName() {
        return PHOTO_LOCALISEE_EN_XY.name();
    }

}
