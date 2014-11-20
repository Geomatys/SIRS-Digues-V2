package fr.sirs.importer.theme.document.related.profilTravers;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.LeveePoints;
import fr.sirs.core.model.LeveeProfilTravers;
import fr.sirs.core.model.Organisme;
import fr.sirs.core.model.ProfilTraversEvenementHydraulique;
import fr.sirs.core.model.RefOrigineProfilTravers;
import fr.sirs.core.model.RefSystemeReleveProfil;
import fr.sirs.core.model.RefTypeProfilTravers;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericImporter;
import fr.sirs.importer.OrganismeImporter;
import fr.sirs.importer.theme.document.related.TypeSystemeReleveProfilImporter;
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
public class ProfilTraversDescriptionImporter extends GenericImporter {

    private Map<Integer, LeveeProfilTravers> leves = null;
    private Map<Integer, List<LeveeProfilTravers>> levesByProfil = null;
    private TypeSystemeReleveProfilImporter typeSystemeReleveProfilImporter;
    private TypeProfilTraversImporter typeProfilTraversImporter;
    private TypeOrigineProfilTraversImporter typeOrigineProfilTraversImporter;
    private OrganismeImporter organismeImporter;
    private ProfilTraversEvenementHydrauliqueImporter profilTraversEvenementHydrauliqueImporter;
    private ProfilTraversPointXYZImporter profilTraversPointXYZImporter;
    
    private ProfilTraversDescriptionImporter(final Database accessDatabase, final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    public ProfilTraversDescriptionImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final TypeSystemeReleveProfilImporter typeSystemeReleveProfilImporter,
            final TypeProfilTraversImporter typeProfilTraversImporter,
            final TypeOrigineProfilTraversImporter typeOrigineProfilTraversImporter,
            final OrganismeImporter organismeImporter,
            final ProfilTraversEvenementHydrauliqueImporter profilTraversEvenementHydrauliqueImporter) {
        this(accessDatabase, couchDbConnector);
        this.typeSystemeReleveProfilImporter = typeSystemeReleveProfilImporter;
        this.typeProfilTraversImporter = typeProfilTraversImporter;
        this.typeOrigineProfilTraversImporter = typeOrigineProfilTraversImporter;
        this.organismeImporter = organismeImporter;
        this.profilTraversEvenementHydrauliqueImporter = profilTraversEvenementHydrauliqueImporter;
        profilTraversPointXYZImporter = new ProfilTraversPointXYZImporter(accessDatabase, couchDbConnector);
    }
    
    public Map<Integer, LeveeProfilTravers> getLeveeProfilTravers() throws IOException, AccessDbImporterException{
        if(leves==null) compute();
        return leves;
    }
    
    public Map<Integer, List<LeveeProfilTravers>> getLeveeProfilTraversByProfilId() throws IOException, AccessDbImporterException{
        if(levesByProfil==null) compute();
        return levesByProfil;
    }
    
    private enum ProfilTraversDescriptionColumns {
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
//        NOM_FICHIER_PLAN_ENSEMBLE, // Pas dans le nouveau modèle
//        NOM_FICHIER_COUPE_IMAGE, // Pas dans le nouveau modèle
        DATE_DERNIERE_MAJ
    }
    
    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (ProfilTraversDescriptionColumns c : ProfilTraversDescriptionColumns.values()) {
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
        
        final Map<Integer, Organisme> organismes = organismeImporter.getOrganismes();
        final Map<Integer, RefSystemeReleveProfil> systemesReleve = typeSystemeReleveProfilImporter.getTypeSystemeReleve();
        final Map<Integer, RefTypeProfilTravers> typesProfil = typeProfilTraversImporter.getTypeProfilTravers();
        final Map<Integer, RefOrigineProfilTravers> typesOrigineProfil = typeOrigineProfilTraversImporter.getTypeOrigineProfilTravers();
        final Map<Integer, List<ProfilTraversEvenementHydraulique>> evenementsHydrauliques = profilTraversEvenementHydrauliqueImporter.getEvenementHydrauliqueByLeveId();
        final Map<Integer, List<LeveePoints>> pointsByLeve = profilTraversPointXYZImporter.getLeveePointByLeveId();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while(it.hasNext()){
            final Row row = it.next();
            final LeveeProfilTravers leve = new LeveeProfilTravers();
            
            if (row.getDate(ProfilTraversDescriptionColumns.DATE_LEVE.toString()) != null) {
                leve.setDateLevee(LocalDateTime.parse(row.getDate(ProfilTraversDescriptionColumns.DATE_LEVE.toString()).toString(), dateTimeFormatter));
            }
            
            final Organisme organisme = organismes.get(row.getInt(ProfilTraversDescriptionColumns.ID_ORG_CREATEUR.toString()));
            if(organisme!=null){
                leve.setOrganismeCreateurId(organisme.getId());
            }
            
            if(row.getInt(ProfilTraversDescriptionColumns.ID_TYPE_SYSTEME_RELEVE_PROFIL.toString())!=null){
                leve.setTypeSystemesReleveId(systemesReleve.get(row.getInt(ProfilTraversDescriptionColumns.ID_TYPE_SYSTEME_RELEVE_PROFIL.toString())).getId());
            }
            
            leve.setReference_papier(row.getString(ProfilTraversDescriptionColumns.REFERENCE_PAPIER.toString()));
            
            leve.setReference_numerique(row.getString(ProfilTraversDescriptionColumns.REFERENCE_NUMERIQUE.toString()));
            
            leve.setReference_calque(row.getString(ProfilTraversDescriptionColumns.REFERENCE_CALQUE.toString()));
            
            if(row.getInt(ProfilTraversDescriptionColumns.ID_TYPE_PROFIL_EN_TRAVERS.toString())!=null){
                leve.setTypeProfilId(typesProfil.get(row.getInt(ProfilTraversDescriptionColumns.ID_TYPE_PROFIL_EN_TRAVERS.toString())).getId());
            }
            
            if(row.getInt(ProfilTraversDescriptionColumns.ID_TYPE_ORIGINE_PROFIL_EN_TRAVERS.toString())!=null){
                leve.setTypeOrigineProfil(typesOrigineProfil.get(row.getInt(ProfilTraversDescriptionColumns.ID_TYPE_ORIGINE_PROFIL_EN_TRAVERS.toString())).getId());
            }
            
            leve.setCommentaire(row.getString(ProfilTraversDescriptionColumns.COMMENTAIRE.toString()));
            
            if (row.getDate(ProfilTraversDescriptionColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                leve.setDateMaj(LocalDateTime.parse(row.getDate(ProfilTraversDescriptionColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            if(pointsByLeve.get(row.getInt(ProfilTraversDescriptionColumns.ID_PROFIL_EN_TRAVERS_LEVE.toString()))!=null){
                leve.setLeveePoints(pointsByLeve.get(row.getInt(ProfilTraversDescriptionColumns.ID_PROFIL_EN_TRAVERS_LEVE.toString())));
            }
            
            if(evenementsHydrauliques.get(row.getInt(ProfilTraversDescriptionColumns.ID_PROFIL_EN_TRAVERS_LEVE.toString()))!=null){
                leve.setProfilTraversEvenementHydraulique(evenementsHydrauliques.get(row.getInt(ProfilTraversDescriptionColumns.ID_PROFIL_EN_TRAVERS_LEVE.toString())));
            }
            
            leves.put(row.getInt(ProfilTraversDescriptionColumns.ID_PROFIL_EN_TRAVERS_LEVE.toString()), leve);
            
            List<LeveeProfilTravers> listByProfil = levesByProfil.get(row.getInt(ProfilTraversDescriptionColumns.ID_PROFIL_EN_TRAVERS.toString()));
            if (listByProfil == null) {
                listByProfil = new ArrayList<>();
                levesByProfil.put(row.getInt(ProfilTraversDescriptionColumns.ID_PROFIL_EN_TRAVERS.toString()), listByProfil);
            }
            listByProfil.add(leve);
        }
    }
}
