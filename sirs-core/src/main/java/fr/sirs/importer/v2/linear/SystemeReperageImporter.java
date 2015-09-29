package fr.sirs.importer.v2.linear;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.SYSTEME_REP_LINEAIRE;
import fr.sirs.importer.v2.AbstractImporter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class SystemeReperageImporter extends AbstractImporter<SystemeReperage> {

    private AbstractImporter<TronconDigue> tronconGestionDigueImporter;

    private enum Columns {
        ID_SYSTEME_REP,
        ID_TRONCON_GESTION,
        LIBELLE_SYSTEME_REP,
        COMMENTAIRE_SYSTEME_REP,
        DATE_DERNIERE_MAJ
    };

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
        return SYSTEME_REP_LINEAIRE.toString();
    }

    @Override
    protected Class<SystemeReperage> getElementClass() {
        return SystemeReperage.class;
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_SYSTEME_REP.name();
    }

    @Override
    protected void postCompute() {
        tronconGestionDigueImporter = null;
    }

    @Override
    protected void preCompute() throws AccessDbImporterException {
        tronconGestionDigueImporter = context.importers.get(TronconDigue.class);
        if (tronconGestionDigueImporter == null) {
            throw new AccessDbImporterException("No valid troncon importer registered.");
        }
    }


    @Override
    public SystemeReperage importRow(Row row, SystemeReperage systemeReperage) throws IOException, AccessDbImporterException {
        super.importRow(row, systemeReperage);

        systemeReperage.setLibelle(row.getString(Columns.LIBELLE_SYSTEME_REP.toString()));
        systemeReperage.setCommentaire(row.getString(Columns.COMMENTAIRE_SYSTEME_REP.toString()));
        final Date DATE_DERNIERE_MAJ = row.getDate(Columns.DATE_DERNIERE_MAJ.toString());
        if (DATE_DERNIERE_MAJ != null) {
            systemeReperage.setDateMaj(context.convertData(DATE_DERNIERE_MAJ, LocalDate.class));
        }

        Integer idTroncon = row.getInt(Columns.ID_TRONCON_GESTION.toString());
        if (idTroncon == null) {
            throw new AccessDbImporterException("No valid troncon ID available for given row.");
        }
        systemeReperage.setLinearId(tronconGestionDigueImporter.getImportedId(idTroncon));

        return systemeReperage;
    }
}
