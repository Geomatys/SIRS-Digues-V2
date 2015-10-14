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
