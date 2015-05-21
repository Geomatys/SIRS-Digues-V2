package fr.sirs.importer.link.photo;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.RefOrientationPhoto;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericTypeReferenceImporter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class OrientationImporter extends GenericTypeReferenceImporter<RefOrientationPhoto> {

    public OrientationImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    private enum Columns {
        ID_ORIENTATION,
        LIBELLE_ORIENTATION,
        ABREGE_TYPE_ORIENTATION,
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
        return DbImporter.TableName.ORIENTATION.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefOrientationPhoto orientation = new RefOrientationPhoto();
            
            orientation.setId(orientation.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_ORIENTATION.toString())));
            orientation.setLibelle(row.getString(Columns.LIBELLE_ORIENTATION.toString()));
            
            orientation.setAbrege(row.getString(Columns.ABREGE_TYPE_ORIENTATION.toString()));
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                orientation.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            orientation.setDesignation(String.valueOf(row.getInt(Columns.ID_ORIENTATION.toString())));
            orientation.setValid(true);
            
            types.put(row.getInt(String.valueOf(Columns.ID_ORIENTATION.toString())), orientation);
        }
        couchDbConnector.executeBulk(types.values());
    }
}
