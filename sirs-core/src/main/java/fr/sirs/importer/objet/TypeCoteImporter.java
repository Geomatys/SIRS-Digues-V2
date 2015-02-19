package fr.sirs.importer.objet;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.RefCote;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericTypeReferenceImporter;
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
        return DbImporter.TableName.TYPE_COTE.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefCote typeCote = new RefCote();
            
            typeCote.setId(typeCote.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_TYPE_COTE.toString())));
            typeCote.setLibelle(row.getString(Columns.LIBELLE_TYPE_COTE.toString()));
            typeCote.setAbrege(row.getString(Columns.ABREGE_TYPE_COTE.toString()));
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeCote.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            typeCote.setPseudoId(String.valueOf(row.getInt(String.valueOf(Columns.ID_TYPE_COTE.toString()))));
            typeCote.setValid(true);
            
            types.put(row.getInt(String.valueOf(Columns.ID_TYPE_COTE.toString())), typeCote);
        }
        couchDbConnector.executeBulk(types.values());
    }
}
