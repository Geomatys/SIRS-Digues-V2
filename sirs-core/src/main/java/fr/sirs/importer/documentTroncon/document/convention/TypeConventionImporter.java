package fr.sirs.importer.documentTroncon.document.convention;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.RefConvention;
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
        return TYPE_CONVENTION.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefConvention typeConvention = createAnonymValidElement(RefConvention.class);
            
            typeConvention.setId(typeConvention.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_TYPE_CONVENTION.toString())));
            typeConvention.setLibelle(row.getString(Columns.LIBELLE_TYPE_CONVENTION.toString()));
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeConvention.setDateMaj(DbImporter.parseLocalDateTime(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            typeConvention.setDesignation(String.valueOf(row.getInt(Columns.ID_TYPE_CONVENTION.toString())));
            
            types.put(row.getInt(String.valueOf(Columns.ID_TYPE_CONVENTION.toString())), typeConvention);
        }
        couchDbConnector.executeBulk(types.values());
    }
}
