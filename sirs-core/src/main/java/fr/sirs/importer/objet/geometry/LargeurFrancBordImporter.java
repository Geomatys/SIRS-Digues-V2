package fr.sirs.importer.objet.geometry;

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
import fr.sirs.core.model.RefCote;
import fr.sirs.core.model.RefFonction;
import fr.sirs.core.model.RefMateriau;
import fr.sirs.core.model.RefNature;
import fr.sirs.core.model.RefPosition;
import fr.sirs.core.model.RefSource;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
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
class LargeurFrancBordImporter extends GenericStructureImporter<LargeurFrancBord> {

    private Map<Integer, LargeurFrancBord> cretes = null;
    private Map<Integer, List<LargeurFrancBord>> cretesByTronconId = null;

    LargeurFrancBordImporter(final Database accessDatabase,
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
    }
    
    private enum CreteColumns {
//ID_ELEMENT_GEOMETRIE,
//id_nom_element,
//ID_SOUS_GROUPE_DONNEES,
//LIBELLE_TYPE_ELEMENT_GEOMETRIE,
//DECALAGE_DEFAUT,
//DECALAGE,
//LIBELLE_SOURCE,
//LIBELLE_SYSTEME_REP,
//NOM_BORNE_DEBUT,
//NOM_BORNE_FIN,
//LIBELLE_TYPE_LARGEUR_FB,
//LIBELLE_TYPE_PROFIL_FB,
//LIBELLE_TYPE_DIST_DIGUE_BERGE,
//ID_TRONCON_GESTION,
//ID_TYPE_ELEMENT_GEOMETRIE,
//ID_SOURCE,
//DATE_DEBUT_VAL,
//DATE_FIN_VAL,
//PR_DEBUT_CALCULE,
//PR_FIN_CALCULE,
//X_DEBUT,
//Y_DEBUT,
//X_FIN,
//Y_FIN,
//ID_SYSTEME_REP,
//ID_BORNEREF_DEBUT,
//AMONT_AVAL_DEBUT,
//DIST_BORNEREF_DEBUT,
//ID_BORNEREF_FIN,
//AMONT_AVAL_FIN,
//DIST_BORNEREF_FIN,
//COMMENTAIRE,
//ID_TYPE_LARGEUR_FB,
//ID_TYPE_PROFIL_FB,
//ID_TYPE_DIST_DIGUE_BERGE,
//ID_AUTO
    };

    /**
     *
     * @return A map containing all LargeurFrancBord instances accessibles from the
     * internal database identifier.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    @Override
    public Map<Integer, LargeurFrancBord> getStructures() throws IOException, AccessDbImporterException {
        if (this.cretes == null) {
            compute();
        }
        return cretes;
    }

    /**
     *
     * @return A map containing all LargeurFrancBord instances accessibles from the
     * internal database <em>TronconDigue</em> identifier.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    @Override
    public Map<Integer, List<LargeurFrancBord>> getStructuresByTronconId() throws IOException, AccessDbImporterException {
        if (this.cretesByTronconId == null) {
            compute();
        }
        return this.cretesByTronconId;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.SYS_EVT_LARGEUR_FRANC_BORD.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {

        this.cretes = new HashMap<>();
        this.cretesByTronconId = new HashMap<>();
        
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        final Map<Integer, TronconDigue> troncons = tronconGestionDigueImporter.getTronconsDigues();
        final Map<Integer, RefSource> typesSource = typeSourceImporter.getTypeSource();
        final Map<Integer, RefPosition> typesPosition = typePositionImporter.getTypePosition();
        final Map<Integer, RefCote> typesCote = typeCoteImporter.getTypeCote();
        final Map<Integer, RefMateriau> typesMateriau = typeMateriauImporter.getTypeMateriau();
        final Map<Integer, RefNature> typesNature = typeNatureImporter.getTypeNature();
        final Map<Integer, RefFonction> typesFonction = typeFonctionImporter.getTypeFonction();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final Crete crete = new Crete();
            
            
//            if(row.getInt(CreteColumns.ID_SOURCE.toString())!=null){
//                crete.setSourceId(typesSource.get(row.getInt(CreteColumns.ID_SOURCE.toString())).getId());
//            }
//            
//            final TronconDigue troncon = troncons.get(row.getInt(CreteColumns.ID_TRONCON_GESTION.toString()));
//            if (troncon.getId() != null) {
//                crete.setTroncon(troncon.getId());
//            } else {
//                throw new AccessDbImporterException("Le tronçon "
//                        + troncons.get(row.getInt(CreteColumns.ID_TRONCON_GESTION.toString())) + " n'a pas encore d'identifiant CouchDb !");
//            }
//            
//            if (row.getDate(CreteColumns.DATE_DEBUT_VAL.toString()) != null) {
//                crete.setDate_debut(LocalDateTime.parse(row.getDate(CreteColumns.DATE_DEBUT_VAL.toString()).toString(), dateTimeFormatter));
//            }
//            
//            if (row.getDate(CreteColumns.DATE_FIN_VAL.toString()) != null) {
//                crete.setDate_fin(LocalDateTime.parse(row.getDate(CreteColumns.DATE_FIN_VAL.toString()).toString(), dateTimeFormatter));
//            }
//            
//            if (row.getDouble(CreteColumns.PR_DEBUT_CALCULE.toString()) != null) {
//                crete.setPR_debut(row.getDouble(CreteColumns.PR_DEBUT_CALCULE.toString()).floatValue());
//            }
//            
//            if (row.getDouble(CreteColumns.PR_FIN_CALCULE.toString()) != null) {
//                crete.setPR_fin(row.getDouble(CreteColumns.PR_FIN_CALCULE.toString()).floatValue());
//            }
//            
//            if (row.getInt(CreteColumns.ID_SYSTEME_REP.toString()) != null) {
//                crete.setSystemeRepId(systemesReperage.get(row.getInt(CreteColumns.ID_SYSTEME_REP.toString())).getId());
//            }
//            
//            if (row.getDouble(CreteColumns.ID_BORNEREF_DEBUT.toString()) != null) {
//                crete.setBorneDebutId(bornes.get((int) row.getDouble(CreteColumns.ID_BORNEREF_DEBUT.toString()).doubleValue()).getId());
//            }
//            
//            crete.setBorne_debut_aval(row.getBoolean(CreteColumns.AMONT_AVAL_DEBUT.toString()));
//            
//            if (row.getDouble(CreteColumns.DIST_BORNEREF_DEBUT.toString()) != null) {
//                crete.setBorne_debut_distance(row.getDouble(CreteColumns.DIST_BORNEREF_DEBUT.toString()).floatValue());
//            }
//            
//            if (row.getDouble(CreteColumns.ID_BORNEREF_FIN.toString()) != null) {
//                crete.setBorneFinId(bornes.get((int) row.getDouble(CreteColumns.ID_BORNEREF_FIN.toString()).doubleValue()).getId());
//            }
//            
//            crete.setBorne_fin_aval(row.getBoolean(CreteColumns.AMONT_AVAL_FIN.toString()));
//            
//            if (row.getDouble(CreteColumns.DIST_BORNEREF_FIN.toString()) != null) {
//                crete.setBorne_fin_distance(row.getDouble(CreteColumns.DIST_BORNEREF_FIN.toString()).floatValue());
//            }
//            
//            crete.setCommentaire(row.getString(CreteColumns.COMMENTAIRE.toString()));
//
//            crete.setNum_couche(row.getInt(CreteColumns.N_COUCHE.toString()));
//            
//            if(row.getInt(CreteColumns.ID_TYPE_MATERIAU.toString())!=null){
//                crete.setMateriauId(typesMateriau.get(row.getInt(CreteColumns.ID_TYPE_MATERIAU.toString())).getId());
//            }
//            
//            if(row.getInt(CreteColumns.ID_TYPE_NATURE.toString())!=null){
//                crete.setNatureId(typesNature.get(row.getInt(CreteColumns.ID_TYPE_NATURE.toString())).getId());
//            }
//            
//            if(row.getInt(CreteColumns.ID_TYPE_FONCTION.toString())!=null){
//                crete.setFonctionId(typesFonction.get(row.getInt(CreteColumns.ID_TYPE_FONCTION.toString())).getId());
//            }
//            
//            if (row.getDouble(CreteColumns.EPAISSEUR.toString()) != null) {
//                crete.setEpaisseur(row.getDouble(CreteColumns.EPAISSEUR.toString()).floatValue());
//            }
//            
//            if(row.getInt(CreteColumns.ID_TYPE_COTE.toString())!=null){
//                crete.setCoteId(typesCote.get(row.getInt(CreteColumns.ID_TYPE_COTE.toString())).getId());
//            }
//            
//            GeometryFactory geometryFactory = new GeometryFactory();
//            final MathTransform lambertToRGF;
//            try {
//                lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), CRS.decode("EPSG:2154"), true);
//
//                try {
//
//                    if (row.getDouble(CreteColumns.X_DEBUT.toString()) != null && row.getDouble(CreteColumns.Y_DEBUT.toString()) != null) {
//                        crete.setPositionDebut((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
//                                row.getDouble(CreteColumns.X_DEBUT.toString()),
//                                row.getDouble(CreteColumns.Y_DEBUT.toString()))), lambertToRGF));
//                    }
//                } catch (MismatchedDimensionException | TransformException ex) {
//                    Logger.getLogger(LargeurFrancBordImporter.class.getName()).log(Level.SEVERE, null, ex);
//                }
//
//                try {
//
//                    if (row.getDouble(CreteColumns.X_FIN.toString()) != null && row.getDouble(CreteColumns.Y_FIN.toString()) != null) {
//                        crete.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
//                                row.getDouble(CreteColumns.X_FIN.toString()),
//                                row.getDouble(CreteColumns.Y_FIN.toString()))), lambertToRGF));
//                    }
//                } catch (MismatchedDimensionException | TransformException ex) {
//                    Logger.getLogger(LargeurFrancBordImporter.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            } catch (FactoryException ex) {
//                Logger.getLogger(LargeurFrancBordImporter.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            
//            if(row.getInt(CreteColumns.ID_TYPE_POSITION.toString())!=null){
//                crete.setPosition_structure(typesPosition.get(row.getInt(CreteColumns.ID_TYPE_POSITION.toString())).getId());
//            }
//            
//
//            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
//            //tronconDigue.setId(String.valueOf(row.getString(TronconDigueColumns.ID.toString())));
//            cretes.put(row.getInt(CreteColumns.ID_ELEMENT_STRUCTURE.toString()), crete);
//
//            // Set the list ByTronconId
//            List<Crete> listByTronconId = cretesByTronconId.get(row.getInt(CreteColumns.ID_TRONCON_GESTION.toString()));
//            if (listByTronconId == null) {
//                listByTronconId = new ArrayList<>();
//                cretesByTronconId.put(row.getInt(CreteColumns.ID_TRONCON_GESTION.toString()), listByTronconId);
//            }
//            listByTronconId.add(crete);
        }
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (CreteColumns c : CreteColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }
}
