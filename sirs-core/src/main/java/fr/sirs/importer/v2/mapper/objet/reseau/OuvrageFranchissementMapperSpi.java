package fr.sirs.importer.v2.mapper.objet.reseau;

import org.springframework.stereotype.Component;

import fr.sirs.core.model.OuvrageFranchissement;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class OuvrageFranchissementMapperSpi extends GenericMapperSpi<OuvrageFranchissement> {

    private final HashMap<String, String> bindings;

    public OuvrageFranchissementMapperSpi() throws IntrospectionException {
        super(OuvrageFranchissement.class);

        bindings = new HashMap(4);
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
