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

import fr.sirs.core.model.MesureMonteeEaux;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class MesureMonteeEauxMapperSpi extends GenericMapperSpi<MesureMonteeEaux> {

    private enum Columns {

        DATE,
        ID_TYPE_REF_HEAU,
        HAUTEUR_EAU,
        DEBIT_MAX,
        ID_INTERV_OBSERVATEUR,
        ID_SOURCE
    };

    private final HashMap<String, String> bindings;

    public MesureMonteeEauxMapperSpi() throws IntrospectionException {
        super(MesureMonteeEaux.class);

        bindings = new HashMap<>(6);
        bindings.put(Columns.DATE.name(), "date");
        bindings.put(Columns.ID_TYPE_REF_HEAU.name(), "referenceHauteurId");
        bindings.put(Columns.HAUTEUR_EAU.name(), "hauteur");
        bindings.put(Columns.DEBIT_MAX.name(), "debitMax");
        bindings.put(Columns.ID_INTERV_OBSERVATEUR.name(), "observateurId");
        bindings.put(Columns.ID_SOURCE.name(), "sourceId");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }

}
