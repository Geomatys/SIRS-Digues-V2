package fr.sirs.importer.v2.document.position;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.ElementCreator;
import fr.sirs.core.model.PositionConvention;
import fr.sirs.importer.v2.AbstractImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class PositionConventionImporter extends AbstractImporter<PositionConvention> {

    @Autowired
    private ConventionTypeRegistry registry;

    @Override
    public Class<PositionConvention> getElementClass() {
        return PositionConvention.class;
    }

    @Override
    public String getTableName() {
        return "DOCUMENT";
    }

    @Override
    public String getRowIdFieldName() {
        return "ID_DOC";
    }

    @Override
    protected PositionConvention createElement(Row input) {
        if (registry.isPositionConvention(input)) {
            return ElementCreator.createAnonymValidElement(getElementClass());
        } else {
            return null;
        }
    }
}
