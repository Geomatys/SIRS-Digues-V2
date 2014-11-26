package fr.sirs.importer.objet.reseau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.ReseauReseau;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.cleanNullString;
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
class ReseauConduiteFermeeImporter extends GenericImporter {

    private Map<Integer, List<ReseauReseau>> reseauConduites = null;
    private final ConduiteFermeeImporter conduiteFermeeImporter;
    
    ReseauConduiteFermeeImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final ConduiteFermeeImporter conduiteFermeeImporter) {
        super(accessDatabase, couchDbConnector);
        this.conduiteFermeeImporter = conduiteFermeeImporter;
    }

    private enum ElementReseauConduiteFermeeColumns {
        ID_ELEMENT_RESEAU,
        ID_ELEMENT_RESEAU_CONDUITE_FERMEE,
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the database ReseauReseau referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, List<ReseauReseau>> getReseauConduiteFermeByReseauId() throws IOException, AccessDbImporterException {
        if(reseauConduites == null) compute();
        return reseauConduites;
    }
    
    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (ElementReseauConduiteFermeeColumns c : ElementReseauConduiteFermeeColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.ELEMENT_RESEAU_CONDUITE_FERMEE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        reseauConduites = new HashMap<>();
        
        final Map<Integer, ReseauHydrauliqueFerme> conduitesFermees = conduiteFermeeImporter.getStructures();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final ReseauReseau reseauConduite = new ReseauReseau();
            
            if(conduitesFermees.get(row.getInt(ElementReseauConduiteFermeeColumns.ID_ELEMENT_RESEAU_CONDUITE_FERMEE.toString()))!=null){
                reseauConduite.setReseauId(cleanNullString(conduitesFermees.get(row.getInt(ElementReseauConduiteFermeeColumns.ID_ELEMENT_RESEAU_CONDUITE_FERMEE.toString())).getId()));
            }
            
            if (row.getDate(ElementReseauConduiteFermeeColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                reseauConduite.setDateMaj(LocalDateTime.parse(row.getDate(ElementReseauConduiteFermeeColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            // Set the list ByTronconId
            List<ReseauReseau> listByReseauId = reseauConduites.get(row.getInt(ElementReseauConduiteFermeeColumns.ID_ELEMENT_RESEAU.toString()));
            if (listByReseauId == null) {
                listByReseauId = new ArrayList<>();
                reseauConduites.put(row.getInt(ElementReseauConduiteFermeeColumns.ID_ELEMENT_RESEAU.toString()), listByReseauId);
            }
            listByReseauId.add(reseauConduite);
        }
    }
    
}
