package fr.sirs.importer.v2.linear;

import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.SystemeReperageBorne;
import static fr.sirs.importer.DbImporter.TableName.BORNE_PAR_SYSTEME_REP;
import fr.sirs.importer.v2.SimpleUpdater;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class SystemeReperageBorneImporter extends SimpleUpdater<SystemeReperageBorne, SystemeReperage> {

    @Override
    public String getDocumentIdField() {
        return "ID_SYSTEME_REP";
    }

    @Override
    public void put(SystemeReperage container, SystemeReperageBorne toPut) {
        container.systemeReperageBornes.add(toPut);
    }

    @Override
    public Class<SystemeReperage> getDocumentClass() {
        return SystemeReperage.class;
    }

    @Override
    public Class<SystemeReperageBorne> getElementClass() {
        return SystemeReperageBorne.class;
    }

    @Override
    public String getRowIdFieldName() {
        return "ID_BORNE";
    }

    @Override
    public String getTableName() {
        return BORNE_PAR_SYSTEME_REP.toString();
    }
}
