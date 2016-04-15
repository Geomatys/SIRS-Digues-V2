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
    public Class<PrZProfilLong> getElementClass() {
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
