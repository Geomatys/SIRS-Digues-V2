package fr.sirs.importer.v2.mapper.objet;

import fr.sirs.core.model.Epi;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class EpiMapperSpi extends GenericMapperSpi<Epi> {

    private final HashMap<String, String> bindings;

    public EpiMapperSpi() throws IntrospectionException {
        super(Epi.class);

        bindings = new HashMap<>(2);
        bindings.put(StructureColumns.ID_SOURCE.name(), "sourceId");
        bindings.put(StructureColumns.ID_TYPE_POSITION.name(), "positionId");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }

}
