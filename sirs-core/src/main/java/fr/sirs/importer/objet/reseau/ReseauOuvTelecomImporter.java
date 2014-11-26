package fr.sirs.importer.objet.reseau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.ReaseauTelecomOuvrageTelecom;
import fr.sirs.core.model.ReseauConduiteFermee;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.ReseauTelecomEnergie;
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
class ReseauOuvTelecomImporter extends GenericImporter {

    private Map<Integer, List<ReaseauTelecomOuvrageTelecom>> reseauOuvrages = null;
    private final ResTelecomImporter resTelecomImporter;
    
    ReseauOuvTelecomImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final ResTelecomImporter resTelecomImporter) {
        super(accessDatabase, couchDbConnector);
        this.resTelecomImporter = resTelecomImporter;
    }

    private enum ReseauOuvrageTelecomColumns {
        ID_ELEMENT_RESEAU,
        ID_ELEMENT_RESEAU_OUVRAGE_TEL_NRJ,
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the database ReaseauTelecomOuvrageTelecom referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, List<ReaseauTelecomOuvrageTelecom>> getReseauOuvrageTelecom() throws IOException, AccessDbImporterException {
        if(reseauOuvrages == null) compute();
        return reseauOuvrages;
    }
    
    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (ReseauOuvrageTelecomColumns c : ReseauOuvrageTelecomColumns.values()) {
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
        reseauOuvrages = new HashMap<>();
        
        final Map<Integer, ReseauTelecomEnergie> reseauxTel = resTelecomImporter.getStructures();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final ReaseauTelecomOuvrageTelecom reseauConduite = new ReaseauTelecomOuvrageTelecom();
            
            if(reseauxTel.get(row.getInt(ReseauOuvrageTelecomColumns.ID_ELEMENT_RESEAU_OUVRAGE_TEL_NRJ.toString()))!=null){
                reseauConduite.setReseauId(cleanNullString(reseauxTel.get(row.getInt(ReseauOuvrageTelecomColumns.ID_ELEMENT_RESEAU_OUVRAGE_TEL_NRJ.toString())).getId()));
            }
            
            if (row.getDate(ReseauOuvrageTelecomColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                reseauConduite.setDateMaj(LocalDateTime.parse(row.getDate(ReseauOuvrageTelecomColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            // Set the list ByTronconId
            List<ReaseauTelecomOuvrageTelecom> listByReseauId = reseauOuvrages.get(row.getInt(ReseauOuvrageTelecomColumns.ID_ELEMENT_RESEAU.toString()));
            if (listByReseauId == null) {
                listByReseauId = new ArrayList<>();
                reseauOuvrages.put(row.getInt(ReseauOuvrageTelecomColumns.ID_ELEMENT_RESEAU.toString()), listByReseauId);
            }
            listByReseauId.add(reseauConduite);
        }
    }
    
}
