package fr.sirs.core.component;

import fr.sirs.core.InjectorCore;
import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.StreamingViewResult;
import org.ektorp.support.View;
import org.ektorp.support.Views;
import org.springframework.beans.factory.annotation.Autowired;

import fr.sirs.core.JacksonIterator;
import fr.sirs.core.SirsCoreRuntimeExecption;
import fr.sirs.core.model.AbstractPositionDocument;
import fr.sirs.core.model.Crete;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Deversoir;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.EchelleLimnimetrique;
import fr.sirs.core.model.ElementCreator;
import fr.sirs.core.model.PositionDocument;
import fr.sirs.core.model.PositionProfilTravers;
import fr.sirs.core.model.Epi;
import fr.sirs.core.model.Fondation;
import fr.sirs.core.model.FrontFrancBord;
import fr.sirs.core.model.GardeTroncon;
import fr.sirs.core.model.LaisseCrue;
import fr.sirs.core.model.LargeurFrancBord;
import fr.sirs.core.model.LigneEau;
import fr.sirs.core.model.MonteeEaux;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.OuvertureBatardable;
import fr.sirs.core.model.OuvrageFranchissement;
import fr.sirs.core.model.OuvrageHydrauliqueAssocie;
import fr.sirs.core.model.OuvrageParticulier;
import fr.sirs.core.model.OuvrageRevanche;
import fr.sirs.core.model.OuvrageTelecomEnergie;
import fr.sirs.core.model.OuvrageVoirie;
import fr.sirs.core.model.PeriodeLocaliseeTroncon;
import fr.sirs.core.model.PiedDigue;
import fr.sirs.core.model.PiedFrontFrancBord;
import fr.sirs.core.model.PistePiedDigue;
import fr.sirs.core.model.Prestation;
import fr.sirs.core.model.ProfilFrontFrancBord;
import fr.sirs.core.model.ProfilLong;
import fr.sirs.core.model.ProprieteTroncon;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.ReseauHydrauliqueCielOuvert;
import fr.sirs.core.model.ReseauTelecomEnergie;
import fr.sirs.core.model.SommetRisberme;
import fr.sirs.core.model.StationPompage;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TalusDigue;
import fr.sirs.core.model.TalusRisberme;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.core.model.VoieAcces;
import fr.sirs.core.model.VoieDigue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import org.apache.sis.util.ArgumentChecks;

/**
 * Outil gérant les échanges avec la bdd CouchDB pour tous les objets tronçons.
 * 
 * Note : Le cache qui permet de garder une instance unique en mémoire pour un 
 * tronçon donné est extrêmement important pour les opérations de sauvegarde. 
 * 
 * Ex : On a un tronçon A, qui contient une crête tata et un talus de digue toto.
 * 
 * On ouvre l'éditeur de toto. Le tronçon A est donc chargé en mémoire. 
 * Dans le même temps on ouvre le panneau d'édition pour tata. On doit donc aussi 
 * charger le tronçon A en mémoire.
 * 
 * Maintenant, que se passe -'il sans cache ? On a deux copies du tronçon A en  
 * mémoire pour une même révision (disons 0).
 * 
 * Si on sauvegarde toto, tronçon A passe en révision 1 dans la bdd, mais pour 
 * UNE SEULE des 2 copies en mémoire. En conséquence de quoi, lorsqu'on veut 
 * sauvegarder tata, on a un problème : on demande à la base de faire une mise à
 * jour de la révision 1 en utilisant un objet de la révision 0.
 * 
 * Résultat : ERREUR ! 
 * 
 * Avec le cache, les deux éditeurs pointent sur la même copie en mémoire. Lorsqu'un
 * éditeur met à jour le tronçon, la révision de la copie est indentée, le deuxième
 * éditeur a donc un tronçon avec un numéro de révision correct.
 * 
 * @author Samuel Andrés (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
@Views({
        @View(name = "all", map = "function(doc) {if(doc['@class']=='fr.sirs.core.model.TronconDigue') {emit(doc._id, doc._id)}}"),
        @View(name = "stream", map = "function(doc) {if(doc['@class']=='fr.sirs.core.model.TronconDigue') {emit(doc._id, doc)}}"),
        @View(name = "streamLight", map = "classpath:TronconDigueLight-map.js"),
        @View(name = "byDigueId", map = "function(doc) {if(doc['@class']=='fr.sirs.core.model.TronconDigue') {emit(doc.digueId, doc._id)}}") })
public class TronconDigueRepository extends AbstractSIRSRepository<TronconDigue> {
    
    private final HashMap<String, Callable<List<? extends Objet>>> viewMapObjets = new HashMap();
    private final HashMap<String, Callable<List<? extends AbstractPositionDocument>>> viewMapDocuments = new HashMap();
    private final HashMap<String, Callable<List<? extends PeriodeLocaliseeTroncon>>> viewMapPeriodesTroncon = new HashMap();
    
    @Autowired
    public TronconDigueRepository(CouchDbConnector db) {
        super(TronconDigue.class, db);
        initStandardDesignDocument();
        viewMapObjets.put(CRETE, this::getAllCretes);
        viewMapObjets.put(DESORDRE, this::getAllDesordres);
        viewMapObjets.put(DEVERSOIR, this::getAllDeversoirs);
        viewMapObjets.put(EPI, this::getAllEpis);
        viewMapObjets.put(FONDATION, this::getAllFondations);
        viewMapObjets.put(FRONT_FRANC_BORD, this::getAllFrontFrancBords);
        viewMapObjets.put(LAISSE_CRUE, this::getAllLaisseCrues);
        viewMapObjets.put(LARGEUR_FRANC_BORD, this::getAllLargeurFrancBords);
        viewMapObjets.put(LIGNE_EAU, this::getAllLigneEaus);
        viewMapObjets.put(MONTEE_EAUX, this::getAllMonteeEaux);
        viewMapObjets.put(OUVERTURE_BATARDABLE, this::getAllOuvertureBatardables);
        viewMapObjets.put(OUVRAGE_FRANCHISSEMENT, this::getAllOuvrageFranchissements);
        viewMapObjets.put(OUVRAGE_HYDRAULIQUE_ASSOCIE, this::getAllOuvrageHydrauliqueAssocies);
        viewMapObjets.put(OUVRAGE_PARTICULIER, this::getAllOuvrageParticuliers);
        viewMapObjets.put(OUVRAGE_REVANCHE, this::getAllOuvrageRevanches);
        viewMapObjets.put(OUVRAGE_TELECOM_ENERGIE, this::getAllOuvrageTelecomEnergies);
        viewMapObjets.put(OUVRAGE_VOIRIE, this::getAllOuvrageVoiries);
        viewMapObjets.put(PIED_DIGUE, this::getAllPiedDigues);
        viewMapObjets.put(PIED_FRONT_FRANC_BORD, this::getAllPiedFrontFrancBords);
        viewMapObjets.put(PISTE_PIED_DIGUE, this::getAllPistePiedDigues);
        viewMapObjets.put(PRESTATION, this::getAllPrestations);
        viewMapObjets.put(PROFIL_FRONT_FRANC_BORD, this::getAllProfilFrontFrancBords);
        viewMapObjets.put(RESEAU_HYDRAULIQUE_FERME, this::getAllReseauHydrauliqueFermes);
        viewMapObjets.put(RESEAU_HYDRAULIQUE_CIEL_OUVERT, this::getAllReseauHydrauliqueCielOuverts);
        viewMapObjets.put(RESEAU_TELECOM_ENERGIE, this::getAllReseauTelecomEnergies);
        viewMapObjets.put(SOMMET_RISBERME, this::getAllSommetRisbermes);
        viewMapObjets.put(STATION_POMPAGE, this::getAllStationPompages);
        viewMapObjets.put(TALUS_DIGUE, this::getAllTalusDigues);
        viewMapObjets.put(TALUS_RISBERME, this::getAllTalusRisbermes);
        viewMapObjets.put(VOIE_ACCES, this::getAllVoieAccess);
        viewMapObjets.put(VOIE_DIGUE, this::getAllVoieDigues);
        viewMapDocuments.put(POSITION_DOCUMENT, this::getAllPositionDocuments);
        viewMapDocuments.put(POSITION_PROFIL_TRAVERS, this::getAllPositionProfilTravers);
        viewMapDocuments.put(PROFIL_LONG, this::getAllProfilLongs);
        viewMapPeriodesTroncon.put(GARDE_TRONCON, this::getAllGardes);
        viewMapPeriodesTroncon.put(PROPRIETE_TRONCON, this::getAllProprietes);
    }

    public List<TronconDigue> getByDigue(final Digue digue) {
        ArgumentChecks.ensureNonNull("Digue parent", digue);
        return this.queryView("byDigueId", digue.getId());
    }

    @Override
    public void remove(TronconDigue entity) {
        ArgumentChecks.ensureNonNull("Tronçon à supprimer", entity);
        constraintDeleteBorneAndSR(entity);
        super.remove(entity);
    }

    @Override
    public Class<TronconDigue> getModelClass() {
        return TronconDigue.class;
    }

    @Override
    public TronconDigue create() {
        final SessionGen session = InjectorCore.getBean(SessionGen.class);
        if(session!=null && session instanceof OwnableSession){
            final ElementCreator elementCreator = ((OwnableSession) session).getElementCreator();
            return elementCreator.createElement(TronconDigue.class);
        } else {
            throw new SirsCoreRuntimeExecption("Pas de session courante");
        }
//        return new TronconDigue();
    }

    /**
     * Return a light version of the tronçon, without sub-structures.
     * 
     * Note : As the objects returned here are incomplete, they're not cached.
     * 
     * @return 
     */
    public List<TronconDigue> getAllLight() {
        final JacksonIterator<TronconDigue> ite = JacksonIterator.create(TronconDigue.class, db.queryForStreamingView(createQuery("streamLight")));
        final List<TronconDigue> lst = new ArrayList<>();
        while (ite.hasNext()) {
            lst.add(ite.next());
        }
        return lst;
    }
    
    public static final String CRETE = "Crete";

    @View(name = CRETE, map = "classpath:Crete-map.js")
    public List<Crete> getAllCretes() {
        return db.queryView(createQuery(CRETE), Crete.class);
    }

    public static final String OUVRAGE_REVANCHE = "OuvrageRevanche";

    @View(name = OUVRAGE_REVANCHE, map = "classpath:OuvrageRevanche-map.js")
    public List<OuvrageRevanche> getAllOuvrageRevanches() {
        return db
                .queryView(createQuery(OUVRAGE_REVANCHE), OuvrageRevanche.class);
    }

    public static final String OUVERTURE_BATARDABLE = "OuvertureBatardable";

    @View(name = OUVERTURE_BATARDABLE, map = "classpath:OuvertureBatardable-map.js")
    public List<OuvertureBatardable> getAllOuvertureBatardables() {
        return db.queryView(createQuery(OUVERTURE_BATARDABLE),
                OuvertureBatardable.class);
    }

    public static final String TALUS_DIGUE = "TalusDigue";

    @View(name = TALUS_DIGUE, map = "classpath:TalusDigue-map.js")
    public List<TalusDigue> getAllTalusDigues() {
        return db.queryView(createQuery(TALUS_DIGUE), TalusDigue.class);
    }

    public static final String TALUS_RISBERME = "TalusRisberme";

    @View(name = TALUS_RISBERME, map = "classpath:TalusRisberme-map.js")
    public List<TalusRisberme> getAllTalusRisbermes() {
        return db.queryView(createQuery(TALUS_RISBERME), TalusRisberme.class);
    }

    public static final String SOMMET_RISBERME = "SommetRisberme";

    @View(name = SOMMET_RISBERME, map = "classpath:SommetRisberme-map.js")
    public List<SommetRisberme> getAllSommetRisbermes() {
        return db.queryView(createQuery(SOMMET_RISBERME), SommetRisberme.class);
    }

    public static final String FONDATION = "Fondation";

    @View(name = FONDATION, map = "classpath:Fondation-map.js")
    public List<Fondation> getAllFondations() {
        return db.queryView(createQuery(FONDATION), Fondation.class);
    }

    @View(name = FONDATION, map = "classpath:Fondation-map.js")
    public StreamingViewResult getAllFondationsIterator() {
        return db.queryForStreamingView(createQuery(FONDATION));
    }

    public static final String PIED_DIGUE = "PiedDigue";

    @View(name = PIED_DIGUE, map = "classpath:PiedDigue-map.js")
    public List<PiedDigue> getAllPiedDigues() {
        return db.queryView(createQuery(PIED_DIGUE), PiedDigue.class);
    }

    public static final String LARGEUR_FRANC_BORD = "LargeurFrancBord";

    @View(name = LARGEUR_FRANC_BORD, map = "classpath:LargeurFrancBord-map.js")
    public List<LargeurFrancBord> getAllLargeurFrancBords() {
        return db.queryView(createQuery(LARGEUR_FRANC_BORD),
                LargeurFrancBord.class);
    }

    public static final String OUVRAGE_FRANCHISSEMENT = "OuvrageFranchissement";

    @View(name = OUVRAGE_FRANCHISSEMENT, map = "classpath:OuvrageFranchissement-map.js")
    public List<OuvrageFranchissement> getAllOuvrageFranchissements() {
        return db.queryView(createQuery(OUVRAGE_FRANCHISSEMENT),
                OuvrageFranchissement.class);
    }

    public static final String VOIE_ACCES = "VoieAcces";

    @View(name = VOIE_ACCES, map = "classpath:VoieAcces-map.js")
    public List<VoieAcces> getAllVoieAccess() {
        return db.queryView(createQuery(VOIE_ACCES), VoieAcces.class);
    }

    public static final String VOIE_DIGUE = "VoieDigue";

    @View(name = VOIE_DIGUE, map = "classpath:VoieDigue-map.js")
    public List<VoieDigue> getAllVoieDigues() {
        return db.queryView(createQuery(VOIE_DIGUE), VoieDigue.class);
    }

    public static final String OUVRAGE_VOIRIE = "OuvrageVoirie";

    @View(name = OUVRAGE_VOIRIE, map = "classpath:OuvrageVoirie-map.js")
    public List<OuvrageVoirie> getAllOuvrageVoiries() {
        return db.queryView(createQuery(OUVRAGE_VOIRIE), OuvrageVoirie.class);
    }

    public static final String STATION_POMPAGE = "StationPompage";

    @View(name = STATION_POMPAGE, map = "classpath:StationPompage-map.js")
    public List<StationPompage> getAllStationPompages() {
        return db.queryView(createQuery(STATION_POMPAGE), StationPompage.class);
    }

    public static final String RESEAU_HYDRAULIQUE_FERME = "ReseauHydrauliqueFerme";

    @View(name = RESEAU_HYDRAULIQUE_FERME, map = "classpath:ReseauHydrauliqueFerme-map.js")
    public List<ReseauHydrauliqueFerme> getAllReseauHydrauliqueFermes() {
        return db.queryView(createQuery(RESEAU_HYDRAULIQUE_FERME),
                ReseauHydrauliqueFerme.class);
    }

    public static final String OUVRAGE_HYDRAULIQUE_ASSOCIE = "OuvrageHydrauliqueAssocie";

    @View(name = OUVRAGE_HYDRAULIQUE_ASSOCIE, map = "classpath:OuvrageHydrauliqueAssocie-map.js")
    public List<OuvrageHydrauliqueAssocie> getAllOuvrageHydrauliqueAssocies() {
        return db.queryView(createQuery(OUVRAGE_HYDRAULIQUE_ASSOCIE),
                OuvrageHydrauliqueAssocie.class);
    }

    public static final String RESEAU_TELECOM_ENERGIE = "ReseauTelecomEnergie";

    @View(name = RESEAU_TELECOM_ENERGIE, map = "classpath:ReseauTelecomEnergie-map.js")
    public List<ReseauTelecomEnergie> getAllReseauTelecomEnergies() {
        return db.queryView(createQuery(RESEAU_TELECOM_ENERGIE),
                ReseauTelecomEnergie.class);
    }

    public static final String OUVRAGE_TELECOM_ENERGIE = "OuvrageTelecomEnergie";

    @View(name = OUVRAGE_TELECOM_ENERGIE, map = "classpath:OuvrageTelecomEnergie-map.js")
    public List<OuvrageTelecomEnergie> getAllOuvrageTelecomEnergies() {
        return db.queryView(createQuery(OUVRAGE_TELECOM_ENERGIE),
                OuvrageTelecomEnergie.class);
    }

    public static final String RESEAU_HYDRAULIQUE_CIEL_OUVERT = "ReseauHydrauliqueCielOuvert";

    @View(name = RESEAU_HYDRAULIQUE_CIEL_OUVERT, map = "classpath:ReseauHydrauliqueCielOuvert-map.js")
    public List<ReseauHydrauliqueCielOuvert> getAllReseauHydrauliqueCielOuverts() {
        return db.queryView(createQuery(RESEAU_HYDRAULIQUE_CIEL_OUVERT),
                ReseauHydrauliqueCielOuvert.class);
    }

    public static final String OUVRAGE_PARTICULIER = "OuvrageParticulier";

    @View(name = OUVRAGE_PARTICULIER, map = "classpath:OuvrageParticulier-map.js")
    public List<OuvrageParticulier> getAllOuvrageParticuliers() {
        return db.queryView(createQuery(OUVRAGE_PARTICULIER),
                OuvrageParticulier.class);
    }

    public static final String ECHELLE_LIMNIMETRIQUE = "EchelleLimnimetrique";

    @View(name = ECHELLE_LIMNIMETRIQUE, map = "classpath:EchelleLimnimetrique-map.js")
    public List<EchelleLimnimetrique> getAllEchelleLimnimetriques() {
        return db.queryView(createQuery(ECHELLE_LIMNIMETRIQUE),
                EchelleLimnimetrique.class);
    }

    public static final String PRESTATION = "Prestation";

    @View(name = PRESTATION, map = "classpath:Prestation-map.js")
    public List<Prestation> getAllPrestations() {
        return db.queryView(createQuery(PRESTATION), Prestation.class);
    }

    public static final String DESORDRE = "Desordre";

    @View(name = DESORDRE, map = "classpath:Desordre-map.js")
    public List<Desordre> getAllDesordres() {
        return db.queryView(createQuery(DESORDRE), Desordre.class);
    }

    public static final String LAISSE_CRUE = "LaisseCrue";

    @View(name = LAISSE_CRUE, map = "classpath:LaisseCrue-map.js")
    public List<LaisseCrue> getAllLaisseCrues() {
        return db.queryView(createQuery(LAISSE_CRUE), LaisseCrue.class);
    }

    public static final String MONTEE_EAUX = "MonteeEaux";

    @View(name = MONTEE_EAUX, map = "classpath:MonteeEaux-map.js")
    public List<MonteeEaux> getAllMonteeEaux() {
        return db.queryView(createQuery(MONTEE_EAUX), MonteeEaux.class);
    }

    public static final String LIGNE_EAU = "LigneEau";

    @View(name = LIGNE_EAU, map = "classpath:LigneEau-map.js")
    public List<LigneEau> getAllLigneEaus() {
        return db.queryView(createQuery(LIGNE_EAU), LigneEau.class);
    }

    public static final String DEVERSOIR = "Deversoir";

    @View(name = DEVERSOIR, map = "classpath:Deversoir-map.js")
    public List<Deversoir> getAllDeversoirs() {
        return db.queryView(createQuery(DEVERSOIR), Deversoir.class);
    }

    public static final String PISTE_PIED_DIGUE = "PistePiedDigue";

    @View(name = PISTE_PIED_DIGUE, map = "classpath:PistePiedDigue-map.js")
    public List<PistePiedDigue> getAllPistePiedDigues() {
        return db.queryView(createQuery(PISTE_PIED_DIGUE), PistePiedDigue.class);
    }

    public static final String PROFIL_FRONT_FRANC_BORD = "ProfilFrontFrancBord";

    @View(name = PROFIL_FRONT_FRANC_BORD, map = "classpath:ProfilFrontFrancBord-map.js")
    public List<ProfilFrontFrancBord> getAllProfilFrontFrancBords() {
        return db.queryView(createQuery(PROFIL_FRONT_FRANC_BORD),
                ProfilFrontFrancBord.class);
    }

    public static final String EPI = "Epi";

    @View(name = EPI, map = "classpath:Epi-map.js")
    public List<Epi> getAllEpis() {
        return db.queryView(createQuery(EPI), Epi.class);
    }

    public static final String FRONT_FRANC_BORD = "FrontFrancBord";

    @View(name = FRONT_FRANC_BORD, map = "classpath:FrontFrancBord-map.js")
    public List<FrontFrancBord> getAllFrontFrancBords() {
        return db.queryView(createQuery(FRONT_FRANC_BORD), FrontFrancBord.class);
    }

    public static final String PIED_FRONT_FRANC_BORD = "PiedFrontFrancBord";

    @View(name = PIED_FRONT_FRANC_BORD, map = "classpath:PiedFrontFrancBord-map.js")
    public List<PiedFrontFrancBord> getAllPiedFrontFrancBords() {
        return db.queryView(createQuery(PIED_FRONT_FRANC_BORD),
                PiedFrontFrancBord.class);
    }

    public static final String POSITION_DOCUMENT = "PositionDocument";

    @View(name = POSITION_DOCUMENT, map = "classpath:PositionDocument-map.js")
    public List<PositionDocument> getAllPositionDocuments() {
        return db.queryView(createQuery(POSITION_DOCUMENT),
                PositionDocument.class);
    }

    public static final String POSITION_DOCUMENT_BY_DOCUMENT_ID = "PositionDocumentByDocumentId";

    @View(name = POSITION_DOCUMENT_BY_DOCUMENT_ID, map = "classpath:PositionDocumentByDocumentId-map.js")
    public List<PositionDocument> getPositionDocumentsByDocumentId(final String documentId) {
        return db.queryView(createQuery(POSITION_DOCUMENT_BY_DOCUMENT_ID).key(documentId),
                PositionDocument.class);
    }

    public static final String POSITION_PROFIL_TRAVERS = "PositionProfilTravers";

    @View(name = POSITION_PROFIL_TRAVERS, map = "classpath:PositionProfilTravers-map.js")
    public List<PositionProfilTravers> getAllPositionProfilTravers() {
        return db.queryView(createQuery(POSITION_PROFIL_TRAVERS),
                PositionProfilTravers.class);
    }

    public static final String POSITION_PROFIL_TRAVERS_BY_DOCUMENT_ID = "PositionProfilTraversByDocumentId";

    @View(name = POSITION_PROFIL_TRAVERS_BY_DOCUMENT_ID, map = "classpath:PositionProfilTraversByDocumentId-map.js")
    public List<PositionProfilTravers> getPositionProfilTraversByDocumentId(final String documentId) {
        return db.queryView(createQuery(POSITION_PROFIL_TRAVERS_BY_DOCUMENT_ID).key(documentId),
                PositionProfilTravers.class);
    }

    public static final String PROFIL_LONG = "ProfilLong";

    @View(name = PROFIL_LONG, map = "classpath:ProfilLong-map.js")
    public List<ProfilLong> getAllProfilLongs() {
        return db.queryView(createQuery(PROFIL_LONG),
                ProfilLong.class);
    }

    public static final String GARDE_TRONCON = "GardeTroncon";

    @View(name = GARDE_TRONCON, map = "classpath:GardeTroncon-map.js")
    public List<GardeTroncon> getAllGardes() {
        return db.queryView(createQuery(GARDE_TRONCON),
                GardeTroncon.class);
    }

    public static final String PROPRIETE_TRONCON = "ProprieteTroncon";

    @View(name = PROPRIETE_TRONCON, map = "classpath:ProprieteTroncon-map.js")
    public List<ProprieteTroncon> getAllProprietes() {
        return db.queryView(createQuery(PROPRIETE_TRONCON),
                ProprieteTroncon.class);
    }

    public JacksonIterator<TronconDigue> getAllIterator() {
        return JacksonIterator.create(TronconDigue.class,
                db.queryForStreamingView(createQuery("stream")));
    }
    
    public JacksonIterator<TronconDigue> getAllLightIterator() {
        return JacksonIterator.create(TronconDigue.class,
                db.queryForStreamingView(createQuery("streamLight")));
    }

    public List<? extends Objet> getAllFromView(Class elementClass) {
        return getAllFromView(elementClass.getSimpleName());
    }
    
    /**
     * Get all elements of the queried view. The view identifiers can be found as
     * public static variables of the current class. It generally is the name of 
     * a structure (Ex : Crete, Desordre, etc.).
     * @param view The view to query.
     * @return The result of the executed view, or null if there's no view with 
     * the given name.
     */    
    public List<? extends Objet> getAllFromView(String view) {
        final Callable<List<? extends Objet>> callable = viewMapObjets.get(view);
        if (callable != null) {
            try {
                return callable.call();
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } 
        } else {
            return null;
        }
    }
    
    /**
     * Get all elements of the queried view. The view identifiers can be found as
     * public static variables of the current class. It generally is the name of 
     * a structure (Ex : Crete, Desordre, etc.).
     * @param view The view to query.
     * @return The result of the executed view, or null if there's no view with 
     * the given name.
     */    
    public List<? extends AbstractPositionDocument> getAllDocumentsFromView(String view) {
        final Callable<List<? extends AbstractPositionDocument>> callable = viewMapDocuments.get(view);
        if (callable != null) {
            try {
                return callable.call();
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } 
        } else {
            return null;
        }
    }
    
    /**
     * Cette contrainte s'assure de supprimer les SR et bornes associées au troncon
     * en cas de suppression.
     * 
     */
    private void constraintDeleteBorneAndSR(TronconDigue entity){
        //on supprime tous les SR associés
        final SystemeReperageRepository srrepo = new SystemeReperageRepository(db);
        final List<SystemeReperage> srs = srrepo.getByTroncon(entity);
        for(SystemeReperage sr : srs){
            srrepo.remove(sr, entity);
        }
        // TODO : Set an end date to bornes which are not used anymore.
    }

    @Override
    protected TronconDigue onLoad(TronconDigue toLoad) {
        new DefaultSRChangeListener(toLoad);
        return toLoad;
    }
}
