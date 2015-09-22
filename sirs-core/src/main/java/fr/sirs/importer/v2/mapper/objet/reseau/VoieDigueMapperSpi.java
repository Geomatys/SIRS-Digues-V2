package fr.sirs.importer.v2.mapper.objet.reseau;

import fr.sirs.core.model.VoieDigue;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class VoieDigueMapperSpi extends GenericMapperSpi<VoieDigue> {

    private final HashMap<String, String> bindings = new HashMap<>();

    public VoieDigueMapperSpi() throws IntrospectionException {
        super(VoieDigue.class);
        bindings.put(ObjetReseauMapperSpi.Columns.ID_TYPE_POSITION.name(), "positionId");
        bindings.put(ObjetReseauMapperSpi.Columns.ID_TYPE_VOIE_SUR_DIGUE.name(), "typeVoieDigueId");
        bindings.put(ObjetReseauMapperSpi.Columns.ID_TYPE_REVETEMENT.name(), "revetementId");
        bindings.put(ObjetReseauMapperSpi.Columns.ID_TYPE_USAGE_VOIE.name(), "usageId");
        bindings.put(ObjetReseauMapperSpi.Columns.LARGEUR.name(), "largeur");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }
}
