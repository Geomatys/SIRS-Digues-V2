package fr.sirs.importer.objet.reseau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.RefEcoulement;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericImporter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeEcoulementImporter extends GenericImporter {

    private Map<Integer, RefEcoulement> typesEcoulement = null;
    
    
    TypeEcoulementImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum TypeEcoulementColumns {
        ID_ECOULEMENT,
        LIBELLE_ECOULEMENT,
        ABREGE_ECOULEMENT,
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the database RefEcoulement referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, RefEcoulement> getTypeEcoulement() throws IOException {
        if(typesEcoulement == null) compute();
        return typesEcoulement;
    }
    
    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TypeEcoulementColumns c : TypeEcoulementColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.ECOULEMENT.toString();
    }

    @Override
    protected void compute() throws IOException {
        typesEcoulement = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefEcoulement typeEcoulement = new RefEcoulement();
            
            typeEcoulement.setLibelle(row.getString(TypeEcoulementColumns.LIBELLE_ECOULEMENT.toString()));
            typeEcoulement.setAbrege(row.getString(TypeEcoulementColumns.ABREGE_ECOULEMENT.toString()));
            if (row.getDate(TypeEcoulementColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeEcoulement.setDateMaj(LocalDateTime.parse(row.getDate(TypeEcoulementColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            typesEcoulement.put(row.getInt(String.valueOf(TypeEcoulementColumns.ID_ECOULEMENT.toString())), typeEcoulement);
        }
        couchDbConnector.executeBulk(typesEcoulement.values());
    }
    
}
