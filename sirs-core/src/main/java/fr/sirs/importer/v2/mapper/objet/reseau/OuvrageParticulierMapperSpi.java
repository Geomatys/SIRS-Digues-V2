package fr.sirs.importer.v2.mapper.objet.reseau;

import fr.sirs.core.model.OuvrageParticulier;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class OuvrageParticulierMapperSpi extends GenericMapperSpi<OuvrageParticulier> {

    private final HashMap<String, String> bindings = new HashMap<>();

    public OuvrageParticulierMapperSpi() throws IntrospectionException {
        super(OuvrageParticulier.class);
        bindings.put(ObjetReseauMapperSpi.Columns.ID_TYPE_POSITION.name(), "positionId");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }
}
