/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 * 
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs.importer.v2.document;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.importer.v2.CorruptionLevel;
import fr.sirs.importer.v2.ErrorReport;
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

    private final HashMap<Object, String> journaux = new HashMap<>();

    @Autowired
    private JournalRegistry(final ImportContext context) throws IOException {
        Iterator<Row> iterator = context.inputDb.getTable("JOURNAL").iterator();
        while (iterator.hasNext()) {
            final Row row = iterator.next();
            final Object jId = row.get("ID_JOURNAL");
            if (jId != null) {
                final String jLabel = row.getString("NOM_JOURNAL");
                if (jLabel != null) {
                    journaux.put(jId, jLabel);
                }
            } else {
                context.reportError(new ErrorReport(null, row, "JOURNAL", "ID_JOURNAL", null, null, "No ID for input row.", CorruptionLevel.ROW));
            }
        }
    }

    public String getTitle(final Object journalId) {
        return journaux.get(journalId);
    }
}
