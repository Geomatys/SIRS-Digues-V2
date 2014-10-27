/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.util.importer;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import fr.symadrem.sirs.core.CouchDBInit;
import fr.symadrem.sirs.core.component.BorneDigueRepository;
import fr.symadrem.sirs.core.component.DigueRepository;
import fr.symadrem.sirs.core.component.OrganismeRepository;
import fr.symadrem.sirs.core.component.SystemeReperageRepository;
import fr.symadrem.sirs.core.component.TronconDigueRepository;
import fr.symadrem.sirs.core.model.Digue;
import fr.symadrem.sirs.core.model.TronconDigue;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ektorp.BulkDeleteDocument;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.lang.Setup;
import org.ektorp.CouchDbConnector;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class DbImporter {
    
    private final CouchDbConnector couchDbConnector;

    private final DigueRepository digueRepository;
    private final TronconDigueRepository tronconDigueRepository;
    private final OrganismeRepository organismeRepository;
    private final SystemeReperageRepository systemeReperageRepository;
    private final BorneDigueRepository borneDigueRepository;

    private Database accessDatabase;
    private Database accessCartoDatabase;
    
    private TypeRiveImporter typeRiveImporter;
    private TronconDigueGeomImporter tronconDigueGeomImporter;
    private SystemeReperageImporter systemeReperageImporter;
    private TronconGestionDigueGestionnaireImporter tronconGestionDigueGestionnaireImporter;
    private OrganismeImporter organismeImporter;
    private TronconGestionDigueImporter tronconGestionDigueImporter;
    private DigueImporter digueImporter;
    private BorneDigueImporter borneDigueImporter;

    public enum TableName{
     BORNE_DIGUE,
     BORNE_PAR_SYSTEME_REP,
     COMMUNE,
     CONVENTION,
     CONVENTION_SIGNATAIRES_PM,
     CONVENTION_SIGNATAIRES_PP,
     DEPARTEMENT,
     DESORDRE,
     DESORDRE_ELEMENT_RESEAU,
     DESORDRE_ELEMENT_STRUCTURE,
     DESORDRE_EVENEMENT_HYDRAU,
     DESORDRE_JOURNAL,
     DESORDRE_OBSERVATION,
     DESORDRE_PRESTATION,
     DIGUE,
     DOCUMENT,
     ECOULEMENT,
     ELEMENT_GEOMETRIE,
     ELEMENT_RESEAU,
     ELEMENT_RESEAU_AUTRE_OUVRAGE_HYDRAU,
     ELEMENT_RESEAU_CHEMIN_ACCES,
     ELEMENT_RESEAU_CONDUITE_FERMEE,
     ELEMENT_RESEAU_CONVENTION,
     ELEMENT_RESEAU_EVENEMENT_HYDRAU,
     ELEMENT_RESEAU_GARDIEN,
     ELEMENT_RESEAU_GESTIONNAIRE,
     ELEMENT_RESEAU_OUVERTURE_BATARDABLE,
     ELEMENT_RESEAU_OUVRAGE_TEL_NRJ,
     ELEMENT_RESEAU_OUVRAGE_VOIRIE,
     ELEMENT_RESEAU_OUVRAGE_VOIRIE_POINT_ACCES,
     ELEMENT_RESEAU_POINT_ACCES,
     ELEMENT_RESEAU_POMPE,
     ELEMENT_RESEAU_PROPRIETAIRE,
     ELEMENT_RESEAU_RESEAU_EAU,
     ELEMENT_RESEAU_SERVITUDE,
     ELEMENT_RESEAU_STRUCTURE,
     ELEMENT_RESEAU_VOIE_SUR_DIGUE,
     ELEMENT_STRUCTURE,
     ELEMENT_STRUCTURE_ABONDANCE_ESSENCE,
     ELEMENT_STRUCTURE_GARDIEN,
     ELEMENT_STRUCTURE_GESTIONNAIRE,
     ELEMENT_STRUCTURE_PROPRIETAIRE,
     EVENEMENT_HYDRAU,
     Export_Output,
     Export_Output_SHAPE_Index,
     GARDIEN_TRONCON_GESTION,
     GDB_AnnoSymbols,
     GDB_AttrRules,
     GDB_CodedDomains,
     GDB_DatabaseLocks,
     GDB_DefaultValues,
     GDB_Domains,
     GDB_EdgeConnRules,
     GDB_Extensions,
     GDB_FeatureClasses,
     GDB_FeatureDataset,
     GDB_FieldInfo,
     GDB_GeomColumns,
     GDB_JnConnRules,
     GDB_ObjectClasses,
     GDB_RangeDomains,
     GDB_RelClasses,
     GDB_ReleaseInfo,
     GDB_RelRules,
     GDB_ReplicaDatasets,
     GDB_Replicas,
     GDB_SpatialRefs,
     GDB_StringDomains,
     GDB_Subtypes,
     GDB_TopoClasses,
     GDB_Topologies,
     GDB_TopoRules,
     GDB_UserMetadata,
     GDB_ValidRules,
     ILE_BANC,
     ILE_TRONCON,
     IMPLANTATION,
     INTERVENANT,
     JOURNAL,
     JOURNAL_ARTICLE,
     LAISSE_CRUE,
     LAISSE_CRUE_JOURNAL,
     LIGNE_EAU,
     LIGNE_EAU_JOURNAL,
     LIGNE_EAU_MESURES_PRZ,
     LIGNE_EAU_MESURES_XYZ,
     MARCHE,
     MARCHE_FINANCEUR,
     MARCHE_MAITRE_OEUVRE,
     METEO,
     MONTEE_DES_EAUX,
     MONTEE_DES_EAUX_JOURNAL,
     MONTEE_DES_EAUX_MESURES,
     observation_urgence_carto,
     ORGANISME,
     ORGANISME_DISPOSE_INTERVENANT,
     ORIENTATION,
     PARCELLE_CADASTRE,
     PARCELLE_LONGE_DIGUE,
     PHOTO_LAISSE,
     PHOTO_LOCALISEE_EN_PR,
     PHOTO_LOCALISEE_EN_XY,
     PRESTATION,
     PRESTATION_DOCUMENT,
     PRESTATION_EVENEMENT_HYDRAU,
     PRESTATION_INTERVENANT,
     PROFIL_EN_LONG,
     PROFIL_EN_LONG_DZ,
     PROFIL_EN_LONG_EVT_HYDRAU,
     PROFIL_EN_LONG_XYZ,
     PROFIL_EN_TRAVERS,
     PROFIL_EN_TRAVERS_DESCRIPTION,
     PROFIL_EN_TRAVERS_DZ,
     PROFIL_EN_TRAVERS_EVT_HYDRAU,
     PROFIL_EN_TRAVERS_STRUCTUREL,
     PROFIL_EN_TRAVERS_TRONCON,
     PROFIL_EN_TRAVERS_XYZ,
     PROPRIETAIRE_TRONCON_GESTION,
     rampes,
     Isere, 
     riviere, 
     crete,
     RAPPORT_ETUDE,
     REQ_ADIDR_CREATION,
     REQ_CC_BORNE_5_EN_5_RD,
     REQ_CC_HAUTEUR_DIGUE_SUR_TN_TMP,
     REQ_CC_LOCALISATION_TMP,
     REQ_CC_RAMPES_ACCES,
     REQ_CC_TMP,
     REQ_CEMAGREF_SENSIBILITE_EVT_HYDRAU,
     REQ_SOGREAH_SENSIBILITE_EVT_HYDRAU,
     RQ_CC_SONDAGES,
     SelectedObjects,
     Selections,
     SOURCE_INFO,
     SYNCHRO_BD_COURANTE,
     SYNCHRO_BD_GENEREE,
     SYNCHRO_BD_TABLE,
     SYNCHRO_FILTRE_TRONCON,
     SYNCHRO_JOURNAL,
     SYNCHRO_ORGANISME_BD,
     SYNCHRO_SUIVI_BD,
     SYNDICAT,
     SYS_DONNEES_LOCALISEES_EN_PR,
     SYS_EVT_AUTRE_OUVRAGE_HYDRAULIQUE,
     SYS_EVT_BRISE_LAME,
     SYS_EVT_CHEMIN_ACCES,
     SYS_EVT_CONDUITE_FERMEE,
     SYS_EVT_CONVENTION,
     SYS_EVT_CRETE,
     SYS_EVT_DESORDRE,
     SYS_EVT_DISTANCE_PIED_DE_DIGUE_TRONCON,
     SYS_EVT_DOCUMENT_A_GRANDE_ECHELLE,
     SYS_EVT_DOCUMENT_MARCHE,
     SYS_EVT_EMPRISE_COMMUNALE,
     SYS_EVT_EMPRISE_SYNDICAT,
     SYS_EVT_EPIS,
     SYS_EVT_FICHE_INSPECTION_VISUELLE,
     SYS_EVT_FONDATION,
     SYS_EVT_GARDIEN_TRONCON,
     SYS_EVT_ILE_TRONCON,
     SYS_EVT_JOURNAL,
     SYS_EVT_LAISSE_CRUE,
     SYS_EVT_LARGEUR_FRANC_BORD,
     SYS_EVT_LIGNE_EAU,
     SYS_EVT_MARCHE,
     SYS_EVT_MONTEE_DES_EAUX_HYDRO,
     SYS_EVT_OUVERTURE_BATARDABLE,
     SYS_EVT_OUVRAGE_PARTICULIER,
     SYS_EVT_OUVRAGE_REVANCHE,
     SYS_EVT_OUVRAGE_TELECOMMUNICATION,
     SYS_EVT_OUVRAGE_VOIRIE,
     SYS_EVT_PHOTO_LOCALISEE_EN_PR,
     SYS_EVT_PIED_DE_DIGUE,
     SYS_EVT_PIED_FRONT_FRANC_BORD,
     SYS_EVT_PLAN_TOPO,
     SYS_EVT_POINT_ACCES,
     SYS_EVT_PRESTATION,
     SYS_EVT_PROFIL_EN_LONG,
     SYS_EVT_PROFIL_EN_TRAVERS,
     SYS_EVT_PROFIL_FRONT_FRANC_BORD,
     SYS_EVT_PROPRIETAIRE_TRONCON,
     SYS_EVT_RAPPORT_ETUDES,
     SYS_EVT_RESEAU_EAU,
     SYS_EVT_RESEAU_TELECOMMUNICATION,
     SYS_EVT_SITUATION_FONCIERE,
     SYS_EVT_SOMMET_RISBERME,
     SYS_EVT_STATION_DE_POMPAGE,
     SYS_EVT_TALUS_DIGUE,
     SYS_EVT_TALUS_FRANC_BORD,
     SYS_EVT_TALUS_RISBERME,
     SYS_EVT_VEGETATION,
     SYS_EVT_VOIE_SUR_DIGUE,
     SYS_IMPORT_POINTS,
     SYS_INDEFINI,
     SYS_OPTIONS,
     SYS_OPTIONS_ETATS,
     SYS_OPTIONS_REQUETES,
     SYS_OUI_NON,
     SYS_OUI_NON_INDEFINI,
     SYS_PHOTO_OPTIONS,
     SYS_RECHERCHE_MIN_MAX_PR_CALCULE,
     SYS_REQ_Temp,
     SYS_REQUETES,
     SYS_REQUETES_INTERNES,
     SYS_REQUETES_PREPROGRAMMEES,
     SYS_RQ_EXTRAIT_DESORDRE_TGD,
     SYS_RQ_MONTANT_PRESTATION_TGD,
     SYS_RQ_PROPRIETAIRE_TRAVERSEE_TGD,
     SYS_RQ_PROPRIETAIRE_TRAVERSEE_TMP,
     SYS_RQ_SENSIBILITE_EVT_HYDRAU_TGD,
     SYS_SEL_FE_FICHE_SUIVI_DESORDRE_TRONCON,
     SYS_SEL_FE_FICHE_SUIVI_DESORDRE_TYPE_DESORDRE,
     SYS_SEL_FE_RAPPORT_SYNTHE_GRAPHIQUE_LIGNE_EAU,
     SYS_SEL_FE_RAPPORT_SYNTHE_GRAPHIQUE_PROFIL_EN_LONG,
     SYS_SEL_FE_RAPPORT_SYNTHE_GRAPHIQUE_TRONCON,
     SYS_SEL_FE_RAPPORT_SYNTHE_GRAPHIQUE_TYPE_ATTRIBUT,
     SYS_SEL_FE_RAPPORT_SYNTHE_GRAPHIQUE_TYPE_DONNEE,
     SYS_SEL_RQ_EXTRAIT_DESORDRE_TRONCON,
     SYS_SEL_RQ_EXTRAIT_DESORDRE_TYPE_DESORDRE,
     SYS_SEL_RQ_MONTANT_PRESTATION_TRONCON,
     SYS_SEL_RQ_MONTANT_PRESTATION_TYPE_PRESTATION,
     SYS_SEL_RQ_SENSIBILITE_EVT_HYDRAU_EVT_HYDRAU,
     SYS_SEL_RQ_SENSIBILITE_EVT_HYDRAU_TRONCON,
     SYS_SEL_TRONCON_GESTION_DIGUE,
     SYS_SEL_TRONCON_GESTION_DIGUE_TMP,
     SYS_SEL_TYPE_DONNEES_SOUS_GROUPE,
     SYS_VEGETATION_TMP,
     SYSTEME_REP_LINEAIRE,
     TRONCON_GESTION_DIGUE,
     TRONCON_GESTION_DIGUE_COMMUNE,
     TRONCON_GESTION_DIGUE_GESTIONNAIRE,
     TRONCON_GESTION_DIGUE_SITUATION_FONCIERE,
     TRONCON_GESTION_DIGUE_SYNDICAT,
     TYPE_COMPOSITION,
     TYPE_CONDUITE_FERMEE,
     TYPE_CONVENTION,
     TYPE_COTE,
     TYPE_DESORDRE,
     TYPE_DEVERS,
     TYPE_DISTANCE_DIGUE_BERGE,
     TYPE_DOCUMENT,
     TYPE_DOCUMENT_A_GRANDE_ECHELLE,
     TYPE_DOCUMENT_DECALAGE,
     TYPE_DONNEES_GROUPE,
     TYPE_DONNEES_SOUS_GROUPE,
     TYPE_DVPT_VEGETATION,
     TYPE_ELEMENT_GEOMETRIE,
     TYPE_ELEMENT_RESEAU,
     TYPE_ELEMENT_RESEAU_COTE,
     TYPE_ELEMENT_STRUCTURE,
     TYPE_ELEMENT_STRUCTURE_COTE,
     TYPE_EMPRISE_PARCELLE,
     TYPE_EVENEMENT_HYDRAU,
     TYPE_FONCTION,
     TYPE_FONCTION_MO,
     TYPE_FREQUENCE_EVENEMENT_HYDRAU,
     TYPE_GENERAL_DOCUMENT,
     TYPE_GLISSIERE,
     TYPE_LARGEUR_FRANC_BORD,
     TYPE_MATERIAU,
     TYPE_MOYEN_MANIP_BATARDEAUX,
     TYPE_NATURE,
     TYPE_NATURE_BATARDEAUX,
     TYPE_ORGANISME,
     TYPE_ORIENTATION_OUVRAGE_FRANCHISSEMENT,
     TYPE_ORIENTATION_VENT,
     TYPE_ORIGINE_PROFIL_EN_LONG,
     TYPE_ORIGINE_PROFIL_EN_TRAVERS,
     TYPE_OUVRAGE_FRANCHISSEMENT,
     TYPE_OUVRAGE_HYDRAU_ASSOCIE,
     TYPE_OUVRAGE_PARTICULIER,
     TYPE_OUVRAGE_TELECOM_NRJ,
     TYPE_OUVRAGE_VOIRIE,
     TYPE_POSITION,
     TYPE_POSITION_PROFIL_EN_LONG_SUR_DIGUE,
     TYPE_POSITION_SUR_DIGUE,
     TYPE_PRESTATION,
     TYPE_PROFIL_EN_TRAVERS,
     TYPE_PROFIL_FRANC_BORD,
     TYPE_PROPRIETAIRE,
     TYPE_RAPPORT_ETUDE,
     TYPE_REF_HEAU,
     TYPE_RESEAU_EAU,
     TYPE_RESEAU_TELECOMMUNIC,
     TYPE_REVETEMENT,
     TYPE_RIVE,
     TYPE_SERVITUDE,
     TYPE_SEUIL,
     TYPE_SIGNATAIRE,
     TYPE_SITUATION_FONCIERE,
     TYPE_SYSTEME_RELEVE_PROFIL,
     TYPE_URGENCE,
     TYPE_USAGE_VOIE,
     TYPE_VEGETATION,
     TYPE_VEGETATION_ABONDANCE,
     TYPE_VEGETATION_ABONDANCE_BRAUN_BLANQUET,
     TYPE_VEGETATION_ESSENCE,
     TYPE_VEGETATION_ETAT_SANITAIRE,
     TYPE_VEGETATION_STRATE_DIAMETRE,
     TYPE_VEGETATION_STRATE_HAUTEUR,
     TYPE_VOIE_SUR_DIGUE,
     UTILISATION_CONDUITE,
     
     
     
     //Tables carto
    CARTO_ILE_BANC,
    CARTO_ILE_BANC_SHAPE_Index,
    CARTO_TRONCON_GESTION_DIGUE,
    CARTO_TRONCON_GESTION_DIGUE_SHAPE_Index,
    //Export_Output,
    //Export_Output_SHAPE_Index,
//    GDB_AnnoSymbols,
//    GDB_AttrRules,
//    GDB_CodedDomains,
//    GDB_DatabaseLocks,
//    GDB_DefaultValues,
//    GDB_Domains,
//    GDB_EdgeConnRules,
//    GDB_Extensions,
//    GDB_FeatureClasses,
//    GDB_FeatureDataset,
//    GDB_FieldInfo,
//    GDB_GeomColumns,
//    GDB_JnConnRules,
//    GDB_ObjectClasses,
//     GDB_RangeDomains,
//     GDB_RelClasses,
//     GDB_ReleaseInfo,
//     GDB_RelRules,
//     GDB_SpatialRefs,
//     GDB_StringDomains,
//     GDB_Subtypes,
//     GDB_UserMetadata,
//     GDB_ValidRules,
//     SelectedObjects,
//     Selections
    }

    public DbImporter(CouchDbConnector couchDbConnector) throws IOException {
        this.couchDbConnector = couchDbConnector;
        this.digueRepository = new DigueRepository(couchDbConnector);
        this.tronconDigueRepository = new TronconDigueRepository(couchDbConnector);
        this.organismeRepository = new OrganismeRepository(couchDbConnector);
        this.systemeReperageRepository = new SystemeReperageRepository(couchDbConnector);
        this.borneDigueRepository = new BorneDigueRepository(couchDbConnector);
    }
    
    public void setDatabase(final Database accessDatabase, final Database accessCartoDatabase) throws IOException{
        this.accessDatabase=accessDatabase;
        this.accessCartoDatabase=accessCartoDatabase;
        this.typeRiveImporter = new TypeRiveImporter(accessDatabase, couchDbConnector);
        this.tronconDigueGeomImporter = new TronconDigueGeomImporter(accessCartoDatabase, couchDbConnector);
        this.systemeReperageImporter = new SystemeReperageImporter(accessDatabase, couchDbConnector, systemeReperageRepository);
        this.organismeImporter = new OrganismeImporter(accessDatabase, couchDbConnector, organismeRepository);
        this.tronconGestionDigueGestionnaireImporter = new TronconGestionDigueGestionnaireImporter(
                accessDatabase, couchDbConnector, this.organismeImporter);
        this.digueImporter = new DigueImporter(accessDatabase, couchDbConnector, digueRepository);
        this.borneDigueImporter = new BorneDigueImporter(accessDatabase, couchDbConnector, borneDigueRepository);
        this.tronconGestionDigueImporter = new TronconGestionDigueImporter(accessDatabase, 
                couchDbConnector, tronconDigueRepository, digueImporter, 
                tronconDigueGeomImporter, typeRiveImporter, systemeReperageImporter, 
                tronconGestionDigueGestionnaireImporter, borneDigueImporter);
    }
    
    public CouchDbConnector getCouchDbConnector(){
        return this.couchDbConnector;
    }

    public Database getDatabase() {
        return this.accessDatabase;
    }

    public Database getCartoDatabase() {
        return this.accessCartoDatabase;
    }

    public void removeDigues() {
        final List<Object> digues = new ArrayList<>();
        digueRepository.getAll().stream().forEach((digue) -> {
            digues.add(BulkDeleteDocument.of(digue));
        });
        couchDbConnector.executeBulk(digues);
    }
    
    public void removeOrganismes() {
        final List<Object> organismes = new ArrayList<>();
        organismeRepository.getAll().stream().forEach((organisme) -> {
            organismes.add(BulkDeleteDocument.of(organisme));
        });
        couchDbConnector.executeBulk(organismes);
    }
    
    public void removeTronconsDigues() {
        final List<Object> troncons = new ArrayList<>();
        tronconDigueRepository.getAll().stream().forEach((tronconDigue) -> {
            troncons.add(BulkDeleteDocument.of(tronconDigue));
        });
        couchDbConnector.executeBulk(troncons);
    }
    
    public void removeSystemesReperage() {
        final List<Object> systemesReperage = new ArrayList<>();
        systemeReperageRepository.getAll().stream().forEach((systemeReperage) -> {
            systemesReperage.add(BulkDeleteDocument.of(systemeReperage));
        });
        couchDbConnector.executeBulk(systemesReperage);
    }
    
    public void removeBornes() {
        final List<Object> bornes = new ArrayList<>();
        borneDigueRepository.getAll().stream().forEach((borne) -> {
            bornes.add(BulkDeleteDocument.of(borne));
        });
        couchDbConnector.executeBulk(bornes);
    }
    
    public void cleanDb(){
        this.removeTronconsDigues();
        this.removeDigues();
        this.removeOrganismes();
        this.removeSystemesReperage();
        this.removeBornes();
    }
    
    public Collection<TronconDigue> importation() throws IOException, AccessDbImporterException{
        return this.tronconGestionDigueImporter.getTronconsDigues().values();
    }

    //TODO remove when import finished
    public static void main(String[] args) throws AccessDbImporterException {
                //Geotoolkit startup
                Setup.initialize(null);
                //work in lazy mode, do your best for lenient datum shift
                Hints.putSystemDefault(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);
        try {
            final ClassPathXmlApplicationContext applicationContext = CouchDBInit.create(
                            "http://geouser:geopw@localhost:5984", "symadrem", "classpath:/symadrem/spring/couchdb-context.xml");
            final CouchDbConnector couchDbConnector = applicationContext.getBean(CouchDbConnector.class);
            DbImporter importer = new DbImporter(couchDbConnector);
            importer.setDatabase(DatabaseBuilder.open(new File("/home/samuel/Bureau/symadrem/data/SIRSDigues_donnees2.mdb")),
                    DatabaseBuilder.open(new File("/home/samuel/Bureau/symadrem/data/SIRSDigues_carto2.mdb")));

//            importer.getDatabase().getTableNames().stream().forEach((tableName) -> {
//                System.out.println(tableName);
//            });
//            
//            importer.getCartoDatabase().getTableNames().stream().forEach((tableName) -> {
//                System.out.println(tableName);
//            });
//            
//            System.out.println("=======================");
//            importer.getCartoDatabase().getTable("CARTO_TRONCON_GESTION_DIGUE").getColumns().stream().forEach((column) -> {
//                System.out.println(column.getName());
//            });
//            System.out.println("++++++++++++++++++++");   
//            
//            for(Row r : importer.getCartoDatabase().getTable("GDB_SpatialRefs")){
//                System.out.println(r);
//        }
//SYS_EVT_PIED_DE_DIGUE
//            System.out.println("=======================");
//            importer.getDatabase().getTable("BORNE_DIGUE").getColumns().stream().forEach((column) -> {
//                System.out.println(column.getName());
//            });
//            System.out.println("++++++++++++++++++++");

//            System.out.println(importer.getDatabase().getTable("ILE_TRONCON").getPrimaryKeyIndex());
//            System.out.println(importer.getDatabase().getTable("SYSTEME_REP_LINEAIRE").getPrimaryKeyIndex());
//            System.out.println(importer.getDatabase().getTable("BORNE_PAR_SYSTEME_REP").getPrimaryKeyIndex());
//            System.out.println(importer.getDatabase().getTable("TRONCON_GESTION_DIGUE").getPrimaryKeyIndex());
//            System.out.println(importer.getDatabase().getTable("BORNE_DIGUE").getPrimaryKeyIndex());
//            
//            System.out.println(importer.getDatabase().getTable("ELEMENT_STRUCTURE").getPrimaryKeyIndex());
//            System.out.println("index size : "+importer.getDatabase().getTable("SYS_EVT_PIED_DE_DIGUE").getForeignKeyIndex(importer.getDatabase().getTable("ELEMENT_STRUCTURE")));
            
//            for(Row row : importer.getDatabase().getTable("ELEMENT_STRUCTURE")){
//                System.out.println(row);
//            }
            System.out.println("=======================");
            importer.getDatabase().getTable("SYS_EVT_DESORDRE").getColumns().stream().forEach((column) -> {
                System.out.println(column.getName());
            });
            System.out.println("++++++++++++++++++++");
            importer.cleanDb();
            System.out.println("=======================");
            importer.getDatabase().getTable("DESORDRE").getColumns().stream().forEach((column) -> {
                System.out.println(column.getName());
            });
            System.out.println("++++++++++++++++++++");
            importer.cleanDb();
            
            for(final TronconDigue troncon : importer.importation()){
//                System.out.println(troncon.getSysteme_reperage_defaut());
//                troncon.getStuctures().stream().forEach((structure) -> {
//                
//                    if(structure instanceof Crete){
//                        System.out.println("======>CRETE<====== : "+structure.getSysteme_rep_id());
//                    }
//                    if(structure instanceof Desordre){
//                        System.out.println("======>DESORDRE<====== : "+ structure.getSysteme_rep_id());
//                    }
//                    if(structure instanceof PiedDigue){
//                        System.out.println("======>PIEDDIGUE<====== : "+structure.getSysteme_rep_id());
//                    }
//                    
//                });
//                troncon.getBorneIds().stream().forEach((borne) -> {
//                    System.out.println(borne.getPositionBorne().toText());
//                });
            }
            System.out.println("fin de l'importation !");

        } catch (IOException ex) {
            Logger.getLogger(DbImporter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
