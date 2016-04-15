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
package fr.sirs.importer.v2.mapper.linear;

import fr.sirs.core.model.SystemeReperageBorne;
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
public class SRBMapperSpi extends GenericMapperSpi<SystemeReperageBorne> {
    private enum Columns {
        ID_BORNE,
        VALEUR_PR
    }

    final HashMap<String, String> bindings;

    public SRBMapperSpi() throws IntrospectionException {
        super(SystemeReperageBorne.class);

        bindings = new HashMap<>(2);
        bindings.put(Columns.ID_BORNE.name(), "borneId");
        bindings.put(Columns.VALEUR_PR.name(), "valeurPR");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }
}
