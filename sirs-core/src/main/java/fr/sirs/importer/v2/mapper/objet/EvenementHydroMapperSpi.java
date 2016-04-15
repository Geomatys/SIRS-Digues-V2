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

import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class EvenementHydroMapperSpi extends GenericMapperSpi<EvenementHydraulique> {

    private enum Columns{
        NOM_EVENEMENT_HYDRAU,
        ID_TYPE_EVENEMENT_HYDRAU,
        ID_TYPE_FREQUENCE_EVENEMENT_HYDRAU,
        VITESSE_MOYENNE,
        DEBIT_MOYEN,
        NOM_MODELEUR_HYDRAU
    }

    private final HashMap<String, String> bindings;

    public EvenementHydroMapperSpi() throws IntrospectionException {
        super(EvenementHydraulique.class);
        
        bindings = new HashMap<>(6);
        bindings.put(Columns.NOM_EVENEMENT_HYDRAU.name(), "libelle");
        bindings.put(Columns.ID_TYPE_EVENEMENT_HYDRAU.name(), "typeEvenementHydrauliqueId");
        bindings.put(Columns.ID_TYPE_FREQUENCE_EVENEMENT_HYDRAU.name(), "frequenceEvenementHydrauliqueId");
        bindings.put(Columns.VITESSE_MOYENNE.name(), "vitesseMoy");
        bindings.put(Columns.DEBIT_MOYEN.name(), "debitMoy");
        bindings.put(Columns.NOM_MODELEUR_HYDRAU.name(), "modeleurHydraulique");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }

}
