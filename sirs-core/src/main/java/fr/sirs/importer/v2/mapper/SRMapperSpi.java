package fr.sirs.importer.v2.mapper;

import fr.sirs.core.model.SystemeReperage;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class SRMapperSpi extends GenericMapperSpi<SystemeReperage> {

    final HashMap<String, String> bindings;

    public SRMapperSpi() throws IntrospectionException {
        super(SystemeReperage.class);

        bindings = new HashMap<>(1);
        bindings.put("ID_TRONCON_GESTION", "linearId");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }
}
