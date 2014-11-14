package fr.sirs.importer.structure;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.component.RefCoteRepository;
import fr.sirs.core.component.RefSourceRepository;
import fr.sirs.core.model.RefCote;
import fr.sirs.core.model.RefSource;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericImporter;
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
public class TypeCoteImporter extends GenericImporter {

    private Map<Integer, RefCote> typesCote = null;
    private final RefCoteRepository refCoteRepository;
    
    
    public TypeCoteImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector, 
            final RefCoteRepository refCoteRepository) {
        super(accessDatabase, couchDbConnector);
        this.refCoteRepository = refCoteRepository;
    }

    private enum TypeCoteColumns {
        ID_TYPE_COTE,
        LIBELLE_TYPE_COTE,
        ABREGE_TYPE_COTE,
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the database RefSource referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, RefCote> getTypeCote() throws IOException {
        if(typesCote == null) compute();
        return typesCote;
    }
    
    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TypeCoteColumns c : TypeCoteColumns.values()) {
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
        typesCote = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefCote typeCote = new RefCote();
            
            typeCote.setLibelle(row.getString(TypeCoteColumns.LIBELLE_TYPE_COTE.toString()));
            typeCote.setAbrege(row.getString(TypeCoteColumns.ABREGE_TYPE_COTE.toString()));
            if (row.getDate(TypeCoteColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeCote.setDateMaj(LocalDateTime.parse(row.getDate(TypeCoteColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            typesCote.put(row.getInt(String.valueOf(TypeCoteColumns.ID_TYPE_COTE.toString())), typeCote);
            refCoteRepository.add(typeCote);
        }
    }
    
}
