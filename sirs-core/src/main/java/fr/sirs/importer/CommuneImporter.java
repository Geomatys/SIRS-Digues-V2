package fr.sirs.importer;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.component.CommuneRepository;
import fr.sirs.core.model.Commune;
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
public class CommuneImporter extends GenericImporter {

    private Map<Integer, Commune> communes = null;

    CommuneImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum Columns {
        ID_COMMUNE,
        CODE_INSEE_COMMUNE,
        LIBELLE_COMMUNE,
        DATE_DERNIERE_MAJ
    };

    public Map<Integer, Commune> getCommunes() throws IOException {
        if (communes == null) compute();
        return communes;
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
        return DbImporter.TableName.COMMUNE.toString();
    }

    @Override
    protected void compute() throws IOException {
        communes = new HashMap<>();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final Commune commune = new Commune();
            
            commune.setCodeInsee(row.getString(Columns.CODE_INSEE_COMMUNE.toString()));
            
            commune.setLibelle(row.getString(Columns.LIBELLE_COMMUNE.toString()));
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                commune.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            communes.put(row.getInt(Columns.ID_COMMUNE.toString()), commune);
        }
        couchDbConnector.executeBulk(communes.values());
    }
}
