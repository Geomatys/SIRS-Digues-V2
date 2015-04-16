package fr.sirs.importer.objet.desordre;

import fr.sirs.core.SirsCore;
import java.util.logging.Level;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.Observation;
import fr.sirs.core.model.RefUrgence;
import fr.sirs.importer.GenericImporter;
import fr.sirs.importer.IntervenantImporter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
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
public class DesordreObservationImporter extends GenericImporter {
    
    private Map<Integer, Observation> observations = null;
    private Map<Integer, List<Observation>> observationsByDesordreId = null;
    private final TypeUrgenceImporter typeUrgenceImporter;
    private final IntervenantImporter intervenantImporter;

    public DesordreObservationImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final IntervenantImporter intervenantImporter) {
        super(accessDatabase, couchDbConnector);
        this.typeUrgenceImporter = new TypeUrgenceImporter(accessDatabase, 
                couchDbConnector);
        this.intervenantImporter = intervenantImporter;
    }

    private enum Columns {
        ID_OBSERVATION,
        ID_DESORDRE,
        ID_TYPE_URGENCE,
        ID_INTERV_OBSERVATEUR,
        DATE_OBSERVATION_DESORDRE,
        SUITE_A_APPORTER,
        EVOLUTIONS,
        NBR_DESORDRE,
        DATE_DERNIERE_MAJ
    };

    @Override
    public String getTableName() {
        return DbImporter.TableName.DESORDRE_OBSERVATION.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        this.observations = new HashMap<>();
        this.observationsByDesordreId = new HashMap<>();
        
        final Map<Integer, RefUrgence> typesUrgence = typeUrgenceImporter.getTypeReferences();
        final Map<Integer, Contact> contacts = intervenantImporter.getIntervenants();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final Observation observation = new Observation();
            
            if(row.getInt(Columns.ID_TYPE_URGENCE.toString())!=null){
                observation.setUrgenceId(typesUrgence.get(row.getInt(Columns.ID_TYPE_URGENCE.toString())).getId());
            }
            
            if(row.getInt(Columns.ID_INTERV_OBSERVATEUR.toString())!=null){
                observation.setObservateurId(contacts.get(row.getInt(Columns.ID_INTERV_OBSERVATEUR.toString())).getId());
            }
            
            if (row.getDate(Columns.DATE_OBSERVATION_DESORDRE.toString()) != null) {
                try{
                    observation.setDate(LocalDateTime.parse(row.getDate(Columns.DATE_OBSERVATION_DESORDRE.toString()).toString(), dateTimeFormatter));
                }
                catch(DateTimeParseException e){
                    SirsCore.LOGGER.log(Level.FINE, e.getMessage());
                }
            }
            
            if (row.getString(Columns.SUITE_A_APPORTER.toString()) != null) {
                observation.setSuite(row.getString(Columns.SUITE_A_APPORTER.toString()));
            }
            
            if (row.getString(Columns.EVOLUTIONS.toString()) != null) {
                observation.setEvolution(row.getString(Columns.EVOLUTIONS.toString()));
            }
            
            if (row.getInt(Columns.NBR_DESORDRE.toString()) != null) {
                observation.setNombreDesordres(row.getInt(Columns.NBR_DESORDRE.toString()));
            }
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                observation.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
        
            observation.setDesignation(String.valueOf(row.getInt(Columns.ID_OBSERVATION.toString())));
            observation.setValid(true);
            
            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            observations.put(row.getInt(Columns.ID_OBSERVATION.toString()), observation);

            // Set the list ByTronconId
            List<Observation> listByTronconId = observationsByDesordreId.get(row.getInt(Columns.ID_DESORDRE.toString()));
            if (listByTronconId == null) {
                listByTronconId = new ArrayList<>();
            }
            listByTronconId.add(observation);
            observationsByDesordreId.put(row.getInt(Columns.ID_DESORDRE.toString()), listByTronconId);
        }
    }
    
    public Map<Integer, List<Observation>> getObservationsByDesordreId() 
            throws IOException, AccessDbImporterException{
        if(observationsByDesordreId==null) compute();
        return observationsByDesordreId;
    }
    
    public Map<Integer, Observation> getObservations() 
            throws IOException, AccessDbImporterException{
        if(observations==null) compute();
        return observations;
    }

    @Override
    protected List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (Columns c : Columns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }
}
