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

import org.springframework.stereotype.Component;

import fr.sirs.core.model.Meteo;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class MeteoMapperSpi extends GenericMapperSpi<Meteo> {

    private enum Columns {
        VITESSE_VENT,
        ID_TYPE_ORIENTATION_VENT,
        PRESSION_ATMOSPHERIQUE
    }

    private final HashMap<String, String> bindings;

    public MeteoMapperSpi() throws IntrospectionException {
        super(Meteo.class);

        bindings = new HashMap<>(3);
        bindings.put(Columns.VITESSE_VENT.name(), "vitesseVent");
        bindings.put(Columns.ID_TYPE_ORIENTATION_VENT.name(), "typeOrientationVentId");
        bindings.put(Columns.PRESSION_ATMOSPHERIQUE.name(), "pression");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }

}
