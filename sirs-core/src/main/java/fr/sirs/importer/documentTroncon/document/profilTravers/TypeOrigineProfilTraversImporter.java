package fr.sirs.importer.documentTroncon.document.profilTravers;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.RefOrigineProfilTravers;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericTypeReferenceImporter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeOrigineProfilTraversImporter extends GenericTypeReferenceImporter<RefOrigineProfilTravers> {

    TypeOrigineProfilTraversImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    private enum Columns {
        ID_TYPE_ORIGINE_PROFIL_EN_TRAVERS,
        LIBELLE_TYPE_ORIGINE_PROFIL_EN_TRAVERS,
//        ABREGE_TYPE_ORIGINE_PROFIL_EN_TRAVERS, // Pas dans le nouveau modèle
        DATE_DERNIERE_MAJ
    };

    @Override
    protected List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (Columns c : Columns.values()) {
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
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefOrigineProfilTravers typeOrigineProfilTravers = new RefOrigineProfilTravers();
            
            typeOrigineProfilTravers.setId(typeOrigineProfilTravers.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_TYPE_ORIGINE_PROFIL_EN_TRAVERS.toString())));
            typeOrigineProfilTravers.setLibelle(row.getString(Columns.LIBELLE_TYPE_ORIGINE_PROFIL_EN_TRAVERS.toString()));
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeOrigineProfilTravers.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            types.put(row.getInt(String.valueOf(Columns.ID_TYPE_ORIGINE_PROFIL_EN_TRAVERS.toString())), typeOrigineProfilTravers);
        }
        couchDbConnector.executeBulk(types.values());
    }
}
