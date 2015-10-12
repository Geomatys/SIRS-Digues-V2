/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2.document.profil;

import fr.sirs.core.model.XYZProfilLong;
import fr.sirs.core.model.ProfilLong;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.v2.SimpleUpdater;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class XYZProfilLongImporter extends SimpleUpdater<XYZProfilLong, ProfilLong> {

    private enum Columns {
        ID_PROFIL_EN_LONG,
        ID_POINT
    }

    @Override
    public String getDocumentIdField() {
        return Columns.ID_PROFIL_EN_LONG.name();
    }

    @Override
    public void put(ProfilLong container, XYZProfilLong toPut) {
        container.pointsLeveXYZ.add(toPut);
    }

    @Override
    public Class<ProfilLong> getDocumentClass() {
        return ProfilLong.class;
    }

    @Override
    public Class<XYZProfilLong> getElementClass() {
        return XYZProfilLong.class;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.PROFIL_EN_LONG_XYZ.toString();
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_POINT.name();
    }

}
