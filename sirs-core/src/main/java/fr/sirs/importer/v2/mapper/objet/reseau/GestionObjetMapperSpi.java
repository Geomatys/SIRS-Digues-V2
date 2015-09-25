package fr.sirs.importer.v2.mapper.objet.reseau;

import fr.sirs.core.model.GestionObjet;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class GestionObjetMapperSpi extends GenericMapperSpi<GestionObjet> {

    private final HashMap<String, String> bindings;

    public GestionObjetMapperSpi() throws IntrospectionException {
        super(GestionObjet.class);

        bindings = new HashMap<>(1);
        bindings.put("ID_ORG_GESTION", "organismeId");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }

}
