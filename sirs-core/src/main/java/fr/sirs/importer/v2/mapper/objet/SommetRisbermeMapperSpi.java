package fr.sirs.importer.v2.mapper.objet;

import fr.sirs.core.model.SommetRisberme;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class SommetRisbermeMapperSpi extends GenericMapperSpi<SommetRisberme> {

    private final HashMap<String, String> bindings;

    public SommetRisbermeMapperSpi() throws IntrospectionException {
        super(SommetRisberme.class);

        bindings = new HashMap<>(6);
        bindings.put(StructureColumns.ID_SOURCE.name(), "sourceId");
        bindings.put(StructureColumns.ID_TYPE_COTE.name(), "coteId");
        bindings.put(StructureColumns.ID_TYPE_MATERIAU.name(), "materiauId");
        bindings.put(StructureColumns.ID_TYPE_NATURE.name(), "natureId");
        bindings.put(StructureColumns.ID_TYPE_FONCTION.name(), "fonctionId");
        bindings.put(StructureColumns.EPAISSEUR.name(), "epaisseur");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }

}
