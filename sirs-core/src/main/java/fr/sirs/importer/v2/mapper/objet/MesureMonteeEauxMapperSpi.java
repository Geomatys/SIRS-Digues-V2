package fr.sirs.importer.v2.mapper.objet;

import fr.sirs.core.model.MesureMonteeEaux;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class MesureMonteeEauxMapperSpi extends GenericMapperSpi<MesureMonteeEaux> {

    private enum Columns {
        DATE,
        ID_TYPE_REF_HEAU,
        HAUTEUR_EAU,
        DEBIT_MAX,
        ID_INTERV_OBSERVATEUR,
        ID_SOURCE
    };

    private final HashMap<String, String> bindings = new HashMap<>();

    public MesureMonteeEauxMapperSpi() throws IntrospectionException {
        super(MesureMonteeEaux.class);

        bindings.put(Columns.DATE.name(), "date");
        bindings.put(Columns.ID_TYPE_REF_HEAU.name(), "referenceHauteurId");
        bindings.put(Columns.HAUTEUR_EAU.name(), "hauteur");
        bindings.put(Columns.DEBIT_MAX.name(), "debitMax");
        bindings.put(Columns.ID_INTERV_OBSERVATEUR.name(), "observateurId");
        bindings.put(Columns.ID_SOURCE.name(), "sourceId");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }

}
