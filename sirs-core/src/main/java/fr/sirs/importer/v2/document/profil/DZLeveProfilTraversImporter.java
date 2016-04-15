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
package fr.sirs.importer.v2.document.profil;

import fr.sirs.core.model.DZLeveProfilTravers;
import fr.sirs.core.model.LeveProfilTravers;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.v2.SimpleUpdater;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
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
    public Class<DZLeveProfilTravers> getElementClass() {
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
