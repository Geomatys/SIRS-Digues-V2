package fr.sirs.importer.objet.desordre;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.RefUrgence;
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
class TypeUrgenceImporter extends GenericTypeReferenceImporter<RefUrgence> {

    TypeUrgenceImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    private enum Columns {
        ID_TYPE_URGENCE,
        LIBELLE_TYPE_URGENCE,
        ABREGE_TYPE_URGENCE,
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
        return TYPE_URGENCE.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefUrgence typeUrgence = createAnonymValidElement(RefUrgence.class);
            
            typeUrgence.setId(typeUrgence.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_TYPE_URGENCE.toString())));
            typeUrgence.setLibelle(row.getString(Columns.LIBELLE_TYPE_URGENCE.toString()));
            typeUrgence.setAbrege(row.getString(Columns.ABREGE_TYPE_URGENCE.toString()));
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeUrgence.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            typeUrgence.setDesignation(String.valueOf(row.getInt(Columns.ID_TYPE_URGENCE.toString())));
            
            types.put(row.getInt(Columns.ID_TYPE_URGENCE.toString()), typeUrgence);
        }
        couchDbConnector.executeBulk(types.values());
    }
}
