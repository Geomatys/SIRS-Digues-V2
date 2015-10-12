package fr.sirs.importer.v2.event;

import fr.sirs.core.model.EvenementHydraulique;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.AbstractImporter;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class EvenementHydrauliqueImporter extends AbstractImporter<EvenementHydraulique> {

    @Override
    public String getTableName() {
        return EVENEMENT_HYDRAU.toString();
    }

    @Override
    public Class<EvenementHydraulique> getElementClass() {
        return EvenementHydraulique.class;
    }

    @Override
    public String getRowIdFieldName() {
        return "ID_EVENEMENT_HYDRAU";
    }
}
