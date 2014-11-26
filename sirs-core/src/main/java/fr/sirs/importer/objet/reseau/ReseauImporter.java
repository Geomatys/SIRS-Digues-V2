package fr.sirs.importer.objet.reseau;

import fr.sirs.importer.objet.geometry.*;
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
import fr.sirs.importer.TronconGestionDigueImporter;
import fr.sirs.core.model.Crete;
import fr.sirs.core.model.LargeurFrancBord;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.RefCote;
import fr.sirs.core.model.RefPosition;
import fr.sirs.core.model.RefSource;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.importer.OrganismeImporter;
import fr.sirs.importer.objet.GenericStructureImporter;
import fr.sirs.importer.objet.TypeCoteImporter;
import fr.sirs.importer.objet.TypeFonctionImporter;
import fr.sirs.importer.objet.TypeMateriauImporter;
import fr.sirs.importer.objet.TypeNatureImporter;
import fr.sirs.importer.objet.TypePositionImporter;
import fr.sirs.importer.objet.TypeSourceImporter;
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
public class ReseauImporter extends GenericStructureImporter {

    private Map<Integer, List<Objet>> structuresByTronconId = null;
    private Map<Integer, Objet> structures = null;
    private final TypeElementReseauImporter typeElementReseauImporter;
    
    private final List<GenericStructureImporter> structureImporters = new ArrayList<>();
//    private final TypeLargeurFrancBordImporter typeLargeurFrancBordImporter;
//    private final LargeurFrancBordImporter largeurFrancBordImporter;
//    private final TypeProfilFrontFrancBordImporter typeProfilFrontFrancBordImporter;
//    private final ProfilFrontFrancBordImporter profilFrontFrancBordImporter;
    

    public ReseauImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final TronconGestionDigueImporter tronconGestionDigueImporter, 
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter, 
            final OrganismeImporter organismeImporter,
            final TypeSourceImporter typeSourceImporter,
            final TypePositionImporter typePositionImporter,
            final TypeCoteImporter typeCoteImporter, 
            final TypeMateriauImporter typeMateriauImporter,
            final TypeNatureImporter typeNatureImporter,
            final TypeFonctionImporter typeFonctionImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter, organismeImporter,
                typeSourceImporter, typeCoteImporter, typePositionImporter, 
                typeMateriauImporter, typeNatureImporter, typeFonctionImporter);
        typeElementReseauImporter = new TypeElementReseauImporter(
                accessDatabase, couchDbConnector);
//        typeLargeurFrancBordImporter = new TypeLargeurFrancBordImporter(
//                accessDatabase, couchDbConnector);
//        largeurFrancBordImporter = new LargeurFrancBordImporter(accessDatabase, 
//                couchDbConnector, tronconGestionDigueImporter, 
//                systemeReperageImporter, borneDigueImporter, organismeImporter, 
//                typeSourceImporter, typePositionImporter, typeCoteImporter, 
//                typeMateriauImporter, typeNatureImporter, typeFonctionImporter, 
//                typeLargeurFrancBordImporter);
//        structureImporters.add(largeurFrancBordImporter);
//        typeProfilFrontFrancBordImporter = new TypeProfilFrontFrancBordImporter(
//                accessDatabase, couchDbConnector);
//        profilFrontFrancBordImporter = new ProfilFrontFrancBordImporter(
//                accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
//                systemeReperageImporter, borneDigueImporter, organismeImporter, 
//                typeSourceImporter, typePositionImporter, typeCoteImporter, 
//                typeMateriauImporter, typeNatureImporter, typeFonctionImporter, 
//                typeProfilFrontFrancBordImporter);
//        structureImporters.add(profilFrontFrancBordImporter);
    }

    private enum ElementReseauColumns {
        ID_ELEMENT_RESEAU,
//        ID_TYPE_ELEMENT_RESEAU,
//        ID_TYPE_COTE,
//        ID_SOURCE,
//        ID_TRONCON_GESTION,
//        DATE_DEBUT_VAL,
//        DATE_FIN_VAL,
//        PR_DEBUT_CALCULE,
//        PR_FIN_CALCULE,
//        X_DEBUT,
//        Y_DEBUT,
//        X_FIN,
//        Y_FIN,
//        ID_SYSTEME_REP,
//        ID_BORNEREF_DEBUT,
//        AMONT_AVAL_DEBUT,
//        DIST_BORNEREF_DEBUT,
//        ID_BORNEREF_FIN,
//        AMONT_AVAL_FIN,
//        DIST_BORNEREF_FIN,
//        COMMENTAIRE,
//        NOM,
//        ID_ECOULEMENT,
//        ID_IMPLANTATION,
//        ID_UTILISATION_CONDUITE,
//        ID_TYPE_CONDUITE_FERMEE,
//        AUTORISE,
//        ID_TYPE_OUVR_HYDRAU_ASSOCIE,
//        ID_TYPE_RESEAU_COMMUNICATION,
//        ID_OUVRAGE_COMM_NRJ,
//        N_SECTEUR,
//        ID_TYPE_VOIE_SUR_DIGUE,
//        ID_OUVRAGE_VOIRIE,
//        ID_TYPE_REVETEMENT,
//        ID_TYPE_USAGE_VOIE,
//        LARGEUR,
//        ID_TYPE_OUVRAGE_VOIRIE,
//        ID_TYPE_POSITION,
//        ID_TYPE_POSITION_HAUTE,
//        HAUTEUR,
//        DIAMETRE,
//        ID_TYPE_RESEAU_EAU,
//        ID_ORG_PROPRIO,
//        ID_ORG_GESTION,
//        ID_INTERV_PROPRIO,
//        ID_INTERV_GARDIEN,
//        ID_TYPE_OUVRAGE_PARTICULIER,
//        DATE_DEBUT_ORGPROPRIO,
//        DATE_FIN_ORGPROPRIO,
//        DATE_DEBUT_GESTION,
//        DATE_FIN_GESTION,
//        DATE_DEBUT_INTERVPROPRIO,
//        DATE_FIN_INTERVPROPRIO,
//        ID_TYPE_OUVRAGE_TELECOM_NRJ,
//        ID_TYPE_ORIENTATION_OUVRAGE_FRANCHISSEMENT,
//        ID_TYPE_OUVRAGE_FRANCHISSEMENT,
//        DATE_DERNIERE_MAJ,
//        Z_SEUIL,
//        ID_TYPE_SEUIL,
//        ID_TYPE_GLISSIERE,
//        ID_TYPE_NATURE_BATARDEAUX,
//        NOMBRE,
//        POIDS,
//        ID_TYPE_MOYEN_MANIP_BATARDEAUX,
//        ID_ORG_STOCKAGE_BATARDEAUX,
//        ID_ORG_MANIP_BATARDEAUX,
//        ID_INTERV_MANIP_BATARDEAUX,
//        ID_TYPE_NATURE,
//        ID_TYPE_NATURE_HAUT,
//        ID_TYPE_NATURE_BAS,
//        ID_TYPE_REVETEMENT_HAUT,
//        ID_TYPE_REVETEMENT_BAS
    };

    /**
     * 
     * @return A map referencing the Structure (geometries) instances from the internal 
     * database tronçon identifier.
     * @throws IOException
     * @throws AccessDbImporterException 
     */
    public Map<Integer, List<Objet>> getStructuresByTronconId() throws IOException, AccessDbImporterException {
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
    public Map<Integer, Objet> getStructures() throws IOException, AccessDbImporterException{
        if(structures==null) compute();
        return structures;
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (ElementReseauColumns c : ElementReseauColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.ELEMENT_RESEAU.toString();
    }
    
    @Override
    protected void compute() throws IOException, AccessDbImporterException {    
        structures = new HashMap<>();
        
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        final Map<Integer, RefSource> typesSource = typeSourceImporter.getTypeSource();
        final Map<Integer, RefPosition> typesPosition = typePositionImporter.getTypePosition();
        final Map<Integer, RefCote> typesCote = typeCoteImporter.getTypeCote();

        for (final GenericStructureImporter gsi : structureImporters){
            final Map<Integer, Objet> objets = gsi.getStructures();
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
//        final Map<Integer, LargeurFrancBord> largeurFrancBord = largeurFrancBordImporter.getStructures();
//        if(cretes!=null) for(final Integer key : cretes.keySet()){
//            if(structures.get(key)!=null) throw new AccessDbImporterException(cretes.get(key).getClass().getCanonicalName()+" : This structure ID is ever used ("+key+") by "+structures.get(key).getClass().getCanonicalName());
//            else structures.put(key, cretes.get(key));
//        }
//
//        final Map<Integer, PiedDigue> piedsDigue = piedDigueImporter.getStructures();
////        if(piedsDigue!=null) for(final Integer key : piedsDigue.keySet()){
////            if(structures.get(key)!=null) throw new AccessDbImporterException(piedsDigue.get(key).getClass().getCanonicalName()+" : This structure ID is ever used ("+key+") by "+structures.get(key).getClass().getCanonicalName());
////            else structures.put(key, piedsDigue.get(key));
////        }
////
//        final Map<Integer, TalusDigue> talusDigue = talusDigueImporter.getStructures();
////        if(talusDigue!=null) for(final Integer key : talusDigue.keySet()){
////            if(structures.get(key)!=null) throw new AccessDbImporterException(talusDigue.get(key).getClass().getCanonicalName()+" : This structure ID is ever used ("+key+") by "+structures.get(key).getClass().getCanonicalName());
////            else structures.put(key, talusDigue.get(key));
////        }
//        
//        final Map<Integer, SommetRisberme> sommetsRisbermes = sommetRisbermeImporter.getStructures();
//        final Map<Integer, TalusRisberme> talusRisbermes = talusRisbermeImporter.getStructures();
        
        //======================================================================


        // Vérification de la cohérence des structures au sens strict.
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();

            final int structureId = row.getInt(ElementReseauColumns.ID_ELEMENT_RESEAU.toString());
            final Class typeStructure = this.typeElementReseauImporter.getTypeElementStructure().get(row.getInt(ElementReseauColumns.ID_ELEMENT_RESEAU.toString()));
            final Objet structure;
            
            if(typeStructure==null){
//                System.out.println("Type de structure non pris en charge !");
                structure = null;
            }
            else if(typeStructure == Crete.class){
//                structure = largeurFrancBord.get(structureId);
//            }
//            else if(typeStructure == TalusDigue.class){
//                structure = talusDigue.get(structureId);
//            }
//            else if(typeStructure == SommetRisberme.class){
//                structure = sommetsRisbermes.get(structureId);
//            }
//            else if(typeStructure == TalusRisberme.class){
//                structure = talusDigue.get(structureId);
//            }
//            else if(typeStructure == PiedDigue.class){
//                structure = piedsDigue.get(structureId);
//            }
//                else if(typeStructure == Crete.class){
//                }
//                else if(typeStructure == Crete.class){
//                }
//            else if(typeStructure == Fondation.class){
//                structure = null;
//            }
//            else if(typeStructure == OuvrageParticulier.class){
//                structure = null;
//            }
//                else if(typeStructure == Crete.class){
//                }
//                else if(typeStructure == Crete.class){
//                }
//                else if(typeStructure == Crete.class){
//                }
//            else if(typeStructure == OuvrageRevanche.class){
//                structure = null;

            } else {
//                System.out.println("Type de structure inconnu !");
                structure = null;
            }


            if (false && structure != null) {
//                structure.setSystemeRepId(systemesReperage.get(row.getInt(ElementGeometryColumns.ID_SYSTEME_REP.toString())).getId());
//                
//                structure.setBorne_debut_aval(row.getBoolean(ElementGeometryColumns.AMONT_AVAL_DEBUT.toString())); 
//                structure.setBorne_fin_aval(row.getBoolean(ElementGeometryColumns.AMONT_AVAL_FIN.toString()));
//                structure.setCommentaire(row.getString(ElementGeometryColumns.COMMENTAIRE.toString()));
//                
//                if (row.getDouble(ElementGeometryColumns.PR_DEBUT_CALCULE.toString()) != null) {
//                    structure.setPR_debut(row.getDouble(ElementGeometryColumns.PR_DEBUT_CALCULE.toString()).floatValue());
//                }
//                if (row.getDouble(ElementGeometryColumns.ID_BORNEREF_DEBUT.toString()) != null) {
//                    structure.setBorneDebutId(bornes.get((int) row.getDouble(ElementGeometryColumns.ID_BORNEREF_DEBUT.toString()).doubleValue()).getId());
//                }
//                if (row.getDouble(ElementGeometryColumns.DIST_BORNEREF_DEBUT.toString()) != null) {
//                    structure.setBorne_debut_distance(row.getDouble(ElementGeometryColumns.DIST_BORNEREF_DEBUT.toString()).floatValue());
//                }
//                if (row.getDouble(ElementGeometryColumns.ID_BORNEREF_FIN.toString()) != null) {
//                    structure.setBorneFinId(bornes.get((int) row.getDouble(ElementGeometryColumns.ID_BORNEREF_FIN.toString()).doubleValue()).getId());
//                }
//                if (row.getDouble(ElementGeometryColumns.DIST_BORNEREF_FIN.toString()) != null) {
//                    structure.setBorne_fin_distance(row.getDouble(ElementGeometryColumns.DIST_BORNEREF_FIN.toString()).floatValue());
//                }
//                if (row.getDouble(ElementGeometryColumns.PR_FIN_CALCULE.toString()) != null) {
//                    structure.setPR_fin(row.getDouble(ElementGeometryColumns.PR_FIN_CALCULE.toString()).floatValue());
//                }
                
//                   if (row.getDouble(ElementStructureColumns.EPAISSEUR.toString()) != null) {
//                structure.setEpaisseur(row.getDouble(ElementStructureColumns.EPAISSEUR.toString()).floatValue());
//            }
//                   
//            structure.setNum_couche(row.getInt(ElementStructureColumns.N_COUCHE.toString()));
                
                
//                if (row.getDate(ElementGeometryColumns.DATE_DEBUT_VAL.toString()) != null) {
//                structure.setDate_debut(LocalDateTime.parse(row.getDate(ElementGeometryColumns.DATE_DEBUT_VAL.toString()).toString(), dateTimeFormatter));
//            }
//            if (row.getDate(ElementGeometryColumns.DATE_FIN_VAL.toString()) != null) {
//                structure.setDate_fin(LocalDateTime.parse(row.getDate(ElementGeometryColumns.DATE_FIN_VAL.toString()).toString(), dateTimeFormatter));
//            }
//                
//                if(row.getInt(ElementGeometryColumns.ID_SOURCE.toString())!=null){
//                structure.setSourceId(typesSource.get(row.getInt(ElementGeometryColumns.ID_SOURCE.toString())).getId());
//            }
//            
//            if(row.getInt(ElementGeometryColumns.ID_TYPE_POSITION.toString())!=null){
//                structure.setPosition_structure(typesPosition.get(row.getInt(ElementGeometryColumns.ID_TYPE_POSITION.toString())).getId());
//            }
//            
//            if(row.getInt(ElementGeometryColumns.ID_TYPE_COTE.toString())!=null){
//                structure.setCoteId(typesCote.get(row.getInt(ElementGeometryColumns.ID_TYPE_COTE.toString())).getId());
//            }
//            
//            GeometryFactory geometryFactory = new GeometryFactory();
//            final MathTransform lambertToRGF;
//            try {
//                lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), CRS.decode("EPSG:2154"), true);
//
//                try {
//
//                    if (row.getDouble(ElementGeometryColumns.X_DEBUT.toString()) != null && row.getDouble(ElementGeometryColumns.Y_DEBUT.toString()) != null) {
//                        structure.setPositionDebut((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
//                                row.getDouble(ElementGeometryColumns.X_DEBUT.toString()),
//                                row.getDouble(ElementGeometryColumns.Y_DEBUT.toString()))), lambertToRGF));
//                    }
//                } catch (MismatchedDimensionException | TransformException ex) {
//                    Logger.getLogger(ReseauImporter.class.getName()).log(Level.SEVERE, null, ex);
//                }
//
//                try {
//
//                    if (row.getDouble(ElementGeometryColumns.X_FIN.toString()) != null && row.getDouble(ElementGeometryColumns.Y_FIN.toString()) != null) {
//                        structure.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
//                                row.getDouble(ElementGeometryColumns.X_FIN.toString()),
//                                row.getDouble(ElementGeometryColumns.Y_FIN.toString()))), lambertToRGF));
//                    }
//                } catch (MismatchedDimensionException | TransformException ex) {
//                    Logger.getLogger(ReseauImporter.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            } catch (FactoryException ex) {
//                Logger.getLogger(ReseauImporter.class.getName()).log(Level.SEVERE, null, ex);
//            }
//                
//                
                
                
                
                
            }
        }
        
        
        //======================================================================
        // Liens avec les désordres
        
        


        //======================================================================

        // Génération de la liste des structures par identifiant de tronçon.
        structuresByTronconId = new HashMap<>();

        
        
        
        // Structures au sens strict
        for (final GenericStructureImporter gsi : structureImporters) {
            final Map<Integer, List<Objet>> objetsByTronconId = gsi.getStructuresByTronconId();

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
