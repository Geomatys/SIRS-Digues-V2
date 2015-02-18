package fr.sirs.importer.objet.reseau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.RefMoyenManipBatardeaux;
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
class TypeMoyenManipBatardeauxImporter extends GenericTypeReferenceImporter<RefMoyenManipBatardeaux> {
    
    TypeMoyenManipBatardeauxImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum Columns {
        ID_TYPE_MOYEN_MANIP_BATARDEAUX,
        LIBELLE_TYPE_MOYEN_MANIP_BATARDEAUX,
        ABREGE_TYPE_MOYEN_MANIP_BATARDEAUX,
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
        return DbImporter.TableName.TYPE_MOYEN_MANIP_BATARDEAUX.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefMoyenManipBatardeaux moyen = new RefMoyenManipBatardeaux();
            
            moyen.setId(moyen.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_TYPE_MOYEN_MANIP_BATARDEAUX.toString())));
            moyen.setLibelle(row.getString(Columns.LIBELLE_TYPE_MOYEN_MANIP_BATARDEAUX.toString()));
            moyen.setAbrege(row.getString(Columns.ABREGE_TYPE_MOYEN_MANIP_BATARDEAUX.toString()));
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                moyen.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            moyen.setPseudoId(String.valueOf(row.getInt(String.valueOf(Columns.ID_TYPE_MOYEN_MANIP_BATARDEAUX.toString()))));
            types.put(row.getInt(String.valueOf(Columns.ID_TYPE_MOYEN_MANIP_BATARDEAUX.toString())), moyen);
        }
        couchDbConnector.executeBulk(types.values());
    }
}
