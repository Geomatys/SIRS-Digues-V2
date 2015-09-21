package fr.sirs.importer.v2.document.profil;

import fr.sirs.core.model.ProfilLong;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.v2.AbstractImporter;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class ProfilLongImporter extends AbstractImporter<ProfilLong> {

    @Override
    protected Class<ProfilLong> getElementClass() {
        return ProfilLong.class;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.PROFIL_EN_LONG.toString();
    }

    @Override
    public String getRowIdFieldName() {
        return "ID_PROFIL_EN_LONG";
    }
}
