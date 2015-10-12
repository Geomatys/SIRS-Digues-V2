package fr.sirs.importer.v2.linear;

import fr.sirs.core.model.Digue;
import static fr.sirs.importer.DbImporter.TableName.DIGUE;
import fr.sirs.importer.v2.AbstractImporter;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class DigueImporter extends AbstractImporter<Digue> {

    @Override
    public Class<Digue> getElementClass() {
        return Digue.class;
    }

    @Override
    public String getRowIdFieldName() {
        return "ID_DIGUE";
    }

    @Override
    public String getTableName() {
        return DIGUE.toString();
    }
}
