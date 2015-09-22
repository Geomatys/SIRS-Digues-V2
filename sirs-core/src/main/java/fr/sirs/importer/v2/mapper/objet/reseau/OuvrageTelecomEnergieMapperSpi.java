package fr.sirs.importer.v2.mapper.objet.reseau;

import fr.sirs.core.model.OuvrageTelecomEnergie;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class OuvrageTelecomEnergieMapperSpi extends GenericMapperSpi<OuvrageTelecomEnergie> {

    private final HashMap<String, String> bindings = new HashMap<>();

    public OuvrageTelecomEnergieMapperSpi() throws IntrospectionException {
        super(OuvrageTelecomEnergie.class);
        bindings.put(ObjetReseauMapperSpi.Columns.ID_TYPE_POSITION.name(), "positionId");
        bindings.put(ObjetReseauMapperSpi.Columns.ID_OUVRAGE_COMM_NRJ.name(), "typeOuvrageTelecomEnergieId");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }
}
