package fr.sirs.importer.objet.reseau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.RefUsageVoie;
import static fr.sirs.importer.DbImporter.TableName.*;
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
class TypeUsageVoieImporter extends GenericTypeReferenceImporter<RefUsageVoie> {
    
    TypeUsageVoieImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum Columns {
        ID_TYPE_USAGE_VOIE,
        LIBELLE_TYPE_USAGE_VOIE,
        ABREGE_TYPE_USAGE_VOIE,
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
        return TYPE_USAGE_VOIE.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefUsageVoie typeUtilisation = createAnonymValidElement(RefUsageVoie.class);
            
            typeUtilisation.setId(typeUtilisation.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_TYPE_USAGE_VOIE.toString())));
            typeUtilisation.setLibelle(row.getString(Columns.LIBELLE_TYPE_USAGE_VOIE.toString()));
            typeUtilisation.setAbrege(row.getString(Columns.ABREGE_TYPE_USAGE_VOIE.toString()));
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeUtilisation.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            typeUtilisation.setDesignation(String.valueOf(row.getInt(String.valueOf(Columns.ID_TYPE_USAGE_VOIE.toString()))));
            
            types.put(row.getInt(String.valueOf(Columns.ID_TYPE_USAGE_VOIE.toString())), typeUtilisation);
        }
        couchDbConnector.executeBulk(types.values());
    }
}
