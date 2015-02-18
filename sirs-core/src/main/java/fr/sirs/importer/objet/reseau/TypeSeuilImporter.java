package fr.sirs.importer.objet.reseau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.RefSeuil;
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
class TypeSeuilImporter extends GenericTypeReferenceImporter<RefSeuil> {
    
    TypeSeuilImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum Columns {
        ID_TYPE_SEUIL,
        LIBELLE_TYPE_SEUIL,
        ABREGE_TYPE_SEUIL,
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
        return DbImporter.TableName.TYPE_SEUIL.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefSeuil typeSeuil = new RefSeuil();
            
            typeSeuil.setId(typeSeuil.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_TYPE_SEUIL.toString())));
            typeSeuil.setLibelle(row.getString(Columns.LIBELLE_TYPE_SEUIL.toString()));
            typeSeuil.setAbrege(row.getString(Columns.ABREGE_TYPE_SEUIL.toString()));
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeSeuil.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            typeSeuil.setPseudoId(String.valueOf(row.getInt(String.valueOf(Columns.ID_TYPE_SEUIL.toString()))));
            types.put(row.getInt(String.valueOf(Columns.ID_TYPE_SEUIL.toString())), typeSeuil);
        }
        couchDbConnector.executeBulk(types.values());
    }
}
