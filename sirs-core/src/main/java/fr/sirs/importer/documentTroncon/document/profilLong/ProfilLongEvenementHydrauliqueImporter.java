package fr.sirs.importer.documentTroncon.document.profilLong;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.core.model.ProfilLongEvenementHydraulique;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericImporter;
import fr.sirs.importer.evenementHydraulique.EvenementHydrauliqueImporter;
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
class ProfilLongEvenementHydrauliqueImporter extends GenericImporter {

    private Map<Integer, List<ProfilLongEvenementHydraulique>> evenementHydrauByProfilLongId = null;
    private EvenementHydrauliqueImporter evenementHydrauliqueImporter;
    
    private ProfilLongEvenementHydrauliqueImporter(final Database accessDatabase, final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    ProfilLongEvenementHydrauliqueImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final EvenementHydrauliqueImporter evenementHydrauliqueImporter) {
        this(accessDatabase, couchDbConnector);
        this.evenementHydrauliqueImporter = evenementHydrauliqueImporter;
    }
    
    public Map<Integer, List<ProfilLongEvenementHydraulique>> getEvenementHydrauliqueByProfilId() throws IOException, AccessDbImporterException{
        if(evenementHydrauByProfilLongId==null) compute();
        return evenementHydrauByProfilLongId;
    }
    
    private enum Columns {
        ID_PROFIL_EN_LONG,
        ID_EVENEMENT_HYDRAU,
        PR_DEBUT_SAISI,
        PR_FIN_SAISI,
        PREMIER_DEBORDEMENT_DEBIT_M3S,
//        PR_DEBUT_CALCULE,
//        PR_FIN_CALCULE,
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
        return DbImporter.TableName.PROFIL_EN_LONG_EVT_HYDRAU.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        evenementHydrauByProfilLongId = new HashMap<>();
        
        final Map<Integer, EvenementHydraulique> evenementHydrauliques = evenementHydrauliqueImporter.getEvenementHydraulique();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while(it.hasNext()){
            final Row row = it.next();
            final ProfilLongEvenementHydraulique profilLongEvenementHydraulique = new ProfilLongEvenementHydraulique();
            
            if(row.getInt(Columns.ID_EVENEMENT_HYDRAU.toString())!=null){
                profilLongEvenementHydraulique.setEvenementHydrauliqueId(evenementHydrauliques.get(row.getInt(Columns.ID_EVENEMENT_HYDRAU.toString())).getId());
            }
            
            if(row.getDouble(Columns.PR_DEBUT_SAISI.toString())!=null){
                profilLongEvenementHydraulique.setPrDebut(row.getDouble(Columns.PR_DEBUT_SAISI.toString()).floatValue());
            }
            
            if(row.getDouble(Columns.PR_FIN_SAISI.toString())!=null){
                profilLongEvenementHydraulique.setPrFin(row.getDouble(Columns.PR_FIN_SAISI.toString()).floatValue());
            }
            
            if(row.getDouble(Columns.PREMIER_DEBORDEMENT_DEBIT_M3S.toString())!=null){
                profilLongEvenementHydraulique.setDebitPremerDebordement(row.getDouble(Columns.PREMIER_DEBORDEMENT_DEBIT_M3S.toString()).floatValue());
            }
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                profilLongEvenementHydraulique.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            // Pas d'id car table de jointure : arbitrairement, on met l'id de l'événement hydrau
            profilLongEvenementHydraulique.setPseudoId(String.valueOf(row.getInt(Columns.ID_EVENEMENT_HYDRAU.toString())));
            
            List<ProfilLongEvenementHydraulique> listByLeve = evenementHydrauByProfilLongId.get(row.getInt(Columns.ID_PROFIL_EN_LONG.toString()));
            if (listByLeve == null) {
                listByLeve = new ArrayList<>();
            }
            listByLeve.add(profilLongEvenementHydraulique);
            
            
            evenementHydrauByProfilLongId.put(row.getInt(Columns.ID_PROFIL_EN_LONG.toString()), listByLeve);
        }
    }
}
