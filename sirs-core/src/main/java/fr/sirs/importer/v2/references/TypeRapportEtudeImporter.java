package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefRapportEtude;
import static fr.sirs.importer.DbImporter.TableName.*;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
class TypeRapportEtudeImporter extends GenericTypeReferenceImporter<RefRapportEtude> {

    @Override
    public String getTableName() {
        return TYPE_RAPPORT_ETUDE.toString();
    }

    @Override
    public Class<RefRapportEtude> getElementClass() {
        return RefRapportEtude.class;
    }
}
