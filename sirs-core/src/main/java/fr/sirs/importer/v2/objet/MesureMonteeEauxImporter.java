/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 * 
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
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
