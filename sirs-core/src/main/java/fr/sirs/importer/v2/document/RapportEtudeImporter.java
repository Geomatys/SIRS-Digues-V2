package fr.sirs.importer.v2.document;

import fr.sirs.core.model.RapportEtude;
import static fr.sirs.importer.DbImporter.TableName.RAPPORT_ETUDE;
import fr.sirs.importer.v2.AbstractImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class RapportEtudeImporter extends AbstractImporter<RapportEtude> {

    private enum Columns {
        ID_RAPPORT_ETUDE,
        TITRE_RAPPORT_ETUDE,
        ID_TYPE_RAPPORT_ETUDE,
        AUTEUR_RAPPORT,
        DATE_RAPPORT
    };

    @Override
    protected Class<RapportEtude> getElementClass() {
        return RapportEtude.class;
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_RAPPORT_ETUDE.name();
    }



    @Override
    public String getTableName() {
        return RAPPORT_ETUDE.toString();
    }
}
