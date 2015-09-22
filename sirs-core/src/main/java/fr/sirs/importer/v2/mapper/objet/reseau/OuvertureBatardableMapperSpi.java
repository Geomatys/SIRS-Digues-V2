package fr.sirs.importer.v2.mapper.objet.reseau;

import fr.sirs.core.model.OuvertureBatardable;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class OuvertureBatardableMapperSpi extends GenericMapperSpi<OuvertureBatardable> {

    private final HashMap<String, String> bindings = new HashMap<>();

    public OuvertureBatardableMapperSpi() throws IntrospectionException {
        super(OuvertureBatardable.class);
        bindings.put(ObjetReseauMapperSpi.Columns.LARGEUR.name(), "largeur");
        bindings.put(ObjetReseauMapperSpi.Columns.HAUTEUR.name(), "hauteur");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }
}
