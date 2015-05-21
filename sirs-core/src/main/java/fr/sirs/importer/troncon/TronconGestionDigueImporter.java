package fr.sirs.importer.troncon;

import fr.sirs.core.SirsCore;
import java.util.logging.Level;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import fr.sirs.core.LinearReferencingUtilities;
import fr.sirs.core.component.BorneDigueRepository;
import fr.sirs.core.component.DigueRepository;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.PeriodeCommune;
import fr.sirs.core.model.Digue;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.GardeTroncon;
import fr.sirs.core.model.GestionTroncon;
import fr.sirs.core.model.RefRive;
import fr.sirs.core.model.ProprieteTroncon;
import fr.sirs.core.model.SyndicTroncon;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.DigueImporter;
import fr.sirs.importer.DocumentsUpdatable;
import fr.sirs.importer.GenericImporter;
import fr.sirs.importer.IntervenantImporter;
import fr.sirs.importer.OrganismeImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.TronconDigueGeomImporter;
import fr.sirs.importer.evenementHydraulique.EvenementHydrauliqueImporter;
import fr.sirs.importer.TypeCoteImporter;
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
//    private Map<String, Integer> tronconsIds = null;
    
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
            final TypeCoteImporter typeCoteImporter,
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
                accessDatabase, couchDbConnector, systemeReperageImporter, 
                borneDigueImporter,intervenantImporter);
        tronconGestionDigueProprietaireImporter = new ProprietaireTronconGestionImporter(
                accessDatabase, couchDbConnector, systemeReperageImporter, 
                borneDigueImporter, intervenantImporter, organismeImporter);
        syndicatImporter = new SyndicatImporter(accessDatabase, couchDbConnector);
        communeImporter = new CommuneImporter(accessDatabase, couchDbConnector);
        this.tronconGestionDigueCommuneImporter = new TronconGestionDigueCommuneImporter(
                accessDatabase, couchDbConnector, systemeReperageImporter, 
                borneDigueImporter, communeImporter, 
                typeCoteImporter);
        this.tronconGestionDigueSyndicatImporter = new TronconGestionDigueSyndicatImporter(
                accessDatabase, couchDbConnector, syndicatImporter);
    }
    
    public BorneDigueRepository getBorneDigueRepository(){return borneDigueRepository;}

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
        return TRONCON_GESTION_DIGUE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        tronconsDigue = new HashMap<>();
//        tronconsIds = new HashMap<>();

        final Map<Integer, Geometry> tronconDigueGeoms = tronconDigueGeomImporter.getTronconDigueGeoms();
        final Map<Integer, RefRive> typesRive = typeRiveImporter.getTypeReferences();
        final Map<Integer, List<GestionTroncon>> gestionsByTroncon = tronconGestionDigueGestionnaireImporter.getGestionsByTronconId();
        final Map<Integer, List<GardeTroncon>> gardiensByTroncon = tronconGestionDigueGardienImporter.getGardiensByTronconId();
        final Map<Integer, List<ProprieteTroncon>> propriosByTroncon = tronconGestionDigueProprietaireImporter.getProprietairesByTronconId();
        final Map<Integer, List<SyndicTroncon>> syndicatsByTroncon = tronconGestionDigueSyndicatImporter.getSyndicatsByTronconId();
        final Map<Integer, List<BorneDigue>> bornesByTroncon = borneDigueImporter.getBorneDigueByTronconId();
        final Map<Integer, SystemeReperage> systemesReperageById = systemeReperageImporter.getSystemeRepLineaire();
        final Map<Integer, Digue> digues = digueImporter.getDigues();
        final Map<Integer, List<PeriodeCommune>> communes = tronconGestionDigueCommuneImporter.getCommunesByTronconId();

        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final TronconDigue tronconDigue = createAnonymValidElement(TronconDigue.class);
            
            tronconDigue.setLibelle(row.getString(Columns.NOM_TRONCON_GESTION.toString()));
            
            tronconDigue.setCommentaire(row.getString(Columns.COMMENTAIRE_TRONCON.toString()));
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                tronconDigue.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            
            if (row.getDate(Columns.DATE_DEBUT_VAL_TRONCON.toString()) != null) {
                tronconDigue.setDate_debut(DbImporter.parse(row.getDate(Columns.DATE_DEBUT_VAL_TRONCON.toString()), dateTimeFormatter));
            }
            
            if (row.getDate(Columns.DATE_FIN_VAL_TRONCON.toString()) != null) {
                tronconDigue.setDate_fin(DbImporter.parse(row.getDate(Columns.DATE_FIN_VAL_TRONCON.toString()), dateTimeFormatter));
            }
                
            if(row.getInt(Columns.ID_SYSTEME_REP_DEFAUT.toString())!=null){
                tronconDigue.setSystemeRepDefautId(systemesReperageById.get(row.getInt(Columns.ID_SYSTEME_REP_DEFAUT.toString())).getId());
            }
            
            // Register the troncon to retrieve a CouchDb ID.
            tronconDigueRepository.add(tronconDigue);
            
            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            tronconsDigue.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), tronconDigue);
//            tronconsIds.put(tronconDigue.getId(), row.getInt(Columns.ID_TRONCON_GESTION.toString()));


            // Set simple references.
            final List<GestionTroncon> gestions = gestionsByTroncon.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            if(gestions!=null) tronconDigue.setGestions(gestions);
            
            final List<GardeTroncon> gardes = gardiensByTroncon.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            if(gardes!=null) tronconDigue.setGardes(gardes);
            
            final List<ProprieteTroncon> proprietes = propriosByTroncon.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            if(proprietes!=null) tronconDigue.setProprietes(proprietes);
            
            final List<SyndicTroncon> syndicats = syndicatsByTroncon.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            if(syndicats!=null) tronconDigue.setSyndics(syndicats);
            // Fin des contacts
            
            final List<PeriodeCommune> communesTroncons = communes.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            if(communesTroncons!=null) tronconDigue.setCommunes(communesTroncons);
            // Fin des communes
            
            final List<BorneDigue> bornes = bornesByTroncon.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            if(bornes != null){
                final List<String> bornesIds = new ArrayList<>();
                bornes.stream().forEach((borne) -> {
                    bornesIds.add(borne.getId());
                });
                tronconDigue.setBorneIds(bornesIds);
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
                final Digue d = createAnonymValidElement(Digue.class);
                digueRepository.add(d);
                tronconDigue.setDigueId(d.getId());
            }
            
            tronconDigue.setDesignation(String.valueOf(row.getInt(Columns.ID_TRONCON_GESTION.toString())));
            
            // Set the geometry
            tronconDigue.setGeometry(tronconDigueGeoms.get(row.getInt(Columns.ID_TRONCON_GESTION.toString())));
        }
        
        //reconstruction des geometries des structures
        for(final Map.Entry<Integer,TronconDigue> entry : tronconsDigue.entrySet()){
            final TronconDigue troncon = entry.getValue();
            final Geometry tronconGeom = (Geometry) troncon.getGeometry();
            
            // NE PAS OUBLIER DE REBRANCHER LES PHOTOS AUX STRUCTURES !!!!!!
//            for(final Objet str : troncon.getStructures()){
//                try{
//                    final LineString structGeom = LinearReferencingUtilities.buildGeometry(tronconGeom, str, borneDigueRepository);
//                    str.setGeometry(structGeom);
//                    for(final Photo photo : str.getPhotos()){
//                        try{
//                            final LineString photoGeom = LinearReferencingUtilities.buildGeometry(structGeom, photo, borneDigueRepository);
//                            photo.setGeometry(photoGeom);
//                        }catch(IllegalArgumentException e){
//                            SirsCore.LOGGER.log(Level.FINE, e.getMessage());
//                        }
//                    }
//                }catch(IllegalArgumentException e){
//                    SirsCore.LOGGER.log(Level.FINE, e.getMessage());
//                }
//            }
//            for(final AbstractPositionDocument doc : troncon.getDocumentTroncon()){
//                try{
//                    final LineString docGeom = LinearReferencingUtilities.buildGeometry(tronconGeom, doc, borneDigueRepository);
//                    doc.setGeometry(docGeom);
//                }catch(IllegalArgumentException e){
//                    SirsCore.LOGGER.log(Level.FINE, e.getMessage());
//                }
//            }
            for(final GardeTroncon doc : troncon.getGardes()){
                try{
                    final LineString docGeom = LinearReferencingUtilities.buildGeometry(tronconGeom, doc, borneDigueRepository);
                    doc.setGeometry(docGeom);
                }catch(IllegalArgumentException e){
                    SirsCore.LOGGER.log(Level.FINE, e.getMessage());
                }
            }
            for(final ProprieteTroncon doc : troncon.getProprietes()){
                try{
                    final LineString docGeom = LinearReferencingUtilities.buildGeometry(tronconGeom, doc, borneDigueRepository);
                    doc.setGeometry(docGeom);
                }catch(IllegalArgumentException e){
                    SirsCore.LOGGER.log(Level.FINE, e.getMessage());
                }
            }
            
            //Update the repository
//            tronconDigueRepository.update(troncon);
        }
        couchDbConnector.executeBulk(tronconsDigue.values());
    }
}
