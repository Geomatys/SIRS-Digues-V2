package fr.sirs.importer;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import fr.sirs.importer.structure.StructureImporter;
import fr.sirs.core.LinearReferencingUtilities;
import fr.sirs.core.component.BorneDigueRepository;
import fr.sirs.core.component.DigueRepository;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.GestionTroncon;
import fr.sirs.core.model.RefRive;
import fr.sirs.core.model.Structure;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
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
public class TronconGestionDigueImporter extends GenericImporter {

    private Map<Integer, TronconDigue> tronconsDigue = null;
    private Map<TronconDigue, Integer> tronconsIds = null;
    private TronconDigueGeomImporter tronconDigueGeomImporter;
    private TypeRiveImporter typeRiveImporter;
    private SystemeReperageImporter systemeReperageImporter;
    private TronconGestionDigueGestionnaireImporter tronconGestionDigueGestionnaireImporter;
    private DigueImporter digueImporter;
    private BorneDigueImporter borneDigueImporter;
    private StructureImporter structureImporter;
    
    private DigueRepository digueRepository;
    private TronconDigueRepository tronconDigueRepository;
    private BorneDigueRepository borneDigueRepository;
    
    private TronconGestionDigueImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    TronconGestionDigueImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final TronconDigueRepository tronconDigueRepository,
            final DigueRepository digueRepository,
            final BorneDigueRepository borneDigueRepository,
            final DigueImporter digueImporter,
            final TronconDigueGeomImporter tronconDigueGeomImporter, 
            final TypeRiveImporter typeRiveImporter, 
            final SystemeReperageImporter systemeReperageImporter,
            final TronconGestionDigueGestionnaireImporter tronconGestionDigueGestionnaireImporter, 
            final BorneDigueImporter borneDigueImporter){
        this(accessDatabase, couchDbConnector);
        this.tronconDigueRepository = tronconDigueRepository;
        this.digueRepository = digueRepository;
        this.borneDigueRepository = borneDigueRepository;
        this.digueImporter = digueImporter;
        this.tronconDigueGeomImporter = tronconDigueGeomImporter;
        this.typeRiveImporter = typeRiveImporter;
        this.systemeReperageImporter = systemeReperageImporter;
        this.tronconGestionDigueGestionnaireImporter = tronconGestionDigueGestionnaireImporter;
        this.borneDigueImporter = borneDigueImporter;
        
        this.structureImporter = new StructureImporter(accessDatabase, couchDbConnector, this, systemeReperageImporter, borneDigueImporter);
    }

    /* TODO : s'occuper du lien avec les gestionnaires.
     * TODO : s'occuper du lien avec les rives.
     = TODO : s'occuper du lien avec les systèmes de repérage.
     = TODO : faire les structures.
     = TODO : s'occuper des bornes.
     */
    private enum TronconGestionDigueColumns {

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
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TronconGestionDigueColumns c : TronconGestionDigueColumns.values()) {
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
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();

        final Map<Integer, Geometry> tronconDigueGeoms = tronconDigueGeomImporter.getTronconDigueGeoms();
        final Map<Integer, RefRive> typesRive = typeRiveImporter.getTypeRive();

        while (it.hasNext()) {
            final Row row = it.next();
            final TronconDigue tronconDigue = new TronconDigue();

            tronconDigue.setNom(row.getString(TronconGestionDigueColumns.NOM_TRONCON_GESTION.toString()));
            tronconDigue.setCommentaire(row.getString(TronconGestionDigueColumns.COMMENTAIRE_TRONCON.toString()));
            if (row.getDate(TronconGestionDigueColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                tronconDigue.setDateMaj(LocalDateTime.parse(row.getDate(TronconGestionDigueColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            if (row.getDate(TronconGestionDigueColumns.DATE_DEBUT_VAL_TRONCON.toString()) != null) {
                tronconDigue.setDate_debut(LocalDateTime.parse(row.getDate(TronconGestionDigueColumns.DATE_DEBUT_VAL_TRONCON.toString()).toString(), dateTimeFormatter));
            }
            if (row.getDate(TronconGestionDigueColumns.DATE_FIN_VAL_TRONCON.toString()) != null) {
                tronconDigue.setDate_fin(LocalDateTime.parse(row.getDate(TronconGestionDigueColumns.DATE_FIN_VAL_TRONCON.toString()).toString(), dateTimeFormatter));
            }

            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            tronconsDigue.put(row.getInt(TronconGestionDigueColumns.ID_TRONCON_GESTION.toString()), tronconDigue);
            tronconsIds.put(tronconDigue, row.getInt(TronconGestionDigueColumns.ID_TRONCON_GESTION.toString()));

            // Register the troncon to retrieve a CouchDb ID.
            tronconDigueRepository.add(tronconDigue);

            // Set simple references.
            final List<GestionTroncon> gestions = tronconGestionDigueGestionnaireImporter.getGestionsByTronconId().get(row.getInt(TronconGestionDigueColumns.ID_TRONCON_GESTION.toString()));
            if(gestions != null) {
                tronconDigue.setGestionnaires(gestions);
            }


            final List<BorneDigue> bornes = borneDigueImporter.getBorneDigueByTronconId().get(row.getInt(TronconGestionDigueColumns.ID_TRONCON_GESTION.toString()));
            if(bornes != null){
                final List<String> bornesIds = new ArrayList<>();
                bornes.stream().forEach((borne) -> {
                    bornesIds.add(borne.getId());
                });
                tronconDigue.setBorneIds(bornesIds);
            }

            final List<SystemeReperage> systemesRep = systemeReperageImporter.getSystemeRepLineaireByTronconId().get(row.getInt(TronconGestionDigueColumns.ID_TRONCON_GESTION.toString()));
            if(systemesRep!=null){
                final List<String> systemesRepIds = new ArrayList<>();
                systemesRep.stream().forEach((systemeRep) -> {
                    systemesRepIds.add(systemeRep.getId());
                    systemeRep.setTronconId(tronconDigue.getId());
                    });
                tronconDigue.setSystemeReperageIds(systemesRepIds);
                tronconDigue.setSysteme_reperage_defaut(systemeReperageImporter.getSystemeRepLineaire().get(row.getInt(TronconGestionDigueColumns.ID_SYSTEME_REP_DEFAUT.toString())).getId());
            }
            
            if (row.getInt(TronconGestionDigueColumns.ID_TYPE_RIVE.toString()) != null) {
                final RefRive typeRive = typesRive.get(row.getInt(TronconGestionDigueColumns.ID_TYPE_RIVE.toString()));
                if(typeRive!=null){
                    tronconDigue.setTypeRive(typeRive.getLibelle());
                }
            }

            // Set the references demanding CouchDb identifier.
            final Digue digue = digueImporter.getDigues().get(row.getInt(TronconGestionDigueColumns.ID_DIGUE.toString()));
//            System.out.println("Le tronçon : "+row.getInt(TronconGestionDigueColumns.ID_TRONCON_GESTION.toString())+"|| "+row.getString(TronconGestionDigueColumns.NOM_TRONCON_GESTION.toString())+"|| la digue : "+row.getInt(TronconGestionDigueColumns.ID_DIGUE.toString()));
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
            tronconDigue.setGeometry(tronconDigueGeoms.get(row.getInt(TronconGestionDigueColumns.ID_TRONCON_GESTION.toString())));
        }


        // Set the references using the this very importer (Structures references TronconDigueId).
        for(final TronconDigue tronconDigue : tronconsDigue.values()){
            List<Structure> structures = tronconDigue.getStructures();
            if(structures==null){
                structures = new ArrayList<>();
                tronconDigue.setStructures(structures);
            }

            if(structureImporter.getStructuresByTronconId().get(tronconsIds.get(tronconDigue))!=null)
                structures.addAll(structureImporter.getStructuresByTronconId().get(tronconsIds.get(tronconDigue)));

            //Update the repository
            tronconDigueRepository.update(tronconDigue);
        }
        
        
        
        //reconstruction des geometries des structures
        for(final Map.Entry<Integer,TronconDigue> entry : tronconsDigue.entrySet()){
            final TronconDigue troncon = entry.getValue();
            final Geometry tronconGeom = (Geometry) troncon.getGeometry();
            for(final Structure str : troncon.getStructures()){
                final LineString structGeom = LinearReferencingUtilities.buildGeometry(tronconGeom, str, borneDigueRepository);
                str.setGeometry(structGeom);
            }
            
            //Update the repository
            tronconDigueRepository.update(troncon);
        }
        
    }
}
