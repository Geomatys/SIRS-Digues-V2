package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefLargeurFrancBord;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.references.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeLargeurFrancBordImporter extends GenericTypeReferenceImporter<RefLargeurFrancBord> {

    @Override
    public String getTableName() {
        return TYPE_LARGEUR_FRANC_BORD.toString();
    }

    @Override
    protected Class<RefLargeurFrancBord> getDocumentClass() {
	return RefLargeurFrancBord.class;
    }
}
