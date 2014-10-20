/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.util.importer;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import fr.symadrem.sirs.core.component.DigueRepository;
import fr.symadrem.sirs.core.component.OrganismeRepository;
import fr.symadrem.sirs.core.component.TronconDigueRepository;
import fr.symadrem.sirs.core.model.Crete;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.lang.Setup;
import org.ektorp.CouchDbConnector;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class DbImporter {

    private final DigueRepository digueRepository;
    private final TronconDigueRepository tronconDigueRepository;
    private final OrganismeRepository organismeRepository;

    private Database accessDatabase;
    private Database accessCartoDatabase;
    
    private TypeRiveImporter typeRiveImporter;
    private TronconDigueGeomImporter tronconDigueGeomImporter;
    private SystemeReperageImporter systemeReperageImporter;
    private TronconGestionDigueGestionnaireImporter tronconGestionDigueGestionnaireImporter;
    private OrganismeImporter organismeImporter;
    private TronconGestionDigueImporter tronconGestionDigueImporter;
    private DigueImporter digueImporter;

    private final DateTimeFormatter dateTimeFormatter;
    /*
     BORNE_DIGUE
     BORNE_PAR_SYSTEME_REP
     COMMUNE
     CONVENTION
     CONVENTION_SIGNATAIRES_PM
     CONVENTION_SIGNATAIRES_PP
     DEPARTEMENT
     DESORDRE
     DESORDRE_ELEMENT_RESEAU
     DESORDRE_ELEMENT_STRUCTURE
     DESORDRE_EVENEMENT_HYDRAU
     DESORDRE_JOURNAL
     DESORDRE_OBSERVATION
     DESORDRE_PRESTATION
     * DIGUE
     DOCUMENT
     ECOULEMENT
     ELEMENT_GEOMETRIE
     ELEMENT_RESEAU
     ELEMENT_RESEAU_AUTRE_OUVRAGE_HYDRAU
     ELEMENT_RESEAU_CHEMIN_ACCES
     ELEMENT_RESEAU_CONDUITE_FERMEE
     ELEMENT_RESEAU_CONVENTION
     ELEMENT_RESEAU_EVENEMENT_HYDRAU
     ELEMENT_RESEAU_GARDIEN
     ELEMENT_RESEAU_GESTIONNAIRE
     ELEMENT_RESEAU_OUVERTURE_BATARDABLE
     ELEMENT_RESEAU_OUVRAGE_TEL_NRJ
     ELEMENT_RESEAU_OUVRAGE_VOIRIE
     ELEMENT_RESEAU_OUVRAGE_VOIRIE_POINT_ACCES
     ELEMENT_RESEAU_POINT_ACCES
     ELEMENT_RESEAU_POMPE
     ELEMENT_RESEAU_PROPRIETAIRE
     ELEMENT_RESEAU_RESEAU_EAU
     ELEMENT_RESEAU_SERVITUDE
     ELEMENT_RESEAU_STRUCTURE
     ELEMENT_RESEAU_VOIE_SUR_DIGUE
     ELEMENT_STRUCTURE
     ELEMENT_STRUCTURE_ABONDANCE_ESSENCE
     ELEMENT_STRUCTURE_GARDIEN
     ELEMENT_STRUCTURE_GESTIONNAIRE
     ELEMENT_STRUCTURE_PROPRIETAIRE
     EVENEMENT_HYDRAU
     Export_Output
     Export_Output_SHAPE_Index
     GARDIEN_TRONCON_GESTION
     GDB_AnnoSymbols
     GDB_AttrRules
     GDB_CodedDomains
     GDB_DatabaseLocks
     GDB_DefaultValues
     GDB_Domains
     GDB_EdgeConnRules
     GDB_Extensions
     GDB_FeatureClasses
     GDB_FeatureDataset
     GDB_FieldInfo
     GDB_GeomColumns
     GDB_JnConnRules
     GDB_ObjectClasses
     GDB_RangeDomains
     GDB_RelClasses
     GDB_ReleaseInfo
     GDB_RelRules
     GDB_ReplicaDatasets
     GDB_Replicas
     GDB_SpatialRefs
     GDB_StringDomains
     GDB_Subtypes
     GDB_TopoClasses
     GDB_Topologies
     GDB_TopoRules
     GDB_UserMetadata
     GDB_ValidRules
     ILE_BANC
     ILE_TRONCON
     IMPLANTATION
     INTERVENANT
     JOURNAL
     JOURNAL_ARTICLE
     LAISSE_CRUE
     LAISSE_CRUE_JOURNAL
     LIGNE_EAU
     LIGNE_EAU_JOURNAL
     LIGNE_EAU_MESURES_PRZ
     LIGNE_EAU_MESURES_XYZ
     MARCHE
     MARCHE_FINANCEUR
     MARCHE_MAITRE_OEUVRE
     METEO
     MONTEE_DES_EAUX
     MONTEE_DES_EAUX_JOURNAL
     MONTEE_DES_EAUX_MESURES
     observation_urgence_carto
     * ORGANISME
     ORGANISME_DISPOSE_INTERVENANT
     ORIENTATION
     PARCELLE_CADASTRE
     PARCELLE_LONGE_DIGUE
     PHOTO_LAISSE
     PHOTO_LOCALISEE_EN_PR
     PHOTO_LOCALISEE_EN_XY
     PRESTATION
     PRESTATION_DOCUMENT
     PRESTATION_EVENEMENT_HYDRAU
     PRESTATION_INTERVENANT
     PROFIL_EN_LONG
     PROFIL_EN_LONG_DZ
     PROFIL_EN_LONG_EVT_HYDRAU
     PROFIL_EN_LONG_XYZ
     PROFIL_EN_TRAVERS
     PROFIL_EN_TRAVERS_DESCRIPTION
     PROFIL_EN_TRAVERS_DZ
     PROFIL_EN_TRAVERS_EVT_HYDRAU
     PROFIL_EN_TRAVERS_STRUCTUREL
     PROFIL_EN_TRAVERS_TRONCON
     PROFIL_EN_TRAVERS_XYZ
     PROPRIETAIRE_TRONCON_GESTION
     rampes Isere riviere crete
     RAPPORT_ETUDE
     REQ_ADIDR_CREATION
     REQ_CC_BORNE_5_EN_5_RD
     REQ_CC_HAUTEUR_DIGUE_SUR_TN_TMP
     REQ_CC_LOCALISATION_TMP
     REQ_CC_RAMPES_ACCES
     REQ_CC_TMP
     REQ_CEMAGREF_SENSIBILITE_EVT_HYDRAU
     REQ_SOGREAH_SENSIBILITE_EVT_HYDRAU
     RQ_CC_SONDAGES
     SelectedObjects
     Selections
     SOURCE_INFO
     SYNCHRO_BD_COURANTE
     SYNCHRO_BD_GENEREE
     SYNCHRO_BD_TABLE
     SYNCHRO_FILTRE_TRONCON
     SYNCHRO_JOURNAL
     SYNCHRO_ORGANISME_BD
     SYNCHRO_SUIVI_BD
     SYNDICAT
     SYS_DONNEES_LOCALISEES_EN_PR
     SYS_EVT_AUTRE_OUVRAGE_HYDRAULIQUE
     SYS_EVT_BRISE_LAME
     SYS_EVT_CHEMIN_ACCES
     SYS_EVT_CONDUITE_FERMEE
     SYS_EVT_CONVENTION
     SYS_EVT_CRETE
     SYS_EVT_DESORDRE
     SYS_EVT_DISTANCE_PIED_DE_DIGUE_TRONCON
     SYS_EVT_DOCUMENT_A_GRANDE_ECHELLE
     SYS_EVT_DOCUMENT_MARCHE
     SYS_EVT_EMPRISE_COMMUNALE
     SYS_EVT_EMPRISE_SYNDICAT
     SYS_EVT_EPIS
     SYS_EVT_FICHE_INSPECTION_VISUELLE
     V SYS_EVT_FONDATION
     SYS_EVT_GARDIEN_TRONCON
     SYS_EVT_ILE_TRONCON
     SYS_EVT_JOURNAL
     SYS_EVT_LAISSE_CRUE
     SYS_EVT_LARGEUR_FRANC_BORD
     SYS_EVT_LIGNE_EAU
     SYS_EVT_MARCHE
     SYS_EVT_MONTEE_DES_EAUX_HYDRO
     SYS_EVT_OUVERTURE_BATARDABLE
     SYS_EVT_OUVRAGE_PARTICULIER
     SYS_EVT_OUVRAGE_REVANCHE
     SYS_EVT_OUVRAGE_TELECOMMUNICATION
     SYS_EVT_OUVRAGE_VOIRIE
     SYS_EVT_PHOTO_LOCALISEE_EN_PR
     SYS_EVT_PIED_DE_DIGUE
     SYS_EVT_PIED_FRONT_FRANC_BORD
     SYS_EVT_PLAN_TOPO
     SYS_EVT_POINT_ACCES
     SYS_EVT_PRESTATION
     SYS_EVT_PROFIL_EN_LONG
     SYS_EVT_PROFIL_EN_TRAVERS
     SYS_EVT_PROFIL_FRONT_FRANC_BORD
     SYS_EVT_PROPRIETAIRE_TRONCON
     SYS_EVT_RAPPORT_ETUDES
     SYS_EVT_RESEAU_EAU
     SYS_EVT_RESEAU_TELECOMMUNICATION
     SYS_EVT_SITUATION_FONCIERE
     SYS_EVT_SOMMET_RISBERME
     SYS_EVT_STATION_DE_POMPAGE
     SYS_EVT_TALUS_DIGUE
     SYS_EVT_TALUS_FRANC_BORD
     SYS_EVT_TALUS_RISBERME
     SYS_EVT_VEGETATION
     SYS_EVT_VOIE_SUR_DIGUE
     SYS_IMPORT_POINTS
     SYS_INDEFINI
     SYS_OPTIONS
     SYS_OPTIONS_ETATS
     SYS_OPTIONS_REQUETES
     SYS_OUI_NON
     SYS_OUI_NON_INDEFINI
     SYS_PHOTO_OPTIONS
     SYS_RECHERCHE_MIN_MAX_PR_CALCULE
     SYS_REQ_Temp
     SYS_REQUETES
     SYS_REQUETES_INTERNES
     SYS_REQUETES_PREPROGRAMMEES
     SYS_RQ_EXTRAIT_DESORDRE_TGD
     SYS_RQ_MONTANT_PRESTATION_TGD
     SYS_RQ_PROPRIETAIRE_TRAVERSEE_TGD
     SYS_RQ_PROPRIETAIRE_TRAVERSEE_TMP
     SYS_RQ_SENSIBILITE_EVT_HYDRAU_TGD
     SYS_SEL_FE_FICHE_SUIVI_DESORDRE_TRONCON
     SYS_SEL_FE_FICHE_SUIVI_DESORDRE_TYPE_DESORDRE
     SYS_SEL_FE_RAPPORT_SYNTHE_GRAPHIQUE_LIGNE_EAU
     SYS_SEL_FE_RAPPORT_SYNTHE_GRAPHIQUE_PROFIL_EN_LONG
     SYS_SEL_FE_RAPPORT_SYNTHE_GRAPHIQUE_TRONCON
     SYS_SEL_FE_RAPPORT_SYNTHE_GRAPHIQUE_TYPE_ATTRIBUT
     SYS_SEL_FE_RAPPORT_SYNTHE_GRAPHIQUE_TYPE_DONNEE
     SYS_SEL_RQ_EXTRAIT_DESORDRE_TRONCON
     SYS_SEL_RQ_EXTRAIT_DESORDRE_TYPE_DESORDRE
     SYS_SEL_RQ_MONTANT_PRESTATION_TRONCON
     SYS_SEL_RQ_MONTANT_PRESTATION_TYPE_PRESTATION
     SYS_SEL_RQ_SENSIBILITE_EVT_HYDRAU_EVT_HYDRAU
     SYS_SEL_RQ_SENSIBILITE_EVT_HYDRAU_TRONCON
     SYS_SEL_TRONCON_GESTION_DIGUE
     SYS_SEL_TRONCON_GESTION_DIGUE_TMP
     SYS_SEL_TYPE_DONNEES_SOUS_GROUPE
     SYS_VEGETATION_TMP
     = SYSTEME_REP_LINEAIRE
     = TRONCON_GESTION_DIGUE
     TRONCON_GESTION_DIGUE_COMMUNE
     * TRONCON_GESTION_DIGUE_GESTIONNAIRE
     TRONCON_GESTION_DIGUE_SITUATION_FONCIERE
     TRONCON_GESTION_DIGUE_SYNDICAT
     TYPE_COMPOSITION
     TYPE_CONDUITE_FERMEE
     TYPE_CONVENTION
     TYPE_COTE
     TYPE_DESORDRE
     TYPE_DEVERS
     TYPE_DISTANCE_DIGUE_BERGE
     TYPE_DOCUMENT
     TYPE_DOCUMENT_A_GRANDE_ECHELLE
     TYPE_DOCUMENT_DECALAGE
     TYPE_DONNEES_GROUPE
     TYPE_DONNEES_SOUS_GROUPE
     TYPE_DVPT_VEGETATION
     TYPE_ELEMENT_GEOMETRIE
     TYPE_ELEMENT_RESEAU
     TYPE_ELEMENT_RESEAU_COTE
     TYPE_ELEMENT_STRUCTURE
     TYPE_ELEMENT_STRUCTURE_COTE
     TYPE_EMPRISE_PARCELLE
     TYPE_EVENEMENT_HYDRAU
     TYPE_FONCTION
     TYPE_FONCTION_MO
     TYPE_FREQUENCE_EVENEMENT_HYDRAU
     TYPE_GENERAL_DOCUMENT
     TYPE_GLISSIERE
     TYPE_LARGEUR_FRANC_BORD
     TYPE_MATERIAU
     TYPE_MOYEN_MANIP_BATARDEAUX
     TYPE_NATURE
     TYPE_NATURE_BATARDEAUX
     TYPE_ORGANISME
     TYPE_ORIENTATION_OUVRAGE_FRANCHISSEMENT
     TYPE_ORIENTATION_VENT
     TYPE_ORIGINE_PROFIL_EN_LONG
     TYPE_ORIGINE_PROFIL_EN_TRAVERS
     TYPE_OUVRAGE_FRANCHISSEMENT
     TYPE_OUVRAGE_HYDRAU_ASSOCIE
     TYPE_OUVRAGE_PARTICULIER
     TYPE_OUVRAGE_TELECOM_NRJ
     TYPE_OUVRAGE_VOIRIE
     TYPE_POSITION
     TYPE_POSITION_PROFIL_EN_LONG_SUR_DIGUE
     TYPE_POSITION_SUR_DIGUE
     TYPE_PRESTATION
     TYPE_PROFIL_EN_TRAVERS
     TYPE_PROFIL_FRANC_BORD
     TYPE_PROPRIETAIRE
     TYPE_RAPPORT_ETUDE
     TYPE_REF_HEAU
     TYPE_RESEAU_EAU
     TYPE_RESEAU_TELECOMMUNIC
     TYPE_REVETEMENT
     * TYPE_RIVE
     TYPE_SERVITUDE
     TYPE_SEUIL
     TYPE_SIGNATAIRE
     TYPE_SITUATION_FONCIERE
     TYPE_SYSTEME_RELEVE_PROFIL
     TYPE_URGENCE
     TYPE_USAGE_VOIE
     TYPE_VEGETATION
     TYPE_VEGETATION_ABONDANCE
     TYPE_VEGETATION_ABONDANCE_BRAUN_BLANQUET
     TYPE_VEGETATION_ESSENCE
     TYPE_VEGETATION_ETAT_SANITAIRE
     TYPE_VEGETATION_STRATE_DIAMETRE
     TYPE_VEGETATION_STRATE_HAUTEUR
     TYPE_VOIE_SUR_DIGUE
     UTILISATION_CONDUITE
    
    
    
    CARTO=======================================================================
    ============================================================================
    CARTO_ILE_BANC
    CARTO_ILE_BANC_SHAPE_Index
    * CARTO_TRONCON_GESTION_DIGUE
    x CARTO_TRONCON_GESTION_DIGUE_SHAPE_Index
    x Export_Output
    x Export_Output_SHAPE_Index
    x GDB_AnnoSymbols
    x GDB_AttrRules
    x GDB_CodedDomains
    x GDB_DatabaseLocks
    x GDB_DefaultValues
    x GDB_Domains
    x GDB_EdgeConnRules
    x GDB_Extensions
    x GDB_FeatureClasses
    x GDB_FeatureDataset
    x GDB_FieldInfo
    x GDB_GeomColumns
    x GDB_JnConnRules
    x GDB_ObjectClasses
    x GDB_RangeDomains
    x GDB_RelClasses
    x GDB_ReleaseInfo
    x GDB_RelRules
    x GDB_SpatialRefs
    x GDB_StringDomains
    x GDB_Subtypes
    x GDB_UserMetadata
    x GDB_ValidRules
    x SelectedObjects
    x Selections
     */

    private DbImporter(CouchDbConnector couchDbConnector) throws IOException {
        this.dateTimeFormatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
        this.digueRepository = new DigueRepository(couchDbConnector);
        this.tronconDigueRepository = new TronconDigueRepository(couchDbConnector);
        this.organismeRepository = new OrganismeRepository(couchDbConnector);
        
    }

//    public DbImporter(CouchDbConnector couchDbConnector, File mdbFile) throws IOException {
//        this.accessDatabase = DatabaseBuilder.open(mdbFile);
//        this.dateTimeFormatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
//        this.digueRepository = new DigueRepository(couchDbConnector);
//        this.tronconDigueRepository = new TronconDigueRepository(couchDbConnector);
//        this.organismeRepository = new OrganismeRepository(couchDbConnector);
//    }
    
    public void setDatabase(final Database accessDatabase, final Database accessCartoDatabase) throws IOException{
        this.accessDatabase=accessDatabase;
        this.accessCartoDatabase=accessCartoDatabase;
        this.typeRiveImporter = new TypeRiveImporter(accessDatabase);
        this.tronconDigueGeomImporter = new TronconDigueGeomImporter(accessCartoDatabase);
        this.systemeReperageImporter = new SystemeReperageImporter(accessDatabase);
        this.organismeImporter = new OrganismeImporter(accessDatabase);
        this.tronconGestionDigueGestionnaireImporter = new TronconGestionDigueGestionnaireImporter(accessDatabase, this.organismeImporter);
        this.digueImporter = new DigueImporter(accessDatabase);
        this.tronconGestionDigueImporter = new TronconGestionDigueImporter(accessDatabase, digueImporter, tronconDigueGeomImporter, typeRiveImporter, systemeReperageImporter, tronconGestionDigueGestionnaireImporter);
    }

    public Database getDatabase() {
        return this.accessDatabase;
    }

    public Database getCartoDatabase() {
        return this.accessCartoDatabase;
    }

    public void removeDigues() {
        digueRepository.getAll().stream().forEach((digue) -> {
            digueRepository.remove(digue);
        });
    }

    public void importDigues() throws IOException {
        digueImporter.getDigues().values().stream().forEach((digue) -> {
            digueRepository.add(digue);
        });
    }

    public void removeOrganismes() {
        organismeRepository.getAll().stream().forEach((organisme) -> {
            organismeRepository.remove(organisme);
        });
    }

    public void importOrganismes() throws IOException {
        organismeImporter.getOrganismes().values().stream().forEach((organisme) -> {
            organismeRepository.add(organisme);
        });
    }
    
    /*==========================================================================
    SYS_EVT_CRETE
    ----------------------------------------------------------------------------
    ID_ELEMENT_STRUCTURE
    id_nom_element
    ID_SOUS_GROUPE_DONNEES
    LIBELLE_TYPE_ELEMENT_STRUCTURE
    DECALAGE_DEFAUT
    DECALAGE
    LIBELLE_SOURCE
    LIBELLE_TYPE_COTE
    LIBELLE_SYSTEME_REP
    NOM_BORNE_DEBUT
    NOM_BORNE_FIN
    LIBELLE_TYPE_MATERIAU
    LIBELLE_TYPE_NATURE
    LIBELLE_TYPE_FONCTION
    LIBELLE_TYPE_NATURE_HAUT
    LIBELLE_TYPE_MATERIAU_HAUT
    LIBELLE_TYPE_NATURE_BAS
    LIBELLE_TYPE_MATERIAU_BAS
    LIBELLE_TYPE_OUVRAGE_PARTICULIER
    LIBELLE_TYPE_POSITION
    RAISON_SOCIALE_ORG_PROPRIO
    RAISON_SOCIALE_ORG_GESTION
    INTERV_PROPRIO
    INTERV_GARDIEN
    LIBELLE_TYPE_COMPOSITION
    LIBELLE_TYPE_VEGETATION
    ID_TYPE_ELEMENT_STRUCTURE
    ID_TYPE_COTE
    ID_SOURCE
    ID_TRONCON_GESTION
    DATE_DEBUT_VAL
    DATE_FIN_VAL
    PR_DEBUT_CALCULE
    PR_FIN_CALCULE
    X_DEBUT
    Y_DEBUT
    X_FIN
    Y_FIN
    ID_SYSTEME_REP
    ID_BORNEREF_DEBUT
    AMONT_AVAL_DEBUT
    DIST_BORNEREF_DEBUT
    ID_BORNEREF_FIN
    AMONT_AVAL_FIN
    DIST_BORNEREF_FIN
    COMMENTAIRE
    N_COUCHE
    ID_TYPE_MATERIAU
    ID_TYPE_NATURE
    ID_TYPE_FONCTION
    EPAISSEUR
    TALUS_INTERCEPTE_CRETE
    ID_TYPE_NATURE_HAUT
    ID_TYPE_MATERIAU_HAUT
    ID_TYPE_MATERIAU_BAS
    ID_TYPE_NATURE_BAS
    LONG_RAMP_HAUT
    LONG_RAMP_BAS
    PENTE_INTERIEURE
    ID_TYPE_OUVRAGE_PARTICULIER
    ID_TYPE_POSITION
    ID_ORG_PROPRIO
    ID_ORG_GESTION
    ID_INTERV_PROPRIO
    ID_INTERV_GARDIEN
    DATE_DEBUT_ORGPROPRIO
    DATE_FIN_ORGPROPRIO
    DATE_DEBUT_GESTION
    DATE_FIN_GESTION
    DATE_DEBUT_INTERVPROPRIO
    DATE_FIN_INTERVPROPRIO
    ID_TYPE_COMPOSITION
    DISTANCE_TRONCON
    LONGUEUR
    DATE_DEBUT_GARDIEN
    DATE_FIN_GARDIEN
    LONGUEUR_PERPENDICULAIRE
    LONGUEUR_PARALLELE
    COTE_AXE
    ID_TYPE_VEGETATION
    HAUTEUR
    DIAMETRE
    DENSITE
    EPAISSEUR_Y11
    EPAISSEUR_Y12
    EPAISSEUR_Y21
    EPAISSEUR_Y22
    ID_AUTO
    */
    
    private Map<Integer, Crete> sysEvtCreteByTronconId = null;
    private List<Crete> getCretes() throws IOException{
        
        final List<Crete> cretes = new ArrayList<>();
        final Iterator<Row> it = this.accessDatabase.getTable("SYS_EVT_CRETE").iterator();
        
        while (it.hasNext()) {
            final Row row = it.next();
            final Crete crete = new Crete();
//            crete.setBorne_debut(borne_debut);
//            crete.setBorne_debut_aval(true);
//            crete.setBorne_debut_distance(borne_debut_distance);
//            crete.setBorne_fin(borne_debut);
//            crete.setBorne_fin_aval(true);
//            crete.setBorne_fin_distance(borne_debut_distance);
//            crete.setCommentaire(null);
//            crete.setContactStructure(null);
//            crete.setConventionIds(null);
//            crete.setCote(null);
//            crete.setDateMaj(LocalDateTime.MIN);
//            crete.setDate_debut(LocalDateTime.MIN);
//            crete.setDate_fin(LocalDateTime.MIN);
//            crete.setEpaisseur(epaisseur);
//            crete.setFonction(null);
//            crete.setGeometry(null);
//            crete.setListeCote(null);
//            crete.setListeFonction(null);
//            crete.setListeMateriau(null);
//            crete.setListeSource(null);
//            crete.setMateriau(null);
//            crete.setNum_couche(num_couche);
//            crete.setOrganismeStructure(null);
//            crete.setPR_debut(PR_debut);
//            crete.setPR_fin(PR_fin);
//            crete.setParent(crete);
//            crete.setPosition(null);
//            crete.setPosition_structure(null);
//            crete.setSource(null);
//            crete.setSysteme_rep_id(systeme_rep_id);
//            crete.setTroncon(null);
            
            
            
//            tronconDigue.setNom(row.getString(TronconGestionDigueColumns.NOM.toString()));
//            tronconDigue.setCommentaire(row.getString(TronconGestionDigueColumns.COMMENTAIRE.toString()));
//            if (row.getDate(TronconGestionDigueColumns.MAJ.toString()) != null) {
//                tronconDigue.setDateMaj(LocalDateTime.parse(row.getDate(TronconGestionDigueColumns.MAJ.toString()).toString(), dateTimeFormatter));
//            }
//            if (row.getDate(TronconGestionDigueColumns.DEBUT_VAL_TRONCON.toString()) != null) {
//                tronconDigue.setDate_debut(LocalDateTime.parse(row.getDate(TronconGestionDigueColumns.DEBUT_VAL_TRONCON.toString()).toString(), dateTimeFormatter));
//            }
//            if (row.getDate(TronconGestionDigueColumns.FIN_VAL_TRONCON.toString()) != null) {
//                tronconDigue.setDate_fin(LocalDateTime.parse(row.getDate(TronconGestionDigueColumns.FIN_VAL_TRONCON.toString()).toString(), dateTimeFormatter));
//            }
//
//            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
//            //tronconDigue.setId(String.valueOf(row.getString(TronconDigueColumns.ID.toString())));
//            tronconDigueIds.put(row.getInt(TronconGestionDigueColumns.ID.toString()), tronconDigue);
//
//            // Set the references.
//            tronconDigue.setDigueId(digueIds.get(row.getInt(TronconGestionDigueColumns.DIGUE.toString())).getId());
//            
//            final List<GestionTroncon> gestions = new ArrayList<>();
//            this.getGestionnaires().stream().forEach((gestion) -> {gestions.add(gestion);});
//            tronconDigue.setGestionnaires(gestions);
//            
//            tronconDigue.setTypeRive(typesRive.get(row.getInt(TronconGestionDigueColumns.TYPE_RIVE.toString())).toString());
//
//            // Set the geometry
//            tronconDigue.setGeometry(tronconDigueGeoms.get(row.getInt(TronconGestionDigueColumns.ID.toString())));
//            
//            tronconsDigues.add(tronconDigue);
        }
        return cretes;
    }

    public void removeTronconsDigues() {
        tronconDigueRepository.getAll().stream().forEach((tronconDigue) -> {
            tronconDigueRepository.remove(tronconDigue);
        });
    }

    public void importTronconsDigues() throws IOException {
        tronconGestionDigueImporter.getTronconsDigues().values().stream().forEach((tronconDigue) -> {
            tronconDigueRepository.add(tronconDigue);
            //System.out.println(tronconDigue.getGeometry().toText());
        });
    }
    
    

    public static void main(String[] args) {
                //Geotoolkit startup
                Setup.initialize(null);
                //work in lazy mode, do your best for lenient datum shift
                Hints.putSystemDefault(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);
        try {
            final ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:/symadrem/spring/import-context.xml");
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
//SYS_EVT_CRETE
//            System.out.println("=======================");
//            importer.getDatabase().getTable("SYS_EVT_CRETE").getColumns().stream().forEach((column) -> {
//                System.out.println(column.getName());
//            });
//            System.out.println("++++++++++++++++++++");

            
            importer.removeDigues();
            importer.importDigues();
            
            importer.removeOrganismes();
            importer.importOrganismes();

            importer.removeTronconsDigues();
            importer.importTronconsDigues();

        } catch (IOException ex) {
            Logger.getLogger(DbImporter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
