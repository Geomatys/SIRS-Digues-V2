/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2.objet;

import fr.sirs.core.model.Prestation;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.v2.AbstractImporter;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class PrestationImporter extends AbstractImporter<Prestation> {

    @Override
    protected Class<Prestation> getElementClass() {
        return Prestation.class;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.PRESTATION.name();
    }

    @Override
    public String getRowIdFieldName() {
        return "ID_PRESTATION";
    }

}
