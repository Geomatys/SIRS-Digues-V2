package fr.sirs.importer.objet.geometry;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.component.RefCoteRepository;
import fr.sirs.core.component.RefLargeurFrancBordRepository;
import fr.sirs.core.component.RefSourceRepository;
import fr.sirs.core.model.RefCote;
import fr.sirs.core.model.RefLargeurFrancBord;
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
public class TypeLargeurFrancBordImporter extends GenericImporter {

    private Map<Integer, RefLargeurFrancBord> typesCote = null;
    private final RefLargeurFrancBordRepository refLargeurFrancBordRepository;
    
    
    public TypeLargeurFrancBordImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector, 
            final RefLargeurFrancBordRepository refLargeurFrancBordRepository) {
        super(accessDatabase, couchDbConnector);
        this.refLargeurFrancBordRepository = refLargeurFrancBordRepository;
    }

    private enum TypeCoteColumns {
//ID_TYPE_LARGEUR_FB
//ABREGE_TYPE_LARGEUR_FB
//LIBELLE_TYPE_LARGEUR_FB
//DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the database RefLargeurFrancBord referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, RefLargeurFrancBord> getTypeCote() throws IOException {
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
        return DbImporter.TableName.TYPE_LARGEUR_FRANC_BORD.toString();
    }

    @Override
    protected void compute() throws IOException {
        typesCote = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefLargeurFrancBord typeCote = new RefLargeurFrancBord();
            
//            typeCote.setLibelle(row.getString(TypeCoteColumns.LIBELLE_TYPE_COTE.toString()));
//            typeCote.setAbrege(row.getString(TypeCoteColumns.ABREGE_TYPE_COTE.toString()));
//            if (row.getDate(TypeCoteColumns.DATE_DERNIERE_MAJ.toString()) != null) {
//                typeCote.setDateMaj(LocalDateTime.parse(row.getDate(TypeCoteColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
//            }
//            typesCote.put(row.getInt(String.valueOf(TypeCoteColumns.ID_TYPE_COTE.toString())), typeCote);
            refLargeurFrancBordRepository.add(typeCote);
        }
    }
    
}
