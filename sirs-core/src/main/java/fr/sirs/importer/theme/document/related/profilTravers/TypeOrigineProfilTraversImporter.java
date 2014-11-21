package fr.sirs.importer.theme.document.related.profilTravers;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.component.RefTypeProfilTraversRepository;
import fr.sirs.core.model.RefOrigineProfilTravers;
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
class TypeOrigineProfilTraversImporter extends GenericImporter {

    private Map<Integer, RefOrigineProfilTravers> typesOriginesProfilsTravers = null;

    TypeOrigineProfilTraversImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    private enum TypeOrigineProfilTraversColumns {
        ID_TYPE_ORIGINE_PROFIL_EN_TRAVERS,
        LIBELLE_TYPE_ORIGINE_PROFIL_EN_TRAVERS,
//        ABREGE_TYPE_ORIGINE_PROFIL_EN_TRAVERS, // Pas dans le nouveau modèle
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the database RefOrigineProfilTravers referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, RefOrigineProfilTravers> getTypeOrigineProfilTravers() throws IOException {
        if(typesOriginesProfilsTravers == null) compute();
        return typesOriginesProfilsTravers;
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TypeOrigineProfilTraversColumns c : TypeOrigineProfilTraversColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.TYPE_ORIGINE_PROFIL_EN_TRAVERS.toString();
    }

    @Override
    protected void compute() throws IOException {
        typesOriginesProfilsTravers = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefOrigineProfilTravers typeOrigineProfilTravers = new RefOrigineProfilTravers();
            
            typeOrigineProfilTravers.setLibelle(row.getString(TypeOrigineProfilTraversColumns.LIBELLE_TYPE_ORIGINE_PROFIL_EN_TRAVERS.toString()));
            
            if (row.getDate(TypeOrigineProfilTraversColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeOrigineProfilTravers.setDateMaj(LocalDateTime.parse(row.getDate(TypeOrigineProfilTraversColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            typesOriginesProfilsTravers.put(row.getInt(String.valueOf(TypeOrigineProfilTraversColumns.ID_TYPE_ORIGINE_PROFIL_EN_TRAVERS.toString())), typeOrigineProfilTravers);
        }
        couchDbConnector.executeBulk(typesOriginesProfilsTravers.values());
    }
}
