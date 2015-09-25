/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2.mapper.objet;

import org.springframework.stereotype.Component;

import fr.sirs.core.model.MonteeEaux;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class MonteeEauxMapperSpi extends GenericMapperSpi<MonteeEaux> {

    private enum Columns {

        ID_EVENEMENT_HYDRAU,
        ID_ECHELLE_LIMNI
    };

    private final HashMap<String, String> bindings = new HashMap<>();

    private MonteeEauxMapperSpi() throws IntrospectionException {
        super(MonteeEaux.class);

        bindings.put(Columns.ID_EVENEMENT_HYDRAU.name(), "evenementHydrauliqueId");
        bindings.put(Columns.ID_ECHELLE_LIMNI.name(), "echelleLimnimetriqueId");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }

}
