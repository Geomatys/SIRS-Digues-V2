package fr.sirs.importer.theme.document.related.profilTravers;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.core.model.ProfilTraversEvenementHydraulique;
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
public class ProfilTraversEvenementHydrauliqueImporter extends GenericImporter {

    private Map<Integer, List<ProfilTraversEvenementHydraulique>> evenementHydrauByLeveId = null;
    private EvenementHydrauliqueImporter evenementHydrauliqueImporter;
    
    private ProfilTraversEvenementHydrauliqueImporter(final Database accessDatabase, final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    public ProfilTraversEvenementHydrauliqueImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final EvenementHydrauliqueImporter evenementHydrauliqueImporter) {
        this(accessDatabase, couchDbConnector);
        this.evenementHydrauliqueImporter = evenementHydrauliqueImporter;
    }
    
    public Map<Integer, List<ProfilTraversEvenementHydraulique>> getEvenementHydrauliqueByLeveId() throws IOException, AccessDbImporterException{
        if(evenementHydrauByLeveId==null) compute();
        return evenementHydrauByLeveId;
    }
    
    private enum ProfilTraversEvenementHydrauliqueColumns {
        ID_PROFIL_EN_TRAVERS_LEVE,
        ID_EVENEMENT_HYDRAU,
        DEBIT_DE_POINTE_M3S,
        VITESSE_DE_POINTE_MS,
        COTE_EAU_NGF,
        COMMENTAIRE,
        DATE_DERNIERE_MAJ
    }
    
    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (ProfilTraversEvenementHydrauliqueColumns c : ProfilTraversEvenementHydrauliqueColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.PROFIL_EN_TRAVERS_EVT_HYDRAU.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        evenementHydrauByLeveId = new HashMap<>();
        
        final Map<Integer, EvenementHydraulique> evenementHydrauliques = evenementHydrauliqueImporter.getEvenementHydraulique();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while(it.hasNext()){
            final Row row = it.next();
            final ProfilTraversEvenementHydraulique profilTraversEvenementHydraulique = new ProfilTraversEvenementHydraulique();
            
            if(row.getInt(ProfilTraversEvenementHydrauliqueColumns.ID_EVENEMENT_HYDRAU.toString())!=null){
                profilTraversEvenementHydraulique.setEvenementHydroliqueId(evenementHydrauliques.get(row.getInt(ProfilTraversEvenementHydrauliqueColumns.ID_EVENEMENT_HYDRAU.toString())).getId());
            }
            
            if(row.getDouble(ProfilTraversEvenementHydrauliqueColumns.DEBIT_DE_POINTE_M3S.toString())!=null){
                profilTraversEvenementHydraulique.setDebitPointe(row.getDouble(ProfilTraversEvenementHydrauliqueColumns.DEBIT_DE_POINTE_M3S.toString()).floatValue());
            }
            
            if(row.getDouble(ProfilTraversEvenementHydrauliqueColumns.VITESSE_DE_POINTE_MS.toString())!=null){
                profilTraversEvenementHydraulique.setVitessePointe(row.getDouble(ProfilTraversEvenementHydrauliqueColumns.VITESSE_DE_POINTE_MS.toString()).floatValue());
            }
            
            if(row.getDouble(ProfilTraversEvenementHydrauliqueColumns.COTE_EAU_NGF.toString())!=null){
                profilTraversEvenementHydraulique.setCoteEau(row.getDouble(ProfilTraversEvenementHydrauliqueColumns.COTE_EAU_NGF.toString()).floatValue());
            }
            
            profilTraversEvenementHydraulique.setCommentaire(row.getString(ProfilTraversEvenementHydrauliqueColumns.COMMENTAIRE.toString()));
            
            if (row.getDate(ProfilTraversEvenementHydrauliqueColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                profilTraversEvenementHydraulique.setDateMaj(LocalDateTime.parse(row.getDate(ProfilTraversEvenementHydrauliqueColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            List<ProfilTraversEvenementHydraulique> listByLeve = evenementHydrauByLeveId.get(row.getInt(ProfilTraversEvenementHydrauliqueColumns.ID_PROFIL_EN_TRAVERS_LEVE.toString()));
            if (listByLeve == null) {
                listByLeve = new ArrayList<>();
            }
            listByLeve.add(profilTraversEvenementHydraulique);
            evenementHydrauByLeveId.put(row.getInt(ProfilTraversEvenementHydrauliqueColumns.ID_PROFIL_EN_TRAVERS_LEVE.toString()), listByLeve);
        }
    }
}
