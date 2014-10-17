/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.util;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Geometry;
import fr.symadrem.sirs.core.component.DigueRepository;
import fr.symadrem.sirs.core.component.OrganismeRepository;
import fr.symadrem.sirs.core.component.TronconDigueRepository;
import fr.symadrem.sirs.core.model.Digue;
import fr.symadrem.sirs.core.model.GestionTroncon;
import fr.symadrem.sirs.core.model.Organisme;
import fr.symadrem.sirs.core.model.SystemeReperage;
import fr.symadrem.sirs.core.model.TronconDigue;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.data.shapefile.shp.ShapeHandler;
import org.geotoolkit.data.shapefile.shp.ShapeType;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.lang.Setup;
import org.geotoolkit.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import fr.symadrem.sirs.core.model.TypeRive;
import java.util.Collection;
import org.ektorp.CouchDbConnector;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class DbImporter {

    private DigueRepository digueRepository;
    private TronconDigueRepository tronconDigueRepository;
    private OrganismeRepository organismeRepository;

    private Database accessDatabase;
    private Database accessCartoDatabase;

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
     SYS_EVT_FONDATION
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
    x CARTO_ILE_BANC
    x CARTO_ILE_BANC_SHAPE_Index
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

    public DbImporter(CouchDbConnector couchDbConnector, File mdbFile) throws IOException {
        this.accessDatabase = DatabaseBuilder.open(mdbFile);
        this.dateTimeFormatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
        this.digueRepository = new DigueRepository(couchDbConnector);
        this.tronconDigueRepository = new TronconDigueRepository(couchDbConnector);
        this.organismeRepository = new OrganismeRepository(couchDbConnector);
    }
    
    public void setDatabase(final Database accessDatabase, final Database accessCartoDatabase){
        this.accessDatabase=accessDatabase;
        this.accessCartoDatabase=accessCartoDatabase;
    }

    public Database getDatabase() {
        return this.accessDatabase;
    }

    public Database getCartoDatabase() {
        return this.accessCartoDatabase;
    }

    /***************************************************************************
     DIGUE
    ----------------------------------------------------------------------------
     x ID_DIGUE
     * LIBELLE_DIGUE
     * COMMENTAIRE_DIGUE
     * DATE_DERNIERE_MAJ
     */
    private static enum DigueColumns {

        ID("ID_DIGUE"), LIBELLE("LIBELLE_DIGUE"), COMMENTAIRE("COMMENTAIRE_DIGUE"), MAJ("DATE_DERNIERE_MAJ");
        private final String column;

        private DigueColumns(final String column) {
            this.column = column;
        }

        @Override
        public String toString() {
            return this.column;
        }
    };
    
    private Map<Integer, Digue> digueIds = null;
    private Collection<Digue> getDigues() throws IOException {

        if(digueIds!=null){
            return digueIds.values();
        }
        else {
            digueIds = new HashMap<>();
            final Collection<Digue> digues = new ArrayList<>();
            final Iterator<Row> it = this.accessDatabase.getTable("DIGUE").iterator();

            while (it.hasNext()) {
                final Row row = it.next();
                final Digue digue = new Digue();
                
                digue.setLibelle(row.getString(DigueColumns.LIBELLE.toString()));
                digue.setCommentaire(row.getString(DigueColumns.COMMENTAIRE.toString()));
                if (row.getDate(DigueColumns.MAJ.toString()) != null) {
                    digue.setDateMaj(LocalDateTime.parse(row.getDate(DigueColumns.MAJ.toString()).toString(), dateTimeFormatter));
                }

                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                //digue.setId(String.valueOf(row.getInt(DigueColumns.ID.toString())));
                digueIds.put(row.getInt(DigueColumns.ID.toString()), digue);

                digues.add(digue);
            }
            return digues;
        }
    }

    public void removeDigues() {
        digueRepository.getAll().stream().forEach((digue) -> {
            digueRepository.remove(digue);
        });
    }

    public void importDigues() throws IOException {
        this.getDigues().stream().forEach((digue) -> {
            digueRepository.add(digue);
        });
    }

    /***************************************************************************
     ORGANISME.
    ----------------------------------------------------------------------------
     x ID_ORGANISME
     * RAISON_SOCIALE // Nom
     * STATUT_JURIDIQUE
     * ADRESSE_L1_ORG
     * ADRESSE_L2_ORG
     * ADRESSE_L3_ORG
     * ADRESSE_CODE_POSTAL_ORG
     * ADRESSE_NOM_COMMUNE_ORG
     * TEL_ORG
     * MAIL_ORG
     * FAX_ORG
     * DATE_DEBUT
     * DATE_FIN
     x DATE_DERNIERE_MAJ // Pas de date de mise à jour dans le modèle.
    ----------------------------------------------------------------------------
    Les adresses 1, 2 et 3 sont concaténées.
    La raison sociale devient le nom.
    La commune devient la localité.
    On n'a pas de pays dans la base.
    
    on n'a pas de date de mise à jour : est-ce normal ?
     */
    private static enum OrganismeColumns {

        ID("ID_ORGANISME"), RAISON_SOCIALE("RAISON_SOCIALE"), STATUT_JURIDIQUE("STATUT_JURIDIQUE"), 
        ADRESSE1("ADRESSE_L1_ORG"), ADRESSE2("ADRESSE_L2_ORG"), ADRESSE3("ADRESSE_L3_ORG"),
        CODE_POSTAL("ADRESSE_CODE_POSTAL_ORG"), COMMUNE("ADRESSE_NOM_COMMUNE_ORG"),
        TEL("TEL_ORG"), COURRIEL("MAIL_ORG"), FAX("FAX_ORG"),
        DEBUT("DATE_DEBUT"), FIN("DATE_FIN"), MAJ("DATE_DERNIERE_MAJ");
        private final String column;

        private OrganismeColumns(final String column) {
            this.column = column;
        }

        @Override
        public String toString() {
            return this.column;
        }
    };
    
    private Map<Integer, Organisme> organismeIds = null;
    private Collection<Organisme> getOrganismes() throws IOException {

        if(organismeIds!=null){
            return organismeIds.values();
        }
        else {
            organismeIds = new HashMap<>();
            final Collection<Organisme> organismes = new ArrayList<>();
            final Iterator<Row> it = this.accessDatabase.getTable("ORGANISME").iterator();

            while (it.hasNext()) {
                final Row row = it.next();
                final Organisme organisme = new Organisme();
                
                organisme.setNom(row.getString(OrganismeColumns.RAISON_SOCIALE.toString()));
                organisme.setStatut_juridique(row.getString(OrganismeColumns.STATUT_JURIDIQUE.toString()));
                organisme.setAdresse(row.getString(OrganismeColumns.ADRESSE1.toString())
                +row.getString(OrganismeColumns.ADRESSE2.toString())
                +row.getString(OrganismeColumns.ADRESSE3.toString()));
                organisme.setCode_postal(String.valueOf(row.getInt(OrganismeColumns.CODE_POSTAL.toString())));
                organisme.setLocalite(row.getString(OrganismeColumns.COMMUNE.toString()));
                organisme.setTelephone(row.getString(OrganismeColumns.TEL.toString()));
                organisme.setEmail(row.getString(OrganismeColumns.COURRIEL.toString()));
                organisme.setFax(row.getString(OrganismeColumns.FAX.toString()));
                if (row.getDate(OrganismeColumns.DEBUT.toString()) != null) {
                    organisme.setDate_debut(LocalDateTime.parse(row.getDate(OrganismeColumns.DEBUT.toString()).toString(), dateTimeFormatter));
                }
                if (row.getDate(OrganismeColumns.FIN.toString()) != null) {
                    organisme.setDate_fin(LocalDateTime.parse(row.getDate(OrganismeColumns.FIN.toString()).toString(), dateTimeFormatter));
                }

                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                organismeIds.put(row.getInt(OrganismeColumns.ID.toString()), organisme);

                organismes.add(organisme);
            }
            return organismes;
        }
    }

    public void removeOrganismes() {
        organismeRepository.getAll().stream().forEach((organisme) -> {
            organismeRepository.remove(organisme);
        });
    }

    public void importOrganismes() throws IOException {
        this.getOrganismes().stream().forEach((organisme) -> {
            organismeRepository.add(organisme);
        });
    }

    /***************************************************************************
     TRONCON_GESTION_DIGUE_GESTIONNAIRE.
    ----------------------------------------------------------------------------
     x ID_TRONCON_GESTION //
     * ID_ORG_GESTION // L'identifiant de gestionnaire est mappé avec les identifiants des organismes créés dans CouchDb
     * DATE_DEBUT_GESTION
     * DATE_FIN_GESTION
     x DATE_DERNIERE_MAJ // On n'a pas de date de mise à jour : est-ce normal ?
    ----------------------------------------------------------------------------
    La classe Gestionnaires est mal nommée ("Gestion" ou "EpisodeGestion" 
    conviendrait mieux.
     */
    private static enum TronconGestionDigueGestionnaireColumns {

        ID_TRONCON_GESTION("ID_TRONCON_GESTION"), 
        ID_ORG_GESTION("ID_ORG_GESTION"),
        DATE_DEBUT_GESTION("DATE_DEBUT_GESTION"), DATE_FIN_GESTION("DATE_FIN_GESTION"),
        MAJ("DATE_DERNIERE_MAJ");
        private final String column;

        private TronconGestionDigueGestionnaireColumns(final String column) {
            this.column = column;
        }

        @Override
        public String toString() {
            return this.column;
        }
    };
    
    private Map<Integer, GestionTroncon> tronconGestionDigueGestionnaireByTronconId = null;
    private Collection<GestionTroncon> getGestionnaires() throws IOException {

        if(tronconGestionDigueGestionnaireByTronconId!=null){
            return tronconGestionDigueGestionnaireByTronconId.values();
        }
        else {
            tronconGestionDigueGestionnaireByTronconId = new HashMap<>();
            final Collection<GestionTroncon> gestions = new ArrayList<>();
            final Iterator<Row> it = this.accessDatabase.getTable("TRONCON_GESTION_DIGUE_GESTIONNAIRE").iterator();

            while (it.hasNext()) {
                final Row row = it.next();
                final GestionTroncon gestion = new GestionTroncon();
                if (row.getDate(TronconGestionDigueGestionnaireColumns.DATE_DEBUT_GESTION.toString()) != null) {
                    gestion.setDate_debut(LocalDateTime.parse(row.getDate(TronconGestionDigueGestionnaireColumns.DATE_DEBUT_GESTION.toString()).toString(), dateTimeFormatter));
                }
                if (row.getDate(TronconGestionDigueGestionnaireColumns.DATE_FIN_GESTION.toString()) != null) {
                    gestion.setDate_fin(LocalDateTime.parse(row.getDate(TronconGestionDigueGestionnaireColumns.DATE_FIN_GESTION.toString()).toString(), dateTimeFormatter));
                }

                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                tronconGestionDigueGestionnaireByTronconId.put(row.getInt(TronconGestionDigueGestionnaireColumns.ID_TRONCON_GESTION.toString()), gestion);

                // Set the references.
                gestion.setGestionnaireId(organismeIds.get(row.getInt(TronconGestionDigueGestionnaireColumns.ID_ORG_GESTION.toString())).getId());

                gestions.add(gestion);
            }
            return gestions;
        }
    }
    

    /***************************************************************************
     TYPE_RIVE.
    ----------------------------------------------------------------------------
     x ID_TRONCON_GESTION //
     * ID_ORG_GESTION // L'identifiant de gestionnaire est mappé avec les identifiants des organismes créés dans CouchDb
     * DATE_DEBUT_GESTION
     * DATE_FIN_GESTION
     x DATE_DERNIERE_MAJ // On n'a pas de date de mise à jour : est-ce normal ?
    ----------------------------------------------------------------------------
    Le type de rive est représenté par une énumération.
     */
    private static enum TypeRiveColumns {
        ID("ID_TYPE_RIVE"), LIBELLE("LIBELLE_TYPE_RIVE"), MAJ("DATE_DERNIERE_MAJ");
        private final String column;

        private TypeRiveColumns(final String column) {
            this.column = column;
        }

        @Override
        public String toString() {
            return this.column;
        }
    };

    private Map<Integer, TypeRive> getTypeRive() throws IOException {

        final Map<Integer, TypeRive> typesRive = new HashMap<>();
        final Iterator<Row> it = this.accessDatabase.getTable("TYPE_RIVE").iterator();
        
        while (it.hasNext()) {
            final Row row = it.next();
            typesRive.put(row.getInt(String.valueOf(TypeRiveColumns.ID.toString())),
                        TypeRive.toTypeRive(row.getString(TypeRiveColumns.LIBELLE.toString())));
        }
        return typesRive;
    }
    
    /*==========================================================================
     CARTO_TRONCON_GESTION_DIGUE.
    ----------------------------------------------------------------------------
     x OBJECTID
     * SHAPE
     x OBJECTID_old
     x SHAPE_Leng
     x SHAPE_Length
     * ID_TRONCON_GESTION
     x LONGUEUR
    ----------------------------------------------------------------------------
    TODO : probleme de reprojection
     */
    private static enum CartoTronconGestionDigueColumns {

        ID("ID_TRONCON_GESTION"), SHAPE("SHAPE");
        private final String column;

        private CartoTronconGestionDigueColumns(final String column) {
            this.column = column;
        }

        @Override
        public String toString() {
            return this.column;
        }
    };

    private Map<Integer, Geometry> getTronconDigueGeoms() throws IOException {
        final Map<Integer, Geometry> tronconDigueGeom = new HashMap<>();
        final Iterator<Row> it = this.accessCartoDatabase.getTable("CARTO_TRONCON_GESTION_DIGUE").iterator();

        while (it.hasNext()) {
            try {
                final Row row = it.next();
                final TronconDigue tronconDigue = new TronconDigue();

                final byte[] bytes = row.getBytes(CartoTronconGestionDigueColumns.SHAPE.toString());
                final ByteBuffer bb = ByteBuffer.wrap(bytes);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                final int id = bb.getInt();
                final ShapeType shapeType = ShapeType.forID(id);
                final ShapeHandler handler = shapeType.getShapeHandler(false);
                Geometry geom = (Geometry) handler.read(bb, shapeType);

                
//                final CoordinateReferenceSystem crsData = CRS.parseWKT("PROJCS[\"NTF_Lambert_Zone_III\",GEOGCS[\"GCS_NTF\",DATUM[\"D_NTF\",SPHEROID[\"Clarke_1880_IGN\",6378249.2,293.46602]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],PROJECTION[\"Lambert_Conformal_Conic\"],PARAMETER[\"False_Easting\",600000.0],PARAMETER[\"False_Northing\",200000.0],PARAMETER[\"Central_Meridian\",2.3372291667],PARAMETER[\"Standard_Parallel_1\",43.1992913889],PARAMETER[\"Standard_Parallel_2\",44.9960938889],PARAMETER[\"Scale_Factor\",1.0],PARAMETER[\"Latitude_Of_Origin\",44.1],UNIT[\"Meter\",1.0]]");
                
                final MathTransform lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), CRS.decode("EPSG:2154"),true);
                geom = JTS.transform(geom, lambertToRGF);

                tronconDigueGeom.put(row.getInt(String.valueOf(CartoTronconGestionDigueColumns.ID.toString())),
                        geom);
            } catch (FactoryException ex) {
                Logger.getLogger(DbImporter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (DataStoreException ex) {
                Logger.getLogger(DbImporter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MismatchedDimensionException ex) {
                Logger.getLogger(DbImporter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformException ex) {
                Logger.getLogger(DbImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return tronconDigueGeom;
    }
    
    /*==========================================================================
    SYSTEME_REP_LINEAIRE
    ----------------------------------------------------------------------------
    ID_SYSTEME_REP
    ID_TRONCON_GESTION
    LIBELLE_SYSTEME_REP
    COMMENTAIRE_SYSTEME_REP
    DATE_DERNIERE_MAJ
    ----------------------------------------------------------------------------
    Remarque : pas de date de mise à jour dans le modèle mais des dates de début
    et de fin qui ne se retrouvent pas dans la base.
    */
    private static enum SystemeRepLineaireColumns {

        ID("ID_SYSTEME_REP"), ID_TRONCON("ID_TRONCON_GESTION"), LIBELLE("LIBELLE_SYSTEME_REP"), 
        COMMENTAIRE("COMMENTAIRE_SYSTEME_REP"), MAJ("DATE_DERNIERE_MAJ");
        private final String column;

        private SystemeRepLineaireColumns(final String column) {
            this.column = column;
        }

        @Override
        public String toString() {
            return this.column;
        }
    };
    
    private Map<Integer, SystemeReperage> systemeRepLineaire = null;
    private Map<Integer, SystemeReperage> getSystemeRepLineaire() throws IOException {

        if(systemeRepLineaire!=null){
            return systemeRepLineaire;
        }
        else {    
            systemeRepLineaire = new HashMap<>();
            final Iterator<Row> it = this.accessDatabase.getTable("SYSTEME_REP_LINEAIRE").iterator();

            while (it.hasNext()) {
                final Row row = it.next();
                final SystemeReperage systemeReperage = new SystemeReperage();
                systemeReperage.setNom(row.getString(SystemeRepLineaireColumns.LIBELLE.toString()));
                systemeReperage.setCommentaire(row.getString(SystemeRepLineaireColumns.COMMENTAIRE.toString()));

                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                systemeRepLineaire.put(row.getInt(SystemeRepLineaireColumns.ID.toString()), systemeReperage);
            }
            return systemeRepLineaire;
        }
    }
    

    /*==========================================================================
     TRONCON_GESTION_DIGUE.
    ----------------------------------------------------------------------------
     x ID_TRONCON_GESTION
     x ID_ORG_GESTIONNAIRE // Dans la table TRONCON_GESTION_DIGUE_GESTIONNAIRE qui contient l'historique des gestionnaires.
     * ID_DIGUE
     * ID_TYPE_RIVE // On part a priori sur une enumeration statique.
     * DATE_DEBUT_VAL_TRONCON
     * DATE_FIN_VAL_TRONCON
     * NOM_TRONCON_GESTION
     * COMMENTAIRE_TRONCON
     x DATE_DEBUT_VAL_GESTIONNAIRE_D // Dans la table TRONCON_GESTION_DIGUE_GESTIONNAIRE qui contient l'historique des gestionnaires.
     x DATE_FIN_VAL_GESTIONNAIRE_D // Dans la table TRONCON_GESTION_DIGUE_GESTIONNAIRE qui contient l'historique des gestionnaires.
     ID_SYSTEME_REP_DEFAUT
     x LIBELLE_TRONCON_GESTION // Les libellés sont nulls et sont appelés à dispararaitre de la nouvelle base.
     * DATE_DERNIERE_MAJ
    ----------------------------------------------------------------------------
     * TODO : s'occuper du lien avec les gestionnaires.
     * TODO : s'occuper du lien avec les rives.
     TODO : s'occuper du lien avec les systèmes de repérage.
     */
    private static enum TronconGestionDigueColumns {

        ID("ID_TRONCON_GESTION"), GESTIONNAIRE("ID_ORG_GESTIONNAIRE"), DIGUE("ID_DIGUE"), TYPE_RIVE("ID_TYPE_RIVE"),
        DEBUT_VAL_TRONCON("DATE_DEBUT_VAL_TRONCON"), FIN_VAL_TRONCON("DATE_FIN_VAL_TRONCON"),
        NOM("NOM_TRONCON_GESTION"), COMMENTAIRE("COMMENTAIRE_TRONCON"),
        DEBUT_VAL_GESTIONNAIRE("DATE_DEBUT_VAL_GESTIONNAIRE_D"), FIN_VAL_GESTIONNAIRE("DATE_FIN_VAL_GESTIONNAIRE_D"), 
        SYSTEME_REP("ID_SYSTEME_REP_DEFAUT"), MAJ("DATE_DERNIERE_MAJ");
        private final String column;

        private TronconGestionDigueColumns(final String column) {
            this.column = column;
        }

        @Override
        public String toString() {
            return this.column;
        }
    };
    
    private final Map<Integer, TronconDigue> tronconDigueIds = new HashMap<>();

    private List<TronconDigue> getTronconsDigues() throws IOException {

        final List<TronconDigue> tronconsDigues = new ArrayList<>();
        final Iterator<Row> it = this.accessDatabase.getTable("TRONCON_GESTION_DIGUE").iterator();
        
        final Map<Integer, Geometry> tronconDigueGeoms = this.getTronconDigueGeoms();
        final Map<Integer, TypeRive> typesRive = this.getTypeRive();
        final Map<Integer, SystemeReperage> systemesRep = this.getSystemeRepLineaire();
        
        while (it.hasNext()) {
            final Row row = it.next();
            final TronconDigue tronconDigue = new TronconDigue();
            tronconDigue.setNom(row.getString(TronconGestionDigueColumns.NOM.toString()));
            tronconDigue.setCommentaire(row.getString(TronconGestionDigueColumns.COMMENTAIRE.toString()));
            if (row.getDate(TronconGestionDigueColumns.MAJ.toString()) != null) {
                tronconDigue.setDateMaj(LocalDateTime.parse(row.getDate(TronconGestionDigueColumns.MAJ.toString()).toString(), dateTimeFormatter));
            }
            if (row.getDate(TronconGestionDigueColumns.DEBUT_VAL_TRONCON.toString()) != null) {
                tronconDigue.setDate_debut(LocalDateTime.parse(row.getDate(TronconGestionDigueColumns.DEBUT_VAL_TRONCON.toString()).toString(), dateTimeFormatter));
            }
            if (row.getDate(TronconGestionDigueColumns.FIN_VAL_TRONCON.toString()) != null) {
                tronconDigue.setDate_fin(LocalDateTime.parse(row.getDate(TronconGestionDigueColumns.FIN_VAL_TRONCON.toString()).toString(), dateTimeFormatter));
            }

            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            //tronconDigue.setId(String.valueOf(row.getString(TronconDigueColumns.ID.toString())));
            tronconDigueIds.put(row.getInt(TronconGestionDigueColumns.ID.toString()), tronconDigue);

            // Set the references.
            tronconDigue.setDigueId(digueIds.get(row.getInt(TronconGestionDigueColumns.DIGUE.toString())).getId());
            
            final List<GestionTroncon> gestions = new ArrayList<>();
            this.getGestionnaires().stream().forEach((gestion) -> {gestions.add(gestion);});
            tronconDigue.setGestionnaires(gestions);
            
            tronconDigue.setTypeRive(typesRive.get(row.getInt(TronconGestionDigueColumns.TYPE_RIVE.toString())).toString());

            // Set the geometry
            tronconDigue.setGeometry(tronconDigueGeoms.get(row.getInt(TronconGestionDigueColumns.ID.toString())));
            
            tronconsDigues.add(tronconDigue);
        }
        return tronconsDigues;
    }

    public void removeTronconsDigues() {
        tronconDigueRepository.getAll().stream().forEach((tronconDigue) -> {
            tronconDigueRepository.remove(tronconDigue);
        });
    }

    public void importTronconsDigues() throws IOException {
        this.getTronconsDigues().stream().forEach((tronconDigue) -> {
            tronconDigueRepository.add(tronconDigue);
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
//
//            System.out.println("=======================");
//            importer.getDatabase().getTable("SYSTEME_REP_LINEAIRE").getColumns().stream().forEach((column) -> {
//                System.out.println(column.getName());
//            });
//            System.out.println("++++++++++++++++++++");
//
//            System.out.println("=======================");
//            importer.getDatabase().getTable("TRONCON_GESTION_DIGUE").getColumns().stream().forEach((column) -> {
//                System.out.println(column.getName());
//            });
//            System.out.println("++++++++++++++++++++");
//
//            System.out.println("=======================");
//            importer.getDatabase().getTable("ORGANISME").getColumns().stream().forEach((column) -> {
//                System.out.println(column.getName());
//            });
//            System.out.println("++++++++++++++++++++");
//
//            System.out.println("=======================");
//            importer.getDatabase().getTable("INTERVENANT").getColumns().stream().forEach((column) -> {
//                System.out.println(column.getName());
//            });
//            System.out.println("++++++++++++++++++++");
//
//            System.out.println("=======================");
//            importer.getDatabase().getTable("SYNDICAT").getColumns().stream().forEach((column) -> {
//                System.out.println(column.getName());
//            });
//            System.out.println("++++++++++++++++++++");

            importer.removeDigues();
            importer.importDigues();
            
            importer.removeOrganismes();
            importer.importOrganismes();

            importer.removeTronconsDigues();
            importer.importTronconsDigues();
            importer.getTronconsDigues();

        } catch (IOException ex) {
            Logger.getLogger(DbImporter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
