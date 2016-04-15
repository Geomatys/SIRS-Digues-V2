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
package fr.sirs.importer.v2.document.position;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.v2.ImportContext;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class ConventionTypeRegistry {

    private final Object typeId;

    private enum Columns {

        ID_TYPE_DOCUMENT,
        NOM_TABLE_EVT
    }

    @Autowired
    private ConventionTypeRegistry(ImportContext context) throws IOException {
        final Table table = context.inputDb.getTable(DbImporter.TableName.TYPE_DOCUMENT.name());
        final Column filterColumn = table.getColumn(Columns.NOM_TABLE_EVT.name());

        final Cursor defaultCursor = table.getDefaultCursor();
        if (defaultCursor.findFirstRow(filterColumn, "SYS_EVT_CONVENTION")) {
            typeId = defaultCursor.getCurrentRow().get(Columns.ID_TYPE_DOCUMENT.name());
        } else {
            typeId = null;
        }
    }

    public boolean isPositionConvention(final Row input) {
        if (typeId == null)
            return false;
        try {
            return typeId.equals(input.get(Columns.ID_TYPE_DOCUMENT.name()));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
