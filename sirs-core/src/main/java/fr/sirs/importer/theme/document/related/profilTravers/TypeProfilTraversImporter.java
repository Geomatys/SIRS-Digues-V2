package fr.sirs.importer.theme.document.related.profilTravers;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.component.RefTypeProfilTraversRepository;
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
public class TypeProfilTraversImporter extends GenericImporter {

    private Map<Integer, RefTypeProfilTravers> typesProfilsTravers = null;
    private final RefTypeProfilTraversRepository refTypeProfilTraversRepository;

    public TypeProfilTraversImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final RefTypeProfilTraversRepository refTypeProfilTraversRepository) {
        super(accessDatabase, couchDbConnector);
        this.refTypeProfilTraversRepository = refTypeProfilTraversRepository;
    }
    
    private enum TypeProfilTraversColumns {
        ID_TYPE_PROFIL_EN_TRAVERS,
        LIBELLE_TYPE_PROFIL_EN_TRAVERS,
//        ABREGE_TYPE_PROFIL_EN_TRAVERS, // Pas dans le nouveau modèle (supprimé)
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the database RefTypeProfilTravers referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, RefTypeProfilTravers> getTypeProfilTravers() throws IOException {
        if(typesProfilsTravers == null) compute();
        return typesProfilsTravers;
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TypeProfilTraversColumns c : TypeProfilTraversColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.TYPE_PROFIL_EN_TRAVERS.toString();
    }

    @Override
    protected void compute() throws IOException {
        typesProfilsTravers = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefTypeProfilTravers typeProfilTravers = new RefTypeProfilTravers();
            
            typeProfilTravers.setLibelle(row.getString(TypeProfilTraversColumns.LIBELLE_TYPE_PROFIL_EN_TRAVERS.toString()));
            
            if (row.getDate(TypeProfilTraversColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeProfilTravers.setDateMaj(LocalDateTime.parse(row.getDate(TypeProfilTraversColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            typesProfilsTravers.put(row.getInt(String.valueOf(TypeProfilTraversColumns.ID_TYPE_PROFIL_EN_TRAVERS.toString())), typeProfilTravers);
            refTypeProfilTraversRepository.add(typeProfilTravers);
        }
    }
}
