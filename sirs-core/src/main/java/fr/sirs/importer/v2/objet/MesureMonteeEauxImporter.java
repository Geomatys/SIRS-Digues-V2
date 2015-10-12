/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2.objet;

import fr.sirs.core.model.MesureMonteeEaux;
import fr.sirs.core.model.MonteeEaux;
import static fr.sirs.importer.DbImporter.TableName.MONTEE_DES_EAUX_MESURES;
import fr.sirs.importer.v2.SimpleUpdater;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class MesureMonteeEauxImporter extends SimpleUpdater<MesureMonteeEaux, MonteeEaux> {

    @Override
    public String getDocumentIdField() {
        return "ID_MONTEE_DES_EAUX";
    }

    @Override
    public void put(MonteeEaux container, MesureMonteeEaux toPut) {
        container.mesures.add(toPut);
    }

    @Override
    public Class<MonteeEaux> getDocumentClass() {
        return MonteeEaux.class;
    }

    @Override
    public Class<MesureMonteeEaux> getElementClass() {
        return MesureMonteeEaux.class;
    }

    @Override
    public String getTableName() {
        return MONTEE_DES_EAUX_MESURES.toString();
    }

    @Override
    public String getRowIdFieldName() {
        //return "DATE";
        return "ID_MONTEE_DES_EAUX";
    }

}
