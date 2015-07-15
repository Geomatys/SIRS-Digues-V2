package fr.sirs.importer.objet;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.RefPosition;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericTypeReferenceImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class TypePositionImporter extends GenericTypeReferenceImporter<RefPosition> {
    
    TypePositionImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum Columns {
        ID_TYPE_POSITION,
        LIBELLE_TYPE_POSITION,
        ABREGE_TYPE_POSITION,
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
        return TYPE_POSITION.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefPosition typePosition = createAnonymValidElement(RefPosition.class);
            
            typePosition.setId(typePosition.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_TYPE_POSITION.toString())));
            typePosition.setLibelle(row.getString(Columns.LIBELLE_TYPE_POSITION.toString()));
            typePosition.setAbrege(row.getString(Columns.ABREGE_TYPE_POSITION.toString()));
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typePosition.setDateMaj(DbImporter.parseLocalDate(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            typePosition.setDesignation(String.valueOf(row.getInt(String.valueOf(Columns.ID_TYPE_POSITION.toString()))));
            
            types.put(row.getInt(String.valueOf(Columns.ID_TYPE_POSITION.toString())), typePosition);
        }
        couchDbConnector.executeBulk(types.values());
    }
    
}
