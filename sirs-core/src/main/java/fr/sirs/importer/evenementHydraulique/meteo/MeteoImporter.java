package fr.sirs.importer.evenementHydraulique.meteo;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.Meteo;
import fr.sirs.core.model.RefOrientationVent;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericImporter;
import java.io.IOException;
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
public class MeteoImporter extends DocumentImporter {

    private Map<Integer, List<Meteo>> meteos = null;
    private final TypeOrientationVentImporter typeOrientationVentImporter;
    
    public MeteoImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
        typeOrientationVentImporter = new TypeOrientationVentImporter(
                accessDatabase, couchDbConnector);
    }
    
    private enum Columns{
        ID_EVENEMENT_HYDRAU,
        DATE_DEBUT_METEO,
        DATE_FIN_METEO,
        VITESSE_VENT,
        ID_TYPE_ORIENTATION_VENT,
        PRESSION_ATMOSPHERIQUE,
        DATE_DERNIERE_MAJ
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
        return METEO.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        meteos = new HashMap<>();
        
        final Map<Integer, RefOrientationVent> typesOrientationVent = typeOrientationVentImporter.getTypeReferences();
        
        final Iterator<Row> it = context.inputDb.getTable(getTableName()).iterator();
        while(it.hasNext()){
            final Row row = it.next();
            final Meteo meteo = createAnonymValidElement(Meteo.class);
            
            if (row.getDate(Columns.DATE_DEBUT_METEO.toString()) != null) {
                meteo.setDateDebut(DbImporter.parseLocalDate(row.getDate(Columns.DATE_DEBUT_METEO.toString()), dateTimeFormatter));
            }
            
            if (row.getDate(Columns.DATE_FIN_METEO.toString()) != null) {
                meteo.setDateFin(DbImporter.parseLocalDate(row.getDate(Columns.DATE_FIN_METEO.toString()), dateTimeFormatter));
            }
            
            if(row.getDouble(Columns.VITESSE_VENT.toString())!=null){
                meteo.setVitesseVent(row.getDouble(Columns.VITESSE_VENT.toString()).floatValue());
            }
            
            if(row.getInt(Columns.ID_TYPE_ORIENTATION_VENT.toString())!=null){
                meteo.setTypeOrientationVentId(typesOrientationVent.get(row.getInt(Columns.ID_TYPE_ORIENTATION_VENT.toString())).getId());
            }
            
            if(row.getDouble(Columns.PRESSION_ATMOSPHERIQUE.toString())!=null){
                meteo.setPression(row.getDouble(Columns.PRESSION_ATMOSPHERIQUE.toString()).floatValue());
            }
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                meteo.setDateMaj(DbImporter.parseLocalDate(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            
            // La météo n'ayant pas d'ID, on affecte l'ID de l'événement hydraulique comme pseudo id.
            meteo.setDesignation(String.valueOf(row.getInt(Columns.ID_EVENEMENT_HYDRAU.toString())));
            
            // Set the list ByTronconId
            List<Meteo> listByTronconId = meteos.get(row.getInt(Columns.ID_EVENEMENT_HYDRAU.toString()));
            if (listByTronconId == null) {
                listByTronconId = new ArrayList<>();
                meteos.put(row.getInt(Columns.ID_EVENEMENT_HYDRAU.toString()), listByTronconId);
            }
            listByTronconId.add(meteo);
        }
        context.outputDb.executeBulk(meteos.values());
    }
    
    public Map<Integer, List<Meteo>> getMeteoByEvenementHydrauliqueId() throws IOException, AccessDbImporterException{
        if(meteos==null) compute();
        return meteos;
    }
    
}
