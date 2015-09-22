package fr.sirs.importer.v2.mapper.objet.reseau;

import fr.sirs.core.model.EchelleLimnimetrique;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class EchelleLimnimetriqueMapperSpi extends GenericMapperSpi<EchelleLimnimetrique> {

    private final HashMap<String, String> bindings = new HashMap<>();

    public EchelleLimnimetriqueMapperSpi() throws IntrospectionException {
        super(EchelleLimnimetrique.class);
        bindings.put(ObjetReseauMapperSpi.Columns.ID_TYPE_POSITION.name(), "positionId");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }
}
