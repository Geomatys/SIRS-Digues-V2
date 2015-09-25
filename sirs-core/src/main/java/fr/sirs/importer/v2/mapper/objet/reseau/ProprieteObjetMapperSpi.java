package fr.sirs.importer.v2.mapper.objet.reseau;

import org.springframework.stereotype.Component;

import fr.sirs.core.model.ProprieteObjet;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class ProprieteObjetMapperSpi extends GenericMapperSpi<ProprieteObjet> {

    private enum Columns {

        ID_ORG_PROPRIO,
        ID_INTERV_PROPRIO
    }

    private final HashMap<String, String> bindings;

    public ProprieteObjetMapperSpi() throws IntrospectionException {
        super(ProprieteObjet.class);

        bindings = new HashMap<>(2);
        bindings.put("ID_ORG_PROPRIO", "organismeId");
        bindings.put("ID_INTERV_PROPRIO", "contactId");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }

}
