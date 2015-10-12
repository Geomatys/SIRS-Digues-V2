package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefUtilisationConduite;
import static fr.sirs.importer.DbImporter.TableName.UTILISATION_CONDUITE;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
class TypeUtilisationConduiteImporter extends GenericTypeReferenceImporter<RefUtilisationConduite> {

    @Override
    public String getTableName() {
        return UTILISATION_CONDUITE.toString();
    }

    @Override
    public Class<RefUtilisationConduite> getElementClass() {
        return RefUtilisationConduite.class;
    }
}
