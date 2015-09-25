package fr.sirs.importer.v2.mapper.objet;

import org.springframework.stereotype.Component;

import fr.sirs.core.model.Fondation;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class FondationMapperSpi extends GenericMapperSpi<Fondation> {

    private final HashMap<String, String> bindings;

    public FondationMapperSpi() throws IntrospectionException {
        super(Fondation.class);

        bindings = new HashMap<>(6);
        bindings.put(StructureColumns.ID_SOURCE.name(), "sourceId");
        bindings.put(StructureColumns.ID_TYPE_MATERIAU.name(), "materiauId");
        bindings.put(StructureColumns.ID_TYPE_NATURE.name(), "natureId");
        bindings.put(StructureColumns.ID_TYPE_FONCTION.name(), "fonctionId");
        bindings.put(StructureColumns.EPAISSEUR.name(), "epaisseur");
        bindings.put(StructureColumns.N_COUCHE.name(), "numCouche");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }

}
