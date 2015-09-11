package fr.sirs.importer;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Digue;
import static fr.sirs.importer.DbImporter.TableName.DIGUE;
import fr.sirs.importer.v2.DocumentImporter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class DigueImporter extends DocumentImporter<Digue> {

    private enum Columns {
        ID_DIGUE,
        LIBELLE_DIGUE,
        COMMENTAIRE_DIGUE,
        DATE_DERNIERE_MAJ
    };

    @Override
    protected Class getDocumentClass() {
        return Digue.class;
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_DIGUE.name();
    }

    @Override
    protected List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for(Columns c : Columns.values())
            columns.add(c.toString());
        return columns;
    }

    @Override
    public String getTableName() {
        return DIGUE.toString();
    }

    @Override
    public Digue importRow(Row row, Digue digue) throws IOException, AccessDbImporterException {
        digue.setLibelle(row.getString(Columns.LIBELLE_DIGUE.toString()));
        digue.setCommentaire(row.getString(Columns.COMMENTAIRE_DIGUE.toString()));
        final Date dateMaj = row.getDate(Columns.DATE_DERNIERE_MAJ.toString());

        if (dateMaj != null) {
            digue.setDateMaj(context.convertData(dateMaj, LocalDate.class));
        }

        return digue;
    }
}
