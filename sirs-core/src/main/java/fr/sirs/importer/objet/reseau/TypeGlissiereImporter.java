package fr.sirs.importer.objet.reseau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.RefTypeGlissiere;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericTypeImporter;
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
class TypeGlissiereImporter extends GenericTypeImporter<RefTypeGlissiere> {
    
    TypeGlissiereImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum Columns {
        ID_TYPE_GLISSIERE,
        LIBELLE_TYPE_GLISSIERE,
        ABREGE_TYPE_GLISSIERE,
        DATE_DERNIERE_MAJ
    };
    
    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (Columns c : Columns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.TYPE_GLISSIERE.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefTypeGlissiere typeGlissiere = new RefTypeGlissiere();
            
            typeGlissiere.setLibelle(row.getString(Columns.LIBELLE_TYPE_GLISSIERE.toString()));
            typeGlissiere.setAbrege(row.getString(Columns.ABREGE_TYPE_GLISSIERE.toString()));
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeGlissiere.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            types.put(row.getInt(String.valueOf(Columns.ID_TYPE_GLISSIERE.toString())), typeGlissiere);
        }
        couchDbConnector.executeBulk(types.values());
    }
}
