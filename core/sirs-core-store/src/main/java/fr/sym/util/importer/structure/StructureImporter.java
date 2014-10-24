/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.util.importer.structure;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sym.util.importer.AccessDbImporterException;
import fr.sym.util.importer.DbImporter;
import fr.sym.util.importer.GenericImporter;
import fr.sym.util.importer.TronconGestionDigueImporter;
import fr.symadrem.sirs.core.model.Crete;
import fr.symadrem.sirs.core.model.Desordre;
import fr.symadrem.sirs.core.model.Fondation;
import fr.symadrem.sirs.core.model.OuvrageParticulier;
import fr.symadrem.sirs.core.model.OuvrageRevanche;
import fr.symadrem.sirs.core.model.PiedDigue;
import fr.symadrem.sirs.core.model.SommetRisberme;
import fr.symadrem.sirs.core.model.Structure;
import fr.symadrem.sirs.core.model.TalusDigue;
import fr.symadrem.sirs.core.model.TalusRisberme;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class StructureImporter extends GenericImporter {

    private Map<Integer, List<Structure>> structuresByTronconId = null;
    private Map<Integer, Structure> structures = null;
    private CreteImporter creteImporter;
    private DesordreImporter desordreImporter;
    private PiedDigueImporter piedDigueImporter;
    private TypeElementStructureImporter typeElementStructureImporter;

    private StructureImporter(Database accessDatabase) {
        super(accessDatabase);
    }

    public StructureImporter(final Database accessDatabase, final TronconGestionDigueImporter tronconGestionDigueImporter) {
        this(accessDatabase);
        this.creteImporter = new CreteImporter(accessDatabase, tronconGestionDigueImporter);
        this.desordreImporter = new DesordreImporter(accessDatabase, tronconGestionDigueImporter);
        this.piedDigueImporter = new PiedDigueImporter(accessDatabase, tronconGestionDigueImporter);
        this.typeElementStructureImporter = new TypeElementStructureImporter(accessDatabase);
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (ElementStructureColumns c : ElementStructureColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.ELEMENT_STRUCTURE.toString();
    }

    private enum ElementStructureColumns {

        ID_ELEMENT_STRUCTURE,
        ID_TYPE_ELEMENT_STRUCTURE,
//        ID_TYPE_COTE,
//        ID_SOURCE,
//        ID_TRONCON_GESTION,
//        DATE_DEBUT_VAL,
//        DATE_FIN_VAL,
//        PR_DEBUT_CALCULE,
//        PR_FIN_CALCULE,
//        X_FIN,
//        Y_FIN,
//        ID_SYSTEME_REP,
//        ID_BORNEREF_DEBUT,
//        AMONT_AVAL_DEBUT,
        DIST_BORNEREF_DEBUT,
//        ID_BORNEREF_FIN,
//        AMONT_AVAL_FIN,
        DIST_BORNEREF_FIN,
//        COMMENTAIRE,
//        N_COUCHE,
//        ID_TYPE_MATERIAU,
//        ID_TYPE_NATURE,
//        ID_TYPE_FONCTION,
//        EPAISSEUR,
//        TALUS_INTERCEPTE_CRETE,
//        ID_TYPE_NATURE_HAUT,
//        ID_TYPE_MATERIAU_HAUT,
//        ID_TYPE_MATERIAU_BAS,
//        ID_TYPE_NATURE_BAS,
//        LONG_RAMP_HAUT,
//        LONG_RAMP_BAS,
//        PENTE_INTERIEURE,
//        ID_TYPE_POSITION,
//        ID_TYPE_VEGETATION,
//        HAUTEUR,
//        EPAISSEUR_Y11,
//        EPAISSEUR_Y21,
//        EPAISSEUR_Y12,
//        EPAISSEUR_Y22,
//        ID_TYPE_VEGETATION_ESSENCE_1,
//        ID_TYPE_VEGETATION_ESSENCE_2,
//        ID_TYPE_VEGETATION_ESSENCE_3,
//        ID_TYPE_VEGETATION_ESSENCE_4,
//        DISTANCE_AXE_M,
//        PENTE_PCT,
//        ID_CONTACT_EAU_ON,
//        RECOUVREMENT_STRATE_1,
//        RECOUVREMENT_STRATE_2,
//        RECOUVREMENT_STRATE_3,
//        RECOUVREMENT_STRATE_4,
//        RECOUVREMENT_STRATE_5,
//        ID_TYPE_VEGETATION_ABONDANCE,
//        ID_TYPE_VEGETATION_STRATE_DIAMETRE,
//        ID_TYPE_VEGETATION_STRATE_HAUTEUR,
//        DENSITE_STRATE_DOMINANTE,
//        ID_TYPE_VEGETATION_ETAT_SANITAIRE,
//        ID_ABONDANCE_BRAUN_BLANQUET_RENOUE,
//        ID_ABONDANCE_BRAUN_BLANQUET_BUDLEIA,
//        ID_ABONDANCE_BRAUN_BLANQUET_SOLIDAGE,
//        ID_ABONDANCE_BRAUN_BLANQUET_VIGNE_VIERGE,
//        ID_ABONDANCE_BRAUN_BLANQUET_S_YEBLE,
//        ID_ABONDANCE_BRAUN_BLANQUET_E_NEGUN,
//        ID_ABONDANCE_BRAUN_BLANQUET_IMPA_GLANDUL,
//        ID_ABONDANCE_BRAUN_BLANQUET_GLOBAL,

        
        // Empty fields
//    X_DEBUT,
//    Y_DEBUT,
//    ID_TYPE_OUVRAGE_PARTICULIER,
//    ID_ORG_PROPRIO,
//    ID_ORG_GESTION,
//    ID_INTERV_PROPRIO,
//    ID_INTERV_GARDIEN,
//    DATE_DEBUT_ORGPROPRIO,
//    DATE_FIN_ORGPROPRIO,
//    DATE_DEBUT_GESTION,
//    DATE_FIN_GESTION,
//    DATE_DEBUT_INTERVPROPRIO,
//    DATE_FIN_INTERVPROPRIO,
//    ID_TYPE_COMPOSITION,
//    DISTANCE_TRONCON,
//    LONGUEUR,
//    DATE_DEBUT_GARDIEN,
//    DATE_FIN_GARDIEN,
//    LONGUEUR_PERPENDICULAIRE,
//    LONGUEUR_PARALLELE,
//    COTE_AXE,
//    DIAMETRE,
//    DENSITE,
//    NUMERO_PARCELLE,
//    NUMERO_FORMATION_VEGETALE,
//    DATE_DERNIERE_MAJ,
//    LARGEUR
    };

    /**
     * 
     * @return A map referencing the Structure instances from the internal 
     * database tronçon identifier.
     * @throws IOException
     * @throws AccessDbImporterException 
     */
    public Map<Integer, List<Structure>> getStructuresByTronconId() throws IOException, AccessDbImporterException {
        this.getStructures();
        if (structuresByTronconId == null) {
            structuresByTronconId = new HashMap<>();

            final Map<Integer, List<Crete>> cretes = creteImporter.getCretesByTronconId();
            cretes.keySet().stream().map((key) -> {
                if (structuresByTronconId.get(key) == null) {
                    structuresByTronconId.put(key, new ArrayList<>());
                }
                return key;
            }).forEach((key) -> {
                structuresByTronconId.get(key).addAll(cretes.get(key));
            });

            final Map<Integer, List<PiedDigue>> piedsDigue = piedDigueImporter.getPiedsDigueByTronconId();
            piedsDigue.keySet().stream().map((key) -> {
                if (structuresByTronconId.get(key) == null) {
                    structuresByTronconId.put(key, new ArrayList<>());
                }
                return key;
            }).forEach((key) -> {
                structuresByTronconId.get(key).addAll(piedsDigue.get(key));
            });

            final Map<Integer, List<Desordre>> desordres = desordreImporter.getDesordresByTronconId();
            desordres.keySet().stream().map((key) -> {
                if (structuresByTronconId.get(key) == null) {
                    structuresByTronconId.put(key, new ArrayList<>());
                }
                return key;
            }).forEach((key) -> {
                structuresByTronconId.get(key).addAll(desordres.get(key));
            });
            
            ////////////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////
          
            
        }
        return structuresByTronconId;
    }
    
    
    
    public Map<Integer, Structure> getStructures() throws IOException, AccessDbImporterException{
        if(structures==null){    
            structures = new HashMap<>();
            
            final Map<Integer, Crete> cretes = creteImporter.getCretes();
            for(final Integer key : cretes.keySet()){
                structures.put(key, cretes.get(key));
            }

            final Map<Integer, PiedDigue> piedsDigue = piedDigueImporter.getPiedsDigue();
            for(final Integer key : piedsDigue.keySet()){
                structures.put(key, piedsDigue.get(key));
            }

            final Map<Integer, Desordre> desordres = desordreImporter.getDesordres();
            for(final Integer key : desordres.keySet()){
                structures.put(key, desordres.get(key));
            }
            
            ////////////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////
            
            
            final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
            
            while (it.hasNext()) {
                final Row row = it.next();
                
                final int structureId = row.getInt(ElementStructureColumns.ID_ELEMENT_STRUCTURE.toString());
                final Class typeStructure = this.typeElementStructureImporter.getTypeElementStructure().get(row.getInt(ElementStructureColumns.ID_TYPE_ELEMENT_STRUCTURE.toString()));
                final Structure structure;
                
                if(typeStructure==null){
                    System.out.println("Type de structure non pris en charge !");
                    System.out.println(row);
                    structure = null;
                }
                else if(typeStructure == Crete.class){
                    structure = this.creteImporter.getCretes().get(structureId);
                }
                else if(typeStructure == TalusDigue.class){
                    structure = null;
                }
                else if(typeStructure == SommetRisberme.class){
                    structure = null;
                }
                else if(typeStructure == TalusRisberme.class){
                    structure = null;
                }
                else if(typeStructure == PiedDigue.class){
                    structure = this.piedDigueImporter.getPiedsDigue().get(structureId);
                }
//                else if(typeStructure == Crete.class){
//                }
//                else if(typeStructure == Crete.class){
//                }
                else if(typeStructure == Fondation.class){
                    structure = null;
                }
                else if(typeStructure == OuvrageParticulier.class){
                    structure = null;
                }
//                else if(typeStructure == Crete.class){
//                }
//                else if(typeStructure == Crete.class){
//                }
//                else if(typeStructure == Crete.class){
//                }
                else if(typeStructure == OuvrageRevanche.class){
                    structure = null;
                    
                } else {
                    System.out.println("Type de structure inconnu !");
                    System.out.println(row);
                    structure = null;
                }
                
                
                if(structure!=null){
                    
                    System.out.println(structure.getBorne_debut_distance()+"  "+structure.getBorne_fin_distance()+(((structure instanceof Crete)||(structure instanceof PiedDigue) ||(structure instanceof Desordre)) ? " "+structure.getClass().getName() :" Autre chose"));
//                if ( row.getDouble(ElementStructureColumns.DIST_BORNEREF_DEBUT.toString()) != null) {
//                    structure.setBorne_debut_distance(row.getDouble(ElementStructureColumns.DIST_BORNEREF_DEBUT.toString()).floatValue());
//                }
//                if (row.getDouble(ElementStructureColumns.DIST_BORNEREF_FIN.toString()) != null) {
//                    structure.setBorne_fin_distance(row.getDouble(ElementStructureColumns.DIST_BORNEREF_FIN.toString()).floatValue());
//                }
                }
                
                
                
                
                
                
                
            }
        }
        return structures;
    }
}
