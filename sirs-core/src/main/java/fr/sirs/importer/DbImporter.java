package fr.sirs.importer;

import fr.sirs.importer.troncon.TronconGestionDigueImporter;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.CouchDBInit;
import fr.sirs.core.Repository;
import fr.sirs.core.component.ArticleJournalRepository;
import fr.sirs.core.component.BorneDigueRepository;
import fr.sirs.core.component.ContactRepository;
import fr.sirs.core.component.ConventionRepository;
import fr.sirs.core.component.DigueRepository;
import fr.sirs.core.component.DocumentRepository;
import fr.sirs.core.component.EvenementHydrauliqueRepository;
import fr.sirs.core.component.MarcheRepository;
import fr.sirs.core.component.OrganismeRepository;
import fr.sirs.core.component.ProfilLongRepository;
import fr.sirs.core.component.ProfilTraversRepository;
import fr.sirs.core.component.RapportEtudeRepository;
import fr.sirs.core.component.RefConduiteFermeeRepository;
import fr.sirs.core.component.RefConventionRepository;
import fr.sirs.core.component.RefCoteRepository;
import fr.sirs.core.component.RefEcoulementRepository;
import fr.sirs.core.component.RefEvenementHydrauliqueRepository;
import fr.sirs.core.component.RefFonctionRepository;
import fr.sirs.core.component.RefFrequenceEvenementHydrauliqueRepository;
import fr.sirs.core.component.RefImplantationRepository;
import fr.sirs.core.component.RefLargeurFrancBordRepository;
import fr.sirs.core.component.RefMateriauRepository;
import fr.sirs.core.component.RefMoyenManipBatardeauxRepository;
import fr.sirs.core.component.RefNatureBatardeauxRepository;
import fr.sirs.core.component.RefNatureRepository;
import fr.sirs.core.component.RefOrientationOuvrageRepository;
import fr.sirs.core.component.RefOrientationVentRepository;
import fr.sirs.core.component.RefOrigineProfilLongRepository;
import fr.sirs.core.component.RefOrigineProfilTraversRepository;
import fr.sirs.core.component.RefOuvrageFranchissementRepository;
import fr.sirs.core.component.RefOuvrageHydrauliqueAssocieRepository;
import fr.sirs.core.component.RefOuvrageParticulierRepository;
import fr.sirs.core.component.RefOuvrageTelecomEnergieRepository;
import fr.sirs.core.component.RefOuvrageVoirieRepository;
import fr.sirs.core.component.RefPositionProfilLongSurDigueRepository;
import fr.sirs.core.component.RefPositionRepository;
import fr.sirs.core.component.RefPrestationRepository;
import fr.sirs.core.component.RefProfilFrancBordRepository;
import fr.sirs.core.component.RefRapportEtudeRepository;
import fr.sirs.core.component.RefReferenceHauteurRepository;
import fr.sirs.core.component.RefReseauHydroCielOuvertRepository;
import fr.sirs.core.component.RefReseauTelecomEnergieRepository;
import fr.sirs.core.component.RefRevetementRepository;
import fr.sirs.core.component.RefRiveRepository;
import fr.sirs.core.component.RefSeuilRepository;
import fr.sirs.core.component.RefSourceRepository;
import fr.sirs.core.component.RefSystemeReleveProfilRepository;
import fr.sirs.core.component.RefTypeDesordreRepository;
import fr.sirs.core.component.RefTypeDocumentRepository;
import fr.sirs.core.component.RefTypeGlissiereRepository;
import fr.sirs.core.component.RefTypeProfilTraversRepository;
import fr.sirs.core.component.RefUsageVoieRepository;
import fr.sirs.core.component.RefUtilisationConduiteRepository;
import fr.sirs.core.component.RefVoieDigueRepository;
import fr.sirs.core.component.SystemeReperageRepository;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.importer.evenementHydraulique.EvenementHydrauliqueImporter;
import fr.sirs.importer.intervenant.OrganismeDisposeIntervenantImporter;
import fr.sirs.importer.link.DesordreEvenementHydrauImporter;
import fr.sirs.importer.link.DesordreJournalImporter;
import fr.sirs.importer.link.ElementReseauConventionImporter;
import fr.sirs.importer.link.ElementReseauGardienImporter;
import fr.sirs.importer.link.ElementReseauGestionnaireImporter;
import fr.sirs.importer.link.ElementReseauProprietaireImporter;
import fr.sirs.importer.link.ElementStructureGardienImporter;
import fr.sirs.importer.link.ElementStructureGestionnaireImporter;
import fr.sirs.importer.link.ElementStructureProprietaireImporter;
import fr.sirs.importer.link.GenericEntityLinker;
import fr.sirs.importer.link.LaisseCrueJournalImporter;
import fr.sirs.importer.link.LigneEauJournalImporter;
import fr.sirs.importer.link.MarcheFinanceurImporter;
import fr.sirs.importer.link.MarcheMaitreOeuvreImporter;
import fr.sirs.importer.link.MonteeDesEauxJournalImporter;
import fr.sirs.importer.link.PrestationDocumentImporter;
import fr.sirs.importer.link.PrestationEvenementHydrauImporter;
import fr.sirs.importer.link.PrestationIntervenantImporter;
import fr.sirs.importer.link.photo.PhotoLocaliseeEnPrImporter;
import fr.sirs.importer.theme.document.DocumentImporter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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
    
    public static final String NULL_STRING_VALUE = "null";
    
    public static final String cleanNullString(String string){
        return (NULL_STRING_VALUE.equals(string) || string==null) ? "" : string;
    }
    
    private final CouchDbConnector couchDbConnector;

    private final DigueRepository digueRepository;
    private final TronconDigueRepository tronconDigueRepository;
    private final OrganismeRepository organismeRepository;
    private final ContactRepository contactRepository;
    private final SystemeReperageRepository systemeReperageRepository;
    private final BorneDigueRepository borneDigueRepository;
    private final RefRiveRepository refRiveRepository;
    private final DocumentRepository documentRepository;
    private final ConventionRepository conventionRepository; 
    private final RefConventionRepository refConventionRepository;
    private final ProfilTraversRepository profilTraversRepository;
    private final RefTypeProfilTraversRepository refTypeProfilTraversRepository;
    private final RefTypeDesordreRepository refTypeDesordreRepository;
    private final RefSourceRepository refSourceRepository;
    private final RefCoteRepository refCoteRepository;
    private final RefPositionRepository refPositionRepository;
    private final RefEvenementHydrauliqueRepository refEvenementHydrauliqueRepository;
    private final EvenementHydrauliqueRepository evenementHydrauliqueRepository;
    private final RefFrequenceEvenementHydrauliqueRepository refFrequenceEvenementHydrauliqueRepository;
    private final RefSystemeReleveProfilRepository refSystemeReleveProfilRepository;
    private final RefRapportEtudeRepository refRapportEtudeRepository;
    private final RapportEtudeRepository rapportEtudeRepository;
    private final RefMateriauRepository refMateriauRepository;
    private final RefNatureRepository refNatureRepository;
    private final RefFonctionRepository refFonctionRepository;
    private final RefOrigineProfilTraversRepository refOrigineProfilTraversRepository;
    private final RefTypeDocumentRepository refTypeDocumentRepository;
    private final ProfilLongRepository profilLongRepository;
    private final RefPositionProfilLongSurDigueRepository refPositionProfilLongSurDigueRepository;
    private final RefOrigineProfilLongRepository refOrigineProfilLongRepository;
    private final ArticleJournalRepository articleJournalRepository;
    private final RefLargeurFrancBordRepository refLargeurFrancBordRepository;
    private final RefProfilFrancBordRepository refProfilFrancBordRepository;
    private final RefEcoulementRepository refEcoulementRepository;
    private final RefImplantationRepository refImplantationRepository;
    private final RefConduiteFermeeRepository refConduiteFermeeRepository;
    private final RefUtilisationConduiteRepository refUtilisationConduiteRepository;
    private final RefReseauTelecomEnergieRepository refReseauTelecomEnergieRepository;
    private final RefOuvrageTelecomEnergieRepository refOuvrageTelecomEnergieRepository;
    private final RefOuvrageHydrauliqueAssocieRepository refOuvrageHydrauliqueAssocieRepository;
    private final RefUsageVoieRepository refUsageVoieRepository;
    private final RefRevetementRepository refRevetementRepository;
    private final RefOrientationOuvrageRepository refOrientationOuvrageRepository;
    private final RefOuvrageFranchissementRepository refOuvrageFranchissementRepository;
    private final RefVoieDigueRepository refVoieDigueRepository;
    private final RefOuvrageVoirieRepository refOuvrageVoirieRepository;
    private final RefReseauHydroCielOuvertRepository refReseauHydroCielOuvertRepository;
    private final RefOuvrageParticulierRepository refOuvrageParticulierRepository;
    private final RefNatureBatardeauxRepository refNatureBatardeauxRepository;
    private final RefMoyenManipBatardeauxRepository refMoyenManipBatardeauxRepository;
    private final RefSeuilRepository refSeuilRepository;
    private final RefTypeGlissiereRepository refTypeGlissiereRepository;
    private final RefReferenceHauteurRepository refReferenceHauteurRepository;
    private final RefPrestationRepository refPrestationRepository;
    private final MarcheRepository marcheRepository;
    private final RefOrientationVentRepository refOrientationVentRepository;
    private final List<Repository> repositories = new ArrayList<>();

    private Database accessDatabase;
    private Database accessCartoDatabase;
    
    private TronconDigueGeomImporter tronconDigueGeomImporter;
    private SystemeReperageImporter systemeReperageImporter;
    private OrganismeImporter organismeImporter;
    private IntervenantImporter intervenantImporter;
    private TronconGestionDigueImporter tronconGestionDigueImporter;
    private DigueImporter digueImporter;
    private BorneDigueImporter borneDigueImporter;
    private SystemeReperageBorneImporter systemeReperageBorneImporter;
    private DocumentImporter documentImporter;
    private EvenementHydrauliqueImporter evenementHydrauliqueImporter;
    private OrganismeDisposeIntervenantImporter organismeDisposeIntervenantImporter;
    
    private DesordreEvenementHydrauImporter desordreEvenementHydrauImporter;
    private PrestationEvenementHydrauImporter prestationEvenementHydrauImporter;
    private DesordreJournalImporter desordreJournalImporter;
    private ElementReseauConventionImporter elementReseauConventionImporter;
    private LaisseCrueJournalImporter laisseCrueJournalImporter;
    private LigneEauJournalImporter ligneEauJournalImporter;
    private MonteeDesEauxJournalImporter monteeDesEauxJournalImporter;
    private PrestationDocumentImporter prestationDocumentImporter;
    private ElementReseauGardienImporter elementReseauGardienImporter;
    private ElementReseauGestionnaireImporter elementReseauGestionnaireImporter;
    private ElementReseauProprietaireImporter elementReseauProprietaireImporter;
    private ElementStructureGardienImporter elementStructureGardienImporter;
    private ElementStructureGestionnaireImporter elementStructureGestionnaireImporter;
    private ElementStructureProprietaireImporter elementStructureProprietaireImporter;
    private PrestationIntervenantImporter prestationIntervenantImporter;
    private MarcheFinanceurImporter marcheFinanceurImporter;
    private MarcheMaitreOeuvreImporter marcheMaitreOeuvreImporter;
    private PhotoLocaliseeEnPrImporter photoLocaliseeEnPrImporter;
    private List<GenericEntityLinker> linkers = new ArrayList<>();

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
     ELEMENT_STRUCTURE_GARDIEN,
     ELEMENT_STRUCTURE_GESTIONNAIRE,
     ELEMENT_STRUCTURE_PROPRIETAIRE,
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
//     observation_urgence_carto, //  Signification ???
     ORGANISME,
     ORGANISME_DISPOSE_INTERVENANT,
     ORIENTATION,
//     PARCELLE_CADASTRE, // Plus de parcelles dans le nouveau modèle
//     PARCELLE_LONGE_DIGUE, // Plus de parcelles dans le nouveau modèle
//     PHOTO_LAISSE,
     PHOTO_LOCALISEE_EN_PR,
//     PHOTO_LOCALISEE_EN_XY,
     PRESTATION,
     PRESTATION_DOCUMENT,
     PRESTATION_EVENEMENT_HYDRAU,
     PRESTATION_INTERVENANT,
     PROFIL_EN_LONG,
//     PROFIL_EN_LONG_DZ, // Même sort que PROFIL_EN_TRAVERS_DZ ?
     PROFIL_EN_LONG_EVT_HYDRAU,
     PROFIL_EN_LONG_XYZ,
     PROFIL_EN_TRAVERS,
     PROFIL_EN_TRAVERS_DESCRIPTION,
//     PROFIL_EN_TRAVERS_DZ, // Ne sera probablement plus dans la v2 (à confirmer)
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
//     SYS_EVT_EMPRISE_COMMUNALE,
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
//     SYS_EVT_PHOTO_LOCALISEE_EN_PR,
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
//     TYPE_DEVERS,
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
//     TYPE_POSITION_SUR_DIGUE,
     TYPE_PRESTATION,
     TYPE_PROFIL_EN_TRAVERS,
     TYPE_PROFIL_FRANC_BORD,
//     TYPE_PROPRIETAIRE,
     TYPE_RAPPORT_ETUDE,
     TYPE_REF_HEAU,
     TYPE_RESEAU_EAU,
     TYPE_RESEAU_TELECOMMUNIC,
     TYPE_REVETEMENT,
     TYPE_RIVE,
//     TYPE_SERVITUDE, // Pas de servitudes dans le nouveau modèle
     TYPE_SEUIL,
//     TYPE_SIGNATAIRE, // Semble inutilisé dans la V1. Pas dans le modèle de la V2.
//     TYPE_SITUATION_FONCIERE,
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

    public DbImporter(final CouchDbConnector couchDbConnector) throws IOException {
        this.couchDbConnector = couchDbConnector;
        digueRepository = new DigueRepository(couchDbConnector); 
        repositories.add(digueRepository);
        tronconDigueRepository = new TronconDigueRepository(couchDbConnector); 
        repositories.add(tronconDigueRepository);
        organismeRepository = new OrganismeRepository(couchDbConnector); 
        repositories.add(organismeRepository);
        systemeReperageRepository = new SystemeReperageRepository(
                couchDbConnector); 
        repositories.add(systemeReperageRepository);
        borneDigueRepository = new BorneDigueRepository(couchDbConnector); 
        repositories.add(borneDigueRepository);
        refRiveRepository = new RefRiveRepository(couchDbConnector);
        repositories.add(refRiveRepository);
        documentRepository = new DocumentRepository(couchDbConnector);
        repositories.add(documentRepository);
        conventionRepository = new ConventionRepository(couchDbConnector);
        repositories.add(conventionRepository);
        refConventionRepository = new RefConventionRepository(couchDbConnector);
        repositories.add(refConventionRepository);
        refTypeDesordreRepository = new RefTypeDesordreRepository(couchDbConnector);
        repositories.add(refTypeDesordreRepository);
        refSourceRepository = new RefSourceRepository(couchDbConnector);
        repositories.add(refSourceRepository);
        refCoteRepository = new RefCoteRepository(couchDbConnector);
        repositories.add(refCoteRepository);
        refPositionRepository = new RefPositionRepository(couchDbConnector);
        repositories.add(refPositionRepository);
        refEvenementHydrauliqueRepository = new RefEvenementHydrauliqueRepository(couchDbConnector);
        repositories.add(refEvenementHydrauliqueRepository);
        refFrequenceEvenementHydrauliqueRepository = new RefFrequenceEvenementHydrauliqueRepository(couchDbConnector);
        repositories.add(refFrequenceEvenementHydrauliqueRepository);
        evenementHydrauliqueRepository = new EvenementHydrauliqueRepository(
                couchDbConnector);
        repositories.add(evenementHydrauliqueRepository);
        profilTraversRepository = new ProfilTraversRepository(couchDbConnector);
        repositories.add(profilTraversRepository);
        refTypeProfilTraversRepository = new RefTypeProfilTraversRepository(
                couchDbConnector);
        repositories.add(refTypeProfilTraversRepository);
        refSystemeReleveProfilRepository = new RefSystemeReleveProfilRepository(
                couchDbConnector);
        repositories.add(refSystemeReleveProfilRepository);
        refRapportEtudeRepository = new RefRapportEtudeRepository(couchDbConnector);
        repositories.add(refRapportEtudeRepository);
        rapportEtudeRepository = new RapportEtudeRepository(couchDbConnector);
        repositories.add(rapportEtudeRepository);
        refMateriauRepository = new RefMateriauRepository(couchDbConnector);
        repositories.add(refMateriauRepository);
        refNatureRepository = new RefNatureRepository(couchDbConnector);
        repositories.add(refNatureRepository);
        contactRepository = new ContactRepository(couchDbConnector);
        repositories.add(contactRepository);
        refFonctionRepository = new RefFonctionRepository(couchDbConnector);
        repositories.add(refFonctionRepository);
        refOrigineProfilTraversRepository = new RefOrigineProfilTraversRepository(couchDbConnector);
        repositories.add(refOrigineProfilTraversRepository);
        refTypeDocumentRepository = new RefTypeDocumentRepository(couchDbConnector);
        repositories.add(refTypeDocumentRepository);
        profilLongRepository = new ProfilLongRepository(couchDbConnector);
        repositories.add(profilLongRepository);
        refPositionProfilLongSurDigueRepository = new RefPositionProfilLongSurDigueRepository(couchDbConnector);
        repositories.add(refPositionProfilLongSurDigueRepository);
        refOrigineProfilLongRepository = new RefOrigineProfilLongRepository(couchDbConnector);
        repositories.add(refOrigineProfilLongRepository);
        articleJournalRepository = new ArticleJournalRepository(couchDbConnector);
        repositories.add(articleJournalRepository);
        refLargeurFrancBordRepository = new RefLargeurFrancBordRepository(couchDbConnector);
        repositories.add(refLargeurFrancBordRepository);
        refProfilFrancBordRepository = new RefProfilFrancBordRepository(couchDbConnector);
        repositories.add(refProfilFrancBordRepository);
        refEcoulementRepository = new RefEcoulementRepository(couchDbConnector);
        repositories.add(refEcoulementRepository);
        refImplantationRepository = new RefImplantationRepository(couchDbConnector);
        repositories.add(refImplantationRepository);
        refConduiteFermeeRepository = new RefConduiteFermeeRepository(couchDbConnector);
        repositories.add(refConduiteFermeeRepository);
        refUtilisationConduiteRepository = new RefUtilisationConduiteRepository(couchDbConnector);
        repositories.add(refUtilisationConduiteRepository);
        refReseauTelecomEnergieRepository = new RefReseauTelecomEnergieRepository(couchDbConnector);
        repositories.add(refReseauTelecomEnergieRepository);
        refOuvrageTelecomEnergieRepository = new RefOuvrageTelecomEnergieRepository(couchDbConnector);
        repositories.add(refOuvrageTelecomEnergieRepository);
        refOuvrageHydrauliqueAssocieRepository = new RefOuvrageHydrauliqueAssocieRepository(couchDbConnector);
        repositories.add(refOuvrageHydrauliqueAssocieRepository);
        refUsageVoieRepository = new RefUsageVoieRepository(couchDbConnector);
        repositories.add(refUsageVoieRepository);
        refRevetementRepository = new RefRevetementRepository(couchDbConnector);
        repositories.add(refRevetementRepository);
        refOrientationOuvrageRepository = new RefOrientationOuvrageRepository(couchDbConnector);
        repositories.add(refOrientationOuvrageRepository);
        refOuvrageFranchissementRepository = new RefOuvrageFranchissementRepository(couchDbConnector);
        repositories.add(refOuvrageFranchissementRepository);
        refVoieDigueRepository = new RefVoieDigueRepository(couchDbConnector);
        repositories.add(refVoieDigueRepository);
        refOuvrageVoirieRepository = new RefOuvrageVoirieRepository(couchDbConnector);
        repositories.add(refOuvrageVoirieRepository);
        refReseauHydroCielOuvertRepository = new RefReseauHydroCielOuvertRepository(couchDbConnector);
        repositories.add(refReseauHydroCielOuvertRepository);
        refOuvrageParticulierRepository = new RefOuvrageParticulierRepository(couchDbConnector);
        repositories.add(refOuvrageParticulierRepository);
        refNatureBatardeauxRepository = new RefNatureBatardeauxRepository(couchDbConnector);
        repositories.add(refNatureBatardeauxRepository);
        refMoyenManipBatardeauxRepository = new RefMoyenManipBatardeauxRepository(couchDbConnector);
        repositories.add(refMoyenManipBatardeauxRepository);
        refSeuilRepository = new RefSeuilRepository(couchDbConnector);
        repositories.add(refSeuilRepository);
        refTypeGlissiereRepository = new RefTypeGlissiereRepository(couchDbConnector);
        repositories.add(refTypeGlissiereRepository);
        refReferenceHauteurRepository = new RefReferenceHauteurRepository(couchDbConnector);
        repositories.add(refReferenceHauteurRepository);
        refPrestationRepository = new RefPrestationRepository(couchDbConnector);
        repositories.add(refPrestationRepository);
        marcheRepository = new MarcheRepository(couchDbConnector);
        repositories.add(marcheRepository);
        refOrientationVentRepository = new RefOrientationVentRepository(couchDbConnector);
        repositories.add(refOrientationVentRepository);
    }
    
    public void setDatabase(final Database accessDatabase, 
            final Database accessCartoDatabase) throws IOException{
        this.accessDatabase=accessDatabase;
        this.accessCartoDatabase=accessCartoDatabase;
        
        intervenantImporter = new IntervenantImporter(accessDatabase, 
                couchDbConnector);
        organismeDisposeIntervenantImporter = new OrganismeDisposeIntervenantImporter(
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
        tronconGestionDigueImporter = new TronconGestionDigueImporter(
                accessDatabase, couchDbConnector, tronconDigueRepository, 
                digueRepository, borneDigueRepository, digueImporter, 
                tronconDigueGeomImporter, systemeReperageImporter, 
                borneDigueImporter, organismeImporter, intervenantImporter, 
                evenementHydrauliqueImporter);
        documentImporter = new DocumentImporter(accessDatabase, 
                couchDbConnector, borneDigueImporter, intervenantImporter, 
                organismeImporter, systemeReperageImporter, 
                evenementHydrauliqueImporter, tronconGestionDigueImporter);
        
        // Linkers
        desordreEvenementHydrauImporter = new DesordreEvenementHydrauImporter(
                accessDatabase, couchDbConnector, 
                tronconGestionDigueImporter.getObjetManager().getDesordreImporter(), 
                evenementHydrauliqueImporter);
        linkers.add(desordreEvenementHydrauImporter);
        prestationEvenementHydrauImporter = new PrestationEvenementHydrauImporter(
                accessDatabase, couchDbConnector, 
                tronconGestionDigueImporter.getObjetManager().getPrestationImporter(), 
                evenementHydrauliqueImporter);
        linkers.add(prestationEvenementHydrauImporter);
        desordreJournalImporter = new DesordreJournalImporter(accessDatabase, 
                couchDbConnector, 
                tronconGestionDigueImporter.getObjetManager().getDesordreImporter(), 
                documentImporter.getJournalArticleImporter());
        linkers.add(desordreJournalImporter);
        elementReseauConventionImporter = new ElementReseauConventionImporter(
                accessDatabase, couchDbConnector, 
                tronconGestionDigueImporter.getObjetManager().getElementReseauImporter(), 
                documentImporter.getConventionImporter());
        linkers.add(elementReseauConventionImporter);
        laisseCrueJournalImporter = new LaisseCrueJournalImporter(
                accessDatabase, couchDbConnector, 
                tronconGestionDigueImporter.getObjetManager().getLaisseCrueImporter(), 
                documentImporter.getJournalArticleImporter());
        linkers.add(laisseCrueJournalImporter);
        ligneEauJournalImporter = new LigneEauJournalImporter(accessDatabase, 
                couchDbConnector, 
                tronconGestionDigueImporter.getObjetManager().getLigneEauImporter(), 
                documentImporter.getJournalArticleImporter());
        linkers.add(ligneEauJournalImporter);
        monteeDesEauxJournalImporter = new MonteeDesEauxJournalImporter(
                accessDatabase, couchDbConnector, 
                tronconGestionDigueImporter.getObjetManager().getMonteeDesEauxImporter(), 
                documentImporter.getJournalArticleImporter());
        linkers.add(monteeDesEauxJournalImporter);
        prestationDocumentImporter = new PrestationDocumentImporter(
                accessDatabase, couchDbConnector, 
                tronconGestionDigueImporter.getObjetManager().getPrestationImporter(), 
                documentImporter);
        linkers.add(prestationDocumentImporter);
        elementReseauGardienImporter = new ElementReseauGardienImporter(
                accessDatabase, couchDbConnector, 
                tronconGestionDigueImporter.getObjetManager().getElementReseauImporter(), 
                intervenantImporter);
        linkers.add(elementReseauGardienImporter);
        elementReseauGestionnaireImporter = new ElementReseauGestionnaireImporter(
                accessDatabase, couchDbConnector, 
                tronconGestionDigueImporter.getObjetManager().getElementReseauImporter(), 
                organismeImporter);
        linkers.add(elementReseauGestionnaireImporter);
        elementReseauProprietaireImporter = new ElementReseauProprietaireImporter(
                accessDatabase, couchDbConnector, 
                tronconGestionDigueImporter.getObjetManager().getElementReseauImporter(), 
                intervenantImporter, organismeImporter);
        linkers.add(elementReseauProprietaireImporter);
        elementStructureGardienImporter = new ElementStructureGardienImporter(
                accessDatabase, couchDbConnector, 
                tronconGestionDigueImporter.getObjetManager().getElementStructureImporter(), 
                intervenantImporter);
        linkers.add(elementStructureGardienImporter);
        elementStructureGestionnaireImporter = new ElementStructureGestionnaireImporter(
                accessDatabase, couchDbConnector, 
                tronconGestionDigueImporter.getObjetManager().getElementStructureImporter(), 
                organismeImporter);
        linkers.add(elementStructureGestionnaireImporter);
        elementStructureProprietaireImporter = new ElementStructureProprietaireImporter(
                accessDatabase, couchDbConnector, 
                tronconGestionDigueImporter.getObjetManager().getElementStructureImporter(), 
                intervenantImporter, organismeImporter);
        linkers.add(elementStructureProprietaireImporter);
        prestationIntervenantImporter = new PrestationIntervenantImporter(
                accessDatabase, couchDbConnector, 
                tronconGestionDigueImporter.getObjetManager().getPrestationImporter(), 
                intervenantImporter);
        linkers.add(prestationIntervenantImporter);
        marcheFinanceurImporter = new MarcheFinanceurImporter(accessDatabase, 
                couchDbConnector, documentImporter.getMarcheImporter(), 
                organismeImporter);
        linkers.add(marcheFinanceurImporter);
        marcheMaitreOeuvreImporter = new MarcheMaitreOeuvreImporter(
                accessDatabase, couchDbConnector, 
                documentImporter.getMarcheImporter(), organismeImporter);
        linkers.add(marcheMaitreOeuvreImporter);
        photoLocaliseeEnPrImporter = new PhotoLocaliseeEnPrImporter(
                accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter, intervenantImporter);
        linkers.add(photoLocaliseeEnPrImporter);
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
    
    public void cleanRepo(final Repository repository) {
        final List<Object> objects = new ArrayList<>();
        repository.getAll().stream().forEach((refRive) -> {
            objects.add(BulkDeleteDocument.of(refRive));
        });
        couchDbConnector.executeBulk(objects);
    }
    
    public void cleanDb(){
        for(final Repository repo : repositories){
            cleanRepo(repo);
        }
    }
    
    public void importation() throws IOException, AccessDbImporterException{
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
        systemeReperageBorneImporter.getByBorneId();
        documentImporter.getDocuments();
        
        for(final GenericEntityLinker linker : linkers) linker.link();
        tronconGestionDigueImporter.update();
        // Normalement les documents et les pièces reliées aux documents ne sont 
        // pas affectées par les linkers car les liaisons sont en sens unique 
        // depuis les objets des troncons. Mais au cas où des associations 
        // bidirectionnelles seraient ajoutées on fait un update().
        documentImporter.update(); 
        evenementHydrauliqueImporter.update();
    }

    //TODO remove when import finished
    public static void main(String[] args) throws AccessDbImporterException {
                //Geotoolkit startup
                Setup.initialize(null);
                //work in lazy mode, do your best for lenient datum shift
                Hints.putSystemDefault(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);
        try {
            final ClassPathXmlApplicationContext applicationContext = CouchDBInit.create(
                            "http://geouser:geopw@localhost:5984", "sirs", "classpath:/fr/sirs/spring/couchdb-context.xml", true, false);
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
            //     SYS_EVT_SOMMET_RISBERME
            System.out.println("=======================");
            Iterator<Row> it = importer.getDatabase().getTable(TableName.ORIENTATION.toString()).iterator();
            
//            while(it.hasNext()){
//                Row row = it.next();
//                System.out.print(row.getInt(TronconGestionDigueImporter.TronconGestionDigueColumns.ID_TRONCON_GESTION.toString())+" || ");
//                System.out.print(row.getInt(TronconGestionDigueImporter.TronconGestionDigueColumns.ID_DIGUE.toString())+" || ");
//                System.out.println(row.getString(TronconGestionDigueImporter.TronconGestionDigueColumns.NOM_TRONCON_GESTION.toString()));
//            }
            
            System.out.println("++++++++++++++++++++");   
//            
//            for(Row r : importer.getCartoDatabase().getTable("GDB_SpatialRefs")){
//                System.out.println(r);
//        }
//SYS_EVT_PIED_DE_DIGUE
            System.out.println("=======================");
            importer.getDatabase().getTable(TableName.ORIENTATION.toString()).getColumns().stream().forEach((column) -> {
                System.out.println(column.getName());
            });
            System.out.println("++++++++++++++++++++");

//            System.out.println(importer.getDatabase().getTable("ILE_TRONCON").getPrimaryKeyIndex());
//            System.out.println(importer.getDatabase().getTable("SYSTEME_REP_LINEAIRE").getPrimaryKeyIndex());
//            System.out.println(importer.getDatabase().getTable("BORNE_PAR_SYSTEME_REP").getPrimaryKeyIndex());
//            System.out.println(importer.getDatabase().getTable("TRONCON_GESTION_DIGUE").getPrimaryKeyIndex());
//            System.out.println(importer.getDatabase().getTable("BORNE_DIGUE").getPrimaryKeyIndex());
            System.out.println(importer.getDatabase().getTable(TableName.ORIENTATION.toString()).getPrimaryKeyIndex());
//            
//            System.out.println(importer.getDatabase().getTable("ELEMENT_STRUCTURE").getPrimaryKeyIndex());
//            System.out.println("index size : "+importer.getDatabase().getTable("SYS_EVT_PIED_DE_DIGUE").getForeignKeyIndex(importer.getDatabase().getTable("ELEMENT_STRUCTURE")));
            
            for(final Row row : importer.getDatabase().getTable(TableName.ORIENTATION.toString())){
//                System.out.println(row);
            }
            System.out.println("=======================");
//            importer.getDatabase().getTable("BORNE_PAR_SYSTEME_REP").getColumns().stream().forEach((column) -> {
//                System.out.println(column.getName());
//            });
//            System.out.println("++++++++++++++++++++");
            importer.cleanDb();
//            importer.importation();
//            for(final TronconDigue troncon : importer.importation()){
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
//            }
            System.out.println("fin de l'importation !");

        } catch (IOException ex) {
            Logger.getLogger(DbImporter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
