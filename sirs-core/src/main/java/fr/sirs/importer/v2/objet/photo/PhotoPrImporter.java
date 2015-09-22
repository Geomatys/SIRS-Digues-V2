package fr.sirs.importer.v2.objet.photo;

import static fr.sirs.importer.DbImporter.TableName.PHOTO_LOCALISEE_EN_PR;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class PhotoPrImporter extends AbstractPhotoImporter {

    @Override
    public String getTableName() {
        return PHOTO_LOCALISEE_EN_PR.name();
    }

}
