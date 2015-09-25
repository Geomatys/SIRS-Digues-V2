package fr.sirs.importer.v2.mapper.objet.reseau;

import org.springframework.stereotype.Component;

import fr.sirs.core.model.StationPompage;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class StationPompageMapperSpi extends GenericMapperSpi<StationPompage> {

    private final HashMap<String, String> bindings = new HashMap<>();

    public StationPompageMapperSpi() throws IntrospectionException {
        super(StationPompage.class);
        bindings.put(ObjetReseauMapperSpi.Columns.ID_TYPE_POSITION.name(), "positionId");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }
}
