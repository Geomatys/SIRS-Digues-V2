package fr.sirs.importer.objet.reseau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.RefNatureBatardeaux;
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
class TypeNatureBatardeauxImporter extends GenericTypeReferenceImporter<RefNatureBatardeaux> {
    
    TypeNatureBatardeauxImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum Columns {
        ID_TYPE_NATURE_BATARDEAUX,
        LIBELLE_TYPE_NATURE_BATARDEAUX,
        ABREGE_TYPE_NATURE_BATARDEAUX,
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
        return DbImporter.TableName.TYPE_NATURE_BATARDEAUX.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefNatureBatardeaux nature = new RefNatureBatardeaux();
            
            nature.setId(nature.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_TYPE_NATURE_BATARDEAUX.toString())));
            nature.setLibelle(row.getString(Columns.LIBELLE_TYPE_NATURE_BATARDEAUX.toString()));
            nature.setAbrege(row.getString(Columns.ABREGE_TYPE_NATURE_BATARDEAUX.toString()));
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                nature.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            nature.setPseudoId(String.valueOf(row.getInt(String.valueOf(Columns.ID_TYPE_NATURE_BATARDEAUX.toString()))));
            types.put(row.getInt(String.valueOf(Columns.ID_TYPE_NATURE_BATARDEAUX.toString())), nature);
        }
        couchDbConnector.executeBulk(types.values());
    }
}
