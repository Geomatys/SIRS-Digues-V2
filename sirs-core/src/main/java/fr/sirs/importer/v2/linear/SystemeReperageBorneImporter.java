package fr.sirs.importer.v2.linear;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.SystemeReperageBorne;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.AbstractImporter;
import static fr.sirs.importer.DbImporter.TableName.BORNE_PAR_SYSTEME_REP;
import fr.sirs.importer.v2.SimpleUpdater;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class SystemeReperageBorneImporter extends SimpleUpdater<SystemeReperageBorne, SystemeReperage> {

    private enum Columns {
        ID_BORNE,
        ID_SYSTEME_REP,
        VALEUR_PR,
        DATE_DERNIERE_MAJ
    };

    private AbstractImporter<BorneDigue> borneDigueImporter;

    @Override
    public String getDocumentIdField() {
        return Columns.ID_SYSTEME_REP.name();
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
    protected Class<SystemeReperageBorne> getElementClass() {
        return SystemeReperageBorne.class;
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_BORNE.name();
    }

    @Override
    public SystemeReperageBorne importRow(Row row, SystemeReperageBorne systemeReperageBorne) throws IOException, AccessDbImporterException {
        systemeReperageBorne = super.importRow(row, systemeReperageBorne);

        final Date DATE_DERNIERE_MAJ = row.getDate(Columns.DATE_DERNIERE_MAJ.toString());
        if (DATE_DERNIERE_MAJ != null) {
            systemeReperageBorne.setDateMaj(context.convertData(DATE_DERNIERE_MAJ, LocalDate.class));
        }

        final Double prValue = row.getDouble(Columns.VALEUR_PR.toString());
        if (prValue != null) {
            systemeReperageBorne.setValeurPR(prValue.floatValue());
        }

        systemeReperageBorne.setBorneId(borneDigueImporter.getImportedId(row.getInt(Columns.ID_BORNE.toString())));

        return systemeReperageBorne;
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
