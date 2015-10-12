package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefProfilFrancBord;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
class TypeProfilFrancBordImporter extends GenericTypeReferenceImporter<RefProfilFrancBord> {

    @Override
    public String getTableName() {
        return TYPE_PROFIL_FRANC_BORD.toString();
    }

    @Override
    public Class<RefProfilFrancBord> getElementClass() {
        return RefProfilFrancBord.class;
    }

}
