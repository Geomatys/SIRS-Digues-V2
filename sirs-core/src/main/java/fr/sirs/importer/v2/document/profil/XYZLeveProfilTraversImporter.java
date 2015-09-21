/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2.document.profil;

import fr.sirs.core.model.XYZLeveProfilTravers;
import fr.sirs.core.model.LeveProfilTravers;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.v2.AbstractUpdater;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class XYZLeveProfilTraversImporter extends AbstractUpdater<XYZLeveProfilTravers, LeveProfilTravers> {

    private enum Columns {
        ID_PROFIL_EN_TRAVERS_LEVE,
        ID_POINT
    }

    @Override
    public String getDocumentIdField() {
        return Columns.ID_PROFIL_EN_TRAVERS_LEVE.name();
    }

    @Override
    public void put(LeveProfilTravers container, XYZLeveProfilTravers toPut) {
        container.pointsLeveXYZ.add(toPut);
    }

    @Override
    public Class<LeveProfilTravers> getDocumentClass() {
        return LeveProfilTravers.class;
    }

    @Override
    protected Class<XYZLeveProfilTravers> getElementClass() {
        return XYZLeveProfilTravers.class;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.PROFIL_EN_TRAVERS_XYZ.toString();
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_POINT.name();
    }

}
