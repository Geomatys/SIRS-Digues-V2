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

import fr.sirs.core.model.LaisseCrue;
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
public class LaisseCrueMapperSpi extends GenericMapperSpi<LaisseCrue> {

    private enum Columns {
        ID_SOURCE,
        ID_EVENEMENT_HYDRAU,
        ID_TYPE_REF_HEAU,
        ID_INTERV_OBSERVATEUR,
        DATE,
        HAUTEUR_EAU,
        POSITION
    };

    private final HashMap<String, String> bindings;

    public LaisseCrueMapperSpi() throws IntrospectionException {
        super(LaisseCrue.class);

        bindings = new HashMap<>(7);
        bindings.put(Columns.ID_EVENEMENT_HYDRAU.name(), "evenementHydrauliqueId");
        bindings.put(Columns.ID_TYPE_REF_HEAU.name(), "referenceHauteurId");
        bindings.put(Columns.ID_INTERV_OBSERVATEUR.name(), "observateurId");
        bindings.put(Columns.POSITION.name(), "positionLaisse");
        bindings.put(Columns.ID_SOURCE.name(), "sourceId");
        bindings.put(Columns.HAUTEUR_EAU.name(), "hauteur");
        bindings.put(Columns.DATE.name(), "date");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }
}
