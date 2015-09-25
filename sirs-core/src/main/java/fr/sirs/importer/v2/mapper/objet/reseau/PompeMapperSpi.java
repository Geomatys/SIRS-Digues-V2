package fr.sirs.importer.v2.mapper.objet.reseau;

import org.springframework.stereotype.Component;

import fr.sirs.core.model.Pompe;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class PompeMapperSpi extends GenericMapperSpi<Pompe> {

    private enum Columns {

        NOM_POMPE,
        PUISSANCE_POMPE,
        DEBIT_POMPE,
        HAUTEUR_REFOUL
    };

    private final HashMap<String, String> bindings = new HashMap<>();

    public PompeMapperSpi() throws IntrospectionException {
        super(Pompe.class);

        bindings.put(Columns.NOM_POMPE.name(), "marque");
        bindings.put(Columns.PUISSANCE_POMPE.name(), "puissance");
        bindings.put(Columns.DEBIT_POMPE.name(), "debit");
        bindings.put(Columns.HAUTEUR_REFOUL.name(), "hauteurRefoulement");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }

}
