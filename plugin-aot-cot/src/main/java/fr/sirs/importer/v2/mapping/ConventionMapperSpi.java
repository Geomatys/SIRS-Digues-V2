package fr.sirs.importer.v2.mapping;

import fr.sirs.core.model.Convention;
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
public class ConventionMapperSpi extends GenericMapperSpi<Convention> {

    private final HashMap<String, String> bindings;

    public ConventionMapperSpi() throws IntrospectionException {
        super(Convention.class);

        bindings = new HashMap<>(1);
        bindings.put("ID_TYPE_CONVENTION", "typeConventionId");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }

}
