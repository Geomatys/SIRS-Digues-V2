package fr.sirs.importer.troncon;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import fr.sirs.core.LinearReferencingUtilities;
import fr.sirs.core.component.BorneDigueRepository;
import fr.sirs.core.component.DigueRepository;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.CommuneTroncon;
import fr.sirs.core.model.ContactTroncon;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.RefRive;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.DigueImporter;
import fr.sirs.importer.DocumentsUpdatable;
import fr.sirs.importer.GenericImporter;
import fr.sirs.importer.IntervenantImporter;
import fr.sirs.importer.OrganismeImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.TronconDigueGeomImporter;
import fr.sirs.importer.evenementHydraulique.EvenementHydrauliqueImporter;
import fr.sirs.importer.objet.ObjetManager;
import fr.sirs.importer.theme.document.related.marche.MarcheImporter;
import java.io.IOException;
import java.time.LocalDateTime;
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
public class TronconGestionDigueImporter 
extends GenericImporter 
implements DocumentsUpdatable {

    private Map<Integer, TronconDigue> tronconsDigue = null;
    private Map<String, Integer> tronconsIds = null;
    
    private final TronconDigueGeomImporter tronconDigueGeomImporter;
    private final TypeRiveImporter typeRiveImporter;
    private final SystemeReperageImporter systemeReperageImporter;
    private final TronconGestionDigueGestionnaireImporter tronconGestionDigueGestionnaireImporter;
    private final GardienTronconGestionImporter tronconGestionDigueGardienImporter;
    private final ProprietaireTronconGestionImporter tronconGestionDigueProprietaireImporter;
    private final TronconGestionDigueCommuneImporter tronconGestionDigueCommuneImporter;
    private final TronconGestionDigueSyndicatImporter tronconGestionDigueSyndicatImporter;
    private final DigueImporter digueImporter;
    private final BorneDigueImporter borneDigueImporter;
    private final SyndicatImporter syndicatImporter;
    private final CommuneImporter communeImporter;
    private final ObjetManager objetManager;
    
    private final DigueRepository digueRepository;
    private final TronconDigueRepository tronconDigueRepository;
    private final BorneDigueRepository borneDigueRepository;
    
    public TronconGestionDigueImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final TronconDigueRepository tronconDigueRepository,
            final DigueRepository digueRepository,
            final BorneDigueRepository borneDigueRepository,
            final DigueImporter digueImporter,
            final TronconDigueGeomImporter tronconDigueGeomImporter, 
            final SystemeReperageImporter systemeReperageImporter,
            final BorneDigueImporter borneDigueImporter, 
            final OrganismeImporter organismeImporter,
            final IntervenantImporter intervenantImporter,
            final MarcheImporter marcheImporter,
            final EvenementHydrauliqueImporter evenementHydrauliqueImporter){
        super(accessDatabase, couchDbConnector);
        this.tronconDigueRepository = tronconDigueRepository;
        this.digueRepository = digueRepository;
        this.borneDigueRepository = borneDigueRepository;
        this.digueImporter = digueImporter;
        this.tronconDigueGeomImporter = tronconDigueGeomImporter;
        this.typeRiveImporter = new TypeRiveImporter(accessDatabase, couchDbConnector);
        this.systemeReperageImporter = systemeReperageImporter;
        
        this.borneDigueImporter = borneDigueImporter;
        tronconGestionDigueGestionnaireImporter = new TronconGestionDigueGestionnaireImporter(
                accessDatabase, couchDbConnector, organismeImporter);
        tronconGestionDigueGardienImporter = new GardienTronconGestionImporter(
                accessDatabase, couchDbConnector, intervenantImporter);
        tronconGestionDigueProprietaireImporter = new ProprietaireTronconGestionImporter(
                accessDatabase, couchDbConnector, intervenantImporter, 
                organismeImporter);
        syndicatImporter = new SyndicatImporter(accessDatabase, couchDbConnector);
        communeImporter = new CommuneImporter(accessDatabase, couchDbConnector);
        objetManager = new ObjetManager(accessDatabase, couchDbConnector, this, 
                systemeReperageImporter, borneDigueImporter, organismeImporter, 
                intervenantImporter, marcheImporter, evenementHydrauliqueImporter);
        this.tronconGestionDigueCommuneImporter = new TronconGestionDigueCommuneImporter(
                accessDatabase, couchDbConnector, systemeReperageImporter, 
                borneDigueImporter, communeImporter, 
                objetManager.getTypeCoteImporter());
        this.tronconGestionDigueSyndicatImporter = new TronconGestionDigueSyndicatImporter(
                accessDatabase, couchDbConnector, syndicatImporter);
    }
    
    public ObjetManager getObjetManager(){return objetManager;}

    @Override
    public void update() throws IOException, AccessDbImporterException {
        if(tronconsDigue==null) compute();
        couchDbConnector.executeBulk(tronconsDigue.values());
    }
    
    private enum Columns {

        ID_TRONCON_GESTION, 
//        ID_ORG_GESTIONNAIRE, //Dans les gestions ?
        ID_DIGUE, 
        ID_TYPE_RIVE,
        DATE_DEBUT_VAL_TRONCON, 
        DATE_FIN_VAL_TRONCON,
        NOM_TRONCON_GESTION, 
        COMMENTAIRE_TRONCON,
//        DATE_DEBUT_VAL_GESTIONNAIRE_D, //Dans les gestions ? 
//        DATE_FIN_VAL_GESTIONNAIRE_D, //Dans les gestions ?
        ID_SYSTEME_REP_DEFAUT, 
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all TronconDigue instances accessibles from 
     * the internal database identifier.
     * @throws IOException 
     * @throws fr.sirs.importer.AccessDbImporterException 
     */
    public Map<Integer, TronconDigue> getTronconsDigues() throws IOException, AccessDbImporterException {
        if(tronconsDigue == null) compute();
        return tronconsDigue;
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
        return DbImporter.TableName.TRONCON_GESTION_DIGUE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        tronconsDigue = new HashMap<>();
        tronconsIds = new HashMap<>();

        final Map<Integer, Geometry> tronconDigueGeoms = tronconDigueGeomImporter.getTronconDigueGeoms();
        final Map<Integer, RefRive> typesRive = typeRiveImporter.getTypes();
        final Map<Integer, List<ContactTroncon>> gestionsByTroncon = tronconGestionDigueGestionnaireImporter.getGestionsByTronconId();
        final Map<Integer, List<ContactTroncon>> gardiensByTroncon = tronconGestionDigueGardienImporter.getGardiensByTronconId();
        final Map<Integer, List<ContactTroncon>> propriosByTroncon = tronconGestionDigueProprietaireImporter.getProprietairesByTronconId();
        final Map<Integer, List<ContactTroncon>> syndicatsByTroncon = tronconGestionDigueSyndicatImporter.getSyndicatsByTronconId();
        final Map<Integer, List<BorneDigue>> bornesByTroncon = borneDigueImporter.getBorneDigueByTronconId();
        final Map<Integer, List<SystemeReperage>> systemesReperageByTroncon = systemeReperageImporter.getSystemeRepLineaireByTronconId();
        final Map<Integer, SystemeReperage> systemesReperageById = systemeReperageImporter.getSystemeRepLineaire();
        final Map<Integer, Digue> digues = digueImporter.getDigues();
        final Map<Integer, List<CommuneTroncon>> communes = tronconGestionDigueCommuneImporter.getCommunesByTronconId();

        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final TronconDigue tronconDigue = new TronconDigue();

            tronconDigue.setLibelle(row.getString(Columns.NOM_TRONCON_GESTION.toString()));
            tronconDigue.setCommentaire(row.getString(Columns.COMMENTAIRE_TRONCON.toString()));
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                tronconDigue.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            if (row.getDate(Columns.DATE_DEBUT_VAL_TRONCON.toString()) != null) {
                tronconDigue.setDate_debut(LocalDateTime.parse(row.getDate(Columns.DATE_DEBUT_VAL_TRONCON.toString()).toString(), dateTimeFormatter));
            }
            if (row.getDate(Columns.DATE_FIN_VAL_TRONCON.toString()) != null) {
                tronconDigue.setDate_fin(LocalDateTime.parse(row.getDate(Columns.DATE_FIN_VAL_TRONCON.toString()).toString(), dateTimeFormatter));
            }

            // Register the troncon to retrieve a CouchDb ID.
            tronconDigueRepository.add(tronconDigue);
            
            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            tronconsDigue.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), tronconDigue);
            tronconsIds.put(tronconDigue.getId(), row.getInt(Columns.ID_TRONCON_GESTION.toString()));


            // Set simple references.
            List<ContactTroncon> contacts;
            
            final List<ContactTroncon> gestions = gestionsByTroncon.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            contacts=gestions;
            
            final List<ContactTroncon> gardiens = gardiensByTroncon.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            if(contacts != null && gardiens!=null) contacts.addAll(gardiens);
            else if(contacts==null) contacts=gardiens;
            
            final List<ContactTroncon> proprietaires = propriosByTroncon.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            if(contacts != null && proprietaires!=null) contacts.addAll(proprietaires);
            else if (contacts==null) contacts=proprietaires;
            
            final List<ContactTroncon> syndicats = syndicatsByTroncon.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            if(contacts != null && syndicats!=null) contacts.addAll(syndicats);
            else if (contacts==null) contacts=syndicats;
            
            if(contacts!=null) tronconDigue.setContacts(contacts);
            // Fin des contacts
            
            final List<CommuneTroncon> communesTroncons = communes.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            if(communesTroncons!=null) tronconDigue.setCommuneTroncon(communesTroncons);
            // Fin des communes
            
            final List<BorneDigue> bornes = bornesByTroncon.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            if(bornes != null){
                final List<String> bornesIds = new ArrayList<>();
                bornes.stream().forEach((borne) -> {
                    bornesIds.add(borne.getId());
                });
                tronconDigue.setBorneIds(bornesIds);
            }

            final List<SystemeReperage> systemesRep = systemesReperageByTroncon.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            if(systemesRep!=null){
                final List<String> systemesRepIds = new ArrayList<>();
                systemesRep.stream().forEach((systemeRep) -> {
                    systemesRepIds.add(systemeRep.getId());
                    //systemeRep.setTronconId(tronconDigue.getId());
                    });
                //tronconDigue.setSystemeReperageIds(systemesRepIds);
                tronconDigue.setSystemeRepDefautId(systemesReperageById.get(row.getInt(Columns.ID_SYSTEME_REP_DEFAUT.toString())).getId());
            }
            
            if (row.getInt(Columns.ID_TYPE_RIVE.toString()) != null) {
                final RefRive typeRive = typesRive.get(row.getInt(Columns.ID_TYPE_RIVE.toString()));
                if(typeRive!=null){
                    tronconDigue.setTypeRiveId(typeRive.getId());
                }
            }
            
            // Set the references demanding CouchDb identifier.
            final Digue digue = digues.get(row.getInt(Columns.ID_DIGUE.toString()));
            if (digue != null) {
                if (digue.getId() != null) {
                    tronconDigue.setDigueId(digue.getId());
                } else {
                    throw new AccessDbImporterException("La digue " + digue + " n'a pas encore d'identifiant CouchDb !");
                }
            } else {
                final Digue d = new Digue();
                digueRepository.add(d);
                tronconDigue.setDigueId(d.getId());
            }

            // Set the geometry
            tronconDigue.setGeometry(tronconDigueGeoms.get(row.getInt(Columns.ID_TRONCON_GESTION.toString())));
        }
        
        objetManager.link();
        
        for(final TronconDigue tronconDigue : tronconsDigue.values()){
            List<Objet> structures = tronconDigue.getStructures();
            
            structures.addAll(objetManager.getByTronconId(tronconsIds.get(tronconDigue.getId())));

            //Update the repository
//            tronconDigueRepository.update(tronconDigue);
        }
        
        //reconstruction des geometries des structures
        for(final Map.Entry<Integer,TronconDigue> entry : tronconsDigue.entrySet()){
            final TronconDigue troncon = entry.getValue();
            final Geometry tronconGeom = (Geometry) troncon.getGeometry();
            for(final Objet str : troncon.getStructures()){
                try{
                    final LineString structGeom = LinearReferencingUtilities.buildGeometry(tronconGeom, str, borneDigueRepository);
                    str.setGeometry(structGeom);
                }catch(IllegalArgumentException e){
                    System.out.println(e.getMessage());
                }
            }
            
            //Update the repository
            tronconDigueRepository.update(troncon);
        }
        couchDbConnector.executeBulk(tronconsDigue.values());
        
    }
}
