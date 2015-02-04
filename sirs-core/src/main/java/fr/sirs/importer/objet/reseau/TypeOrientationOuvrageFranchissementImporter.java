package fr.sirs.importer.objet.reseau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.RefOrientationOuvrage;
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
class TypeOrientationOuvrageFranchissementImporter extends GenericTypeReferenceImporter<RefOrientationOuvrage> {
    
    TypeOrientationOuvrageFranchissementImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum Columns {
        ID_TYPE_ORIENTATION_OUVRAGE_FRANCHISSEMENT,
        LIBELLE_TYPE_ORIENTATION_OUVRAGE_FRANCHISSEMENT,
        ABREGE_TYPE_ORIENTATION_OUVRAGE_FRANCHISSEMENT,
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
        return DbImporter.TableName.TYPE_ORIENTATION_OUVRAGE_FRANCHISSEMENT.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefOrientationOuvrage typeUtilisation = new RefOrientationOuvrage();
            
            typeUtilisation.setId(typeUtilisation.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_TYPE_ORIENTATION_OUVRAGE_FRANCHISSEMENT.toString())));
            typeUtilisation.setLibelle(row.getString(Columns.LIBELLE_TYPE_ORIENTATION_OUVRAGE_FRANCHISSEMENT.toString()));
            typeUtilisation.setAbrege(row.getString(Columns.ABREGE_TYPE_ORIENTATION_OUVRAGE_FRANCHISSEMENT.toString()));
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeUtilisation.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            types.put(row.getInt(String.valueOf(Columns.ID_TYPE_ORIENTATION_OUVRAGE_FRANCHISSEMENT.toString())), typeUtilisation);
        }
        couchDbConnector.executeBulk(types.values());
    }
}
