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
package fr.sirs.importer.v2.mapper.objet;

import fr.sirs.core.model.Observation;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class ObservationMapperSpi extends GenericMapperSpi<Observation> {

    private enum Columns {

        ID_OBSERVATION,
        ID_DESORDRE,
        ID_TYPE_URGENCE,
        ID_INTERV_OBSERVATEUR,
        DATE_OBSERVATION_DESORDRE,
        SUITE_A_APPORTER,
        EVOLUTIONS,
        NBR_DESORDRE
    }

    private final HashMap<String, String> bindings = new HashMap<>();

    public ObservationMapperSpi() throws IntrospectionException {
        super(Observation.class);

        bindings.put(Columns.ID_TYPE_URGENCE.name(), "urgenceId");
        bindings.put(Columns.ID_INTERV_OBSERVATEUR.name(), "observateurId");
        bindings.put(Columns.DATE_OBSERVATION_DESORDRE.name(), "date");
        bindings.put(Columns.SUITE_A_APPORTER.name(), "suite");
        bindings.put(Columns.EVOLUTIONS.name(), "evolution");
        bindings.put(Columns.NBR_DESORDRE.name(), "nombreDesordres");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }

}
