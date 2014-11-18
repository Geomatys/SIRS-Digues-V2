package fr.sirs.importer.structure;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.component.RefMateriauRepository;
import fr.sirs.core.model.RefMateriau;
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
public class TypeMateriauImporter extends GenericImporter {

    private Map<Integer, RefMateriau> typesMateriau = null;
    private final RefMateriauRepository refMateriauRepository;
    
    
    public TypeMateriauImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector, 
            final RefMateriauRepository refMateriauRepository) {
        super(accessDatabase, couchDbConnector);
        this.refMateriauRepository = refMateriauRepository;
    }

    private enum TypeMateriauColumns {
        ID_TYPE_MATERIAU,
        ABREGE_TYPE_MATERIAU,
        LIBELLE_TYPE_MATERIAU,
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the database RefMateriau referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, RefMateriau> getTypeMateriau() throws IOException {
        if(typesMateriau == null) compute();
        return typesMateriau;
    }
    
    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TypeMateriauColumns c : TypeMateriauColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.TYPE_MATERIAU.toString();
    }

    @Override
    protected void compute() throws IOException {
        typesMateriau = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefMateriau typeMateriau = new RefMateriau();
            
            typeMateriau.setLibelle(row.getString(TypeMateriauColumns.LIBELLE_TYPE_MATERIAU.toString()));
            typeMateriau.setAbrege(row.getString(TypeMateriauColumns.ABREGE_TYPE_MATERIAU.toString()));
            if (row.getDate(TypeMateriauColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeMateriau.setDateMaj(LocalDateTime.parse(row.getDate(TypeMateriauColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            typesMateriau.put(row.getInt(String.valueOf(TypeMateriauColumns.ID_TYPE_MATERIAU.toString())), typeMateriau);
            refMateriauRepository.add(typeMateriau);
        }
    }
    
}
