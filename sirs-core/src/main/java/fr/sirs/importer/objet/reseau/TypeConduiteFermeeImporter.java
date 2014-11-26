package fr.sirs.importer.objet.reseau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.RefConduiteFermee;
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
class TypeConduiteFermeeImporter extends GenericImporter {

    private Map<Integer, RefConduiteFermee> typesEcoulement = null;
    
    
    TypeConduiteFermeeImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum TypeConduiteFermeeColumns {
        ID_TYPE_CONDUITE_FERMEE,
        LIBELLE_TYPE_CONDUITE_FERMEE,
        ABREGE_TYPE_CONDUITE_FERMEE,
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the database RefConduiteFermee referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, RefConduiteFermee> getTypeConduiteFerme() throws IOException {
        if(typesEcoulement == null) compute();
        return typesEcoulement;
    }
    
    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TypeConduiteFermeeColumns c : TypeConduiteFermeeColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.TYPE_CONDUITE_FERMEE.toString();
    }

    @Override
    protected void compute() throws IOException {
        typesEcoulement = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefConduiteFermee typeConduite = new RefConduiteFermee();
            
            typeConduite.setLibelle(row.getString(TypeConduiteFermeeColumns.LIBELLE_TYPE_CONDUITE_FERMEE.toString()));
            typeConduite.setAbrege(row.getString(TypeConduiteFermeeColumns.ABREGE_TYPE_CONDUITE_FERMEE.toString()));
            if (row.getDate(TypeConduiteFermeeColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeConduite.setDateMaj(LocalDateTime.parse(row.getDate(TypeConduiteFermeeColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            typesEcoulement.put(row.getInt(String.valueOf(TypeConduiteFermeeColumns.ID_TYPE_CONDUITE_FERMEE.toString())), typeConduite);
        }
        couchDbConnector.executeBulk(typesEcoulement.values());
    }
    
}
