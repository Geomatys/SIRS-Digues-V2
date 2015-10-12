package fr.sirs.importer.v2.contact;

import fr.sirs.core.model.Organisme;
import static fr.sirs.importer.DbImporter.TableName.ORGANISME;
import fr.sirs.importer.v2.AbstractImporter;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class OrganismeImporter extends AbstractImporter<Organisme> {

    @Override
    public Class<Organisme> getElementClass() {
        return Organisme.class;
    }

    @Override
    public String getRowIdFieldName() {
        return "ID_ORGANISME";
    }

    @Override
    public String getTableName() {
        return ORGANISME.toString();
    }
}
