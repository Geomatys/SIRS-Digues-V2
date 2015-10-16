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
