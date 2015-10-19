package fr.sirs.importer.v2.mapper.objet;

import org.springframework.stereotype.Component;

import fr.sirs.core.model.OuvrageRevanche;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class OuvrageRevancheMapperSpi extends GenericMapperSpi<OuvrageRevanche> {

    private final HashMap<String, String> bindings;

    public OuvrageRevancheMapperSpi() throws IntrospectionException {
        super(OuvrageRevanche.class);

        bindings = new HashMap<>(8);
        bindings.put(StructureColumns.ID_SOURCE.name(), "sourceId");
        bindings.put(StructureColumns.ID_TYPE_COTE.name(), "coteId");

        bindings.put(StructureColumns.ID_TYPE_MATERIAU_HAUT.name(), "materiauHautId");
        bindings.put(StructureColumns.ID_TYPE_NATURE_HAUT.name(), "natureHautId");

        bindings.put(StructureColumns.ID_TYPE_MATERIAU_BAS.name(), "materiauBasId");
        bindings.put(StructureColumns.ID_TYPE_NATURE_BAS.name(), "natureBasId");

        bindings.put(StructureColumns.ID_TYPE_POSITION.name(), "positionId");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }

}