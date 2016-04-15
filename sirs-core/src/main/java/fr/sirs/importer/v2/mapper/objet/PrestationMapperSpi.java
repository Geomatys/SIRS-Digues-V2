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

import fr.sirs.core.model.Prestation;
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
public class PrestationMapperSpi extends GenericMapperSpi<Prestation> {

    private enum Columns {
        ID_MARCHE,
        REALISATION_INTERNE,
        ID_TYPE_PRESTATION,
        COUT_AU_METRE,
        COUT_GLOBAL,
        ID_TYPE_COTE,
        ID_TYPE_POSITION,
        ////        ID_INTERV_REALISATEUR, // Ne sert à rien : voir la table PRESTATION_INTERVENANT
        ID_SOURCE,
    };

    private final HashMap<String, String> bindings;

    public PrestationMapperSpi() throws IntrospectionException {
        super(Prestation.class);

        bindings = new HashMap<>();
        bindings.put(Columns.ID_TYPE_PRESTATION.name(), "typePrestationId");
        bindings.put(Columns.ID_TYPE_COTE.name(), "coteId");
        bindings.put(Columns.ID_TYPE_POSITION.name(), "positionId");
        bindings.put(Columns.ID_SOURCE.name(), "sourceId");
        bindings.put(Columns.REALISATION_INTERNE.name(), "realisationInterne");
        bindings.put(Columns.COUT_AU_METRE.name(), "coutMetre");
        bindings.put(Columns.COUT_GLOBAL.name(), "coutGlobal");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }
}
