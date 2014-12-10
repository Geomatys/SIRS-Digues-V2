package fr.sirs.importer.objet.structure;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.troncon.TronconGestionDigueImporter;
import fr.sirs.core.model.Crete;
import fr.sirs.core.model.Fondation;
import fr.sirs.core.model.OuvrageParticulier;
import fr.sirs.core.model.OuvrageRevanche;
import fr.sirs.core.model.PiedDigue;
import fr.sirs.core.model.SommetRisberme;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.RefCote;
import fr.sirs.core.model.RefPosition;
import fr.sirs.core.model.RefSource;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TalusDigue;
import fr.sirs.core.model.TalusRisberme;
import fr.sirs.importer.objet.GenericObjetImporter;
import fr.sirs.importer.objet.TypeCoteImporter;
import fr.sirs.importer.objet.TypeFonctionImporter;
import fr.sirs.importer.objet.TypeMateriauImporter;
import fr.sirs.importer.objet.TypeNatureImporter;
import fr.sirs.importer.objet.TypePositionImporter;
import fr.sirs.importer.objet.SourceInfoImporter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ektorp.CouchDbConnector;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class ElementStructureImporter extends GenericStructureImporter<Objet> {
    
    private final TypeElementStructureImporter typeElementStructureImporter;
    
    private final List<GenericObjetImporter> structureImporters = new ArrayList<>();
    private final SysEvtCreteImporter creteImporter;
    private final SysEvtPiedDeDigueImporter piedDigueImporter;
    private final SysEvtTalusDigueImporter talusDigueImporter;
    private final SysEvtSommetRisbermeImporter sommetRisbermeImporter;
    private final SysEvtTalusRisbermeImporter talusRisbermeImporter;
    private final SysEvtFondationImporter fondationImporter;
    private final SysEvtEpisImporter epiImporter;
    private final SysEvtOuvrageRevancheImporter ouvrageRevancheImporter;
    private final SysEvtTalusFrancBordImporter talusFrancBordImporter;
    private final SysEvtPiedFrontFrancBordImporter sysEvtPiedFrontFrancBordImporter;
    

    public ElementStructureImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final TronconGestionDigueImporter tronconGestionDigueImporter, 
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter, 
            final SourceInfoImporter typeSourceImporter,
            final TypePositionImporter typePositionImporter,
            final TypeCoteImporter typeCoteImporter, 
            final TypeMateriauImporter typeMateriauImporter,
            final TypeNatureImporter typeNatureImporter,
            final TypeFonctionImporter typeFonctionImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter,
                typeSourceImporter, typeCoteImporter, 
                typePositionImporter, typeMateriauImporter, typeNatureImporter, 
                typeFonctionImporter);
        typeElementStructureImporter = new TypeElementStructureImporter(
                accessDatabase, couchDbConnector);
        creteImporter = new SysEvtCreteImporter(accessDatabase, couchDbConnector, 
                tronconGestionDigueImporter, systemeReperageImporter, 
                borneDigueImporter, typeSourceImporter, typeCoteImporter, 
                typePositionImporter, typeMateriauImporter, typeNatureImporter, 
                typeFonctionImporter);
        structureImporters.add(creteImporter);
        piedDigueImporter = new SysEvtPiedDeDigueImporter(accessDatabase, 
                couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter, 
                typeSourceImporter, typeCoteImporter, typePositionImporter, 
                typeMateriauImporter, typeNatureImporter, 
                typeFonctionImporter);
        structureImporters.add(piedDigueImporter);
        talusDigueImporter = new SysEvtTalusDigueImporter(accessDatabase, 
                couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter, 
                typeSourceImporter, typeCoteImporter, typePositionImporter, 
                typeMateriauImporter, typeNatureImporter, 
                typeFonctionImporter);
        structureImporters.add(talusDigueImporter);
        sommetRisbermeImporter = new SysEvtSommetRisbermeImporter(accessDatabase, 
                couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter, 
                typeSourceImporter, typeCoteImporter, typePositionImporter, 
                typeMateriauImporter, typeNatureImporter, 
                typeFonctionImporter);
        structureImporters.add(sommetRisbermeImporter);
        talusRisbermeImporter = new SysEvtTalusRisbermeImporter(accessDatabase, 
                couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter, 
                typeSourceImporter, typeCoteImporter, typePositionImporter, 
                typeMateriauImporter, typeNatureImporter, 
                typeFonctionImporter);
        structureImporters.add(talusRisbermeImporter);
        fondationImporter = new  SysEvtFondationImporter(accessDatabase, 
                couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter, 
                typeSourceImporter, typeCoteImporter, typePositionImporter, 
                typeMateriauImporter, typeNatureImporter, 
                typeFonctionImporter);
        structureImporters.add(fondationImporter);
        epiImporter = new SysEvtEpisImporter(accessDatabase, couchDbConnector, 
                tronconGestionDigueImporter, systemeReperageImporter, 
                borneDigueImporter, 
                typeSourceImporter, typeCoteImporter, typePositionImporter);
        structureImporters.add(epiImporter);
        ouvrageRevancheImporter = new SysEvtOuvrageRevancheImporter(accessDatabase, 
                couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter,
                typeSourceImporter, typeCoteImporter, typePositionImporter, 
                typeMateriauImporter, typeNatureImporter);
        structureImporters.add(ouvrageRevancheImporter);
        talusFrancBordImporter = new SysEvtTalusFrancBordImporter(
                accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter,
                typeSourceImporter, typeCoteImporter, typePositionImporter, 
                typeMateriauImporter, typeNatureImporter, 
                typeFonctionImporter);
        structureImporters.add(talusFrancBordImporter);
        sysEvtPiedFrontFrancBordImporter = new SysEvtPiedFrontFrancBordImporter(
                accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter, 
                typeSourceImporter, typeCoteImporter, typePositionImporter, 
                typeMateriauImporter, typeNatureImporter);
        structureImporters.add(sysEvtPiedFrontFrancBordImporter);
    }

    private enum Columns {

        ID_ELEMENT_STRUCTURE,
        ID_TYPE_ELEMENT_STRUCTURE,
        ID_TYPE_COTE,
        ID_SOURCE,
//        ID_TRONCON_GESTION,
        DATE_DEBUT_VAL,
        DATE_FIN_VAL,
        PR_DEBUT_CALCULE,
        PR_FIN_CALCULE,
        X_FIN,
        Y_FIN,
        ID_SYSTEME_REP,
        ID_BORNEREF_DEBUT,
        AMONT_AVAL_DEBUT,
        DIST_BORNEREF_DEBUT,
        ID_BORNEREF_FIN,
        AMONT_AVAL_FIN,
        DIST_BORNEREF_FIN,
        COMMENTAIRE,
//        N_COUCHE, // N'est pas disponible au niveau des structures dans le nouveau modèle
//        ID_TYPE_MATERIAU,
//        ID_TYPE_NATURE,
//        ID_TYPE_FONCTION,
//        EPAISSEUR, // N'est pas disponible au niveau des structures dans le nouveau modèle
//        TALUS_INTERCEPTE_CRETE,
//        ID_TYPE_NATURE_HAUT,
//        ID_TYPE_MATERIAU_HAUT,
//        ID_TYPE_MATERIAU_BAS,
//        ID_TYPE_NATURE_BAS,
//        LONG_RAMP_HAUT,
//        LONG_RAMP_BAS,
//        PENTE_INTERIEURE,
        ID_TYPE_POSITION,
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
    X_DEBUT,
    Y_DEBUT,
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
        return DbImporter.TableName.ELEMENT_STRUCTURE.toString();
    }
    
    @Override
    protected void compute() throws IOException, AccessDbImporterException {    
        structures = new HashMap<>();
        
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        final Map<Integer, RefSource> typesSource = sourceInfoImporter.getTypes();
        final Map<Integer, RefPosition> typesPosition = typePositionImporter.getTypes();
        final Map<Integer, RefCote> typesCote = typeCoteImporter.getTypes();

        for (final GenericObjetImporter gsi : structureImporters){
            final Map<Integer, Objet> objets = gsi.getById();
            if(objets!=null){
                for (final Integer key : objets.keySet()){
                    if(structures.get(key)!=null){
                        throw new AccessDbImporterException(objets.get(key).getClass().getCanonicalName()+" : This structure ID is ever used ("+key+") by "+structures.get(key).getClass().getCanonicalName());
                    }
                    else {
                        structures.put(key, objets.get(key));
                    }
                }
            }
        }
        
//        // Importation détaillée de toutes les structures au sens strict.
        final Map<Integer, Crete> cretes = creteImporter.getById();
//        if(cretes!=null) for(final Integer key : cretes.keySet()){
//            if(structures.get(key)!=null) throw new AccessDbImporterException(cretes.get(key).getClass().getCanonicalName()+" : This structure ID is ever used ("+key+") by "+structures.get(key).getClass().getCanonicalName());
//            else structures.put(key, cretes.get(key));
//        }
//
        final Map<Integer, PiedDigue> piedsDigue = piedDigueImporter.getById();
//        if(piedsDigue!=null) for(final Integer key : piedsDigue.keySet()){
//            if(structures.get(key)!=null) throw new AccessDbImporterException(piedsDigue.get(key).getClass().getCanonicalName()+" : This structure ID is ever used ("+key+") by "+structures.get(key).getClass().getCanonicalName());
//            else structures.put(key, piedsDigue.get(key));
//        }
//
        final Map<Integer, TalusDigue> talusDigue = talusDigueImporter.getById();
//        if(talusDigue!=null) for(final Integer key : talusDigue.keySet()){
//            if(structures.get(key)!=null) throw new AccessDbImporterException(talusDigue.get(key).getClass().getCanonicalName()+" : This structure ID is ever used ("+key+") by "+structures.get(key).getClass().getCanonicalName());
//            else structures.put(key, talusDigue.get(key));
//        }
        
        final Map<Integer, SommetRisberme> sommetsRisbermes = sommetRisbermeImporter.getById();
        final Map<Integer, TalusRisberme> talusRisbermes = talusRisbermeImporter.getById();
        
        //======================================================================


        // Vérification de la cohérence des structures au sens strict.
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();

            final int structureId = row.getInt(Columns.ID_ELEMENT_STRUCTURE.toString());
            final Class typeStructure = this.typeElementStructureImporter.getTypes().get(row.getInt(Columns.ID_TYPE_ELEMENT_STRUCTURE.toString()));
            final Objet structure;
            
            if(typeStructure==null){
//                System.out.println("Type de structure non pris en charge !");
                structure = null;
            }
            else if(typeStructure == Crete.class){
                structure = cretes.get(structureId);
            }
            else if(typeStructure == TalusDigue.class){
                structure = talusDigue.get(structureId);
            }
            else if(typeStructure == SommetRisberme.class){
                structure = sommetsRisbermes.get(structureId);
            }
            else if(typeStructure == TalusRisberme.class){
                structure = talusDigue.get(structureId);
            }
            else if(typeStructure == PiedDigue.class){
                structure = piedsDigue.get(structureId);
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
                structure.setSystemeRepId(systemesReperage.get(row.getInt(Columns.ID_SYSTEME_REP.toString())).getId());
                
                structure.setBorne_debut_aval(row.getBoolean(Columns.AMONT_AVAL_DEBUT.toString())); 
                structure.setBorne_fin_aval(row.getBoolean(Columns.AMONT_AVAL_FIN.toString()));
                structure.setCommentaire(row.getString(Columns.COMMENTAIRE.toString()));
                
                if (row.getDouble(Columns.PR_DEBUT_CALCULE.toString()) != null) {
                    structure.setPR_debut(row.getDouble(Columns.PR_DEBUT_CALCULE.toString()).floatValue());
                }
                if (row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()) != null) {
                    structure.setBorneDebutId(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()).doubleValue()).getId());
                }
                if (row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()) != null) {
                    structure.setBorne_debut_distance(row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()).floatValue());
                }
                if (row.getDouble(Columns.ID_BORNEREF_FIN.toString()) != null) {
                    structure.setBorneFinId(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_FIN.toString()).doubleValue()).getId());
                }
                if (row.getDouble(Columns.DIST_BORNEREF_FIN.toString()) != null) {
                    structure.setBorne_fin_distance(row.getDouble(Columns.DIST_BORNEREF_FIN.toString()).floatValue());
                }
                if (row.getDouble(Columns.PR_FIN_CALCULE.toString()) != null) {
                    structure.setPR_fin(row.getDouble(Columns.PR_FIN_CALCULE.toString()).floatValue());
                }
                
//                   if (row.getDouble(ElementStructureColumns.EPAISSEUR.toString()) != null) {
//                structure.setEpaisseur(row.getDouble(ElementStructureColumns.EPAISSEUR.toString()).floatValue());
//            }
//                   
//            structure.setNum_couche(row.getInt(ElementStructureColumns.N_COUCHE.toString()));
                
                
                if (row.getDate(Columns.DATE_DEBUT_VAL.toString()) != null) {
                structure.setDate_debut(LocalDateTime.parse(row.getDate(Columns.DATE_DEBUT_VAL.toString()).toString(), dateTimeFormatter));
            }
            if (row.getDate(Columns.DATE_FIN_VAL.toString()) != null) {
                structure.setDate_fin(LocalDateTime.parse(row.getDate(Columns.DATE_FIN_VAL.toString()).toString(), dateTimeFormatter));
            }
                
                if(row.getInt(Columns.ID_SOURCE.toString())!=null){
                structure.setSourceId(typesSource.get(row.getInt(Columns.ID_SOURCE.toString())).getId());
            }
            
            if(row.getInt(Columns.ID_TYPE_POSITION.toString())!=null){
                structure.setPosition_structure(typesPosition.get(row.getInt(Columns.ID_TYPE_POSITION.toString())).getId());
            }
            
            if(row.getInt(Columns.ID_TYPE_COTE.toString())!=null){
                structure.setCoteId(typesCote.get(row.getInt(Columns.ID_TYPE_COTE.toString())).getId());
            }
            
            GeometryFactory geometryFactory = new GeometryFactory();
            final MathTransform lambertToRGF;
            try {
                lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), getOutputCrs(), true);

                try {

                    if (row.getDouble(Columns.X_DEBUT.toString()) != null && row.getDouble(Columns.Y_DEBUT.toString()) != null) {
                        structure.setPositionDebut((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(Columns.X_DEBUT.toString()),
                                row.getDouble(Columns.Y_DEBUT.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(ElementStructureImporter.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {

                    if (row.getDouble(Columns.X_FIN.toString()) != null && row.getDouble(Columns.Y_FIN.toString()) != null) {
                        structure.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(Columns.X_FIN.toString()),
                                row.getDouble(Columns.Y_FIN.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(ElementStructureImporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (FactoryException ex) {
                Logger.getLogger(ElementStructureImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
                
                
                
                
                
                
            }
        }
        
        
        //======================================================================
        // Liens avec les désordres
        
        


        //======================================================================

        // Génération de la liste des structures par identifiant de tronçon.
        structuresByTronconId = new HashMap<>();

        
        
        
        // Structures au sens strict
        for (final GenericObjetImporter gsi : structureImporters) {
            final Map<Integer, List<Objet>> objetsByTronconId = gsi.getByTronconId();

            if (objetsByTronconId != null) {
                objetsByTronconId.keySet().stream().map((key) -> {
                    if (structuresByTronconId.get(key) == null) {
                        structuresByTronconId.put(key, new ArrayList<>());
                    }
                    return key;
                }).forEach((key) -> {
                    if (objetsByTronconId.get(key) != null) {
                        structuresByTronconId.get(key).addAll(objetsByTronconId.get(key));
                    }
                });
            }

        }

        
        
        
//        final Map<Integer, List<Crete>> cretesByTroncon = creteImporter.getStructuresByTronconId();
//        if(cretesByTroncon!=null){
//            cretesByTroncon.keySet().stream().map((key) -> {
//                if (structuresByTronconId.get(key) == null) {
//                    structuresByTronconId.put(key, new ArrayList<>());
//                }   return key;
//            }).forEach((key) -> {
//                if(cretesByTroncon.get(key)!=null)
//                    structuresByTronconId.get(key).addAll(cretesByTroncon.get(key));
//            });
//        }
//
//        final Map<Integer, List<PiedDigue>> piedsDigueByTroncon = piedDigueImporter.getStructuresByTronconId();
//        if(piedsDigueByTroncon!=null){
//        piedsDigue.keySet().stream().map((key) -> {
//            if (structuresByTronconId.get(key) == null) {
//                structuresByTronconId.put(key, new ArrayList<>());
//            }
//            return key;
//        }).forEach((key) -> {
//            if(piedsDigueByTroncon.get(key)!=null)
//                structuresByTronconId.get(key).addAll(piedsDigueByTroncon.get(key));
//        });
//        }
//
//        final Map<Integer, List<TalusDigue>> talusDigueByTroncon = talusDigueImporter.getStructuresByTronconId();
//        if(talusDigueByTroncon!=null){
//        talusDigue.keySet().stream().map((key) -> {
//            if (structuresByTronconId.get(key) == null) {
//                structuresByTronconId.put(key, new ArrayList<>());
//            }
//            return key;
//        }).forEach((key) -> {
//            if(talusDigueByTroncon.get(key)!=null)
//                structuresByTronconId.get(key).addAll(talusDigueByTroncon.get(key));
//        });
//        }
    }
}
