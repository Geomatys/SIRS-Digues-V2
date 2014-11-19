package fr.sirs.importer.structure;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.component.RefFonctionRepository;
import fr.sirs.core.model.RefFonction;
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
public class TypeFonctionImporter extends GenericImporter {

    private Map<Integer, RefFonction> typesNature = null;
    private final RefFonctionRepository refFonctionRepository;
    
    
    public TypeFonctionImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector, 
            final RefFonctionRepository refFonctionRepository) {
        super(accessDatabase, couchDbConnector);
        this.refFonctionRepository = refFonctionRepository;
    }

    private enum TypeFonctionColumns {
        ID_TYPE_FONCTION,
        LIBELLE_TYPE_FONCTION,
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the database RefFonction referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, RefFonction> getTypeFonction() throws IOException {
        if(typesNature == null) compute();
        return typesNature;
    }
    
    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TypeFonctionColumns c : TypeFonctionColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.TYPE_FONCTION.toString();
    }

    @Override
    protected void compute() throws IOException {
        typesNature = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefFonction typeFonction = new RefFonction();
            
            typeFonction.setLibelle(row.getString(TypeFonctionColumns.LIBELLE_TYPE_FONCTION.toString()));
            if (row.getDate(TypeFonctionColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeFonction.setDateMaj(LocalDateTime.parse(row.getDate(TypeFonctionColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            typesNature.put(row.getInt(String.valueOf(TypeFonctionColumns.ID_TYPE_FONCTION.toString())), typeFonction);
            refFonctionRepository.add(typeFonction);
        }
    }
    
}
