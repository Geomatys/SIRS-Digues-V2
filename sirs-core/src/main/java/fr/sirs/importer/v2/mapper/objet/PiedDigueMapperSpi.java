package fr.sirs.importer.v2.mapper.objet;

import fr.sirs.core.model.Crete;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class PiedDigueMapperSpi extends GenericMapperSpi<Crete> {

    private final HashMap<String, String> bindings;

    public PiedDigueMapperSpi() throws IntrospectionException {
        super(Crete.class);

        bindings = new HashMap<>(5);
        bindings.put(StructureColumns.ID_SOURCE.name(), "sourceId");
        bindings.put(StructureColumns.ID_TYPE_COTE.name(), "coteId");
        bindings.put(StructureColumns.ID_TYPE_MATERIAU.name(), "materiauId");
        bindings.put(StructureColumns.ID_TYPE_NATURE.name(), "natureId");
        bindings.put(StructureColumns.ID_TYPE_FONCTION.name(), "fonctionId");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }

}
