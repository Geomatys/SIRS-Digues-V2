package fr.sirs.importer.v2.mapper.objet.reseau;

import org.springframework.stereotype.Component;

import fr.sirs.core.model.ReseauTelecomEnergie;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class ReseauTelecomEnergieMapperSpi extends GenericMapperSpi<ReseauTelecomEnergie> {

    private final HashMap<String, String> bindings = new HashMap<>();

    public ReseauTelecomEnergieMapperSpi() throws IntrospectionException {
        super(ReseauTelecomEnergie.class);
        bindings.put(ObjetReseauMapperSpi.Columns.ID_TYPE_POSITION.name(), "positionId");
        bindings.put(ObjetReseauMapperSpi.Columns.ID_IMPLANTATION.name(), "implantationId");
        bindings.put(ObjetReseauMapperSpi.Columns.ID_TYPE_RESEAU_COMMUNICATION.name(), "typeReseauTelecomEnergieId");
        bindings.put(ObjetReseauMapperSpi.Columns.HAUTEUR.name(), "hauteur");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }
}
