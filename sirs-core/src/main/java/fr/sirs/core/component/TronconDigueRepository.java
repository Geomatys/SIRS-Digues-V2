package fr.sirs.core.component;

import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.StreamingViewResult;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;
import org.ektorp.support.Views;
import org.springframework.beans.factory.annotation.Autowired;

import fr.sirs.core.JacksonIterator;
import fr.sirs.core.Repository;
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

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Views({
        @View(name = "all", map = "function(doc) {if(doc['@class']=='fr.sirs.core.model.TronconDigue') {emit(doc._id, doc._id)}}"),
        @View(name = "stream", map = "function(doc) {if(doc['@class']=='fr.sirs.core.model.TronconDigue') {emit(doc._id, doc)}}"),
        @View(name = "streamLight", map = "classpath:TronconDigueLight-map.js"),
        @View(name = "byDigueId", map = "function(doc) {if(doc['@class']=='fr.sirs.core.model.TronconDigue') {emit(doc.digueId, doc._id)}}") })
public class TronconDigueRepository extends
        CouchDbRepositorySupport<TronconDigue> implements
        Repository<TronconDigue> {

    @Autowired
    public TronconDigueRepository(CouchDbConnector db) {
        super(TronconDigue.class, db);
        initStandardDesignDocument();
    }

    public List<TronconDigue> getByDigue(final Digue digue) {
        return this.queryView("byDigueId", digue.getId());
    }

    @Override
    public void remove(TronconDigue entity) {
        //on supprime tous les SR associés
        final SystemeReperageRepository srrepo = new SystemeReperageRepository(db);
        final List<SystemeReperage> srs = srrepo.getByTroncon(entity);
        for(SystemeReperage sr : srs){
            srrepo.remove(sr);
        }
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

}
