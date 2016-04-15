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
package fr.sirs.importer.v2.event;

import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.core.model.Meteo;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.SimpleUpdater;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class MeteoImporter extends SimpleUpdater<Meteo, EvenementHydraulique> {

    @Override
    public Class<Meteo> getElementClass() {
        return Meteo.class;
    }

    @Override
    public String getRowIdFieldName() {
        return "ID_EVENEMENT_HYDRAU";
    }

    @Override
    public String getDocumentIdField() {
        return "ID_EVENEMENT_HYDRAU";
    }

    @Override
    public void put(EvenementHydraulique container, Meteo toPut) {
        container.meteos.add(toPut);
    }

    @Override
    public Class<EvenementHydraulique> getDocumentClass() {
        return EvenementHydraulique.class;
    }

    @Override
    public String getTableName() {
        return METEO.toString();
    }
}
