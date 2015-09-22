package fr.sirs.importer;

import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.linear.DigueImporter;
import fr.sirs.importer.v2.contact.OrganismeImporter;
import fr.sirs.importer.v2.contact.IntervenantImporter;
import fr.sirs.importer.v2.references.TypeCoteImporter;
import fr.sirs.importer.v2.linear.SystemeReperageBorneImporter;
import fr.sirs.importer.v2.linear.SystemeReperageImporter;
import fr.sirs.importer.v2.linear.BorneDigueImporter;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Index;
import fr.sirs.core.SirsCore;
import fr.sirs.core.component.AbstractTronconDigueRepository;
import fr.sirs.core.component.BorneDigueRepository;
import fr.sirs.core.component.DatabaseRegistry;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.documentTroncon.CoreTypeDocumentImporter;
import fr.sirs.importer.documentTroncon.PositionDocumentImporter;
import fr.sirs.importer.v2.event.EvenementHydrauliqueImporter;
import fr.sirs.importer.intervenant.OrganismeContactModifier;
import fr.sirs.importer.link.DesordreEvenementHydrauImporter;
import fr.sirs.importer.link.DesordreJournalImporter;
import fr.sirs.importer.link.ElementReseauGardienImporter;
import fr.sirs.importer.link.ElementReseauGestionnaireImporter;
import fr.sirs.importer.link.ElementReseauProprietaireImporter;
import fr.sirs.importer.link.GenericEntityLinker;
import fr.sirs.importer.link.LaisseCrueJournalImporter;
import fr.sirs.importer.link.LigneEauJournalImporter;
import fr.sirs.importer.link.MarcheFinanceurImporter;
import fr.sirs.importer.link.MarcheMaitreOeuvreImporter;
import fr.sirs.importer.link.MonteeDesEauxJournalImporter;
import fr.sirs.importer.link.PrestationDocumentImporter;
import fr.sirs.importer.link.PrestationEvenementHydrauImporter;
import fr.sirs.importer.link.PrestationIntervenantImporter;
import fr.sirs.importer.v2.references.TypeOrientationImporter;
import fr.sirs.importer.link.photo.PhotoLocaliseeEnPrImporter;
import fr.sirs.importer.link.photo.PhotoLocaliseeEnXyImporter;
import fr.sirs.importer.objet.ObjetManager;
import fr.sirs.importer.system.TypeDonneesSousGroupeImporter;
import fr.sirs.importer.v2.linear.management.GardienTronconGestionImporter;
import fr.sirs.importer.v2.linear.management.ProprietaireTronconGestionImporter;
import fr.sirs.importer.v2.linear.TronconGestionDigueImporter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.esrigeodb.GeoDBStore;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.feature.type.FeatureType;
import org.geotoolkit.feature.type.GeometryDescriptor;
import org.geotoolkit.lang.Setup;
import org.geotoolkit.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;
import org.opengis.util.GenericName;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class DbImporter {

    public static final String NULL_STRING_VALUE = "null";

    public static final String cleanNullString(String string){
        return (NULL_STRING_VALUE.equals(string) || string==null) ? "" : string;
    }

    private final ApplicationContext context;
    private final Map<Class<? extends Element>, CouchDbRepositorySupport> repositories = new HashMap<>();

    private Database accessDatabase;
    private Database accessCartoDatabase;

    private SystemeReperageImporter systemeReperageImporter;
    private OrganismeImporter organismeImporter;
    private IntervenantImporter intervenantImporter;
    private TronconGestionDigueImporter tronconGestionDigueImporter;
    private DigueImporter digueImporter;
    private BorneDigueImporter borneDigueImporter;
    private SystemeReperageBorneImporter systemeReperageBorneImporter;
    private ObjetManager objetManager;
    private GardienTronconGestionImporter tronconGestionDigueGardienImporter;
    private ProprietaireTronconGestionImporter tronconGestionDigueProprietaireImporter;
    private CoreTypeDocumentImporter typeDocumentImporter;
    private PositionDocumentImporter positionDocumentImporter;
    private EvenementHydrauliqueImporter evenementHydrauliqueImporter;
    private OrganismeContactModifier organismeDisposeIntervenantImporter;
    private TypeCoteImporter typeCoteImporter;
    private TypeDonneesSousGroupeImporter typeDonneesSousGroupeImporter;

    private TypeOrientationImporter orientationImporter;
    private DesordreEvenementHydrauImporter desordreEvenementHydrauImporter;
    private PrestationEvenementHydrauImporter prestationEvenementHydrauImporter;
    private DesordreJournalImporter desordreJournalImporter;
    private LaisseCrueJournalImporter laisseCrueJournalImporter;
    private LigneEauJournalImporter ligneEauJournalImporter;
    private MonteeDesEauxJournalImporter monteeDesEauxJournalImporter;
    private PrestationDocumentImporter prestationDocumentImporter;
    private ElementReseauGardienImporter elementReseauGardienImporter;
    private ElementReseauGestionnaireImporter elementReseauGestionnaireImporter;
    private ElementReseauProprietaireImporter elementReseauProprietaireImporter;
    private PrestationIntervenantImporter prestationIntervenantImporter;
    private MarcheFinanceurImporter marcheFinanceurImporter;
    private MarcheMaitreOeuvreImporter marcheMaitreOeuvreImporter;
    private PhotoLocaliseeEnPrImporter photoLocaliseeEnPrImporter;
    private PhotoLocaliseeEnXyImporter photoLocaliseeEnXyImporter;
    private final List<GenericEntityLinker> linkers = new ArrayList<>();

    public enum TableName{
     BORNE_DIGUE,
     BORNE_PAR_SYSTEME_REP,
     COMMUNE,
     CONVENTION,
     CONVENTION_SIGNATAIRES_PM,
     CONVENTION_SIGNATAIRES_PP,
//     DEPARTEMENT, // Plus dans le nouveau modèle
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
//     ELEMENT_RESEAU_CHEMIN_ACCES, // Plus de liens dans le nouveau modèle
     ELEMENT_RESEAU_CONDUITE_FERMEE,
     ELEMENT_RESEAU_CONVENTION,
//     ELEMENT_RESEAU_EVENEMENT_HYDRAU, // Plus de liens dans le nouveau modèle
     ELEMENT_RESEAU_GARDIEN,
     ELEMENT_RESEAU_GESTIONNAIRE,
//     ELEMENT_RESEAU_OUVERTURE_BATARDABLE, // N'existe plus dans le nouveau modèle
     ELEMENT_RESEAU_OUVRAGE_TEL_NRJ,
//     ELEMENT_RESEAU_OUVRAGE_VOIRIE, // A priori n'existe plus dans le nouveau modèle. Mais demande de confirmation car la table contient beaucoup de données entre les voiries d'une part et d'autre part des voies sur digue et des ouvrages de franchissement.
//     ELEMENT_RESEAU_OUVRAGE_VOIRIE_POINT_ACCES, // Idem que ELEMENT_RESEAU_OUVRAGE_VOIRIE
     ELEMENT_RESEAU_POINT_ACCES,
     ELEMENT_RESEAU_POMPE,
     ELEMENT_RESEAU_PROPRIETAIRE,
     ELEMENT_RESEAU_RESEAU_EAU,
//     ELEMENT_RESEAU_SERVITUDE, // Ni les parcelles cadastrales, ni les servitudes ni les liens qu'elles pouvaient entretenir n'existent dans le nouveau modèle
//     ELEMENT_RESEAU_STRUCTURE, // Plus de liens dans le nouveau modèle
     ELEMENT_RESEAU_VOIE_SUR_DIGUE,
     ELEMENT_STRUCTURE,
//     ELEMENT_STRUCTURE_ABONDANCE_ESSENCE, // Végétation (module à part)
//     ELEMENT_STRUCTURE_GARDIEN, // Pas dans le nouveau modèle
//     ELEMENT_STRUCTURE_GESTIONNAIRE, // Pas dans le nouveau modèle
//     ELEMENT_STRUCTURE_PROPRIETAIRE, // Pas dans le nouveau modèle
     EVENEMENT_HYDRAU,
//     Export_Output,
//     Export_Output_SHAPE_Index,
     GARDIEN_TRONCON_GESTION,
//     GDB_AnnoSymbols,
//     GDB_AttrRules,
//     GDB_CodedDomains,
//     GDB_DatabaseLocks,
//     GDB_DefaultValues,
//     GDB_Domains,
//     GDB_EdgeConnRules,
//     GDB_Extensions,
//     GDB_FeatureClasses,
//     GDB_FeatureDataset,
//     GDB_FieldInfo,
//     GDB_GeomColumns,
//     GDB_JnConnRules,
//     GDB_ObjectClasses,
//     GDB_RangeDomains,
//     GDB_RelClasses,
//     GDB_ReleaseInfo,
//     GDB_RelRules,
//     GDB_ReplicaDatasets,
//     GDB_Replicas,
//     GDB_SpatialRefs,
//     GDB_StringDomains,
//     GDB_Subtypes,
//     GDB_TopoClasses,
//     GDB_Topologies,
//     GDB_TopoRules,
//     GDB_UserMetadata,
//     GDB_ValidRules,
//     ILE_BANC,
//     ILE_TRONCON,
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
//     observation_urgence_carto, //  Signification ??? / A ignorer
     ORGANISME,
     ORGANISME_DISPOSE_INTERVENANT,
     ORIENTATION,
//     PARCELLE_CADASTRE, // Plus de parcelles dans le nouveau modèle
//     PARCELLE_LONGE_DIGUE, // Plus de parcelles dans le nouveau modèle
//     PHOTO_LAISSE, // Vide dans les bases de l'Isère et du Rhône. Seble obsolète en comparaison de PHOTO_LOCALISEE_EN_XY et surtout de PHOTO_LOCALISEE_EN_PR
     PHOTO_LOCALISEE_EN_PR,
     PHOTO_LOCALISEE_EN_XY,
     PRESTATION,
     PRESTATION_DOCUMENT,
     PRESTATION_EVENEMENT_HYDRAU,
     PRESTATION_INTERVENANT,
     PROFIL_EN_LONG,
     PROFIL_EN_LONG_DZ, // Même sort que PROFIL_EN_TRAVERS_DZ ?
     PROFIL_EN_LONG_EVT_HYDRAU,
     PROFIL_EN_LONG_XYZ,
     PROFIL_EN_TRAVERS,
     PROFIL_EN_TRAVERS_DESCRIPTION,
     PROFIL_EN_TRAVERS_DZ, // Ne sera probablement plus dans la v2 (à confirmer) // SI !
     PROFIL_EN_TRAVERS_EVT_HYDRAU,
//     PROFIL_EN_TRAVERS_STRUCTUREL, // Ne sera plus dans la v2
     PROFIL_EN_TRAVERS_TRONCON,
     PROFIL_EN_TRAVERS_XYZ,
     PROPRIETAIRE_TRONCON_GESTION,
//     rampes,
//     Isere,
//     riviere,
//     crete,
     RAPPORT_ETUDE,
//     REQ_ADIDR_CREATION,
//     REQ_CC_BORNE_5_EN_5_RD,
//     REQ_CC_HAUTEUR_DIGUE_SUR_TN_TMP,
//     REQ_CC_LOCALISATION_TMP,
//     REQ_CC_RAMPES_ACCES,
//     REQ_CC_TMP,
//     REQ_CEMAGREF_SENSIBILITE_EVT_HYDRAU,
//     REQ_SOGREAH_SENSIBILITE_EVT_HYDRAU,
//     RQ_CC_SONDAGES,
//     SelectedObjects,
//     Selections,
     SOURCE_INFO,
//     SYNCHRO_BD_COURANTE,
//     SYNCHRO_BD_GENEREE,
//     SYNCHRO_BD_TABLE,
//     SYNCHRO_FILTRE_TRONCON,
//     SYNCHRO_JOURNAL,
//     SYNCHRO_ORGANISME_BD,
//     SYNCHRO_SUIVI_BD,
//     SYNDICAT,
//     SYS_DONNEES_LOCALISEES_EN_PR,
     SYS_EVT_AUTRE_OUVRAGE_HYDRAULIQUE,
//     SYS_EVT_BRISE_LAME, // Dans le module "ouvrages à la mer" (2015)
     SYS_EVT_CHEMIN_ACCES,
     SYS_EVT_CONDUITE_FERMEE,
     SYS_EVT_CONVENTION,
     SYS_EVT_CRETE,
     SYS_EVT_DESORDRE,
//     SYS_EVT_DISTANCE_PIED_DE_DIGUE_TRONCON, // Dans le module "berges" (2015)
     SYS_EVT_DOCUMENT_A_GRANDE_ECHELLE,
//     SYS_EVT_DOCUMENT_MARCHE, // Hypothèse que cette table est remplacée par SYS_EVT_MARCHE
//     SYS_EVT_EMPRISE_COMMUNALE, // Inutile : toute l'information est dans TRONCON_GESTION_DIGUE_COMMUNE
//     SYS_EVT_EMPRISE_SYNDICAT,
     SYS_EVT_EPIS,
//     SYS_EVT_FICHE_INSPECTION_VISUELLE,
     SYS_EVT_FONDATION,
//     SYS_EVT_GARDIEN_TRONCON, // Inutile : toute l'information est dans GARDIEN_TRONCON_GESTION
//     SYS_EVT_ILE_TRONCON,
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
//     SYS_EVT_PHOTO_LOCALISEE_EN_PR, // Inutile : toute l'information est redondante par rapport à PHOTO_LOCALISEE_EN_PR
     SYS_EVT_PIED_DE_DIGUE,
     SYS_EVT_PIED_FRONT_FRANC_BORD,
//     SYS_EVT_PLAN_TOPO, // Vide en Isère / Inexistante dans le Rhône ?
     SYS_EVT_POINT_ACCES,
     SYS_EVT_PRESTATION,
     SYS_EVT_PROFIL_EN_LONG,
     SYS_EVT_PROFIL_EN_TRAVERS,
     SYS_EVT_PROFIL_FRONT_FRANC_BORD,
//     SYS_EVT_PROPRIETAIRE_TRONCON, // Inutile : toute l'information est dans PROPRIETAIRE_TRONCON_GESTION
     SYS_EVT_RAPPORT_ETUDES,
     SYS_EVT_RESEAU_EAU,
     SYS_EVT_RESEAU_TELECOMMUNICATION,
//     SYS_EVT_SITUATION_FONCIERE, // Plus dans le modèle
     SYS_EVT_SOMMET_RISBERME,
     SYS_EVT_STATION_DE_POMPAGE,
     SYS_EVT_TALUS_DIGUE,
     SYS_EVT_TALUS_FRANC_BORD,
     SYS_EVT_TALUS_RISBERME,
//     SYS_EVT_VEGETATION, // Végétation (module à part)
     SYS_EVT_VOIE_SUR_DIGUE,
//     SYS_IMPORT_POINTS,
//     SYS_INDEFINI,
//     SYS_OPTIONS,
//     SYS_OPTIONS_ETATS,
//     SYS_OPTIONS_REQUETES,
//     SYS_OUI_NON,
//     SYS_OUI_NON_INDEFINI,
//     SYS_PHOTO_OPTIONS,
//     SYS_RECHERCHE_MIN_MAX_PR_CALCULE,
//     SYS_REQ_Temp,
//     SYS_REQUETES,
//     SYS_REQUETES_INTERNES,
//     SYS_REQUETES_PREPROGRAMMEES,
//     SYS_RQ_EXTRAIT_DESORDRE_TGD,
//     SYS_RQ_MONTANT_PRESTATION_TGD,
//     SYS_RQ_PROPRIETAIRE_TRAVERSEE_TGD,
//     SYS_RQ_PROPRIETAIRE_TRAVERSEE_TMP,
//     SYS_RQ_SENSIBILITE_EVT_HYDRAU_TGD,
//     SYS_SEL_FE_FICHE_SUIVI_DESORDRE_TRONCON,
//     SYS_SEL_FE_FICHE_SUIVI_DESORDRE_TYPE_DESORDRE,
//     SYS_SEL_FE_RAPPORT_SYNTHE_GRAPHIQUE_LIGNE_EAU,
//     SYS_SEL_FE_RAPPORT_SYNTHE_GRAPHIQUE_PROFIL_EN_LONG,
//     SYS_SEL_FE_RAPPORT_SYNTHE_GRAPHIQUE_TRONCON,
//     SYS_SEL_FE_RAPPORT_SYNTHE_GRAPHIQUE_TYPE_ATTRIBUT,
//     SYS_SEL_FE_RAPPORT_SYNTHE_GRAPHIQUE_TYPE_DONNEE,
//     SYS_SEL_RQ_EXTRAIT_DESORDRE_TRONCON,
//     SYS_SEL_RQ_EXTRAIT_DESORDRE_TYPE_DESORDRE,
//     SYS_SEL_RQ_MONTANT_PRESTATION_TRONCON,
//     SYS_SEL_RQ_MONTANT_PRESTATION_TYPE_PRESTATION,
//     SYS_SEL_RQ_SENSIBILITE_EVT_HYDRAU_EVT_HYDRAU,
//     SYS_SEL_RQ_SENSIBILITE_EVT_HYDRAU_TRONCON,
//     SYS_SEL_TRONCON_GESTION_DIGUE,
//     SYS_SEL_TRONCON_GESTION_DIGUE_TMP,
//     SYS_SEL_TYPE_DONNEES_SOUS_GROUPE,
//     SYS_VEGETATION_TMP,
     SYSTEME_REP_LINEAIRE,
     TRONCON_GESTION_DIGUE,
     TRONCON_GESTION_DIGUE_COMMUNE,
     TRONCON_GESTION_DIGUE_GESTIONNAIRE,
//     TRONCON_GESTION_DIGUE_SITUATION_FONCIERE, // Plus dans le modèle
//     TRONCON_GESTION_DIGUE_SYNDICAT,
//     TYPE_COMPOSITION, // Pas dans le nouveau modèle.
     TYPE_CONDUITE_FERMEE,
     TYPE_CONVENTION,
     TYPE_COTE,
     TYPE_DESORDRE,
//     TYPE_DEVERS, // Semble ne plus être à jour (utilisé dans les structuresL qui ne sont plus dans le modèle)
//     TYPE_DISTANCE_DIGUE_BERGE, // Dans le module "berges" (2015)
     TYPE_DOCUMENT,
     TYPE_DOCUMENT_A_GRANDE_ECHELLE,
//     TYPE_DOCUMENT_DECALAGE, // Affichage
//     TYPE_DONNEES_GROUPE, // Redondance avec les types
     TYPE_DONNEES_SOUS_GROUPE, // Redondance avec les types
//     TYPE_DVPT_VEGETATION, // Dans le module "vegetation" (2015)
     TYPE_ELEMENT_GEOMETRIE,
     TYPE_ELEMENT_RESEAU,
//     TYPE_ELEMENT_RESEAU_COTE, // Concerne l'affichage
     TYPE_ELEMENT_STRUCTURE,
//     TYPE_ELEMENT_STRUCTURE_COTE, // Concerne l'affichage
//     TYPE_EMPRISE_PARCELLE, // Pas de parcelles cadastrales dans le nouveau modèle
     TYPE_EVENEMENT_HYDRAU,
     TYPE_FONCTION,
     TYPE_FONCTION_MO,
     TYPE_FREQUENCE_EVENEMENT_HYDRAU,
//     TYPE_GENERAL_DOCUMENT, // Plus dans le nouveau modèle
     TYPE_GLISSIERE,
     TYPE_LARGEUR_FRANC_BORD,
     TYPE_MATERIAU,
     TYPE_MOYEN_MANIP_BATARDEAUX,
     TYPE_NATURE,
     TYPE_NATURE_BATARDEAUX,
//     TYPE_ORGANISME, // Vide dans la base de l'Isère. Semble ne correspondre à rien en l'absence de données et de champ ID_TYPE_ORGANISME dans la table ORGANISME
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
//     TYPE_POSITION_SUR_DIGUE, // Semble inutilisé
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
//     TYPE_SERVITUDE, // Pas de servitudes dans le nouveau modèle
     TYPE_SEUIL,
//     TYPE_SIGNATAIRE, // Semble inutilisé dans la V1. Pas dans le modèle de la V2.
//     TYPE_SITUATION_FONCIERE, // Pas dans le nouveau modèle
     TYPE_SYSTEME_RELEVE_PROFIL,
     TYPE_URGENCE,
     TYPE_USAGE_VOIE,
//     TYPE_VEGETATION, // Végétation (module à part)
//     TYPE_VEGETATION_ABONDANCE, // Végétation (module à part)
//     TYPE_VEGETATION_ABONDANCE_BRAUN_BLANQUET, // Végétation (module à part)
//     TYPE_VEGETATION_ESSENCE, // Végétation (module à part)
//     TYPE_VEGETATION_ETAT_SANITAIRE, // Végétation (module à part)
//     TYPE_VEGETATION_STRATE_DIAMETRE, // Végétation (module à part)
//     TYPE_VEGETATION_STRATE_HAUTEUR, // Végétation (module à part)
     TYPE_VOIE_SUR_DIGUE,
     UTILISATION_CONDUITE,



     //Tables carto
//    CARTO_ILE_BANC,
//    CARTO_ILE_BANC_SHAPE_Index,
    CARTO_TRONCON_GESTION_DIGUE,
//    CARTO_TRONCON_GESTION_DIGUE_SHAPE_Index,
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

    final CouchDbConnector couchDbConnector;

    public DbImporter(final ApplicationContext applicationContext) throws IOException {
        ArgumentChecks.ensureNonNull("Application context", applicationContext);
        context = applicationContext;
        repositories.put(TronconDigue.class, applicationContext.getBean(TronconDigueRepository.class));
        repositories.put(BorneDigue.class, applicationContext.getBean(BorneDigueRepository.class));
        couchDbConnector = context.getBean(CouchDbConnector.class);
    }

    public void setDatabase(final Database accessDatabase,
            final Database accessCartoDatabase, CoordinateReferenceSystem crs) throws IOException{
        AbstractImporter.outputCrs = crs;
        this.accessDatabase=accessDatabase;
        this.accessCartoDatabase=accessCartoDatabase;

        intervenantImporter = new IntervenantImporter(accessDatabase,
                couchDbConnector);
        organismeDisposeIntervenantImporter = new OrganismeContactModifier(
                accessDatabase, couchDbConnector, intervenantImporter);
        organismeImporter = new OrganismeImporter(accessDatabase,
                couchDbConnector, organismeDisposeIntervenantImporter);

        digueImporter = new DigueImporter(accessDatabase, couchDbConnector);
        borneDigueImporter = new BorneDigueImporter(accessDatabase,
                couchDbConnector);
        systemeReperageImporter = new SystemeReperageImporter(accessDatabase,
                couchDbConnector);
        systemeReperageBorneImporter = new SystemeReperageBorneImporter(
                accessDatabase, couchDbConnector, systemeReperageImporter,
                borneDigueImporter);
        evenementHydrauliqueImporter = new EvenementHydrauliqueImporter(
                accessDatabase, couchDbConnector);
        tronconDigueGeomImporter = new TronconDigueGeomImporter(
                accessCartoDatabase, couchDbConnector);

        typeCoteImporter = new TypeCoteImporter(accessDatabase, couchDbConnector);
        tronconGestionDigueImporter = new TronconGestionDigueImporter(
                accessDatabase, couchDbConnector,
                (AbstractTronconDigueRepository) repositories.get(TronconDigue.class),
                (BorneDigueRepository) repositories.get(BorneDigue.class),
                digueImporter, tronconDigueGeomImporter, systemeReperageImporter,
                borneDigueImporter, organismeImporter);

        tronconGestionDigueGardienImporter = new GardienTronconGestionImporter(
                accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter, intervenantImporter);
        tronconGestionDigueProprietaireImporter = new ProprietaireTronconGestionImporter(
                accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter, intervenantImporter,
                organismeImporter);
        typeDocumentImporter = new CoreTypeDocumentImporter(accessDatabase,
                couchDbConnector);
        positionDocumentImporter = new PositionDocumentImporter(accessDatabase,
                couchDbConnector, tronconGestionDigueImporter, borneDigueImporter,
                intervenantImporter, organismeImporter, systemeReperageImporter,
                evenementHydrauliqueImporter, typeDocumentImporter);
        objetManager = new ObjetManager(accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter, organismeImporter,
                intervenantImporter, positionDocumentImporter.getDocumentManager().getMarcheImporter(), evenementHydrauliqueImporter, typeCoteImporter);

        orientationImporter = new TypeOrientationImporter(
                accessDatabase, couchDbConnector);

        // Linkers
        desordreEvenementHydrauImporter = new DesordreEvenementHydrauImporter(
                accessDatabase, couchDbConnector,
                objetManager.getDesordreImporter(),
                evenementHydrauliqueImporter);
        linkers.add(desordreEvenementHydrauImporter);
        prestationEvenementHydrauImporter = new PrestationEvenementHydrauImporter(
                accessDatabase, couchDbConnector,
                objetManager.getPrestationImporter(),
                evenementHydrauliqueImporter);
        linkers.add(prestationEvenementHydrauImporter);
        desordreJournalImporter = new DesordreJournalImporter(accessDatabase,
                couchDbConnector,
                objetManager.getDesordreImporter(),
                positionDocumentImporter.getDocumentManager().getJournalArticleImporter());
        linkers.add(desordreJournalImporter);
        laisseCrueJournalImporter = new LaisseCrueJournalImporter(
                accessDatabase, couchDbConnector,
                objetManager.getLaisseCrueImporter(),
                positionDocumentImporter.getDocumentManager().getJournalArticleImporter());
        linkers.add(laisseCrueJournalImporter);
        ligneEauJournalImporter = new LigneEauJournalImporter(accessDatabase,
                couchDbConnector,
                objetManager.getLigneEauImporter(),
                positionDocumentImporter.getDocumentManager().getJournalArticleImporter());
        linkers.add(ligneEauJournalImporter);
        monteeDesEauxJournalImporter = new MonteeDesEauxJournalImporter(
                accessDatabase, couchDbConnector,
                objetManager.getMonteeDesEauxImporter(),
                positionDocumentImporter.getDocumentManager().getJournalArticleImporter());
        linkers.add(monteeDesEauxJournalImporter);
        prestationDocumentImporter = new PrestationDocumentImporter(
                accessDatabase, couchDbConnector,
                objetManager.getPrestationImporter(),
                positionDocumentImporter);
        linkers.add(prestationDocumentImporter);
        elementReseauGardienImporter = new ElementReseauGardienImporter(
                accessDatabase, couchDbConnector,
                objetManager.getElementReseauImporter(),
                intervenantImporter);
        linkers.add(elementReseauGardienImporter);
        elementReseauGestionnaireImporter = new ElementReseauGestionnaireImporter(
                accessDatabase, couchDbConnector,
                objetManager.getElementReseauImporter(),
                organismeImporter);
        linkers.add(elementReseauGestionnaireImporter);
        elementReseauProprietaireImporter = new ElementReseauProprietaireImporter(
                accessDatabase, couchDbConnector,
                objetManager.getElementReseauImporter(),
                intervenantImporter, organismeImporter);
        linkers.add(elementReseauProprietaireImporter);
        prestationIntervenantImporter = new PrestationIntervenantImporter(
                accessDatabase, couchDbConnector,
                objetManager.getPrestationImporter(),
                intervenantImporter);
        linkers.add(prestationIntervenantImporter);
        marcheFinanceurImporter = new MarcheFinanceurImporter(accessDatabase,
                couchDbConnector, positionDocumentImporter.getDocumentManager().getMarcheImporter(),
                organismeImporter);
        linkers.add(marcheFinanceurImporter);
        marcheMaitreOeuvreImporter = new MarcheMaitreOeuvreImporter(
                accessDatabase, couchDbConnector,
                positionDocumentImporter.getDocumentManager().getMarcheImporter(), organismeImporter);
        linkers.add(marcheMaitreOeuvreImporter);
        typeDonneesSousGroupeImporter = new TypeDonneesSousGroupeImporter(
                accessDatabase, couchDbConnector);
        photoLocaliseeEnPrImporter = new PhotoLocaliseeEnPrImporter(
                accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                objetManager, systemeReperageImporter, borneDigueImporter,
                intervenantImporter, positionDocumentImporter, orientationImporter,
                typeCoteImporter, typeDonneesSousGroupeImporter);
        linkers.add(photoLocaliseeEnPrImporter);
        photoLocaliseeEnXyImporter = new PhotoLocaliseeEnXyImporter(
                accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                objetManager, intervenantImporter, positionDocumentImporter,
                orientationImporter, typeCoteImporter, typeDonneesSousGroupeImporter);
        linkers.add(photoLocaliseeEnXyImporter);
    }

    public Database getDatabase() {
        return this.accessDatabase;
    }

    public Database getCartoDatabase() {
        return this.accessCartoDatabase;
    }

    public CouchDbConnector getConnector(){
        return couchDbConnector;
    }

    public OrganismeImporter getOrganismeImporter(){
        return organismeImporter;
    }

    public IntervenantImporter getIntervenantImporter(){
        return intervenantImporter;
    }

    public TronconGestionDigueImporter getTronconGestionDigueImporter(){
        return tronconGestionDigueImporter;
    }

    public BorneDigueImporter getBorneDigueImporter(){
        return borneDigueImporter;
    }

    public SystemeReperageImporter getSystemeReperageImporter(){
        return systemeReperageImporter;
    }

    public PositionDocumentImporter getPositionDocumentImporter() {
        return positionDocumentImporter;
    }

    public CoreTypeDocumentImporter getTypeDocumentImporter() {
        return typeDocumentImporter;
    }

    public ObjetManager getObjetManager(){
        return objetManager;
    }

    public static CoordinateReferenceSystem IMPORT_CRS;
    public void importation(final File cartoDbFile) throws IOException, AccessDbImporterException{

        FeatureStore store;
        CoordinateReferenceSystem crs = null;
        try {
            store = new GeoDBStore("test", cartoDbFile.toURI().toURL());

            for(final GenericName name : store.getNames()){
                final FeatureType type = store.getFeatureType(name);
                GeometryDescriptor geomDesc = type.getGeometryDescriptor();
                if(geomDesc!=null){
                    crs = geomDesc.getType().getCoordinateReferenceSystem();
                    if(crs!=null) break;
                }

            }
        } catch (DataStoreException ex) {
            Logger.getLogger(DbImporter.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("C.R.S : "+crs);
        if(crs==null) try {
            crs = CRS.decode("EPSG:27593", true);
        } catch (FactoryException ex) {
            Logger.getLogger(DbImporter.class.getName()).log(Level.SEVERE, null, ex);
        }

        IMPORT_CRS=crs;
/*
        => troncons
            => digues
            => bornes
            => systemes rep
            => structures
            => organismes
            => geometries
        => bornes / systemes de repérage
        => documents
*/
        tronconGestionDigueImporter.getTronconsDigues();

        // Once TronconGestionDigue has been imported, SRs needs to be updated
        // with TronconGestionDigue IDs.
        systemeReperageImporter.setTronconGestionDigueImporter(tronconGestionDigueImporter);
        systemeReperageImporter.update();

        systemeReperageBorneImporter.getByBorneId();
        positionDocumentImporter.getPositions();
        objetManager.compute();
        objetManager.link();
        tronconGestionDigueGardienImporter.compute();
        tronconGestionDigueProprietaireImporter.compute();

        for(final GenericEntityLinker linker : linkers) linker.link();
        tronconGestionDigueImporter.update();

        // Normalement les documents et les pièces reliées aux documents ne sont
        // pas affectées par les linkers car les liaisons sont en sens unique
        // depuis les objets des troncons. Mais au cas où des associations
        // bidirectionnelles seraient ajoutées on fait un update().
//        documentImporter.update();
        evenementHydrauliqueImporter.update();
        objetManager.update();
        positionDocumentImporter.update();


        // Importation des plugins
        final Iterator<PluginImporter> pluginImporterIt = ServiceLoader.load(PluginImporter.class).iterator();
        while(pluginImporterIt.hasNext()){
            pluginImporterIt.next().importation(this);
        }
    }

    //TODO remove when import finished
    public static void main(String[] args) throws AccessDbImporterException {
                //Geotoolkit startup
                Setup.initialize(null);
                //work in lazy mode, do your best for lenient datum shift
                Hints.putSystemDefault(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);
        try (final ConfigurableApplicationContext applicationContext = new DatabaseRegistry().connectToSirsDatabase("sirs", true, false, false)) {;
            final DbImporter importer = new DbImporter(applicationContext);
            importer.setDatabase(DatabaseBuilder.open(new File("/home/samuel/Bureau/symadrem/data/SIRSDigues_ad_isere_donnees.mdb")),
                    DatabaseBuilder.open(new File("/home/samuel/Bureau/symadrem/data/SIRSDigues_ad_isere_carto.mdb")),null);

//            SirsCore.LOGGER.log(Level.FINE, "=======================");
            importer.getDatabase().getTable(TableName.CARTO_TRONCON_GESTION_DIGUE.toString()).getColumns().stream().forEach((column) -> {
                SirsCore.LOGGER.log(Level.FINE, column.getName());
            });
//            SirsCore.LOGGER.log(Level.FINE, "++++++++++++++++++++");
//
            SirsCore.LOGGER.log(Level.FINE, importer.getDatabase().getTable(TableName.CARTO_TRONCON_GESTION_DIGUE.toString()).getPrimaryKeyIndex().getName());
            for(final Index index :importer.getDatabase().getTable(TableName.ELEMENT_STRUCTURE.toString()).getIndexes()){
                System.out.println(index);
            }
//            for(final Row row : importer.getDatabase().getTable(TableName.CARTO_TRONCON_GESTION_DIGUE.toString())){
//                System.out.println(index);
//                SirsCore.LOGGER.log(Level.FINE, row.toString());
//            }
//            SirsCore.LOGGER.log(Level.FINE, "=======================");
//
//            importer.cleanDb();
//            importer.importation();

            SirsCore.LOGGER.log(Level.FINE, "fin de l'importation !");

        } catch (IOException ex) {
            Logger.getLogger(DbImporter.class.getName()).log(Level.INFO, null, ex);
        }
    }
}
