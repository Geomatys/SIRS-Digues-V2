package fr.sirs.importer.v2.mapper.objet;

import org.springframework.stereotype.Component;

import fr.sirs.core.model.Meteo;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class MeteoMapperSpi extends GenericMapperSpi<Meteo> {

    private enum Columns {
        VITESSE_VENT,
        ID_TYPE_ORIENTATION_VENT,
        PRESSION_ATMOSPHERIQUE
    }

    private final HashMap<String, String> bindings;

    public MeteoMapperSpi() throws IntrospectionException {
        super(Meteo.class);

        bindings = new HashMap<>(3);
        bindings.put(Columns.VITESSE_VENT.name(), "vitesseVent");
        bindings.put(Columns.ID_TYPE_ORIENTATION_VENT.name(), "typeOrientationVentId");
        bindings.put(Columns.PRESSION_ATMOSPHERIQUE.name(), "pression");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }

}
