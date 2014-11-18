package fr.sirs.importer;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.component.DepartementRepository;
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
    private DepartementRepository departementRepository;

    private DepartementImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    DepartementImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final DepartementRepository departementRepository) {
        this(accessDatabase, couchDbConnector);
        this.departementRepository = departementRepository;
    }

    private enum DepartementColumns {
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
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (DepartementColumns c : DepartementColumns.values()) {
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
            
            departement.setCodeInsee(row.getString(DepartementColumns.CODE_INSEE_DEPARTEMENT.toString()));
            
            departement.setLibelle(row.getString(DepartementColumns.LIBELLE_DEPARTEMENT.toString()));
            
            if (row.getDate(DepartementColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                departement.setDateMaj(LocalDateTime.parse(row.getDate(DepartementColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            departements.put(row.getInt(DepartementColumns.ID_DEPARTEMENT.toString()), departement);
        }
        couchDbConnector.executeBulk(departements.values());
    }
}
