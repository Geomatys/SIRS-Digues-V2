package fr.sirs.importer.documentTroncon.document.profilLong;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.Organisme;
import fr.sirs.core.model.ProfilLong;
import fr.sirs.core.model.ParametreHydrauliqueProfilLong;
import fr.sirs.core.model.PrZProfilLong;
import fr.sirs.core.model.RefOrigineProfilLong;
import fr.sirs.core.model.RefPositionProfilLongSurDigue;
import fr.sirs.core.model.RefSystemeReleveProfil;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.XYZProfilLong;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericImporter;
import fr.sirs.importer.OrganismeImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.evenementHydraulique.EvenementHydrauliqueImporter;
import fr.sirs.importer.documentTroncon.document.TypeSystemeReleveProfilImporter;
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
public class ProfilEnLongImporter extends GenericImporter {
    
    private final TypeSystemeReleveProfilImporter typeSystemeReleveProfilImporter;
    private final TypePositionProfilLongImporter typePositionProfilLongImporter;
    private final TypeOrigineProfilLongImporter typeOrigineProfilLongImporter;
    private final ProfilLongPointXYZImporter profilLongPointXYZImporter;
    private final ProfilLongPointDZImporter profilLongPointDZImporter;
    private final ProfilLongEvenementHydrauliqueImporter profilLongEvenementHydrauliqueImporter;
    private final SystemeReperageImporter systemeReperageImporter;
    
    private final OrganismeImporter organismeImporter;
    
    protected Map<Integer, ProfilLong> related = null;

    public ProfilEnLongImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final OrganismeImporter organismeImporter,
            final SystemeReperageImporter systemeReperageImporter,
            final EvenementHydrauliqueImporter evenementHydrauliqueImporter,
            final TypeSystemeReleveProfilImporter typeSystemeReleveProfilImporter){
        super(accessDatabase, couchDbConnector);
        this.organismeImporter = organismeImporter;
        this.systemeReperageImporter = systemeReperageImporter;
        this.typeSystemeReleveProfilImporter = typeSystemeReleveProfilImporter;
        this.profilLongEvenementHydrauliqueImporter = new ProfilLongEvenementHydrauliqueImporter(
                accessDatabase, couchDbConnector, evenementHydrauliqueImporter);
        this.typePositionProfilLongImporter = new TypePositionProfilLongImporter(
                accessDatabase, couchDbConnector);
        this.typeOrigineProfilLongImporter = new TypeOrigineProfilLongImporter(
                accessDatabase, couchDbConnector);
        profilLongPointXYZImporter = new ProfilLongPointXYZImporter(accessDatabase, couchDbConnector);
        profilLongPointDZImporter = new ProfilLongPointDZImporter(accessDatabase, couchDbConnector);
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
//        ID_DOC_RAPPORT_ETUDES, // Mettre une référence vers UN rapport d'études
        COMMENTAIRE,
        ID_SYSTEME_REP_DZ,
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
        return PROFIL_EN_LONG.toString();
    }
    
    public Map<Integer, ProfilLong> getRelated() throws IOException, AccessDbImporterException{
        if(related==null) compute();
        return related;
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        related = new HashMap<>();
        
        final Map<Integer, Organisme> organismes = organismeImporter.getOrganismes();
        final Map<Integer, SystemeReperage> srs = systemeReperageImporter.getSystemeRepLineaire();
        final Map<Integer, RefSystemeReleveProfil> systemesReleve = typeSystemeReleveProfilImporter.getTypeReferences();
        final Map<Integer, RefPositionProfilLongSurDigue> typesPositionProfil = typePositionProfilLongImporter.getTypeReferences();
        final Map<Integer, RefOrigineProfilLong> typesOrigineProfil = typeOrigineProfilLongImporter.getTypeReferences();
        final Map<Integer, List<XYZProfilLong>> pointsByLeveXYZ = profilLongPointXYZImporter.getLeveePointByProfilId();
        final Map<Integer, List<PrZProfilLong>> pointsByLeveDZ = profilLongPointDZImporter.getLeveePointByProfilId();
        final Map<Integer, List<ParametreHydrauliqueProfilLong>> evenementsHydrauliques = profilLongEvenementHydrauliqueImporter.getEvenementHydrauliqueByProfilId();
    
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while(it.hasNext()){
            final Row row = it.next();
            final ProfilLong profil = createAnonymValidElement(ProfilLong.class);
            
            profil.setLibelle(row.getString(Columns.NOM.toString()));
            
            if (row.getDate(Columns.DATE_LEVE.toString()) != null) {
                profil.setDateLevee(DbImporter.parse(row.getDate(Columns.DATE_LEVE.toString()), dateTimeFormatter));
            }
            
            final Organisme organisme = organismes.get(row.getInt(Columns.ID_ORG_CREATEUR.toString()));
            if(organisme!=null){
                profil.setOrganismeCreateurId(organisme.getId());
            }
            
            if(row.getInt(Columns.ID_TYPE_SYSTEME_RELEVE_PROFIL.toString())!=null){
                profil.setTypeSystemesReleveId(systemesReleve.get(row.getInt(Columns.ID_TYPE_SYSTEME_RELEVE_PROFIL.toString())).getId());
            }
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                profil.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            
            profil.setReferencePapier(row.getString(Columns.REFERENCE_PAPIER.toString()));
            
            profil.setChemin(row.getString(Columns.REFERENCE_NUMERIQUE.toString()));
            
            profil.setReferenceCalque(row.getString(Columns.REFERENCE_CALQUE.toString()));
            
            if(row.getInt(Columns.ID_SYSTEME_REP_DZ.toString())!=null){
                profil.setSystemeRepDzId(srs.get(row.getInt(Columns.ID_SYSTEME_REP_DZ.toString())).getId());
            }
            
            if(row.getInt(Columns.ID_TYPE_ORIGINE_PROFIL_EN_LONG.toString())!=null){
                profil.setOrigineProfilLongId(typesOrigineProfil.get(row.getInt(Columns.ID_TYPE_ORIGINE_PROFIL_EN_LONG.toString())).getId());
            }
            
            if(row.getInt(Columns.ID_TYPE_POSITION_PROFIL_EN_LONG.toString())!=null){
                profil.setPositionProfilLongSurDigueId(typesPositionProfil.get(row.getInt(Columns.ID_TYPE_POSITION_PROFIL_EN_LONG.toString())).getId());
            }
            
            if(pointsByLeveXYZ.get(row.getInt(Columns.ID_PROFIL_EN_LONG.toString()))!=null){
                profil.setPointsLeveXYZ(pointsByLeveXYZ.get(row.getInt(Columns.ID_PROFIL_EN_LONG.toString())));
            }
            
            if(pointsByLeveDZ.get(row.getInt(Columns.ID_PROFIL_EN_LONG.toString()))!=null){
                profil.setPointsLeveDZ(pointsByLeveDZ.get(row.getInt(Columns.ID_PROFIL_EN_LONG.toString())));
            }
            
            if(evenementsHydrauliques.get(row.getInt(Columns.ID_PROFIL_EN_LONG.toString()))!=null){
                profil.setParametresHydrauliques(evenementsHydrauliques.get(row.getInt(Columns.ID_PROFIL_EN_LONG.toString())));
            }
            
            
            profil.setCommentaire(row.getString(Columns.COMMENTAIRE.toString()));
            
            profil.setDesignation(String.valueOf(row.getInt(Columns.ID_PROFIL_EN_LONG.toString())));
            
            related.put(row.getInt(Columns.ID_PROFIL_EN_LONG.toString()), profil);
        }
        
    }
}
