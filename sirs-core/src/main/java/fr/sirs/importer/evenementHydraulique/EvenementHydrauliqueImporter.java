package fr.sirs.importer.evenementHydraulique;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.component.EvenementHydrauliqueRepository;
import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.core.model.RefEvenementHydraulique;
import fr.sirs.core.model.RefFrequenceEvenementHydraulique;
import fr.sirs.importer.AccessDbImporterException;
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
public class EvenementHydrauliqueImporter extends GenericImporter {

    private Map<Integer, EvenementHydraulique> evenements = null;
    private final TypeEvenementHydrauliqueImporter typeEvenementHydrauliqueImporter;
    private final TypeFrequenceEvenementHydrauliqueImporter typeFrequenceEvenementHydrauliqueImporter;
    
    public EvenementHydrauliqueImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final TypeEvenementHydrauliqueImporter typeEvenementHydrauliqueImporter,
            final TypeFrequenceEvenementHydrauliqueImporter typeFrequenceEvenementHydrauliqueImporter) {
        super(accessDatabase, couchDbConnector);
        this.typeEvenementHydrauliqueImporter = typeEvenementHydrauliqueImporter;
        this.typeFrequenceEvenementHydrauliqueImporter = typeFrequenceEvenementHydrauliqueImporter;
    }
    
    private enum EvenementHydrauliqueColumns{
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
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (EvenementHydrauliqueColumns c : EvenementHydrauliqueColumns.values()) {
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
        
        final Map<Integer, RefEvenementHydraulique> types = typeEvenementHydrauliqueImporter.getTypeEvenementHydraulique();
        final Map<Integer, RefFrequenceEvenementHydraulique> frequences = typeFrequenceEvenementHydrauliqueImporter.getTypeFrequenceEvenementHydraulique();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while(it.hasNext()){
            final Row row = it.next();
            final EvenementHydraulique evenement = new EvenementHydraulique();
            
            evenement.setNom(row.getString(EvenementHydrauliqueColumns.NOM_EVENEMENT_HYDRAU.toString()));
            if(row.getDouble(EvenementHydrauliqueColumns.VITESSE_MOYENNE.toString())!=null){
                evenement.setVitesse_moy(row.getDouble(EvenementHydrauliqueColumns.VITESSE_MOYENNE.toString()).floatValue());
            }
            if(row.getDouble(EvenementHydrauliqueColumns.DEBIT_MOYEN.toString())!=null){
                evenement.setDebit_moy(row.getDouble(EvenementHydrauliqueColumns.DEBIT_MOYEN.toString()).floatValue());
            }
            evenement.setModeleur_hydraulique(row.getString(EvenementHydrauliqueColumns.NOM_MODELEUR_HYDRAU.toString()));
            evenement.setCommentaire(row.getString(EvenementHydrauliqueColumns.COMMENTAIRE.toString()));
            
            if (row.getDate(EvenementHydrauliqueColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                evenement.setDate_debut(LocalDateTime.parse(row.getDate(EvenementHydrauliqueColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            if (row.getDate(EvenementHydrauliqueColumns.DATE_DEBUT.toString()) != null) {
                evenement.setDate_fin(LocalDateTime.parse(row.getDate(EvenementHydrauliqueColumns.DATE_DEBUT.toString()).toString(), dateTimeFormatter));
            }
            if (row.getDate(EvenementHydrauliqueColumns.DATE_FIN.toString()) != null) {
                evenement.setDateMaj(LocalDateTime.parse(row.getDate(EvenementHydrauliqueColumns.DATE_FIN.toString()).toString(), dateTimeFormatter));
            }
            
            if(row.getInt(EvenementHydrauliqueColumns.ID_TYPE_EVENEMENT_HYDRAU.toString())!=null){
                evenement.setType_evenement(types.get(row.getInt(EvenementHydrauliqueColumns.ID_TYPE_EVENEMENT_HYDRAU.toString())).getId());
            }
            if(row.getInt(EvenementHydrauliqueColumns.ID_TYPE_FREQUENCE_EVENEMENT_HYDRAU.toString())!=null){
                evenement.setFrequence(frequences.get(row.getInt(EvenementHydrauliqueColumns.ID_TYPE_FREQUENCE_EVENEMENT_HYDRAU.toString())).getId());
            }
            evenements.put(row.getInt(EvenementHydrauliqueColumns.ID_EVENEMENT_HYDRAU.toString()), evenement);
        }
        couchDbConnector.executeBulk(evenements.values());
    }
    
    public Map<Integer, EvenementHydraulique> getEvenementHydraulique() throws IOException, AccessDbImporterException{
        if(evenements==null) compute();
        return evenements;
    }
    
}
