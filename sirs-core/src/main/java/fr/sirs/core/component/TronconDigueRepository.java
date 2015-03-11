package fr.sirs.core.component;

import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.StreamingViewResult;
import org.ektorp.support.View;
import org.ektorp.support.Views;
import org.springframework.beans.factory.annotation.Autowired;

import fr.sirs.core.JacksonIterator;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Crete;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.Deversoire;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.Epi;
import fr.sirs.core.model.Fondation;
import fr.sirs.core.model.FrontFrancBord;
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
import fr.sirs.core.model.PiedDigue;
import fr.sirs.core.model.PiedFrontFrancBord;
import fr.sirs.core.model.PistePiedDigue;
import fr.sirs.core.model.Prestation;
import fr.sirs.core.model.ProfilFrontFrancBord;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.ReseauHydroCielOuvert;
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
import org.ektorp.DocumentNotFoundException;

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
    
    private final HashMap<String, Callable<List<? extends Objet>>> viewMap = new HashMap();
    
    @Autowired
    public TronconDigueRepository(CouchDbConnector db) {
        super(TronconDigue.class, db);
        initStandardDesignDocument();
        viewMap.put(CRETE, this::getAllCretes);
        viewMap.put(DESORDRE, this::getAllDesordres);
        viewMap.put(DEVERSOIRE, this::getAllDeversoires);
        viewMap.put(EPI, this::getAllEpis);
        viewMap.put(FONDATION, this::getAllFondations);
        viewMap.put(FRONTFRANCBORD, this::getAllFrontFrancBords);
        viewMap.put(LAISSECRUE, this::getAllLaisseCrues);
        viewMap.put(LARGEURFRANCBORD, this::getAllLargeurFrancBords);
        viewMap.put(LIGNEEAU, this::getAllLigneEaus);
        viewMap.put(MONTEEEAUX, this::getAllMonteeEaux);
        viewMap.put(OUVERTUREBATARDABLE, this::getAllOuvertureBatardables);
        viewMap.put(OUVRAGEFRANCHISSEMENT, this::getAllOuvrageFranchissements);
        viewMap.put(OUVRAGEHYDRAULIQUEASSOCIE, this::getAllOuvrageHydrauliqueAssocies);
        viewMap.put(OUVRAGEPARTICULIER, this::getAllOuvrageParticuliers);
        viewMap.put(OUVRAGEREVANCHE, this::getAllOuvrageRevanches);
        viewMap.put(OUVRAGETELECOMENERGIE, this::getAllOuvrageTelecomEnergies);
        viewMap.put(OUVRAGEVOIRIE, this::getAllOuvrageVoiries);
        viewMap.put(PIEDDIGUE, this::getAllPiedDigues);
        viewMap.put(PIEDFRONTFRANCBORD, this::getAllPiedFrontFrancBords);
        viewMap.put(PISTEPIEDDIGUE, this::getAllPistePiedDigues);
        viewMap.put(PRESTATION, this::getAllPrestations);
        viewMap.put(PROFILFRONTFRANCBORD, this::getAllProfilFrontFrancBords);
        viewMap.put(RESEAUHYDRAULIQUEFERME, this::getAllReseauHydrauliqueFermes);
        viewMap.put(RESEAUHYDROCIELOUVERT, this::getAllReseauHydroCielOuverts);
        viewMap.put(RESEAUTELECOMENERGIE, this::getAllReseauTelecomEnergies);
        viewMap.put(SOMMETRISBERME, this::getAllSommetRisbermes);
        viewMap.put(STATIONPOMPAGE, this::getAllStationPompages);
        viewMap.put(TALUSDIGUE, this::getAllTalusDigues);
        viewMap.put(TALUSRISBERME, this::getAllTalusRisbermes);
        viewMap.put(VOIEACCES, this::getAllVoieAccess);
        viewMap.put(VOIEDIGUE, this::getAllVoieDigues);
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
        return new TronconDigue();
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

    public static final String OUVRAGEREVANCHE = "OuvrageRevanche";

    @View(name = OUVRAGEREVANCHE, map = "classpath:OuvrageRevanche-map.js")
    public List<OuvrageRevanche> getAllOuvrageRevanches() {
        return db
                .queryView(createQuery(OUVRAGEREVANCHE), OuvrageRevanche.class);
    }

    public static final String OUVERTUREBATARDABLE = "OuvertureBatardable";

    @View(name = OUVERTUREBATARDABLE, map = "classpath:OuvertureBatardable-map.js")
    public List<OuvertureBatardable> getAllOuvertureBatardables() {
        return db.queryView(createQuery(OUVERTUREBATARDABLE),
                OuvertureBatardable.class);
    }

    public static final String TALUSDIGUE = "TalusDigue";

    @View(name = TALUSDIGUE, map = "classpath:TalusDigue-map.js")
    public List<TalusDigue> getAllTalusDigues() {
        return db.queryView(createQuery(TALUSDIGUE), TalusDigue.class);
    }

    public static final String TALUSRISBERME = "TalusRisberme";

    @View(name = TALUSRISBERME, map = "classpath:TalusRisberme-map.js")
    public List<TalusRisberme> getAllTalusRisbermes() {
        return db.queryView(createQuery(TALUSRISBERME), TalusRisberme.class);
    }

    public static final String SOMMETRISBERME = "SommetRisberme";

    @View(name = SOMMETRISBERME, map = "classpath:SommetRisberme-map.js")
    public List<SommetRisberme> getAllSommetRisbermes() {
        return db.queryView(createQuery(SOMMETRISBERME), SommetRisberme.class);
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

    public static final String PIEDDIGUE = "PiedDigue";

    @View(name = PIEDDIGUE, map = "classpath:PiedDigue-map.js")
    public List<PiedDigue> getAllPiedDigues() {
        return db.queryView(createQuery(PIEDDIGUE), PiedDigue.class);
    }

    public static final String LARGEURFRANCBORD = "LargeurFrancBord";

    @View(name = LARGEURFRANCBORD, map = "classpath:LargeurFrancBord-map.js")
    public List<LargeurFrancBord> getAllLargeurFrancBords() {
        return db.queryView(createQuery(LARGEURFRANCBORD),
                LargeurFrancBord.class);
    }

    public static final String OUVRAGEFRANCHISSEMENT = "OuvrageFranchissement";

    @View(name = OUVRAGEFRANCHISSEMENT, map = "classpath:OuvrageFranchissement-map.js")
    public List<OuvrageFranchissement> getAllOuvrageFranchissements() {
        return db.queryView(createQuery(OUVRAGEFRANCHISSEMENT),
                OuvrageFranchissement.class);
    }

    public static final String VOIEACCES = "VoieAcces";

    @View(name = VOIEACCES, map = "classpath:VoieAcces-map.js")
    public List<VoieAcces> getAllVoieAccess() {
        return db.queryView(createQuery(VOIEACCES), VoieAcces.class);
    }

    public static final String VOIEDIGUE = "VoieDigue";

    @View(name = VOIEDIGUE, map = "classpath:VoieDigue-map.js")
    public List<VoieDigue> getAllVoieDigues() {
        return db.queryView(createQuery(VOIEDIGUE), VoieDigue.class);
    }

    public static final String OUVRAGEVOIRIE = "OuvrageVoirie";

    @View(name = OUVRAGEVOIRIE, map = "classpath:OuvrageVoirie-map.js")
    public List<OuvrageVoirie> getAllOuvrageVoiries() {
        return db.queryView(createQuery(OUVRAGEVOIRIE), OuvrageVoirie.class);
    }

    public static final String STATIONPOMPAGE = "StationPompage";

    @View(name = STATIONPOMPAGE, map = "classpath:StationPompage-map.js")
    public List<StationPompage> getAllStationPompages() {
        return db.queryView(createQuery(STATIONPOMPAGE), StationPompage.class);
    }

    public static final String RESEAUHYDRAULIQUEFERME = "ReseauHydrauliqueFerme";

    @View(name = RESEAUHYDRAULIQUEFERME, map = "classpath:ReseauHydrauliqueFerme-map.js")
    public List<ReseauHydrauliqueFerme> getAllReseauHydrauliqueFermes() {
        return db.queryView(createQuery(RESEAUHYDRAULIQUEFERME),
                ReseauHydrauliqueFerme.class);
    }

    public static final String OUVRAGEHYDRAULIQUEASSOCIE = "OuvrageHydrauliqueAssocie";

    @View(name = OUVRAGEHYDRAULIQUEASSOCIE, map = "classpath:OuvrageHydrauliqueAssocie-map.js")
    public List<OuvrageHydrauliqueAssocie> getAllOuvrageHydrauliqueAssocies() {
        return db.queryView(createQuery(OUVRAGEHYDRAULIQUEASSOCIE),
                OuvrageHydrauliqueAssocie.class);
    }

    public static final String RESEAUTELECOMENERGIE = "ReseauTelecomEnergie";

    @View(name = RESEAUTELECOMENERGIE, map = "classpath:ReseauTelecomEnergie-map.js")
    public List<ReseauTelecomEnergie> getAllReseauTelecomEnergies() {
        return db.queryView(createQuery(RESEAUTELECOMENERGIE),
                ReseauTelecomEnergie.class);
    }

    public static final String OUVRAGETELECOMENERGIE = "OuvrageTelecomEnergie";

    @View(name = OUVRAGETELECOMENERGIE, map = "classpath:OuvrageTelecomEnergie-map.js")
    public List<OuvrageTelecomEnergie> getAllOuvrageTelecomEnergies() {
        return db.queryView(createQuery(OUVRAGETELECOMENERGIE),
                OuvrageTelecomEnergie.class);
    }

    public static final String RESEAUHYDROCIELOUVERT = "ReseauHydroCielOuvert";

    @View(name = RESEAUHYDROCIELOUVERT, map = "classpath:ReseauHydroCielOuvert-map.js")
    public List<ReseauHydroCielOuvert> getAllReseauHydroCielOuverts() {
        return db.queryView(createQuery(RESEAUHYDROCIELOUVERT),
                ReseauHydroCielOuvert.class);
    }

    public static final String OUVRAGEPARTICULIER = "OuvrageParticulier";

    @View(name = OUVRAGEPARTICULIER, map = "classpath:OuvrageParticulier-map.js")
    public List<OuvrageParticulier> getAllOuvrageParticuliers() {
        return db.queryView(createQuery(OUVRAGEPARTICULIER),
                OuvrageParticulier.class);
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

    public static final String LAISSECRUE = "LaisseCrue";

    @View(name = LAISSECRUE, map = "classpath:LaisseCrue-map.js")
    public List<LaisseCrue> getAllLaisseCrues() {
        return db.queryView(createQuery(LAISSECRUE), LaisseCrue.class);
    }

    public static final String MONTEEEAUX = "MonteeEaux";

    @View(name = MONTEEEAUX, map = "classpath:MonteeEaux-map.js")
    public List<MonteeEaux> getAllMonteeEaux() {
        return db.queryView(createQuery(MONTEEEAUX), MonteeEaux.class);
    }

    public static final String LIGNEEAU = "LigneEau";

    @View(name = LIGNEEAU, map = "classpath:LigneEau-map.js")
    public List<LigneEau> getAllLigneEaus() {
        return db.queryView(createQuery(LIGNEEAU), LigneEau.class);
    }

    public static final String DEVERSOIRE = "Deversoire";

    @View(name = DEVERSOIRE, map = "classpath:Deversoire-map.js")
    public List<Deversoire> getAllDeversoires() {
        return db.queryView(createQuery(DEVERSOIRE), Deversoire.class);
    }

    public static final String PISTEPIEDDIGUE = "PistePiedDigue";

    @View(name = PISTEPIEDDIGUE, map = "classpath:PistePiedDigue-map.js")
    public List<PistePiedDigue> getAllPistePiedDigues() {
        return db.queryView(createQuery(PISTEPIEDDIGUE), PistePiedDigue.class);
    }

    public static final String PROFILFRONTFRANCBORD = "ProfilFrontFrancBord";

    @View(name = PROFILFRONTFRANCBORD, map = "classpath:ProfilFrontFrancBord-map.js")
    public List<ProfilFrontFrancBord> getAllProfilFrontFrancBords() {
        return db.queryView(createQuery(PROFILFRONTFRANCBORD),
                ProfilFrontFrancBord.class);
    }

    public static final String EPI = "Epi";

    @View(name = EPI, map = "classpath:Epi-map.js")
    public List<Epi> getAllEpis() {
        return db.queryView(createQuery(EPI), Epi.class);
    }

    public static final String FRONTFRANCBORD = "FrontFrancBord";

    @View(name = FRONTFRANCBORD, map = "classpath:FrontFrancBord-map.js")
    public List<FrontFrancBord> getAllFrontFrancBords() {
        return db.queryView(createQuery(FRONTFRANCBORD), FrontFrancBord.class);
    }

    public static final String PIEDFRONTFRANCBORD = "PiedFrontFrancBord";

    @View(name = PIEDFRONTFRANCBORD, map = "classpath:PiedFrontFrancBord-map.js")
    public List<PiedFrontFrancBord> getAllPiedFrontFrancBords() {
        return db.queryView(createQuery(PIEDFRONTFRANCBORD),
                PiedFrontFrancBord.class);
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
        final Callable<List<? extends Objet>> callable = viewMap.get(view);
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
     * Cette contraint s'assure de supprimer les SR et bornes associées au troncon
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
        //on supprime toutes les bornes du troncon
        final BorneDigueRepository bdrepo = new BorneDigueRepository(db);
        for(String bdid : entity.getBorneIds()){
            try{
                final BorneDigue bd = bdrepo.get(bdid);
                bdrepo.remove(bd);
            }catch(DocumentNotFoundException ex){
                //la borne n'existe pas.
            }
        }
    }
    
}
