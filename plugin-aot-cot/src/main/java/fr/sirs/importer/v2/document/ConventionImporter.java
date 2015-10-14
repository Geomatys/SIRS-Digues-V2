package fr.sirs.importer.v2.document;

import fr.sirs.core.model.Convention;
import fr.sirs.importer.v2.AbstractImporter;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class ConventionImporter extends AbstractImporter<Convention> {

    @Override
    public Class<Convention> getElementClass() {
        return Convention.class;
    }

    @Override
    public String getTableName() {
        return "CONVENTION";
    }

    @Override
    public String getRowIdFieldName() {
        return "ID_CONVENTION";
    }

}
