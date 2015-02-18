package fr.sirs.importer.evenementHydraulique;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.core.model.Meteo;
import fr.sirs.core.model.RefEvenementHydraulique;
import fr.sirs.core.model.RefFrequenceEvenementHydraulique;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.DocumentsUpdatable;
import fr.sirs.importer.GenericImporter;
import fr.sirs.importer.evenementHydraulique.meteo.MeteoImporter;
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
public class EvenementHydrauliqueImporter 
extends GenericImporter 
implements DocumentsUpdatable {

    private Map<Integer, EvenementHydraulique> evenements = null;
    private final TypeEvenementHydrauliqueImporter typeEvenementHydrauliqueImporter;
    private final TypeFrequenceEvenementHydrauliqueImporter typeFrequenceEvenementHydrauliqueImporter;
    private final MeteoImporter meteoImporter;
    
    public EvenementHydrauliqueImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
        typeEvenementHydrauliqueImporter = new TypeEvenementHydrauliqueImporter(
                accessDatabase, couchDbConnector);
        typeFrequenceEvenementHydrauliqueImporter = new TypeFrequenceEvenementHydrauliqueImporter(
                accessDatabase, couchDbConnector);
        meteoImporter = new MeteoImporter(accessDatabase, couchDbConnector);
    }

    @Override
    public void update() throws IOException, AccessDbImporterException {
        if(evenements==null) compute();
        couchDbConnector.executeBulk(evenements.values());
    }
    
    private enum Columns{
        ID_EVENEMENT_HYDRAU,
        NOM_EVENEMENT_HYDRAU,
        ID_TYPE_EVENEMENT_HYDRAU,
        ID_TYPE_FREQUENCE_EVENEMENT_HYDRAU,
        VITESSE_MOYENNE,
        DEBIT_MOYEN,
        DATE_DEBUT,
        DATE_FIN,
        COMMENTAIRE,
        NOM_MODELEUR_HYDRAU,
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
        return DbImporter.TableName.EVENEMENT_HYDRAU.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        evenements = new HashMap<>();
        
        final Map<Integer, RefEvenementHydraulique> types = typeEvenementHydrauliqueImporter.getTypeReferences();
        final Map<Integer, RefFrequenceEvenementHydraulique> frequences = typeFrequenceEvenementHydrauliqueImporter.getTypeReferences();
        final Map<Integer, List<Meteo>> meteos = meteoImporter.getMeteoByEvenementHydrauliqueId();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while(it.hasNext()){
            final Row row = it.next();
            final EvenementHydraulique evenement = new EvenementHydraulique();
            
            evenement.setLibelle(row.getString(Columns.NOM_EVENEMENT_HYDRAU.toString()));
            
            if(row.getDouble(Columns.VITESSE_MOYENNE.toString())!=null){
                evenement.setVitesse_moy(row.getDouble(Columns.VITESSE_MOYENNE.toString()).floatValue());
            }
            
            if(row.getDouble(Columns.DEBIT_MOYEN.toString())!=null){
                evenement.setDebit_moy(row.getDouble(Columns.DEBIT_MOYEN.toString()).floatValue());
            }
            
            evenement.setModeleur_hydraulique(row.getString(Columns.NOM_MODELEUR_HYDRAU.toString()));
            
            evenement.setCommentaire(row.getString(Columns.COMMENTAIRE.toString()));
            
            if (row.getDate(Columns.DATE_DEBUT.toString()) != null) {
                evenement.setDate_debut(LocalDateTime.parse(row.getDate(Columns.DATE_DEBUT.toString()).toString(), dateTimeFormatter));
            }
            if (row.getDate(Columns.DATE_FIN.toString()) != null) {
                evenement.setDate_fin(LocalDateTime.parse(row.getDate(Columns.DATE_FIN.toString()).toString(), dateTimeFormatter));
            }
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                evenement.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            if(row.getInt(Columns.ID_TYPE_EVENEMENT_HYDRAU.toString())!=null){
                evenement.setTypeEvenementHydrauliqueId(types.get(row.getInt(Columns.ID_TYPE_EVENEMENT_HYDRAU.toString())).getId());
            }
            
            if(row.getInt(Columns.ID_TYPE_FREQUENCE_EVENEMENT_HYDRAU.toString())!=null){
                evenement.setFrequenceEvenementHydrauliqueId(frequences.get(row.getInt(Columns.ID_TYPE_FREQUENCE_EVENEMENT_HYDRAU.toString())).getId());
            }
            
            final List<Meteo> meteoEvt = meteos.get(row.getInt(Columns.ID_EVENEMENT_HYDRAU.toString()));
            if(meteoEvt!=null){
                evenement.setEvenementMeteoIds(meteoEvt);
            }
            evenement.setPseudoId(String.valueOf(row.getInt(Columns.ID_EVENEMENT_HYDRAU.toString())));
            
            evenements.put(row.getInt(Columns.ID_EVENEMENT_HYDRAU.toString()), evenement);
        }
        couchDbConnector.executeBulk(evenements.values());
    }
    
    public Map<Integer, EvenementHydraulique> getEvenementHydraulique() throws IOException, AccessDbImporterException{
        if(evenements==null) compute();
        return evenements;
    }
    
}
