package fr.sirs.importer.objet;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.component.RefMateriauRepository;
import fr.sirs.core.component.RefNatureRepository;
import fr.sirs.core.model.RefMateriau;
import fr.sirs.core.model.RefNature;
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
public class TypeNatureImporter extends GenericImporter {

    private Map<Integer, RefNature> typesNature = null;
    private final RefNatureRepository refNatureRepository;
    
    
    public TypeNatureImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector, 
            final RefNatureRepository refNatureRepository) {
        super(accessDatabase, couchDbConnector);
        this.refNatureRepository = refNatureRepository;
    }

    private enum TypeNatureColumns {
        ID_TYPE_NATURE,
        ABREGE_TYPE_NATURE,
        LIBELLE_TYPE_NATURE,
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the database RefNature referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, RefNature> getTypeNature() throws IOException {
        if(typesNature == null) compute();
        return typesNature;
    }
    
    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TypeNatureColumns c : TypeNatureColumns.values()) {
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
        typesNature = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefNature typeNature = new RefNature();
            
            typeNature.setLibelle(row.getString(TypeNatureColumns.LIBELLE_TYPE_NATURE.toString()));
            typeNature.setAbrege(row.getString(TypeNatureColumns.ABREGE_TYPE_NATURE.toString()));
            if (row.getDate(TypeNatureColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeNature.setDateMaj(LocalDateTime.parse(row.getDate(TypeNatureColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            typesNature.put(row.getInt(String.valueOf(TypeNatureColumns.ID_TYPE_NATURE.toString())), typeNature);
            refNatureRepository.add(typeNature);
        }
    }
    
}
