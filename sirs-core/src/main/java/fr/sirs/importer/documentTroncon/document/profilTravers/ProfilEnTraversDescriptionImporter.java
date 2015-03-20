package fr.sirs.importer.documentTroncon.document.profilTravers;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.PointLeve;
import fr.sirs.core.model.LeveProfilTravers;
import fr.sirs.core.model.Organisme;
import fr.sirs.core.model.ProfilTraversEvenementHydraulique;
import fr.sirs.core.model.RefOrigineProfilTravers;
import fr.sirs.core.model.RefSystemeReleveProfil;
import fr.sirs.core.model.RefTypeProfilTravers;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericImporter;
import fr.sirs.importer.OrganismeImporter;
import fr.sirs.importer.evenementHydraulique.EvenementHydrauliqueImporter;
import fr.sirs.importer.documentTroncon.document.TypeSystemeReleveProfilImporter;
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
public class ProfilEnTraversDescriptionImporter extends GenericImporter {

    private Map<Integer, LeveProfilTravers> leves = null;
    private Map<Integer, List<LeveProfilTravers>> levesByProfil = null;
    private Map<Integer, List<Integer>> levesIdsByProfil = null;
    
    private TypeSystemeReleveProfilImporter typeSystemeReleveProfilImporter;
    private TypeProfilTraversImporter typeProfilTraversImporter;
    private TypeOrigineProfilTraversImporter typeOrigineProfilTraversImporter;
    private OrganismeImporter organismeImporter;
    private ProfilTraversEvenementHydrauliqueImporter profilTraversEvenementHydrauliqueImporter;
    private ProfilTraversPointXYZImporter profilTraversPointXYZImporter;
    
    private ProfilEnTraversDescriptionImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    public ProfilEnTraversDescriptionImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final TypeSystemeReleveProfilImporter typeSystemeReleveProfilImporter,
            final OrganismeImporter organismeImporter,
            final EvenementHydrauliqueImporter evenementHydrauliqueImporter){
        this(accessDatabase, couchDbConnector);
        this.typeSystemeReleveProfilImporter = typeSystemeReleveProfilImporter;
        this.organismeImporter = organismeImporter;
        this.profilTraversEvenementHydrauliqueImporter = new ProfilTraversEvenementHydrauliqueImporter(
                accessDatabase, couchDbConnector, evenementHydrauliqueImporter);
        typeProfilTraversImporter = new TypeProfilTraversImporter(accessDatabase, couchDbConnector);
        typeOrigineProfilTraversImporter = new TypeOrigineProfilTraversImporter(accessDatabase, couchDbConnector);
        profilTraversPointXYZImporter = new ProfilTraversPointXYZImporter(accessDatabase, couchDbConnector);
    }
    
    public Map<Integer, LeveProfilTravers> getLeveProfilTravers() throws IOException, AccessDbImporterException{
        if(leves==null) compute();
        return leves;
    }
    
    public Map<Integer, List<LeveProfilTravers>> getLeveProfilTraversByProfilId() throws IOException, AccessDbImporterException{
        if(levesByProfil==null) compute();
        return levesByProfil;
    }
    
    public Map<Integer, List<Integer>> getLeveProfilTraversIdsByProfilId() throws IOException, AccessDbImporterException{
        if(levesIdsByProfil==null) compute();
        return levesIdsByProfil;
    }
    
    private enum Columns {
        ID_PROFIL_EN_TRAVERS_LEVE,
        ID_PROFIL_EN_TRAVERS,
        DATE_LEVE,
        ID_ORG_CREATEUR,
        ID_TYPE_SYSTEME_RELEVE_PROFIL,
        REFERENCE_PAPIER,
        REFERENCE_NUMERIQUE,
        REFERENCE_CALQUE,
        ID_TYPE_PROFIL_EN_TRAVERS,
        ID_TYPE_ORIGINE_PROFIL_EN_TRAVERS,
//        ID_DOC_RAPPORT_ETUDES,
        COMMENTAIRE,
        NOM_FICHIER_PLAN_ENSEMBLE,
        NOM_FICHIER_COUPE_IMAGE,
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
        return DbImporter.TableName.PROFIL_EN_TRAVERS_DESCRIPTION.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        leves = new HashMap<>();
        levesByProfil = new HashMap<>();
        levesIdsByProfil = new HashMap<>();
        
        final Map<Integer, Organisme> organismes = organismeImporter.getOrganismes();
        final Map<Integer, RefSystemeReleveProfil> systemesReleve = typeSystemeReleveProfilImporter.getTypeReferences();
        final Map<Integer, RefTypeProfilTravers> typesProfil = typeProfilTraversImporter.getTypeReferences();
        final Map<Integer, RefOrigineProfilTravers> typesOrigineProfil = typeOrigineProfilTraversImporter.getTypeReferences();
        final Map<Integer, List<ProfilTraversEvenementHydraulique>> evenementsHydrauliques = profilTraversEvenementHydrauliqueImporter.getEvenementHydrauliqueByLeveId();
        final Map<Integer, List<PointLeve>> pointsByLeve = profilTraversPointXYZImporter.getLeveePointByLeveId();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while(it.hasNext()){
            final Row row = it.next();
            final LeveProfilTravers leve = new LeveProfilTravers();
            
            if (row.getDate(Columns.DATE_LEVE.toString()) != null) {
                leve.setDateLevee(LocalDateTime.parse(row.getDate(Columns.DATE_LEVE.toString()).toString(), dateTimeFormatter));
            }
            
            final Organisme organisme = organismes.get(row.getInt(Columns.ID_ORG_CREATEUR.toString()));
            if(organisme!=null){
                leve.setOrganismeCreateurId(organisme.getId());
            }
            
            if(row.getInt(Columns.ID_TYPE_SYSTEME_RELEVE_PROFIL.toString())!=null){
                leve.setTypeSystemesReleveId(systemesReleve.get(row.getInt(Columns.ID_TYPE_SYSTEME_RELEVE_PROFIL.toString())).getId());
            }
            
            leve.setReferencePapier(row.getString(Columns.REFERENCE_PAPIER.toString()));
            
            leve.setChemin(row.getString(Columns.REFERENCE_NUMERIQUE.toString()));
            
            leve.setReferenceCalque(row.getString(Columns.REFERENCE_CALQUE.toString()));
            
            leve.setNomFichierCoupeImage(row.getString(Columns.NOM_FICHIER_COUPE_IMAGE.toString()));
            
            leve.setNomFichierPlanEnsemble(row.getString(Columns.NOM_FICHIER_PLAN_ENSEMBLE.toString()));
            
            if(row.getInt(Columns.ID_TYPE_PROFIL_EN_TRAVERS.toString())!=null){
                leve.setTypeProfilId(typesProfil.get(row.getInt(Columns.ID_TYPE_PROFIL_EN_TRAVERS.toString())).getId());
            }
            
            if(row.getInt(Columns.ID_TYPE_ORIGINE_PROFIL_EN_TRAVERS.toString())!=null){
                leve.setOriginesProfil(typesOrigineProfil.get(row.getInt(Columns.ID_TYPE_ORIGINE_PROFIL_EN_TRAVERS.toString())).getId());
            }
            
            leve.setCommentaire(row.getString(Columns.COMMENTAIRE.toString()));
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                leve.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            if(pointsByLeve.get(row.getInt(Columns.ID_PROFIL_EN_TRAVERS_LEVE.toString()))!=null){
                leve.setPointsLeve(pointsByLeve.get(row.getInt(Columns.ID_PROFIL_EN_TRAVERS_LEVE.toString())));
            }
            
            if(evenementsHydrauliques.get(row.getInt(Columns.ID_PROFIL_EN_TRAVERS_LEVE.toString()))!=null){
                leve.setProfilTraversEvenementHydraulique(evenementsHydrauliques.get(row.getInt(Columns.ID_PROFIL_EN_TRAVERS_LEVE.toString())));
            }
            
            leve.setDesignation(String.valueOf(row.getInt(Columns.ID_PROFIL_EN_TRAVERS_LEVE.toString())));
            leve.setValid(true);
            
            leves.put(row.getInt(Columns.ID_PROFIL_EN_TRAVERS_LEVE.toString()), leve);
            
            List<LeveProfilTravers> listByProfil = levesByProfil.get(row.getInt(Columns.ID_PROFIL_EN_TRAVERS.toString()));
            if (listByProfil == null) {
                listByProfil = new ArrayList<>();
                levesByProfil.put(row.getInt(Columns.ID_PROFIL_EN_TRAVERS.toString()), listByProfil);
            }
            listByProfil.add(leve);
            
            List<Integer> listIdsByProfil = levesIdsByProfil.get(row.getInt(Columns.ID_PROFIL_EN_TRAVERS.toString()));
            if (listIdsByProfil == null) {
                listIdsByProfil = new ArrayList<>();
                levesIdsByProfil.put(row.getInt(Columns.ID_PROFIL_EN_TRAVERS.toString()), listIdsByProfil);
            }
            listIdsByProfil.add(row.getInt(Columns.ID_PROFIL_EN_TRAVERS_LEVE.toString()));
        }
    }
}
