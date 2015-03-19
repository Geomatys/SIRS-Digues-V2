package fr.sirs.importer.documentTroncon.document.convention;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.RefConvention;
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
class TypeConventionImporter extends GenericTypeReferenceImporter<RefConvention> {

    TypeConventionImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    private enum Columns {
        ID_TYPE_CONVENTION,
        LIBELLE_TYPE_CONVENTION,
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
        return DbImporter.TableName.TYPE_CONVENTION.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefConvention typeConvention = new RefConvention();
            
            typeConvention.setId(typeConvention.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_TYPE_CONVENTION.toString())));
            typeConvention.setLibelle(row.getString(Columns.LIBELLE_TYPE_CONVENTION.toString()));
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeConvention.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            typeConvention.setDesignation(String.valueOf(row.getInt(Columns.ID_TYPE_CONVENTION.toString())));
            typeConvention.setValid(true);
            
            types.put(row.getInt(String.valueOf(Columns.ID_TYPE_CONVENTION.toString())), typeConvention);
        }
        couchDbConnector.executeBulk(types.values());
    }
}
