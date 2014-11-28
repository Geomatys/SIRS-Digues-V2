package fr.sirs.importer.theme.document.related.profilLong;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.component.ProfilLongRepository;
import fr.sirs.core.model.LeveePoints;
import fr.sirs.core.model.Organisme;
import fr.sirs.core.model.ProfilLong;
import fr.sirs.core.model.ProfilLongEvenementHydraulique;
import fr.sirs.core.model.RefOrigineProfilLong;
import fr.sirs.core.model.RefPositionProfilLongSurDigue;
import fr.sirs.core.model.RefSystemeReleveProfil;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericImporter;
import fr.sirs.importer.OrganismeImporter;
import fr.sirs.importer.evenementHydraulique.EvenementHydrauliqueImporter;
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
public class ProfilLongImporter extends GenericImporter {

    private Map<Integer, ProfilLong> profils = null;
    private ProfilLongRepository profilLongRepository;
    private TypeSystemeReleveProfilImporter typeSystemeReleveProfilImporter;
    private TypePositionProfilLongImporter typePositionProfilLongImporter;
    private TypeOrigineProfilLongImporter typeOrigineProfilLongImporter;
    private ProfilLongPointXYZImporter profilTraversPointXYZImporter;
    private ProfilLongEvenementHydrauliqueImporter profilLongEvenementHydrauliqueImporter;
    
    private OrganismeImporter organismeImporter;
    
    private ProfilLongImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    public ProfilLongImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final ProfilLongRepository profilTraversRepository,
            final OrganismeImporter organismeImporter,
            final EvenementHydrauliqueImporter evenementHydrauliqueImporter,
            final TypeSystemeReleveProfilImporter typeSystemeReleveProfilImporter){
        this(accessDatabase, couchDbConnector);
        this.profilLongRepository = profilTraversRepository;
        this.organismeImporter = organismeImporter;
        this.typeSystemeReleveProfilImporter = typeSystemeReleveProfilImporter;
        this.profilLongEvenementHydrauliqueImporter = new ProfilLongEvenementHydrauliqueImporter(
                accessDatabase, couchDbConnector, evenementHydrauliqueImporter);
        this.typePositionProfilLongImporter = new TypePositionProfilLongImporter(
                accessDatabase, couchDbConnector);
        this.typeOrigineProfilLongImporter = new TypeOrigineProfilLongImporter(
                accessDatabase, couchDbConnector);
        profilTraversPointXYZImporter = new ProfilLongPointXYZImporter(accessDatabase, couchDbConnector);
    }
    
    public Map<Integer, ProfilLong> getProfilLong() throws IOException, AccessDbImporterException{
        if(profils==null) compute();
        return profils;
    }
    
    private enum Columns {
        ID_PROFIL_EN_LONG,
        NOM,
        DATE_LEVE,
        ID_ORG_CREATEUR,
        ID_TYPE_SYSTEME_RELEVE_PROFIL,
        REFERENCE_PAPIER,
        REFERENCE_NUMERIQUE,
        REFERENCE_CALQUE,
        ID_TYPE_POSITION_PROFIL_EN_LONG,
        ID_TYPE_ORIGINE_PROFIL_EN_LONG,
//        ID_DOC_RAPPORT_ETUDES,
        COMMENTAIRE,
//        ID_SYSTEME_REP_DZ,
//        NOM_FICHIER_PLAN_ENSEMBLE,
//        NOM_FICHIER_COUPE_IMAGE,
        DATE_DERNIERE_MAJ,
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
        return DbImporter.TableName.PROFIL_EN_LONG.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        profils = new HashMap<>();
        
        final Map<Integer, Organisme> organismes = organismeImporter.getOrganismes();
        final Map<Integer, RefSystemeReleveProfil> systemesReleve = typeSystemeReleveProfilImporter.getTypes();
        final Map<Integer, RefPositionProfilLongSurDigue> typesPositionProfil = typePositionProfilLongImporter.getTypes();
        final Map<Integer, RefOrigineProfilLong> typesOrigineProfil = typeOrigineProfilLongImporter.getTypes();
        final Map<Integer, List<LeveePoints>> pointsByLeve = profilTraversPointXYZImporter.getLeveePointByProfilId();
        final Map<Integer, List<ProfilLongEvenementHydraulique>> evenementsHydrauliques = profilLongEvenementHydrauliqueImporter.getEvenementHydrauliqueByProfilId();
    
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while(it.hasNext()){
            final Row row = it.next();
            final ProfilLong profil = new ProfilLong();
            
            profil.setLibelle(row.getString(Columns.NOM.toString()));
            
            if (row.getDate(Columns.DATE_LEVE.toString()) != null) {
                profil.setDateLevee(LocalDateTime.parse(row.getDate(Columns.DATE_LEVE.toString()).toString(), dateTimeFormatter));
            }
            
            final Organisme organisme = organismes.get(row.getInt(Columns.ID_ORG_CREATEUR.toString()));
            if(organisme!=null){
                profil.setOrganismeCreateurId(organisme.getId());
            }
            
            if(row.getInt(Columns.ID_TYPE_SYSTEME_RELEVE_PROFIL.toString())!=null){
                profil.setTypeSystemesReleveId(systemesReleve.get(row.getInt(Columns.ID_TYPE_SYSTEME_RELEVE_PROFIL.toString())).getId());
            }
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                profil.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            profil.setReference_papier(row.getString(Columns.REFERENCE_PAPIER.toString()));
            
            profil.setReference_numerique(row.getString(Columns.REFERENCE_NUMERIQUE.toString()));
            
            profil.setReference_calque(row.getString(Columns.REFERENCE_CALQUE.toString()));
            
            if(row.getInt(Columns.ID_TYPE_ORIGINE_PROFIL_EN_LONG.toString())!=null){
                profil.setOrigineProfilLongId(typesOrigineProfil.get(row.getInt(Columns.ID_TYPE_ORIGINE_PROFIL_EN_LONG.toString())).getId());
            }
            
            if(row.getInt(Columns.ID_TYPE_POSITION_PROFIL_EN_LONG.toString())!=null){
                profil.setPositionProfilLongSurDigueId(typesPositionProfil.get(row.getInt(Columns.ID_TYPE_POSITION_PROFIL_EN_LONG.toString())).getId());
            }
            
            if(pointsByLeve.get(row.getInt(Columns.ID_PROFIL_EN_LONG.toString()))!=null){
                profil.setLeveePoints(pointsByLeve.get(row.getInt(Columns.ID_PROFIL_EN_LONG.toString())));
            }
            
            if(evenementsHydrauliques.get(row.getInt(Columns.ID_PROFIL_EN_LONG.toString()))!=null){
                profil.setProfilLongEvenementHydraulique(evenementsHydrauliques.get(row.getInt(Columns.ID_PROFIL_EN_LONG.toString())));
            }
            
            
            profil.setCommentaire(row.getString(Columns.COMMENTAIRE.toString()));
            
            profils.put(row.getInt(Columns.ID_PROFIL_EN_LONG.toString()), profil);
        }
        couchDbConnector.executeBulk(profils.values());
    }
    
}
