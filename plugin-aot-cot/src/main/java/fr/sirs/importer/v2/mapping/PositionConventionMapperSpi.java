package fr.sirs.importer.v2.mapping;

import fr.sirs.core.model.PositionConvention;
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
public class PositionConventionMapperSpi extends GenericMapperSpi<PositionConvention> {

    private final HashMap<String, String> bindings;

    public PositionConventionMapperSpi() throws IntrospectionException {
        super(PositionConvention.class);

        bindings = new HashMap<>(1);
        bindings.put("ID_DOC", "sirsdocument");
        bindings.put("ID_CONVENTION", "sirsdocument");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }

}
