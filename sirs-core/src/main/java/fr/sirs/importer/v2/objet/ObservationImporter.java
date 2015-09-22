package fr.sirs.importer.v2.objet;

import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Observation;
import static fr.sirs.importer.DbImporter.TableName.DESORDRE_OBSERVATION;
import fr.sirs.importer.v2.SimpleUpdater;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class ObservationImporter extends SimpleUpdater<Observation, Desordre> {
    private enum Columns {
        ID_OBSERVATION,
        ID_DESORDRE
    }

    @Override
    public String getDocumentIdField() {
        return Columns.ID_OBSERVATION.name();
    }

    @Override
    public void put(Desordre container, Observation toPut) {
        container.observations.add(toPut);
    }

    @Override
    public Class<Desordre> getDocumentClass() {
        return Desordre.class;
    }

    @Override
    protected Class<Observation> getElementClass() {
        return Observation.class;
    }

    @Override
    public String getTableName() {
        return DESORDRE_OBSERVATION.name();
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_OBSERVATION.name();
    }

}
