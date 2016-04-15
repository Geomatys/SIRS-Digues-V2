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
package fr.sirs.importer.v2.linear;

import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.SystemeReperageBorne;
import static fr.sirs.importer.DbImporter.TableName.BORNE_PAR_SYSTEME_REP;
import fr.sirs.importer.v2.SimpleUpdater;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class SystemeReperageBorneImporter extends SimpleUpdater<SystemeReperageBorne, SystemeReperage> {

    @Override
    public String getDocumentIdField() {
        return "ID_SYSTEME_REP";
    }

    @Override
    public void put(SystemeReperage container, SystemeReperageBorne toPut) {
        container.systemeReperageBornes.add(toPut);
    }

    @Override
    public Class<SystemeReperage> getDocumentClass() {
        return SystemeReperage.class;
    }

    @Override
    public Class<SystemeReperageBorne> getElementClass() {
        return SystemeReperageBorne.class;
    }

    @Override
    public String getRowIdFieldName() {
        return "ID_BORNE";
    }

    @Override
    public String getTableName() {
        return BORNE_PAR_SYSTEME_REP.toString();
    }
}
