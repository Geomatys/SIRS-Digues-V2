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

import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Observation;
import static fr.sirs.importer.DbImporter.TableName.DESORDRE_OBSERVATION;
import fr.sirs.importer.v2.SimpleUpdater;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class ObservationImporter extends SimpleUpdater<Observation, Desordre> {
    private enum Columns {
        ID_OBSERVATION,
        ID_DESORDRE
    }

    @Override
    public String getDocumentIdField() {
        return Columns.ID_DESORDRE.name();
    }

    @Override
    public void put(Desordre container, Observation toPut) {
        container.observations.add(toPut);
    }

    @Override
    public Class<Desordre> getDocumentClass() {
        return Desordre.class;
    }

    @Override
    public Class<Observation> getElementClass() {
        return Observation.class;
    }

    @Override
    public String getTableName() {
        return DESORDRE_OBSERVATION.name();
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_OBSERVATION.name();
    }

}
