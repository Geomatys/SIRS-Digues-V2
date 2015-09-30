/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2.document.profil;

import fr.sirs.core.model.PrZProfilLong;
import fr.sirs.core.model.ProfilLong;
import static fr.sirs.importer.DbImporter.TableName.PROFIL_EN_LONG_DZ;
import fr.sirs.importer.v2.SimpleUpdater;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class PrZImporter extends SimpleUpdater<PrZProfilLong, ProfilLong> {

    private enum Columns {
        ID_PROFIL_EN_LONG,
        ID_POINT
    }

    @Override
    public String getDocumentIdField() {
        return Columns.ID_PROFIL_EN_LONG.name();
    }

    @Override
    public void put(ProfilLong container, PrZProfilLong toPut) {
        container.pointsLeveDZ.add(toPut);
    }

    @Override
    public Class<ProfilLong> getDocumentClass() {
        return ProfilLong.class;
    }

    @Override
    protected Class<PrZProfilLong> getElementClass() {
        return PrZProfilLong.class;
    }

    @Override
    public String getTableName() {
        return PROFIL_EN_LONG_DZ.toString();
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_POINT.name();
    }

}
