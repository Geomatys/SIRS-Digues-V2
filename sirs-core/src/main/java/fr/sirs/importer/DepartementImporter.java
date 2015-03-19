package fr.sirs.importer;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Departement;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class DepartementImporter extends GenericImporter {

    private Map<Integer, Departement> departements = null;

    DepartementImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum Columns {
        ID_DEPARTEMENT,
        CODE_INSEE_DEPARTEMENT,
        LIBELLE_DEPARTEMENT,
        DATE_DERNIERE_MAJ
    };

    public Map<Integer, Departement> getDepartements() throws IOException {
        if (departements == null) compute();
        return departements;
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
        return DbImporter.TableName.DEPARTEMENT.toString();
    }

    @Override
    protected void compute() throws IOException {
        departements = new HashMap<>();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final Departement departement = new Departement();
            
            departement.setCodeInsee(row.getString(Columns.CODE_INSEE_DEPARTEMENT.toString()));
            
            departement.setLibelle(row.getString(Columns.LIBELLE_DEPARTEMENT.toString()));
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                departement.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            departement.setDesignation(String.valueOf(row.getInt(Columns.ID_DEPARTEMENT.toString())));
            departement.setValid(true);
            
            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            departements.put(row.getInt(Columns.ID_DEPARTEMENT.toString()), departement);
        }
        couchDbConnector.executeBulk(departements.values());
    }
}
