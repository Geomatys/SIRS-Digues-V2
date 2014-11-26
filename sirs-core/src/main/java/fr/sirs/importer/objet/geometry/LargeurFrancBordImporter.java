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
import fr.sirs.core.model.LargeurFrancBord;
import fr.sirs.core.model.RefLargeurFrancBord;
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

    private Map<Integer, LargeurFrancBord> largeurs = null;
    private Map<Integer, List<LargeurFrancBord>> largeursByTronconId = null;
    
    private final TypeLargeurFrancBordImporter typeLargeurFrancBordImporter;

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
            final TypeFonctionImporter typeFonctionImporter,
            final TypeLargeurFrancBordImporter typeLargeurFrancBordImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter, organismeImporter,
                typeSourceImporter, typeCoteImporter, typePositionImporter, 
                typeMateriauImporter, typeNatureImporter, typeFonctionImporter);
        this.typeLargeurFrancBordImporter = typeLargeurFrancBordImporter;
    }
    
    private enum LargeurFrancBordColumns {
        ID_ELEMENT_GEOMETRIE,
//        id_nom_element, // Redondant avec ID_ELEMENT_GEOMETRIE
//        ID_SOUS_GROUPE_DONNEES, // Redondant avec le type de données
//        LIBELLE_TYPE_ELEMENT_GEOMETRIE, // Redondant avec l'importaton des types de géométries
//        DECALAGE_DEFAUT, // Affichage
//        DECALAGE, // Affichage
//        LIBELLE_SOURCE, // Redondant avec l'importation des sources
//        LIBELLE_SYSTEME_REP, // Redondant avec l'importation des systèmes de repérage
//        NOM_BORNE_DEBUT, // Redondant avec l'importation des bornes
//        NOM_BORNE_FIN, // Redondant avec l'importation des bornes
//        LIBELLE_TYPE_LARGEUR_FB, // Redondant avec l'importation des types de largeur de FB
//        LIBELLE_TYPE_PROFIL_FB, // Redondant avec l'importation des types de profil de front de FB
//        LIBELLE_TYPE_DIST_DIGUE_BERGE, // Redondant avec l'importation des distances digue/berge
        ID_TRONCON_GESTION,
//        ID_TYPE_ELEMENT_GEOMETRIE,
        ID_SOURCE,
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
        ID_TYPE_LARGEUR_FB,
//        ID_TYPE_PROFIL_FB,
//        ID_TYPE_DIST_DIGUE_BERGE,
//        ID_AUTO
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
        if (this.largeurs == null) {
            compute();
        }
        return largeurs;
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
        if (this.largeursByTronconId == null) {
            compute();
        }
        return this.largeursByTronconId;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.SYS_EVT_LARGEUR_FRANC_BORD.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {

        this.largeurs = new HashMap<>();
        this.largeursByTronconId = new HashMap<>();
        
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        final Map<Integer, TronconDigue> troncons = tronconGestionDigueImporter.getTronconsDigues();
        final Map<Integer, RefSource> typesSource = typeSourceImporter.getTypeSource();
        final Map<Integer, RefLargeurFrancBord> typesLargeur = typeLargeurFrancBordImporter.getTypeLargeur();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final LargeurFrancBord largeur = new LargeurFrancBord();
            
            final TronconDigue troncon = troncons.get(row.getInt(LargeurFrancBordColumns.ID_TRONCON_GESTION.toString()));
            if (troncon.getId() != null) {
                largeur.setTroncon(troncon.getId());
            } else {
                throw new AccessDbImporterException("Le tronçon "
                        + troncons.get(row.getInt(LargeurFrancBordColumns.ID_TRONCON_GESTION.toString())) + " n'a pas encore d'identifiant CouchDb !");
            }
            
            if(row.getInt(LargeurFrancBordColumns.ID_SOURCE.toString())!=null){
                largeur.setSourceId(typesSource.get(row.getInt(LargeurFrancBordColumns.ID_SOURCE.toString())).getId());
            }
            
            if (row.getDate(LargeurFrancBordColumns.DATE_DEBUT_VAL.toString()) != null) {
                largeur.setDate_debut(LocalDateTime.parse(row.getDate(LargeurFrancBordColumns.DATE_DEBUT_VAL.toString()).toString(), dateTimeFormatter));
            }
            
            if (row.getDate(LargeurFrancBordColumns.DATE_FIN_VAL.toString()) != null) {
                largeur.setDate_fin(LocalDateTime.parse(row.getDate(LargeurFrancBordColumns.DATE_FIN_VAL.toString()).toString(), dateTimeFormatter));
            }
            
            if (row.getDouble(LargeurFrancBordColumns.PR_DEBUT_CALCULE.toString()) != null) {
                largeur.setPR_debut(row.getDouble(LargeurFrancBordColumns.PR_DEBUT_CALCULE.toString()).floatValue());
            }
            
            if (row.getDouble(LargeurFrancBordColumns.PR_FIN_CALCULE.toString()) != null) {
                largeur.setPR_fin(row.getDouble(LargeurFrancBordColumns.PR_FIN_CALCULE.toString()).floatValue());
            }
            
            GeometryFactory geometryFactory = new GeometryFactory();
            final MathTransform lambertToRGF;
            try {
                lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), CRS.decode("EPSG:2154"), true);

                try {

                    if (row.getDouble(LargeurFrancBordColumns.X_DEBUT.toString()) != null && row.getDouble(LargeurFrancBordColumns.Y_DEBUT.toString()) != null) {
                        largeur.setPositionDebut((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(LargeurFrancBordColumns.X_DEBUT.toString()),
                                row.getDouble(LargeurFrancBordColumns.Y_DEBUT.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(LargeurFrancBordImporter.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {

                    if (row.getDouble(LargeurFrancBordColumns.X_FIN.toString()) != null && row.getDouble(LargeurFrancBordColumns.Y_FIN.toString()) != null) {
                        largeur.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(LargeurFrancBordColumns.X_FIN.toString()),
                                row.getDouble(LargeurFrancBordColumns.Y_FIN.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(LargeurFrancBordImporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (FactoryException ex) {
                Logger.getLogger(LargeurFrancBordImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if (row.getInt(LargeurFrancBordColumns.ID_SYSTEME_REP.toString()) != null) {
                largeur.setSystemeRepId(systemesReperage.get(row.getInt(LargeurFrancBordColumns.ID_SYSTEME_REP.toString())).getId());
            }
            
            if (row.getDouble(LargeurFrancBordColumns.ID_BORNEREF_DEBUT.toString()) != null) {
                largeur.setBorneDebutId(bornes.get((int) row.getDouble(LargeurFrancBordColumns.ID_BORNEREF_DEBUT.toString()).doubleValue()).getId());
            }
            
            largeur.setBorne_debut_aval(row.getBoolean(LargeurFrancBordColumns.AMONT_AVAL_DEBUT.toString()));
            
            if (row.getDouble(LargeurFrancBordColumns.DIST_BORNEREF_DEBUT.toString()) != null) {
                largeur.setBorne_debut_distance(row.getDouble(LargeurFrancBordColumns.DIST_BORNEREF_DEBUT.toString()).floatValue());
            }
            
            if (row.getDouble(LargeurFrancBordColumns.ID_BORNEREF_FIN.toString()) != null) {
                largeur.setBorneFinId(bornes.get((int) row.getDouble(LargeurFrancBordColumns.ID_BORNEREF_FIN.toString()).doubleValue()).getId());
            }
            
            largeur.setBorne_fin_aval(row.getBoolean(LargeurFrancBordColumns.AMONT_AVAL_FIN.toString()));
            
            if (row.getDouble(LargeurFrancBordColumns.DIST_BORNEREF_FIN.toString()) != null) {
                largeur.setBorne_fin_distance(row.getDouble(LargeurFrancBordColumns.DIST_BORNEREF_FIN.toString()).floatValue());
            }
            
            largeur.setCommentaire(row.getString(LargeurFrancBordColumns.COMMENTAIRE.toString()));
            
            if(row.getInt(LargeurFrancBordColumns.ID_TYPE_LARGEUR_FB.toString())!=null){
                largeur.setTypeLargeurFrancBord(typesLargeur.get(row.getInt(LargeurFrancBordColumns.ID_TYPE_LARGEUR_FB.toString())).getId());
            }
            
            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            //tronconDigue.setId(String.valueOf(row.getString(TronconDigueColumns.ID.toString())));
            largeurs.put(row.getInt(LargeurFrancBordColumns.ID_ELEMENT_GEOMETRIE.toString()), largeur);

            // Set the list ByTronconId
            List<LargeurFrancBord> listByTronconId = largeursByTronconId.get(row.getInt(LargeurFrancBordColumns.ID_TRONCON_GESTION.toString()));
            if (listByTronconId == null) {
                listByTronconId = new ArrayList<>();
                largeursByTronconId.put(row.getInt(LargeurFrancBordColumns.ID_TRONCON_GESTION.toString()), listByTronconId);
            }
            listByTronconId.add(largeur);
        }
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (LargeurFrancBordColumns c : LargeurFrancBordColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }
}
