/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.util.importer.structure;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sym.util.importer.AccessDbImporterException;
import fr.sym.util.importer.BorneDigueImporter;
import fr.sym.util.importer.DbImporter;
import fr.sym.util.importer.SystemeReperageImporter;
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
import java.util.Map.Entry;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class StructureImporter extends GenericStructureImporter {

    private Map<Integer, List<Structure>> structuresByTronconId = null;
    private Map<Integer, Structure> structures = null;
    private final CreteImporter creteImporter;
    private final DesordreImporter desordreImporter;
    private final PiedDigueImporter piedDigueImporter;
    private final TypeElementStructureImporter typeElementStructureImporter;

    public StructureImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final TronconGestionDigueImporter tronconGestionDigueImporter, 
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter, systemeReperageImporter, borneDigueImporter);
        this.creteImporter = new CreteImporter(accessDatabase, couchDbConnector, tronconGestionDigueImporter, systemeReperageImporter, borneDigueImporter);
        this.desordreImporter = new DesordreImporter(accessDatabase, couchDbConnector, tronconGestionDigueImporter, systemeReperageImporter, borneDigueImporter);
        this.piedDigueImporter = new PiedDigueImporter(accessDatabase, couchDbConnector, tronconGestionDigueImporter, systemeReperageImporter, borneDigueImporter);
        this.typeElementStructureImporter = new TypeElementStructureImporter(accessDatabase, couchDbConnector);
    }

    private enum ElementStructureColumns {

        ID_ELEMENT_STRUCTURE,
        ID_TYPE_ELEMENT_STRUCTURE,
//        ID_TYPE_COTE,
//        ID_SOURCE,
//        ID_TRONCON_GESTION,
//        DATE_DEBUT_VAL,
//        DATE_FIN_VAL,
        PR_DEBUT_CALCULE,
        PR_FIN_CALCULE,
//        X_FIN,
//        Y_FIN,
        ID_SYSTEME_REP,
        ID_BORNEREF_DEBUT,
        AMONT_AVAL_DEBUT,
        DIST_BORNEREF_DEBUT,
        ID_BORNEREF_FIN,
        AMONT_AVAL_FIN,
        DIST_BORNEREF_FIN,
        COMMENTAIRE,
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
        if (structuresByTronconId == null)  compute();
        return structuresByTronconId;
    }
    
    /**
     * 
     * @return A map referencing the Structure instances from their internal 
     * database identifier.
     * @throws IOException
     * @throws AccessDbImporterException 
     */
    public Map<Integer, Structure> getStructures() throws IOException, AccessDbImporterException{
        if(structures==null) compute();
        return structures;
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
    
    @Override
    protected void compute() throws IOException, AccessDbImporterException {    
        structures = new HashMap<>();

        final Map<Integer, Crete> cretes = creteImporter.getCretes();
        if(cretes!=null) for(final Integer key : cretes.keySet()){
            structures.put(key, cretes.get(key));
        }

        final Map<Integer, PiedDigue> piedsDigue = piedDigueImporter.getPiedsDigue();
        if(piedsDigue!=null) for(final Integer key : piedsDigue.keySet()){
            structures.put(key, piedsDigue.get(key));
        }

        final Map<Integer, Desordre> desordres = desordreImporter.getDesordres();
        if(desordres!=null) for(final Integer key : desordres.keySet()){
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
//                System.out.println("Type de structure non pris en charge !");
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
//                System.out.println("Type de structure inconnu !");
                structure = null;
            }


            if (false && structure != null) {
                structure.setSysteme_rep_id(systemeReperageImporter.getSystemeRepLineaire().get(row.getInt(ElementStructureColumns.ID_SYSTEME_REP.toString())).getId());
                
                structure.setBorne_debut_aval(row.getBoolean(ElementStructureColumns.AMONT_AVAL_DEBUT.toString())); 
                structure.setBorne_fin_aval(row.getBoolean(ElementStructureColumns.AMONT_AVAL_FIN.toString()));
                structure.setCommentaire(row.getString(ElementStructureColumns.COMMENTAIRE.toString()));
                
                if (row.getDouble(ElementStructureColumns.PR_DEBUT_CALCULE.toString()) != null) {
                    structure.setPR_debut(row.getDouble(ElementStructureColumns.PR_DEBUT_CALCULE.toString()).floatValue());
                }
                if (row.getDouble(ElementStructureColumns.PR_FIN_CALCULE.toString()) != null) {
                    structure.setPR_fin(row.getDouble(ElementStructureColumns.PR_FIN_CALCULE.toString()).floatValue());
                }
                if (row.getDouble(ElementStructureColumns.ID_BORNEREF_DEBUT.toString()) != null) {
                    structure.setBorne_debut(borneDigueImporter.getBorneDigue().get((int) row.getDouble(ElementStructureColumns.ID_BORNEREF_DEBUT.toString()).doubleValue()).getId());
                }
                if (row.getDouble(ElementStructureColumns.DIST_BORNEREF_DEBUT.toString()) != null) {
                    structure.setBorne_debut_distance(row.getDouble(ElementStructureColumns.DIST_BORNEREF_DEBUT.toString()).floatValue());
                }
                if (row.getDouble(ElementStructureColumns.ID_BORNEREF_FIN.toString()) != null) {
                    structure.setBorne_fin(borneDigueImporter.getBorneDigue().get((int) row.getDouble(ElementStructureColumns.ID_BORNEREF_FIN.toString()).doubleValue()).getId());
                }
                if (row.getDouble(ElementStructureColumns.DIST_BORNEREF_FIN.toString()) != null) {
                    structure.setBorne_fin_distance(row.getDouble(ElementStructureColumns.DIST_BORNEREF_FIN.toString()).floatValue());
                }
            }
        }







        structuresByTronconId = new HashMap<>();

        final Map<Integer, List<Crete>> cretesByTroncon = creteImporter.getCretesByTronconId();
        if(cretesByTroncon!=null){
            cretesByTroncon.keySet().stream().map((key) -> {
                if (structuresByTronconId.get(key) == null) {
                    structuresByTronconId.put(key, new ArrayList<>());
                }   return key;
            }).forEach((key) -> {
                if(cretesByTroncon.get(key)!=null)
                    structuresByTronconId.get(key).addAll(cretesByTroncon.get(key));
            });
        }

        final Map<Integer, List<PiedDigue>> piedsDigueByTroncon = piedDigueImporter.getPiedsDigueByTronconId();
        if(piedsDigueByTroncon!=null){
        piedsDigue.keySet().stream().map((key) -> {
            if (structuresByTronconId.get(key) == null) {
                structuresByTronconId.put(key, new ArrayList<>());
            }
            return key;
        }).forEach((key) -> {
            if(piedsDigueByTroncon.get(key)!=null)
                structuresByTronconId.get(key).addAll(piedsDigueByTroncon.get(key));
        });
        }

        final Map<Integer, List<Desordre>> desordresByTroncon = desordreImporter.getDesordresByTronconId();
        if(desordresByTroncon!=null){
        desordres.keySet().stream().map((key) -> {
            if (structuresByTronconId.get(key) == null) {
                structuresByTronconId.put(key, new ArrayList<>());
            }
            return key;
        }).forEach((key) -> {
            if(desordresByTroncon.get(key)!=null)
                structuresByTronconId.get(key).addAll(desordresByTroncon.get(key));
        });
        }

        ////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////

    }
}
