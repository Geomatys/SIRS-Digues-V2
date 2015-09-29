package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefFrequenceEvenementHydraulique;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
class TypeFrequenceEvenementHydrauliqueImporter extends GenericTypeReferenceImporter<RefFrequenceEvenementHydraulique> {

    @Override
    public String getTableName() {
        return TYPE_FREQUENCE_EVENEMENT_HYDRAU.toString();
    }

    @Override
    protected Class<RefFrequenceEvenementHydraulique> getElementClass() {
        return RefFrequenceEvenementHydraulique.class;
    }
}
