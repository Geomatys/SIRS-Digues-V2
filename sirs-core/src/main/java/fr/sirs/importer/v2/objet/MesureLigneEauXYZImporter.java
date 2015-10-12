/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2.objet;

import fr.sirs.core.model.LigneEau;
import fr.sirs.core.model.MesureLigneEauXYZ;
import static fr.sirs.importer.DbImporter.TableName.LIGNE_EAU_MESURES_XYZ;
import fr.sirs.importer.v2.SimpleUpdater;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class MesureLigneEauXYZImporter extends SimpleUpdater<MesureLigneEauXYZ, LigneEau> {

    @Override
    public String getDocumentIdField() {
        return masterImporter.getRowIdFieldName();
    }

    @Override
    public Class<MesureLigneEauXYZ> getElementClass() {
        return MesureLigneEauXYZ.class;
    }

    @Override
    public String getTableName() {
        return LIGNE_EAU_MESURES_XYZ.name();
    }

    @Override
    public String getRowIdFieldName() {
        return "ID_POINT";
    }

    @Override
    public void put(LigneEau container, MesureLigneEauXYZ toPut) {
        container.getMesuresXYZ().add(toPut);
    }

    @Override
    public Class<LigneEau> getDocumentClass() {
        return LigneEau.class;
    }

}
