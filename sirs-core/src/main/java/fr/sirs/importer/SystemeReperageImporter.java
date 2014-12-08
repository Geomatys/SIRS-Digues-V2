package fr.sirs.importer;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.SystemeReperage;
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
public class SystemeReperageImporter extends GenericImporter {

    private Map<Integer, SystemeReperage> systemesReperage = null;
    private Map<Integer, List<SystemeReperage>> systemesReperageByTronconId = null;

    SystemeReperageImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    private enum Columns {
        ID_SYSTEME_REP, 
        ID_TRONCON_GESTION, 
        LIBELLE_SYSTEME_REP,
        COMMENTAIRE_SYSTEME_REP, 
        DATE_DERNIERE_MAJ
    };
    
    /**
     * 
     * @return A map containing the SystemeRepLineaire instances references by
     * the tronconDigue internal database identifier.
     * @throws IOException 
     */
    public Map<Integer, List<SystemeReperage>> getSystemeRepLineaireByTronconId() throws IOException{
        if(systemesReperageByTronconId==null) compute();
        return systemesReperageByTronconId;
    }

    /**
     * 
     * @return A map containing the SystemeRepLineaire instances references by
     * their internal database identifier.
     * @throws IOException 
     */
    public Map<Integer, SystemeReperage> getSystemeRepLineaire() throws IOException {
        if(systemesReperage==null) compute();
        return systemesReperage;
    }

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
        return DbImporter.TableName.SYSTEME_REP_LINEAIRE.toString();
    }
    
    @Override
    protected void compute() throws IOException{
        systemesReperage = new HashMap<>();
        systemesReperageByTronconId = new HashMap<>();

        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        final List<SystemeReperage> systemes = new ArrayList<>();
        
        while (it.hasNext()) {
            final Row row = it.next();
            final SystemeReperage systemeReperage = new SystemeReperage();
            
            systemeReperage.setLibelle(row.getString(Columns.LIBELLE_SYSTEME_REP.toString()));
            systemeReperage.setCommentaire(row.getString(Columns.COMMENTAIRE_SYSTEME_REP.toString()));
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                systemeReperage.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            systemesReperage.put(row.getInt(Columns.ID_SYSTEME_REP.toString()), systemeReperage);
            
            // Set the list ByTronconId
            List<SystemeReperage> listByTronconId = systemesReperageByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            if (listByTronconId == null) {
                listByTronconId = new ArrayList<>();
//                systemesReperageByTronconId.put(row.getInt(SystemeRepLineaireColumns.ID_TRONCON_GESTION.toString()), listByTronconId);
            }
            listByTronconId.add(systemeReperage);
            systemesReperageByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
            
            // Register the systemeReperage to retrieve a CouchDb ID.
            systemes.add(systemeReperage);
        }
        couchDbConnector.executeBulk(systemes);
    }
}
