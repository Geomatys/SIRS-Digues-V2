package fr.sirs.importer.objet.reseau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.RefRevetement;
import static fr.sirs.importer.DbImporter.TableName.*;
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
class TypeRevetementImporter extends GenericTypeReferenceImporter<RefRevetement> {
    
    TypeRevetementImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum Columns {
        ID_TYPE_REVETEMENT,
        LIBELLE_TYPE_REVETEMENT,
        ABREGE_TYPE_REVETEMENT,
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
        return TYPE_REVETEMENT.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefRevetement typeRevetement = createAnonymValidElement(RefRevetement.class);
            
            typeRevetement.setId(typeRevetement.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_TYPE_REVETEMENT.toString())));
            typeRevetement.setLibelle(row.getString(Columns.LIBELLE_TYPE_REVETEMENT.toString()));
            typeRevetement.setAbrege(row.getString(Columns.ABREGE_TYPE_REVETEMENT.toString()));
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeRevetement.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            typeRevetement.setDesignation(String.valueOf(row.getInt(String.valueOf(Columns.ID_TYPE_REVETEMENT.toString()))));
            
            types.put(row.getInt(String.valueOf(Columns.ID_TYPE_REVETEMENT.toString())), typeRevetement);
        }
        couchDbConnector.executeBulk(types.values());
    }
}
