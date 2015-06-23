package fr.sirs.importer.objet.prestation;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.RefPrestation;
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
public class TypePrestationImporter extends GenericTypeReferenceImporter<RefPrestation> {
    
    public TypePrestationImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum Columns {
        ID_TYPE_PRESTATION,
        LIBELLE_TYPE_PRESTATION,
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
        return TYPE_PRESTATION.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefPrestation typePrestation = createAnonymValidElement(RefPrestation.class);
            
            typePrestation.setId(typePrestation.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_TYPE_PRESTATION.toString())));
            typePrestation.setLibelle(row.getString(Columns.LIBELLE_TYPE_PRESTATION.toString()));
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typePrestation.setDateMaj(DbImporter.parseLocalDateTime(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            typePrestation.setDesignation(String.valueOf(row.getInt(Columns.ID_TYPE_PRESTATION.toString())));
            
            types.put(row.getInt(String.valueOf(Columns.ID_TYPE_PRESTATION.toString())), typePrestation);
        }
        couchDbConnector.executeBulk(types.values());
    }
    
}
