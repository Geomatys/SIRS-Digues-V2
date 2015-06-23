package fr.sirs.importer;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.RefCote;
import static fr.sirs.importer.DbImporter.TableName.*;
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
public class TypeCoteImporter extends GenericTypeReferenceImporter<RefCote> {
    
    TypeCoteImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum Columns {
        ID_TYPE_COTE,
        LIBELLE_TYPE_COTE,
        ABREGE_TYPE_COTE,
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
        return TYPE_COTE.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefCote typeCote = createAnonymValidElement(RefCote.class);
            
            typeCote.setId(typeCote.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_TYPE_COTE.toString())));
            typeCote.setLibelle(row.getString(Columns.LIBELLE_TYPE_COTE.toString()));
            typeCote.setAbrege(row.getString(Columns.ABREGE_TYPE_COTE.toString()));
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeCote.setDateMaj(DbImporter.parseLocalDateTime(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            typeCote.setDesignation(String.valueOf(row.getInt(String.valueOf(Columns.ID_TYPE_COTE.toString()))));
            
            types.put(row.getInt(String.valueOf(Columns.ID_TYPE_COTE.toString())), typeCote);
        }
        couchDbConnector.executeBulk(types.values());
    }
}
