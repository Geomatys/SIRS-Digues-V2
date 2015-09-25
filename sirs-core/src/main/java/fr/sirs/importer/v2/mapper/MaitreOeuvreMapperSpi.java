package fr.sirs.importer.v2.mapper;

import fr.sirs.core.model.MaitreOeuvreMarche;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class MaitreOeuvreMapperSpi extends GenericMapperSpi<MaitreOeuvreMarche> {

    private final HashMap<String, String> bindings;

    public MaitreOeuvreMapperSpi() throws IntrospectionException {
        super(MaitreOeuvreMarche.class);

        bindings = new HashMap<>(2);
        bindings.put("ID_FONCTION_MO", "fonctionMaitreOeuvre");
        bindings.put("ID_ORGANISME", "organismeId");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }

}
