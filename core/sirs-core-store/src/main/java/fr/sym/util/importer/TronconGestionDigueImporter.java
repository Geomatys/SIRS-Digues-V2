/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.util.importer;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Geometry;
import fr.sym.util.importer.structure.StructureImporter;
import fr.symadrem.sirs.core.component.TronconDigueRepository;
import fr.symadrem.sirs.core.model.BorneDigue;
import fr.symadrem.sirs.core.model.Digue;
import fr.symadrem.sirs.core.model.GestionTroncon;
import fr.symadrem.sirs.core.model.Structure;
import fr.symadrem.sirs.core.model.SystemeReperage;
import fr.symadrem.sirs.core.model.TronconDigue;
import fr.symadrem.sirs.core.model.TypeRive;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
    
    private TronconDigueRepository tronconDigueRepository;
    
    private TronconGestionDigueImporter(Database accessDatabase) {
        super(accessDatabase);
    }
    
    TronconGestionDigueImporter(final Database accessDatabase, 
            final TronconDigueRepository tronconDigueRepository,
            final DigueImporter digueImporter,
            final TronconDigueGeomImporter tronconDigueGeomImporter, 
            final TypeRiveImporter typeRiveImporter, 
            final SystemeReperageImporter systemeReperageImporter,
            final TronconGestionDigueGestionnaireImporter tronconGestionDigueGestionnaireImporter, 
            final BorneDigueImporter borneDigueImporter){
        this(accessDatabase);
        this.tronconDigueRepository = tronconDigueRepository;
        this.digueImporter = digueImporter;
        this.tronconDigueGeomImporter = tronconDigueGeomImporter;
        this.typeRiveImporter = typeRiveImporter;
        this.systemeReperageImporter = systemeReperageImporter;
        this.tronconGestionDigueGestionnaireImporter = tronconGestionDigueGestionnaireImporter;
        this.borneDigueImporter = borneDigueImporter;
        
        this.structureImporter = new StructureImporter(accessDatabase, this);
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
        return "TRONCON_GESTION_DIGUE";
    }

    /* TODO : s'occuper du lien avec les gestionnaires.
     * TODO : s'occuper du lien avec les rives.
     = TODO : s'occuper du lien avec les systèmes de repérage.
     = TODO : faire les structures.
     = TODO : s'occuper des bornes.
     */
    private enum TronconGestionDigueColumns {

        ID_TRONCON_GESTION, 
//        ID_ORG_GESTIONNAIRE, 
        ID_DIGUE, 
        ID_TYPE_RIVE,
        DATE_DEBUT_VAL_TRONCON, 
        DATE_FIN_VAL_TRONCON,
        NOM_TRONCON_GESTION, 
        COMMENTAIRE_TRONCON,
//        DATE_DEBUT_VAL_GESTIONNAIRE_D, 
//        DATE_FIN_VAL_GESTIONNAIRE_D, 
        ID_SYSTEME_REP_DEFAUT, 
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all TronconDigue instances accessibles from 
     * the internal database identifier.
     * @throws IOException 
     * @throws fr.sym.util.importer.AccessDbImporterException 
     */
    public Map<Integer, TronconDigue> getTronconsDigues() throws IOException, AccessDbImporterException {

        if(tronconsDigue == null){
            tronconsDigue = new HashMap<>();
            tronconsIds = new HashMap<>();
            final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();

            final Map<Integer, Geometry> tronconDigueGeoms = tronconDigueGeomImporter.getTronconDigueGeoms();
            final Map<Integer, TypeRive> typesRive = typeRiveImporter.getTypeRive();
            final Map<Integer, SystemeReperage> systemesRep = systemeReperageImporter.getSystemeRepLineaire();

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
                
                // nécessite que les systèmes de repérage aient été enregistrés comme des documents
                //SystemeReperage systemeReperageDefaut = systemesRep.get(row.getInt(TronconGestionDigueColumns.ID_SYSTEME_REP_DEFAUT.toString()));
                //tronconDigue.setSysteme_reperage_defaut(systemeReperageDefaut.getId());

                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                tronconsDigue.put(row.getInt(TronconGestionDigueColumns.ID_TRONCON_GESTION.toString()), tronconDigue);
                tronconsIds.put(tronconDigue, row.getInt(TronconGestionDigueColumns.ID_TRONCON_GESTION.toString()));

                // Register the troncon to retrieve a CouchDb ID.
                tronconDigueRepository.add(tronconDigue);
                
                // Set simple references.
                final List<GestionTroncon> gestions = tronconGestionDigueGestionnaireImporter.getGestionsByTronconId().get(row.getInt(TronconGestionDigueColumns.ID_TRONCON_GESTION.toString()));
                if(gestions != null) tronconDigue.setGestionnaires(gestions);
                
                
                final List<BorneDigue> bornes = borneDigueImporter.getBorneDigueByTronconId().get(row.getInt(TronconGestionDigueColumns.ID_TRONCON_GESTION.toString()));
                if(bornes != null) tronconDigue.setBorneIds(bornes);

                tronconDigue.setTypeRive(typesRive.get(row.getInt(TronconGestionDigueColumns.ID_TYPE_RIVE.toString())).toString());
                
                // Set the references demanding CouchDb identifier.
                final Digue digue = digueImporter.getDigues().get(row.getInt(TronconGestionDigueColumns.ID_DIGUE.toString()));
                if(digue.getId()!=null){
                    tronconDigue.setDigueId(digue.getId());
                }else {
                    throw new AccessDbImporterException("La digue "+digue+" n'a pas encore d'identifiant CouchDb !");
                }
                
                // Set the geometry
                tronconDigue.setGeometry(tronconDigueGeoms.get(row.getInt(TronconGestionDigueColumns.ID_TRONCON_GESTION.toString())));
            }
            
            
            // Set the references using the this very importer (Structures references TronconDigueId).
            for(final TronconDigue tronconDigue : tronconsDigue.values()){
                
                List<Structure> structures = tronconDigue.getStuctures();
                if(structures==null){
                    structures = new ArrayList<>();
                    tronconDigue.setStuctures(structures);
                }
                
                if(structureImporter.getStructuresByTronconId().get(tronconsIds.get(tronconDigue))!=null)
                    structures.addAll(structureImporter.getStructuresByTronconId().get(tronconsIds.get(tronconDigue)));
                
                //Update the repository
                tronconDigueRepository.update(tronconDigue);
            }
        }
            return tronconsDigue;
    }
}
