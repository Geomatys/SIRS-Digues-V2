package fr.sirs.importer;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import fr.sirs.core.SessionCore;
import fr.sirs.core.component.SirsDBInfoRepository;
import fr.sirs.core.model.Role;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.ImportContext;
import fr.sirs.importer.v2.Linker;
import fr.sirs.importer.v2.linear.TronconDigueUpdater;
import fr.sirs.util.ImportParameters;
import java.io.File;
import java.io.IOException;
import org.apache.sis.metadata.iso.citation.Citations;
import org.apache.sis.referencing.IdentifiedObjects;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.CouchDbConnector;
import org.opengis.metadata.Identifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class DbImporter {

    public static final String NULL_STRING_VALUE = "null";

    public static final String cleanNullString(String string) {
        return (NULL_STRING_VALUE.equals(string) || string == null) ? "" : string;
    }

    private final ConfigurableApplicationContext databaseContext;

    public enum TableName {

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

    /**
     * Prepare to import data in database pointed by given application context.
     *
     * @param dbContext The context of the database to fill. Cannot be null.
     */
    public DbImporter(final ConfigurableApplicationContext dbContext) {
        ArgumentChecks.ensureNonNull("Output database context", dbContext);
        databaseContext = dbContext;
    }

    public void importation(
            final File mainAccessDbFile,
            final File cartoAccessDbFile,
            final CoordinateReferenceSystem outputCRS,
            final String newAdminLogin,
            final String newAdminPwd)
            throws IOException, AccessDbImporterException {

        SirsDBInfoRepository sirsDBInfoRepository = databaseContext.getBean(SirsDBInfoRepository.class);
        final Identifier crsId = IdentifiedObjects.getIdentifier(outputCRS, Citations.EPSG);
        sirsDBInfoRepository.setSRID(crsId.getCodeSpace() + ':' + crsId.getCode());

        if (newAdminLogin != null) {
            databaseContext.getBean(SessionCore.class).createUser(newAdminLogin, newAdminPwd, Role.ADMIN);
        }

        try (final Database mainDb = DatabaseBuilder.open(mainAccessDbFile);
                final Database cartoDb = DatabaseBuilder.open(cartoAccessDbFile)) {

            final ImportParameters params = new ImportParameters(mainDb, cartoDb, databaseContext.getBean(CouchDbConnector.class), outputCRS);
            databaseContext.getBeanFactory().registerSingleton(ImportParameters.class.getCanonicalName(), params);

            // Open spring context for import.
            try (final ClassPathXmlApplicationContext importCtx = new ClassPathXmlApplicationContext(new String[]{"classpath:/fr/sirs/spring/importer-context.xml"}, databaseContext)) {
                final ImportContext context = importCtx.getBean(ImportContext.class);

                /*
                 * We have to import entire linear referencing environment first
                 * if we want to be able to compute object geometries before they're
                 * posted.
                 */
                context.importers.get(TronconDigue.class).compute();
                importCtx.getBean(TronconDigueUpdater.class).compute();

                for (final AbstractImporter importer : context.importers.values()) {
                    importer.compute();
                }

                for (final Linker l : context.linkers) {
                    l.link();
                }

                context.outputDb.compact();
            } finally {
                databaseContext.getBeanFactory().destroyBean(params);
            }
        }
    }
}
