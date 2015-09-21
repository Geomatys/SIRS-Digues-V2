package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefProfilFrancBord;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.references.GenericTypeReferenceImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeProfilFrancBordImporter extends GenericTypeReferenceImporter<RefProfilFrancBord> {

    @Override
    public String getTableName() {
        return TYPE_PROFIL_FRANC_BORD.toString();
    }

    @Override
    protected Class<RefProfilFrancBord> getElementClass() {
	return RefProfilFrancBord.class;
    }

}
