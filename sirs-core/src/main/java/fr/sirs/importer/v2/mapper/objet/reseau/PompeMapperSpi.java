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
package fr.sirs.importer.v2.mapper.objet.reseau;

import org.springframework.stereotype.Component;

import fr.sirs.core.model.Pompe;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class PompeMapperSpi extends GenericMapperSpi<Pompe> {

    private enum Columns {

        NOM_POMPE,
        PUISSANCE_POMPE,
        DEBIT_POMPE,
        HAUTEUR_REFOUL
    };

    private final HashMap<String, String> bindings = new HashMap<>();

    public PompeMapperSpi() throws IntrospectionException {
        super(Pompe.class);

        bindings.put(Columns.NOM_POMPE.name(), "marque");
        bindings.put(Columns.PUISSANCE_POMPE.name(), "puissance");
        bindings.put(Columns.DEBIT_POMPE.name(), "debit");
        bindings.put(Columns.HAUTEUR_REFOUL.name(), "hauteurRefoulement");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }

}
