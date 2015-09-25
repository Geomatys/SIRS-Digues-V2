package fr.sirs.importer.v2.mapper.objet.reseau;

import org.springframework.stereotype.Component;

import fr.sirs.core.model.OuvrageTelecomEnergie;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class OuvrageVoirieMapperSpi extends GenericMapperSpi<OuvrageTelecomEnergie> {

    private final HashMap<String, String> bindings = new HashMap<>();

    public OuvrageVoirieMapperSpi() throws IntrospectionException {
        super(OuvrageTelecomEnergie.class);
        bindings.put(ObjetReseauMapperSpi.Columns.ID_TYPE_POSITION.name(), "positionId");
        bindings.put(ObjetReseauMapperSpi.Columns.ID_TYPE_OUVRAGE_VOIRIE.name(), "typeOuvrageVoirieId");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }
}
