package fr.sirs.importer.v2.event;

import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.core.model.Meteo;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.SimpleUpdater;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class MeteoImporter extends SimpleUpdater<Meteo, EvenementHydraulique> {

    @Override
    protected Class<Meteo> getElementClass() {
        return Meteo.class;
    }

    @Override
    public String getRowIdFieldName() {
        return "ID_EVENEMENT_HYDRAU";
    }

    @Override
    public String getDocumentIdField() {
        return "ID_EVENEMENT_HYDRAU";
    }

    @Override
    public void put(EvenementHydraulique container, Meteo toPut) {
        container.meteos.add(toPut);
    }

    @Override
    public Class<EvenementHydraulique> getDocumentClass() {
        return EvenementHydraulique.class;
    }

    @Override
    public String getTableName() {
        return METEO.toString();
    }
}
