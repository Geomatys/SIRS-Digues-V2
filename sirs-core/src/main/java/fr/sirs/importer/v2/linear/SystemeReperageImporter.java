package fr.sirs.importer.v2.linear;

import fr.sirs.core.model.SystemeReperage;
import static fr.sirs.importer.DbImporter.TableName.SYSTEME_REP_LINEAIRE;
import fr.sirs.importer.v2.AbstractImporter;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class SystemeReperageImporter extends AbstractImporter<SystemeReperage> {

    @Override
    public String getTableName() {
        return SYSTEME_REP_LINEAIRE.toString();
    }

    @Override
    protected Class<SystemeReperage> getElementClass() {
        return SystemeReperage.class;
    }

    @Override
    public String getRowIdFieldName() {
        return "ID_SYSTEME_REP";
    }
}
