package fr.sirs.importer.troncon;

import com.healthmarketscience.jackcess.Database;
import static fr.sirs.importer.DbImporter.TableName.COMMUNE;
import fr.sirs.importer.GenericImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class CommuneImporter extends GenericImporter {

//    private Map<Integer, Commune> communes = null;

    CommuneImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum Columns {
        ID_COMMUNE,
        CODE_INSEE_COMMUNE,
        LIBELLE_COMMUNE,
        DATE_DERNIERE_MAJ
    };

//    public Map<Integer, Commune> getCommunes() throws IOException {
//        if (communes == null) compute();
//        return communes;
//    }

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
        return COMMUNE.toString();
    }

    @Override
    protected void compute() throws IOException {
//        communes = new HashMap<>();
//        
//        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
//        while (it.hasNext()) {
//            final Row row = it.next();
//            final Commune commune = createAnonymValidElement(Commune.class);
//            
//            commune.setCodeInsee(row.getString(Columns.CODE_INSEE_COMMUNE.toString()));
//            
//            commune.setLibelle(row.getString(Columns.LIBELLE_COMMUNE.toString()));
//            
//            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
//                commune.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
//            }
//            
//            commune.setDesignation(String.valueOf(row.getInt(Columns.ID_COMMUNE.toString())));
//            
//            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
//            communes.put(row.getInt(Columns.ID_COMMUNE.toString()), commune);
//        }
//        couchDbConnector.executeBulk(communes.values());
    }
}
