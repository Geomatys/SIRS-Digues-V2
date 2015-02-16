package fr.sirs.importer.documentTroncon.document.profilLong;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.RefOrigineProfilLong;
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
class TypeOrigineProfilLongImporter extends GenericTypeReferenceImporter<RefOrigineProfilLong> {

    TypeOrigineProfilLongImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    private enum Columns {
        ID_TYPE_ORIGINE_PROFIL_EN_LONG,
        LIBELLE_TYPE_ORIGINE_PROFIL_EN_LONG,
//        ABREGE_TYPE_ORIGINE_PROFIL_EN_LONG,
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
        return DbImporter.TableName.TYPE_ORIGINE_PROFIL_EN_LONG.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefOrigineProfilLong typeOrigineProfilLong = new RefOrigineProfilLong();
            
            typeOrigineProfilLong.setId(typeOrigineProfilLong.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_TYPE_ORIGINE_PROFIL_EN_LONG.toString())));
            typeOrigineProfilLong.setLibelle(row.getString(Columns.LIBELLE_TYPE_ORIGINE_PROFIL_EN_LONG.toString()));
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeOrigineProfilLong.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            typeOrigineProfilLong.setPseudoId(row.getInt(Columns.ID_TYPE_ORIGINE_PROFIL_EN_LONG.toString()));
            types.put(row.getInt(String.valueOf(Columns.ID_TYPE_ORIGINE_PROFIL_EN_LONG.toString())), typeOrigineProfilLong);
        }
        couchDbConnector.executeBulk(types.values());
    }
}
