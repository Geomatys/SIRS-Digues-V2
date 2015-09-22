package fr.sirs.importer.v2.document.profil;

import fr.sirs.core.model.ParametreHydrauliqueProfilLong;
import fr.sirs.core.model.ProfilLong;
import static fr.sirs.importer.DbImporter.TableName.PROFIL_EN_LONG_EVT_HYDRAU;
import fr.sirs.importer.v2.SimpleUpdater;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class ParametrePLongImporter extends SimpleUpdater<ParametreHydrauliqueProfilLong, ProfilLong> {

    private enum Columns {
        ID_PROFIL_EN_LONG,
        ID_EVENEMENT_HYDRAU
    }

    @Override
    protected Class<ParametreHydrauliqueProfilLong> getElementClass() {
        return ParametreHydrauliqueProfilLong.class;
    }

    @Override
    public String getTableName() {
        return PROFIL_EN_LONG_EVT_HYDRAU.name();
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_EVENEMENT_HYDRAU.name();
    }

    @Override
    public String getDocumentIdField() {
        return Columns.ID_PROFIL_EN_LONG.name();
    }

    @Override
    public void put(ProfilLong container, ParametreHydrauliqueProfilLong toPut) {
        container.parametresHydrauliques.add(toPut);
    }

    @Override
    public Class<ProfilLong> getDocumentClass() {
        return ProfilLong.class;
    }
}
