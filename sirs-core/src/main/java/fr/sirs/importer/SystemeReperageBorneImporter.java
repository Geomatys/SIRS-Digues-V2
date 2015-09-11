package fr.sirs.importer;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.BorneDigue;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.SystemeReperageBorne;
import static fr.sirs.importer.DbImporter.TableName.BORNE_PAR_SYSTEME_REP;
import fr.sirs.importer.v2.DocumentUpdater;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class SystemeReperageBorneImporter extends DocumentUpdater<SystemeReperage> {

    private GenericImporter<BorneDigue> borneDigueImporter;

    private enum Columns {
        ID_BORNE,
        ID_SYSTEME_REP,
        VALEUR_PR,
        DATE_DERNIERE_MAJ
    };


    @Override
    protected Class<SystemeReperage> getDocumentClass() {
        return SystemeReperage.class;
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_SYSTEME_REP.name();
    }

    @Override
    public SystemeReperage importRow(Row row, SystemeReperage output) throws IOException, AccessDbImporterException {
        final SystemeReperageBorne systemeReperageBorne = createAnonymValidElement(SystemeReperageBorne.class);

        final Date DATE_DERNIERE_MAJ = row.getDate(Columns.DATE_DERNIERE_MAJ.toString());
        if (DATE_DERNIERE_MAJ != null) {
            systemeReperageBorne.setDateMaj(context.convertData(DATE_DERNIERE_MAJ, LocalDate.class));
        }

        final Double prValue = row.getDouble(Columns.VALEUR_PR.toString());
        if (prValue != null) {
            systemeReperageBorne.setValeurPR(prValue.floatValue());
        }

        systemeReperageBorne.setBorneId(borneDigueImporter.getImportedId(row.getInt(Columns.ID_BORNE.toString())));

        // Table de jointure, donc pas d'id propre : on affecte arbitrairement l'id de la borne comme pseudo id.
        systemeReperageBorne.setDesignation(String.valueOf(row.getInt(Columns.ID_BORNE.toString())));

        output.systemeReperageBornes.add(systemeReperageBorne);
        return output;
    }

    @Override
    protected List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (Columns c : Columns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return BORNE_PAR_SYSTEME_REP.toString();
    }
}
