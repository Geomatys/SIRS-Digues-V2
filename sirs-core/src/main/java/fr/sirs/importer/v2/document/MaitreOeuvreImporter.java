package fr.sirs.importer.v2.document;

import fr.sirs.core.model.MaitreOeuvreMarche;
import fr.sirs.core.model.Marche;
import static fr.sirs.importer.DbImporter.TableName.MARCHE_MAITRE_OEUVRE;
import fr.sirs.importer.v2.SimpleUpdater;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class MaitreOeuvreImporter extends SimpleUpdater<MaitreOeuvreMarche, Marche> {

    private enum Columns {
        ID_MARCHE,
        ID_ORGANISME,
        ID_FONCTION_MO
    }

    @Override
    public void put(Marche container, MaitreOeuvreMarche toPut) {
        container.maitreOeuvre.add(toPut);
    }

    @Override
    public String getDocumentIdField() {
        return Columns.ID_MARCHE.name();
    }

    @Override
    public Class<Marche> getDocumentClass() {
        return Marche.class;
    }

    @Override
    public Class<MaitreOeuvreMarche> getElementClass() {
        return MaitreOeuvreMarche.class;
    }

    @Override
    public String getTableName() {
        return MARCHE_MAITRE_OEUVRE.name();
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_ORGANISME.name();
    }
}
