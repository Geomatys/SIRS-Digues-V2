package fr.sirs.importer.documentTroncon.document.profilLong;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.RefPositionProfilLongSurDigue;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericTypeReferenceImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypePositionProfilLongImporter extends GenericTypeReferenceImporter<RefPositionProfilLongSurDigue> {

    TypePositionProfilLongImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    private enum Columns {
        ID_TYPE_POSITION_PROFIL_EN_LONG,
        LIBELLE_TYPE_POSITION_PROFIL_EN_LONG,
        ABREGE_TYPE_POSITION_PROFIL_EN_LONG,
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
        return TYPE_POSITION_PROFIL_EN_LONG_SUR_DIGUE.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefPositionProfilLongSurDigue typePositionProfilLong = createAnonymValidElement(RefPositionProfilLongSurDigue.class);
            
            typePositionProfilLong.setId(typePositionProfilLong.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_TYPE_POSITION_PROFIL_EN_LONG.toString())));
            typePositionProfilLong.setLibelle(row.getString(Columns.LIBELLE_TYPE_POSITION_PROFIL_EN_LONG.toString()));
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typePositionProfilLong.setDateMaj(DbImporter.parseLocalDate(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            typePositionProfilLong.setDesignation(String.valueOf(row.getInt(Columns.ID_TYPE_POSITION_PROFIL_EN_LONG.toString())));
            
            types.put(row.getInt(String.valueOf(Columns.ID_TYPE_POSITION_PROFIL_EN_LONG.toString())), typePositionProfilLong);
        }
        couchDbConnector.executeBulk(types.values());
    }
}
