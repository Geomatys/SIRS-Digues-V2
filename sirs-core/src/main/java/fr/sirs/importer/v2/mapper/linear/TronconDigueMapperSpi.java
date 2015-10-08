package fr.sirs.importer.v2.mapper.linear;

import fr.sirs.core.model.TronconDigue;
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
public class TronconDigueMapperSpi extends GenericMapperSpi<TronconDigue> {

    enum Columns {
        ID_DIGUE,
        ID_TYPE_RIVE,
        NOM_TRONCON_GESTION,
        //ID_SYSTEME_REP_DEFAUT Done by another component "TronconDigueUpdater".
    }

    final HashMap<String, String> bindings;

    public TronconDigueMapperSpi() throws IntrospectionException {
        super(TronconDigue.class);

        bindings = new HashMap<>(3);
        bindings.put(Columns.ID_DIGUE.name(), "digueId");
        bindings.put(Columns.ID_TYPE_RIVE.name(), "typeRiveId");
        bindings.put(Columns.NOM_TRONCON_GESTION.name(), "libelle");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }
}
