package fr.sirs.importer.structure.desordre;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.component.RefPositionRepository;
import fr.sirs.core.component.RefSourceRepository;
import fr.sirs.core.model.RefPosition;
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
public class TypePositionImporter extends GenericImporter {

    private Map<Integer, RefPosition> typesPosition = null;
    private final RefPositionRepository refPositionRepository;
    
    
    public TypePositionImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector, 
            final RefPositionRepository refPositionRepository) {
        super(accessDatabase, couchDbConnector);
        this.refPositionRepository = refPositionRepository;
    }

    private enum TypePositionColumns {
        ID_TYPE_POSITION,
        LIBELLE_TYPE_POSITION,
        ABREGE_TYPE_POSITION,
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the database RefSource referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, RefPosition> getTypePosition() throws IOException {
        if(typesPosition == null) compute();
        return typesPosition;
    }
    
    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TypePositionColumns c : TypePositionColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.TYPE_POSITION.toString();
    }

    @Override
    protected void compute() throws IOException {
        typesPosition = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefPosition typePosition = new RefPosition();
            
            typePosition.setLibelle(row.getString(TypePositionColumns.LIBELLE_TYPE_POSITION.toString()));
            typePosition.setAbrege(row.getString(TypePositionColumns.ABREGE_TYPE_POSITION.toString()));
            if (row.getDate(TypePositionColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                typePosition.setDateMaj(LocalDateTime.parse(row.getDate(TypePositionColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            typesPosition.put(row.getInt(String.valueOf(TypePositionColumns.ID_TYPE_POSITION.toString())), typePosition);
            refPositionRepository.add(typePosition);
        }
    }
    
}
