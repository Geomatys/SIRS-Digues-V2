package fr.sirs.importer.objet;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.RefMateriau;
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
public class TypeMateriauImporter extends GenericTypeReferenceImporter<RefMateriau> {
    
    TypeMateriauImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum Columns {
        ID_TYPE_MATERIAU,
        ABREGE_TYPE_MATERIAU,
        LIBELLE_TYPE_MATERIAU,
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
        return TYPE_MATERIAU.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefMateriau typeMateriau = createAnonymValidElement(RefMateriau.class);
            
            typeMateriau.setId(typeMateriau.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_TYPE_MATERIAU.toString())));
            typeMateriau.setLibelle(row.getString(Columns.LIBELLE_TYPE_MATERIAU.toString()));
            typeMateriau.setAbrege(row.getString(Columns.ABREGE_TYPE_MATERIAU.toString()));
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeMateriau.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            typeMateriau.setDesignation(String.valueOf(row.getInt(String.valueOf(Columns.ID_TYPE_MATERIAU.toString()))));
            
            types.put(row.getInt(String.valueOf(Columns.ID_TYPE_MATERIAU.toString())), typeMateriau);
        }
        couchDbConnector.executeBulk(types.values());
    }
    
}
