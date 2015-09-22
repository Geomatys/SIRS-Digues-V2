package fr.sirs.importer.v2.mapper.objet.reseau;

import fr.sirs.core.model.ReseauHydrauliqueCielOuvert;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class ReseauHydroCielOuvertMapperSpi extends GenericMapperSpi<ReseauHydrauliqueCielOuvert> {

    private final HashMap<String, String> bindings = new HashMap<>();

    public ReseauHydroCielOuvertMapperSpi() throws IntrospectionException {
        super(ReseauHydrauliqueCielOuvert.class);
        bindings.put(ObjetReseauMapperSpi.Columns.ID_TYPE_POSITION.name(), "positionId");
        bindings.put(ObjetReseauMapperSpi.Columns.ID_TYPE_RESEAU_EAU.name(), "typeReseauHydroCielOuvertId");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }
}
