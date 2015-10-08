package fr.sirs.importer.v2.mapper.linear;

import fr.sirs.core.model.SystemeReperageBorne;
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
public class SRBMapperSpi extends GenericMapperSpi<SystemeReperageBorne> {
    private enum Columns {
        ID_BORNE,
        VALEUR_PR
    }

    final HashMap<String, String> bindings;

    public SRBMapperSpi() throws IntrospectionException {
        super(SystemeReperageBorne.class);

        bindings = new HashMap<>(2);
        bindings.put(Columns.ID_BORNE.name(), "borneId");
        bindings.put(Columns.VALEUR_PR.name(), "valeurPR");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }
}
