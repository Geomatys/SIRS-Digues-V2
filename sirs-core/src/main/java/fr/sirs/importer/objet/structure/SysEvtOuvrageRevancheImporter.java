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
import fr.sirs.core.model.OuvrageRevanche;
import fr.sirs.core.model.RefCote;
import fr.sirs.core.model.RefMateriau;
import fr.sirs.core.model.RefNature;
import fr.sirs.core.model.RefPosition;
import fr.sirs.core.model.RefSource;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.objet.TypeCoteImporter;
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
class SysEvtOuvrageRevancheImporter extends GenericStructureImporter<OuvrageRevanche> {

    SysEvtOuvrageRevancheImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final SystemeReperageImporter systemeReperageImporter,
            final BorneDigueImporter borneDigueImporter, 
            final SourceInfoImporter typeSourceImporter,
            final TypeCoteImporter typeCoteImporter,
            final TypePositionImporter typePositionImporter,
            final TypeMateriauImporter typeMateriauImporter,
            final TypeNatureImporter typeNatureImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter,
                typeSourceImporter, typeCoteImporter, 
                typePositionImporter, typeMateriauImporter, typeNatureImporter, 
                null);
    }
    
    private enum Columns {
       ID_ELEMENT_STRUCTURE,
//        id_nom_element, // Redondant avec ID_ELEMENT_STRUCTURE
//        ID_SOUS_GROUPE_DONNEES, // Redondant avec le type de données
//        LIBELLE_TYPE_ELEMENT_STRUCTURE, // Redondant avec le type de données
//        DECALAGE_DEFAUT, // Affichage
//        DECALAGE, // Affichage
//        LIBELLE_SOURCE, // Redondant avec l'importation des sources
//        LIBELLE_TYPE_COTE, // Redondant avec l'importation des types de côté
//        LIBELLE_SYSTEME_REP, // Redondant avec l'importation des SR
//        NOM_BORNE_DEBUT, // Redondant avec l'importation des bornes
//        NOM_BORNE_FIN, // Redondant avec l'importation des bornes
//        LIBELLE_TYPE_MATERIAU, // Redondant avec l'importation des matériaux
//        LIBELLE_TYPE_NATURE, // Redondant avec l'importation des natures
//        LIBELLE_TYPE_FONCTION, // Redondant avec l'importation des fonctions
//        LIBELLE_TYPE_NATURE_HAUT, // Redondant avec l'importation des natures
//        LIBELLE_TYPE_MATERIAU_HAUT, // Redondant avec l'importation des matériaux
//        LIBELLE_TYPE_NATURE_BAS, // Redondant avec l'importation des natures
//        LIBELLE_TYPE_MATERIAU_BAS, // Redondant avec l'importation des matériaux
//        LIBELLE_TYPE_OUVRAGE_PARTICULIER,
//        LIBELLE_TYPE_POSITION, // Redondant avec l'importation des positions
//        RAISON_SOCIALE_ORG_PROPRIO, // Redondant avec l'importation des organismes
//        RAISON_SOCIALE_ORG_GESTION, // Redondant avec l'importation des organismes
//        INTERV_PROPRIO,
//        INTERV_GARDIEN,
//        LIBELLE_TYPE_COMPOSITION,
//        LIBELLE_TYPE_VEGETATION,
//        ID_TYPE_ELEMENT_STRUCTURE, // Redondant avec le type de données
        ID_TYPE_COTE,
        ID_SOURCE,
        ID_TRONCON_GESTION,
        DATE_DEBUT_VAL,
        DATE_FIN_VAL,
        PR_DEBUT_CALCULE,
        PR_FIN_CALCULE,
        X_DEBUT,
        Y_DEBUT,
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
//        N_COUCHE, // Pas dans le nouveau modèle
//        ID_TYPE_MATERIAU, // Pas dans le nouveau modèle
//        ID_TYPE_NATURE, // Pas dans le nouveau modèle
//        ID_TYPE_FONCTION, // Pas dans le nouveau modèle
//        EPAISSEUR, // Pas dans le nouveau modèle
//        TALUS_INTERCEPTE_CRETE,
        ID_TYPE_NATURE_HAUT,
        ID_TYPE_MATERIAU_HAUT,
        ID_TYPE_MATERIAU_BAS,
        ID_TYPE_NATURE_BAS,
//        LONG_RAMP_HAUT,
//        LONG_RAMP_BAS,
//        PENTE_INTERIEURE,
//        ID_TYPE_OUVRAGE_PARTICULIER,
        ID_TYPE_POSITION,
//        ID_ORG_PROPRIO,
//        ID_ORG_GESTION,
//        ID_INTERV_PROPRIO,
//        ID_INTERV_GARDIEN,
//        DATE_DEBUT_ORGPROPRIO,
//        DATE_FIN_ORGPROPRIO,
//        DATE_DEBUT_GESTION,
//        DATE_FIN_GESTION,
//        DATE_DEBUT_INTERVPROPRIO,
//        DATE_FIN_INTERVPROPRIO,
//        ID_TYPE_COMPOSITION,
//        DISTANCE_TRONCON,
//        LONGUEUR,
//        DATE_DEBUT_GARDIEN,
//        DATE_FIN_GARDIEN,
//        LONGUEUR_PERPENDICULAIRE,
//        LONGUEUR_PARALLELE,
//        COTE_AXE,
//        ID_TYPE_VEGETATION,
//        HAUTEUR,
//        DIAMETRE,
//        DENSITE,
//        EPAISSEUR_Y11,
//        EPAISSEUR_Y12,
//        EPAISSEUR_Y21,
//        EPAISSEUR_Y22,
//        ID_AUTO
    };

    @Override
    public String getTableName() {
        return DbImporter.TableName.SYS_EVT_OUVRAGE_REVANCHE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {

        this.structures = new HashMap<>();
        this.structuresByTronconId = new HashMap<>();
        
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        final Map<Integer, TronconDigue> troncons = tronconGestionDigueImporter.getTronconsDigues();
        
        final Map<Integer, RefSource> typesSource = sourceInfoImporter.getTypes();
        final Map<Integer, RefCote> typesCote = typeCoteImporter.getTypes();
        final Map<Integer, RefPosition> typesPosition = typePositionImporter.getTypes();
        final Map<Integer, RefMateriau> typesMateriau = typeMateriauImporter.getTypes();
        final Map<Integer, RefNature> typesNature = typeNatureImporter.getTypes();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final OuvrageRevanche ouvrage = new OuvrageRevanche();
            
            if(row.getInt(Columns.ID_TYPE_COTE.toString())!=null){
                ouvrage.setCoteId(typesCote.get(row.getInt(Columns.ID_TYPE_COTE.toString())).getId());
            }
            
            if(row.getInt(Columns.ID_SOURCE.toString())!=null){
                ouvrage.setSourceId(typesSource.get(row.getInt(Columns.ID_SOURCE.toString())).getId());
            }
            
            final TronconDigue troncon = troncons.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            if (troncon.getId() != null) {
                ouvrage.setTroncon(troncon.getId());
            } else {
                throw new AccessDbImporterException("Le tronçon "
                        + troncons.get(row.getInt(Columns.ID_TRONCON_GESTION.toString())) + " n'a pas encore d'identifiant CouchDb !");
            }
            
            if (row.getDate(Columns.DATE_DEBUT_VAL.toString()) != null) {
                ouvrage.setDate_debut(LocalDateTime.parse(row.getDate(Columns.DATE_DEBUT_VAL.toString()).toString(), dateTimeFormatter));
            }
            
            if (row.getDate(Columns.DATE_FIN_VAL.toString()) != null) {
                ouvrage.setDate_fin(LocalDateTime.parse(row.getDate(Columns.DATE_FIN_VAL.toString()).toString(), dateTimeFormatter));
            }
            
            if (row.getDouble(Columns.PR_DEBUT_CALCULE.toString()) != null) {
                ouvrage.setPR_debut(row.getDouble(Columns.PR_DEBUT_CALCULE.toString()).floatValue());
            }
            
            if (row.getDouble(Columns.PR_FIN_CALCULE.toString()) != null) {
                ouvrage.setPR_fin(row.getDouble(Columns.PR_FIN_CALCULE.toString()).floatValue());
            }
            
            GeometryFactory geometryFactory = new GeometryFactory();
            final MathTransform lambertToRGF;
            try {
                lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), getOutputCrs(), true);

                try {

                    if (row.getDouble(Columns.X_DEBUT.toString()) != null && row.getDouble(Columns.Y_DEBUT.toString()) != null) {
                        ouvrage.setPositionDebut((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(Columns.X_DEBUT.toString()),
                                row.getDouble(Columns.Y_DEBUT.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(SysEvtOuvrageRevancheImporter.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {

                    if (row.getDouble(Columns.X_FIN.toString()) != null && row.getDouble(Columns.Y_FIN.toString()) != null) {
                        ouvrage.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(Columns.X_FIN.toString()),
                                row.getDouble(Columns.Y_FIN.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(SysEvtOuvrageRevancheImporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (FactoryException ex) {
                Logger.getLogger(SysEvtOuvrageRevancheImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if (row.getInt(Columns.ID_SYSTEME_REP.toString()) != null) {
                ouvrage.setSystemeRepId(systemesReperage.get(row.getInt(Columns.ID_SYSTEME_REP.toString())).getId());
            }
            
            if (row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()) != null) {
                ouvrage.setBorneDebutId(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()).doubleValue()).getId());
            }
            
            ouvrage.setBorne_debut_aval(row.getBoolean(Columns.AMONT_AVAL_DEBUT.toString()));
            
            if (row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()) != null) {
                ouvrage.setBorne_debut_distance(row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()).floatValue());
            }
            
            if (row.getDouble(Columns.ID_BORNEREF_FIN.toString()) != null) {
                ouvrage.setBorneFinId(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_FIN.toString()).doubleValue()).getId());
            }
            
            ouvrage.setBorne_fin_aval(row.getBoolean(Columns.AMONT_AVAL_FIN.toString()));
            
            if (row.getDouble(Columns.DIST_BORNEREF_FIN.toString()) != null) {
                ouvrage.setBorne_fin_distance(row.getDouble(Columns.DIST_BORNEREF_FIN.toString()).floatValue());
            }
            
            ouvrage.setCommentaire(row.getString(Columns.COMMENTAIRE.toString()));
            
            if(row.getInt(Columns.ID_TYPE_NATURE_HAUT.toString())!=null){
                ouvrage.setNatureHautId(typesNature.get(row.getInt(Columns.ID_TYPE_NATURE_HAUT.toString())).getId());
            }
            
            if(row.getInt(Columns.ID_TYPE_MATERIAU_HAUT.toString())!=null){
                ouvrage.setMateriauHautId(typesMateriau.get(row.getInt(Columns.ID_TYPE_MATERIAU_HAUT.toString())).getId());
            }
            
            if(row.getInt(Columns.ID_TYPE_MATERIAU_BAS.toString())!=null){
                ouvrage.setMateriauBasId(typesMateriau.get(row.getInt(Columns.ID_TYPE_MATERIAU_BAS.toString())).getId());
            }
            
            if(row.getInt(Columns.ID_TYPE_NATURE_BAS.toString())!=null){
                ouvrage.setNatureBasId(typesNature.get(row.getInt(Columns.ID_TYPE_NATURE_BAS.toString())).getId());
            }
            
            if(row.getInt(Columns.ID_TYPE_POSITION.toString())!=null){
                ouvrage.setPosition_structure(typesPosition.get(row.getInt(Columns.ID_TYPE_POSITION.toString())).getId());
            }
            

            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            //tronconDigue.setId(String.valueOf(row.getString(TronconDigueColumns.ID.toString())));
            structures.put(row.getInt(Columns.ID_ELEMENT_STRUCTURE.toString()), ouvrage);

            // Set the list ByTronconId
            List<OuvrageRevanche> listByTronconId = structuresByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            if (listByTronconId == null) {
                listByTronconId = new ArrayList<>();
                structuresByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
            }
            listByTronconId.add(ouvrage);
        }
    }

    @Override
    protected List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (Columns c : Columns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }
}
