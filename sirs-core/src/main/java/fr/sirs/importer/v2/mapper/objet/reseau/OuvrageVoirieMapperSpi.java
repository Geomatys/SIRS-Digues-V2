package fr.sirs.importer.v2.mapper.objet.reseau;

import org.springframework.stereotype.Component;

import fr.sirs.core.model.OuvrageVoirie;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class OuvrageVoirieMapperSpi extends GenericMapperSpi<OuvrageVoirie> {

    private final HashMap<String, String> bindings;

    public OuvrageVoirieMapperSpi() throws IntrospectionException {
        super(OuvrageVoirie.class);

        bindings = new HashMap<>(2);
        bindings.put(ObjetReseauMapperSpi.Columns.ID_TYPE_POSITION.name(), "positionId");
        bindings.put(ObjetReseauMapperSpi.Columns.ID_TYPE_OUVRAGE_VOIRIE.name(), "typeOuvrageVoirieId");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }
}
