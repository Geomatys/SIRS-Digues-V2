package fr.sym.util.importer;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.symadrem.sirs.core.component.RefRiveRepository;
import fr.symadrem.sirs.core.model.RefRive;
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
public class TypeRiveImporter extends GenericImporter {

    private Map<Integer, RefRive> typesRive = null;
    private final RefRiveRepository refRiveRepository;

    TypeRiveImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final RefRiveRepository refRiveRepository) {
        super(accessDatabase, couchDbConnector);
        this.refRiveRepository = refRiveRepository;
    }
    
    private enum TypeRiveColumns {
        ID_TYPE_RIVE, 
        LIBELLE_TYPE_RIVE, 
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the database TypeRive referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, RefRive> getTypeRive() throws IOException {
        if(typesRive == null) compute();
        return typesRive;
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TypeRiveColumns c : TypeRiveColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.TYPE_RIVE.toString();
    }

    @Override
    protected void compute() throws IOException {
        typesRive = new HashMap<>();
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();

        while (it.hasNext()) {
            final Row row = it.next();
            final RefRive typeRive = new RefRive();
            typeRive.setLibelle(row.getString(TypeRiveColumns.LIBELLE_TYPE_RIVE.toString()));
            
            if (row.getDate(TypeRiveColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeRive.setDateMaj(LocalDateTime.parse(row.getDate(TypeRiveColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            typesRive.put(row.getInt(String.valueOf(TypeRiveColumns.ID_TYPE_RIVE.toString())), typeRive);
            refRiveRepository.add(typeRive);
        }
    }
}
