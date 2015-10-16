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
