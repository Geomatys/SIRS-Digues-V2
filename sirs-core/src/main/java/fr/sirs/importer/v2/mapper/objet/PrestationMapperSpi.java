package fr.sirs.importer.v2.mapper.objet;

import fr.sirs.core.model.Prestation;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
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
