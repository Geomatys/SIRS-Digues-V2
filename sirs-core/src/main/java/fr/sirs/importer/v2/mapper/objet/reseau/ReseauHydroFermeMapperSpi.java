package fr.sirs.importer.v2.mapper.objet.reseau;

import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class ReseauHydroFermeMapperSpi extends GenericMapperSpi<ReseauHydrauliqueFerme> {

    private final HashMap<String, String> bindings = new HashMap<>();

    public ReseauHydroFermeMapperSpi() throws IntrospectionException {
        super(ReseauHydrauliqueFerme.class);
        bindings.put(ObjetReseauMapperSpi.Columns.ID_TYPE_POSITION.name(), "positionId");
        bindings.put(ObjetReseauMapperSpi.Columns.ID_ECOULEMENT.name(), "ecoulementId");
        bindings.put(ObjetReseauMapperSpi.Columns.ID_IMPLANTATION.name(), "implantationId");
        bindings.put(ObjetReseauMapperSpi.Columns.ID_UTILISATION_CONDUITE.name(), "utilisationConduiteId");
        bindings.put(ObjetReseauMapperSpi.Columns.ID_TYPE_CONDUITE_FERMEE.name(), "typeConduiteFermeeId");
        bindings.put(ObjetReseauMapperSpi.Columns.AUTORISE.name(), "autorise");
        bindings.put(ObjetReseauMapperSpi.Columns.DIAMETRE.name(), "diametre");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }
}
