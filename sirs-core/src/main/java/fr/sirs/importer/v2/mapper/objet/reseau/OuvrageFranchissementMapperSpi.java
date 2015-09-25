package fr.sirs.importer.v2.mapper.objet.reseau;

import org.springframework.stereotype.Component;

import fr.sirs.core.model.VoieAcces;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class OuvrageFranchissementMapperSpi extends GenericMapperSpi<VoieAcces> {

    private final HashMap<String, String> bindings = new HashMap<>();

    public OuvrageFranchissementMapperSpi() throws IntrospectionException {
        super(VoieAcces.class);
        bindings.put(ObjetReseauMapperSpi.Columns.ID_TYPE_POSITION.name(), "positionId");
        bindings.put(ObjetReseauMapperSpi.Columns.LARGEUR.name(), "largeur");
        bindings.put(ObjetReseauMapperSpi.Columns.ID_TYPE_REVETEMENT_HAUT.name(), "revetementHautId");
        bindings.put(ObjetReseauMapperSpi.Columns.ID_TYPE_REVETEMENT_BAS.name(), "revetementBasId");
        bindings.put(ObjetReseauMapperSpi.Columns.ID_TYPE_USAGE_VOIE.name(), "usageId");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }
}
