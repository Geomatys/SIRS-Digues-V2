package fr.sirs.importer.evenementHydraulique.meteo;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.RefOrientationVent;
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
public class TypeOrientationVentImporter extends GenericTypeReferenceImporter<RefOrientationVent> {
    
    TypeOrientationVentImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum Columns {
        ID_TYPE_ORIENTATION_VENT,
        LIBELLE_TYPE_ORIENTATION_VENT,
        ABREGE_TYPE_ORIENTATION_VENT,
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
        return DbImporter.TableName.TYPE_ORIENTATION_VENT.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefOrientationVent typeOrientation = new RefOrientationVent();
            
            typeOrientation.setId(typeOrientation.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_TYPE_ORIENTATION_VENT.toString())));
            typeOrientation.setLibelle(row.getString(Columns.LIBELLE_TYPE_ORIENTATION_VENT.toString()));
            typeOrientation.setAbrege(row.getString(Columns.ABREGE_TYPE_ORIENTATION_VENT.toString()));
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeOrientation.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            typeOrientation.setDesignation(String.valueOf(row.getInt(Columns.ID_TYPE_ORIENTATION_VENT.toString())));
            typeOrientation.setValid(true);
            
            types.put(row.getInt(String.valueOf(Columns.ID_TYPE_ORIENTATION_VENT.toString())), typeOrientation);
        }
        couchDbConnector.executeBulk(types.values());
    }
}
