package fr.sirs.importer.objet;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.RefNature;
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
public class TypeNatureImporter extends GenericTypeReferenceImporter<RefNature> {
    
    TypeNatureImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum Columns {
        ID_TYPE_NATURE,
        ABREGE_TYPE_NATURE,
        LIBELLE_TYPE_NATURE,
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
        return DbImporter.TableName.TYPE_NATURE.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefNature typeNature = new RefNature();
            
            typeNature.setId(typeNature.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_TYPE_NATURE.toString())));
            typeNature.setLibelle(row.getString(Columns.LIBELLE_TYPE_NATURE.toString()));
            typeNature.setAbrege(row.getString(Columns.ABREGE_TYPE_NATURE.toString()));
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeNature.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            typeNature.setDesignation(String.valueOf(row.getInt(String.valueOf(Columns.ID_TYPE_NATURE.toString()))));
            typeNature.setValid(true);
            
            types.put(row.getInt(String.valueOf(Columns.ID_TYPE_NATURE.toString())), typeNature);
        }
        couchDbConnector.executeBulk(types.values());
    }
    
}
