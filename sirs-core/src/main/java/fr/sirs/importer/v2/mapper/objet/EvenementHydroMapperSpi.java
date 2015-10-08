package fr.sirs.importer.v2.mapper.objet;

import org.springframework.stereotype.Component;

import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.importer.v2.mapper.GenericMapperSpi;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class EvenementHydroMapperSpi extends GenericMapperSpi<EvenementHydraulique> {

    private enum Columns{
        NOM_EVENEMENT_HYDRAU,
        ID_TYPE_EVENEMENT_HYDRAU,
        ID_TYPE_FREQUENCE_EVENEMENT_HYDRAU,
        VITESSE_MOYENNE,
        DEBIT_MOYEN,
        NOM_MODELEUR_HYDRAU
    }

    private final HashMap<String, String> bindings;

    public EvenementHydroMapperSpi() throws IntrospectionException {
        super(EvenementHydraulique.class);
        
        bindings = new HashMap<>(6);
        bindings.put(Columns.NOM_EVENEMENT_HYDRAU.name(), "libelle");
        bindings.put(Columns.ID_TYPE_EVENEMENT_HYDRAU.name(), "typeEvenementHydrauliqueId");
        bindings.put(Columns.ID_TYPE_FREQUENCE_EVENEMENT_HYDRAU.name(), "frequenceEvenementHydrauliqueId");
        bindings.put(Columns.VITESSE_MOYENNE.name(), "vitesseMoy");
        bindings.put(Columns.DEBIT_MOYEN.name(), "debitMoy");
        bindings.put(Columns.NOM_MODELEUR_HYDRAU.name(), "modeleurHydraulique");
    }

    @Override
    public Map<String, String> getBindings() {
        return bindings;
    }

}
