package fr.sirs.importer.v2.mapper.objet.reseau;

import fr.sirs.core.model.GardeObjet;
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
public class GardeObjetMapperSpi extends GenericMapperSpi<GardeObjet> {

    private final HashMap<String, String> bindings;

    public GardeObjetMapperSpi() throws IntrospectionException {
        super(GardeObjet.class);

        bindings = new HashMap<>(1);
        bindings.put("ID_INTERV_GARDIEN", "contactId");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }

}
