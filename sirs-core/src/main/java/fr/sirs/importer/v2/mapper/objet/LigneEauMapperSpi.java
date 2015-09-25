package fr.sirs.importer.v2.mapper.objet;

import fr.sirs.core.model.LigneEau;
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
public class LigneEauMapperSpi extends GenericMapperSpi<LigneEau> {

    private enum Columns {

        ID_EVENEMENT_HYDRAU,
        ID_TYPE_REF_HEAU,
        ID_SYSTEME_REP_PRZ,
        DATE,
    };

    private final HashMap<String, String> bindings = new HashMap<>();

    private LigneEauMapperSpi() throws IntrospectionException {
        super(LigneEau.class);

        bindings.put(Columns.ID_EVENEMENT_HYDRAU.name(), "evenementHydrauliqueId");
        bindings.put(Columns.ID_TYPE_REF_HEAU.name(), "referenceHauteurId");
        bindings.put(Columns.ID_SYSTEME_REP_PRZ.name(), "systemeRepDzId");
        bindings.put(Columns.DATE.name(), "date");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }
}
