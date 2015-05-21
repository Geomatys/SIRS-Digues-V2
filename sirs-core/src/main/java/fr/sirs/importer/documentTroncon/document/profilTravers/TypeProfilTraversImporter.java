package fr.sirs.importer.documentTroncon.document.profilTravers;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.RefTypeProfilTravers;
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
class TypeProfilTraversImporter extends GenericTypeReferenceImporter<RefTypeProfilTravers> {

    TypeProfilTraversImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    private enum Columns {
        ID_TYPE_PROFIL_EN_TRAVERS,
        LIBELLE_TYPE_PROFIL_EN_TRAVERS,
//        ABREGE_TYPE_PROFIL_EN_TRAVERS, // Pas dans le nouveau modèle (supprimé)
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
        return DbImporter.TableName.TYPE_PROFIL_EN_TRAVERS.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefTypeProfilTravers typeProfilTravers = new RefTypeProfilTravers();
            
            typeProfilTravers.setId(typeProfilTravers.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_TYPE_PROFIL_EN_TRAVERS.toString())));
            typeProfilTravers.setLibelle(row.getString(Columns.LIBELLE_TYPE_PROFIL_EN_TRAVERS.toString()));
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeProfilTravers.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            typeProfilTravers.setDesignation(String.valueOf(row.getInt(Columns.ID_TYPE_PROFIL_EN_TRAVERS.toString())));
            typeProfilTravers.setValid(true);
            
            types.put(row.getInt(String.valueOf(Columns.ID_TYPE_PROFIL_EN_TRAVERS.toString())), typeProfilTravers);
        }
        couchDbConnector.executeBulk(types.values());
    }
}
