/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2.document.profil;

import fr.sirs.core.model.DZLeveProfilTravers;
import fr.sirs.core.model.LeveProfilTravers;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.v2.SimpleUpdater;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class DZLeveProfilTraversImporter extends SimpleUpdater<DZLeveProfilTravers, LeveProfilTravers> {

    private enum Columns {
        ID_PROFIL_EN_TRAVERS_LEVE,
        ID_POINT
    }

    @Override
    public String getDocumentIdField() {
        return Columns.ID_PROFIL_EN_TRAVERS_LEVE.name();
    }

    @Override
    public void put(LeveProfilTravers container, DZLeveProfilTravers toPut) {
        container.pointsLeveDZ.add(toPut);
    }

    @Override
    public Class<LeveProfilTravers> getDocumentClass() {
        return LeveProfilTravers.class;
    }

    @Override
    protected Class<DZLeveProfilTravers> getElementClass() {
        return DZLeveProfilTravers.class;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.PROFIL_EN_TRAVERS_DZ.toString();
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_POINT.name();
    }

}
