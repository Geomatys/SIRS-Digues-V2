package fr.sirs.importer.v2.document;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.importer.v2.ImportContext;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class JournalRegistry {

    private final HashMap<Integer, String> journaux = new HashMap<>();

    @Autowired
    private JournalRegistry(final ImportContext context) throws IOException {
        Iterator<Row> iterator = context.inputDb.getTable("JOURNAL").iterator();
        // TODO : error report on null value.
        while (iterator.hasNext()) {
            final Row row = iterator.next();
            final Integer jId = row.getInt("ID_JOURNAL");
            if (jId != null) {
                final String jLabel = row.getString("NOM_JOURNAL");
                if (jLabel != null) {
                    journaux.put(jId, jLabel);
                }
            }
        }
    }
    
    public String getTitle(final Integer journalId) {
        return journaux.get(journalId);
    }
}
