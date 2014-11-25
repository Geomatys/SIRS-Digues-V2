package fr.sirs.importer.theme.document.related.journal;

import fr.sirs.importer.*;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import static fr.sirs.importer.DbImporter.cleanNullString;
import java.io.IOException;
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
class JournalImporter extends GenericImporter {

    private Map<Integer, String> journalName = null;
    
    JournalImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum JournalColumns {
        ID_JOURNAL,
        NOM_JOURNAL,
//        DATE_JOURNAL, // ?
//        ID_CRUE, // ???
//        DATE_DERNIERE_MAJ // ?
    };
    
    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (JournalColumns c : JournalColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.JOURNAL.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        journalName = new HashMap<>();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            
            journalName.put(row.getInt(JournalColumns.ID_JOURNAL.toString()), 
                    cleanNullString(row.getString(JournalColumns.NOM_JOURNAL.toString())));
        }
    }
    
    /**
     *
     * @return A map containing all Journal names accessibles from the
     * internal database identifier.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    public Map<Integer, String> getJournalNames() throws IOException, AccessDbImporterException {
        if (journalName == null)  compute();
        return journalName;
    }
}
