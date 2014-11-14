package fr.sirs.importer.structure;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.component.RefSourceRepository;
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
public class TypeSourceImporter extends GenericImporter {

    private Map<Integer, RefSource> typesSource = null;
    private final RefSourceRepository refSourceRepository;
    
    
    public TypeSourceImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector, 
            final RefSourceRepository refSourceRepository) {
        super(accessDatabase, couchDbConnector);
        this.refSourceRepository = refSourceRepository;
    }

    private enum TypeSourceColumns {
        ID_SOURCE,
        LIBELLE_SOURCE,
        ABREGE_TYPE_SOURCE_INFO,
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the database RefSource referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, RefSource> getTypeSource() throws IOException {
        if(typesSource == null) compute();
        return typesSource;
    }
    
    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TypeSourceColumns c : TypeSourceColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.SOURCE_INFO.toString();
    }

    @Override
    protected void compute() throws IOException {
        typesSource = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefSource typeDesordre = new RefSource();
            
            typeDesordre.setLibelle(row.getString(TypeSourceColumns.LIBELLE_SOURCE.toString()));
            typeDesordre.setAbrege(row.getString(TypeSourceColumns.ABREGE_TYPE_SOURCE_INFO.toString()));
            if (row.getDate(TypeSourceColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeDesordre.setDateMaj(LocalDateTime.parse(row.getDate(TypeSourceColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            typesSource.put(row.getInt(String.valueOf(TypeSourceColumns.ID_SOURCE.toString())), typeDesordre);
            refSourceRepository.add(typeDesordre);
        }
    }
    
}
