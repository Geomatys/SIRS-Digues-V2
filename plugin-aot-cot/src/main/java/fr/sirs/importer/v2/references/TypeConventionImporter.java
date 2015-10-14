package fr.sirs.importer.v2.references;

import fr.sirs.core.model.RefConvention;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class TypeConventionImporter extends GenericTypeReferenceImporter<RefConvention> {

    @Override
    public Class<RefConvention> getElementClass() {
        return RefConvention.class;
    }

    @Override
    public String getTableName() {
        return "TYPE_CONVENTION";
    }
}
