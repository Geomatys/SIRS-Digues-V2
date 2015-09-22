package fr.sirs.importer.v2.objet.reseau;

import fr.sirs.core.model.Pompe;
import fr.sirs.core.model.StationPompage;
import static fr.sirs.importer.DbImporter.TableName.ELEMENT_RESEAU_POMPE;
import fr.sirs.importer.v2.SimpleUpdater;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class PompeImporter extends SimpleUpdater<Pompe, StationPompage> {

    private enum Columns {
        ID_POMPE,
        ID_ELEMENT_RESEAU
    };

    @Override
    public String getDocumentIdField() {
        return Columns.ID_ELEMENT_RESEAU.name();
    }

    @Override
    public void put(StationPompage container, Pompe toPut) {
        container.pompes.add(toPut);
    }

    @Override
    public Class<StationPompage> getDocumentClass() {
        return StationPompage.class;
    }

    @Override
    protected Class<Pompe> getElementClass() {
        return Pompe.class;
    }

    @Override
    public String getTableName() {
        return ELEMENT_RESEAU_POMPE.name();
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_POMPE.name();
    }

}
