package fr.sirs.importer.documentTroncon.document.profilTravers;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.core.model.ParametreHydrauliqueProfilTravers;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericImporter;
import fr.sirs.importer.evenementHydraulique.EvenementHydrauliqueImporter;
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
class ProfilTraversEvenementHydrauliqueImporter extends GenericImporter {

    private Map<Integer, List<ParametreHydrauliqueProfilTravers>> evenementHydrauByLeveId = null;
    private EvenementHydrauliqueImporter evenementHydrauliqueImporter;
    
    private ProfilTraversEvenementHydrauliqueImporter(final Database accessDatabase, final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    ProfilTraversEvenementHydrauliqueImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final EvenementHydrauliqueImporter evenementHydrauliqueImporter) {
        this(accessDatabase, couchDbConnector);
        this.evenementHydrauliqueImporter = evenementHydrauliqueImporter;
    }
    
    public Map<Integer, List<ParametreHydrauliqueProfilTravers>> getEvenementHydrauliqueByLeveId() throws IOException, AccessDbImporterException{
        if(evenementHydrauByLeveId==null) compute();
        return evenementHydrauByLeveId;
    }
    
    private enum Columns {
        ID_PROFIL_EN_TRAVERS_LEVE,
        ID_EVENEMENT_HYDRAU,
        DEBIT_DE_POINTE_M3S,
        VITESSE_DE_POINTE_MS,
        COTE_EAU_NGF,
        COMMENTAIRE,
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
        return PROFIL_EN_TRAVERS_EVT_HYDRAU.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        evenementHydrauByLeveId = new HashMap<>();
        
        final Map<Integer, EvenementHydraulique> evenementHydrauliques = evenementHydrauliqueImporter.getEvenementHydraulique();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while(it.hasNext()){
            final Row row = it.next();
            final ParametreHydrauliqueProfilTravers profilTraversEvenementHydraulique = createAnonymValidElement(ParametreHydrauliqueProfilTravers.class);
            
            if(row.getInt(Columns.ID_EVENEMENT_HYDRAU.toString())!=null){
                profilTraversEvenementHydraulique.setEvenementHydroliqueId(evenementHydrauliques.get(row.getInt(Columns.ID_EVENEMENT_HYDRAU.toString())).getId());
            }
            
            if(row.getDouble(Columns.DEBIT_DE_POINTE_M3S.toString())!=null){
                profilTraversEvenementHydraulique.setDebitPointe(row.getDouble(Columns.DEBIT_DE_POINTE_M3S.toString()).floatValue());
            }
            
            if(row.getDouble(Columns.VITESSE_DE_POINTE_MS.toString())!=null){
                profilTraversEvenementHydraulique.setVitessePointe(row.getDouble(Columns.VITESSE_DE_POINTE_MS.toString()).floatValue());
            }
            
            if(row.getDouble(Columns.COTE_EAU_NGF.toString())!=null){
                profilTraversEvenementHydraulique.setCoteEau(row.getDouble(Columns.COTE_EAU_NGF.toString()).floatValue());
            }
            
            profilTraversEvenementHydraulique.setCommentaire(row.getString(Columns.COMMENTAIRE.toString()));
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                profilTraversEvenementHydraulique.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            
            // Table de jointure : on prend l'id de l'événement hydraulique comme pseudo id en l'absence d'identifiant véritable
            profilTraversEvenementHydraulique.setDesignation(String.valueOf(row.getInt(Columns.ID_EVENEMENT_HYDRAU.toString())));
            
            List<ParametreHydrauliqueProfilTravers> listByLeve = evenementHydrauByLeveId.get(row.getInt(Columns.ID_PROFIL_EN_TRAVERS_LEVE.toString()));
            if (listByLeve == null) {
                listByLeve = new ArrayList<>();
            }
            listByLeve.add(profilTraversEvenementHydraulique);
            evenementHydrauByLeveId.put(row.getInt(Columns.ID_PROFIL_EN_TRAVERS_LEVE.toString()), listByLeve);
        }
    }
}
