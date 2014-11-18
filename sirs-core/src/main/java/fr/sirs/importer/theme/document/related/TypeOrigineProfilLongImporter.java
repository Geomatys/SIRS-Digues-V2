package fr.sirs.importer.theme.document.related;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.component.RefTypeProfilTraversRepository;
import fr.sirs.core.model.RefOrigineProfilLong;
import fr.sirs.core.model.RefTypeProfilTravers;
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
public class TypeOrigineProfilLongImporter extends GenericImporter {

    private Map<Integer, RefOrigineProfilLong> typesOriginesProfilsLong = null;
    private final RefTypeProfilTraversRepository refTypeProfilTraversRepository;

    public TypeOrigineProfilLongImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final RefTypeProfilTraversRepository refTypeProfilTraversRepository) {
        super(accessDatabase, couchDbConnector);
        this.refTypeProfilTraversRepository = refTypeProfilTraversRepository;
    }
    
    private enum TypeOrigineProfilLongColumns {
        ID_TYPE_ORIGINE_PROFIL_EN_LONG,
        LIBELLE_TYPE_ORIGINE_PROFIL_EN_LONG,
//        ABREGE_TYPE_ORIGINE_PROFIL_EN_LONG,
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the database RefOrigineProfilLong referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, RefOrigineProfilLong> getTypeOrigineProfilLong() throws IOException {
        if(typesOriginesProfilsLong == null) compute();
        return typesOriginesProfilsLong;
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TypeOrigineProfilLongColumns c : TypeOrigineProfilLongColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.TYPE_ORIGINE_PROFIL_EN_LONG.toString();
    }

    @Override
    protected void compute() throws IOException {
        typesOriginesProfilsLong = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefOrigineProfilLong refOrigineProfilLong = new RefOrigineProfilLong();
            
            refOrigineProfilLong.setLibelle(row.getString(TypeOrigineProfilLongColumns.LIBELLE_TYPE_ORIGINE_PROFIL_EN_LONG.toString()));
            
            if (row.getDate(TypeOrigineProfilLongColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                refOrigineProfilLong.setDateMaj(LocalDateTime.parse(row.getDate(TypeOrigineProfilLongColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            typesOriginesProfilsLong.put(row.getInt(String.valueOf(TypeOrigineProfilLongColumns.ID_TYPE_ORIGINE_PROFIL_EN_LONG.toString())), refOrigineProfilLong);
//            refTypeProfilTraversRepository.add(typeProfilTravers);
        }
    }
}
