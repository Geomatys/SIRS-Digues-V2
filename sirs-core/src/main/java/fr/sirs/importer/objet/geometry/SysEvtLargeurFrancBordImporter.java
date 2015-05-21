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
import fr.sirs.core.model.LargeurFrancBord;
import fr.sirs.core.model.RefLargeurFrancBord;
import fr.sirs.core.model.RefSource;
import fr.sirs.core.model.SystemeReperage;
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
class SysEvtLargeurFrancBordImporter extends GenericGeometrieImporter<LargeurFrancBord> {

    private final TypeLargeurFrancBordImporter typeLargeurFrancBordImporter;

    SysEvtLargeurFrancBordImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final SystemeReperageImporter systemeReperageImporter,
            final BorneDigueImporter borneDigueImporter,
            final SourceInfoImporter typeSourceImporter,
            final TypeLargeurFrancBordImporter typeLargeurFrancBordImporter) {
        super(accessDatabase, couchDbConnector,
                systemeReperageImporter, borneDigueImporter, typeSourceImporter);
        this.typeLargeurFrancBordImporter = typeLargeurFrancBordImporter;
    }

    private enum Columns {

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
//        ID_TYPE_PROFIL_FB, // Ne concerne pas cette table
//        ID_TYPE_DIST_DIGUE_BERGE, // Pas dans le modèle actuel
//        ID_AUTO
    };

    @Override
    public String getTableName() {
        return DbImporter.TableName.SYS_EVT_LARGEUR_FRANC_BORD.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {

        this.structures = new HashMap<>();
        this.structuresByTronconId = new HashMap<>();

        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final LargeurFrancBord largeur = importRow(row);

            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            //tronconDigue.setId(String.valueOf(row.getString(TronconDigueColumns.ID.toString())));
            structures.put(row.getInt(Columns.ID_ELEMENT_GEOMETRIE.toString()), largeur);

            // Set the list ByTronconId
            List<LargeurFrancBord> listByTronconId = structuresByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            if (listByTronconId == null) {
                listByTronconId = new ArrayList<>();
                structuresByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
            }
            listByTronconId.add(largeur);
        }
    }

    @Override
    public LargeurFrancBord importRow(Row row) throws IOException, AccessDbImporterException {

        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();

        final Map<Integer, RefSource> typesSource = sourceInfoImporter.getTypeReferences();

        final Map<Integer, RefLargeurFrancBord> typesLargeur = typeLargeurFrancBordImporter.getTypeReferences();

        final LargeurFrancBord largeur = new LargeurFrancBord();

        if (row.getInt(Columns.ID_SOURCE.toString()) != null) {
            largeur.setSourceId(typesSource.get(row.getInt(Columns.ID_SOURCE.toString())).getId());
        }

        if (row.getDate(Columns.DATE_DEBUT_VAL.toString()) != null) {
            largeur.setDate_debut(DbImporter.parse(row.getDate(Columns.DATE_DEBUT_VAL.toString()), dateTimeFormatter));
        }

        if (row.getDate(Columns.DATE_FIN_VAL.toString()) != null) {
            largeur.setDate_fin(DbImporter.parse(row.getDate(Columns.DATE_FIN_VAL.toString()), dateTimeFormatter));
        }

        if (row.getDouble(Columns.PR_DEBUT_CALCULE.toString()) != null) {
            largeur.setPR_debut(row.getDouble(Columns.PR_DEBUT_CALCULE.toString()).floatValue());
        }

        if (row.getDouble(Columns.PR_FIN_CALCULE.toString()) != null) {
            largeur.setPR_fin(row.getDouble(Columns.PR_FIN_CALCULE.toString()).floatValue());
        }

        GeometryFactory geometryFactory = new GeometryFactory();
        final MathTransform lambertToRGF;
        try {
            lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), getOutputCrs(), true);

            try {

                if (row.getDouble(Columns.X_DEBUT.toString()) != null && row.getDouble(Columns.Y_DEBUT.toString()) != null) {
                    largeur.setPositionDebut((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                            row.getDouble(Columns.X_DEBUT.toString()),
                            row.getDouble(Columns.Y_DEBUT.toString()))), lambertToRGF));
                }
            } catch (MismatchedDimensionException | TransformException ex) {
                Logger.getLogger(SysEvtLargeurFrancBordImporter.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {

                if (row.getDouble(Columns.X_FIN.toString()) != null && row.getDouble(Columns.Y_FIN.toString()) != null) {
                    largeur.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                            row.getDouble(Columns.X_FIN.toString()),
                            row.getDouble(Columns.Y_FIN.toString()))), lambertToRGF));
                }
            } catch (MismatchedDimensionException | TransformException ex) {
                Logger.getLogger(SysEvtLargeurFrancBordImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FactoryException ex) {
            Logger.getLogger(SysEvtLargeurFrancBordImporter.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (row.getInt(Columns.ID_SYSTEME_REP.toString()) != null) {
            largeur.setSystemeRepId(systemesReperage.get(row.getInt(Columns.ID_SYSTEME_REP.toString())).getId());
        }

        if (row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()) != null) {
            largeur.setBorneDebutId(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()).doubleValue()).getId());
        }

        largeur.setBorne_debut_aval(row.getBoolean(Columns.AMONT_AVAL_DEBUT.toString()));

        if (row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()) != null) {
            largeur.setBorne_debut_distance(row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()).floatValue());
        }

        if (row.getDouble(Columns.ID_BORNEREF_FIN.toString()) != null) {
            largeur.setBorneFinId(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_FIN.toString()).doubleValue()).getId());
        }

        largeur.setBorne_fin_aval(row.getBoolean(Columns.AMONT_AVAL_FIN.toString()));

        if (row.getDouble(Columns.DIST_BORNEREF_FIN.toString()) != null) {
            largeur.setBorne_fin_distance(row.getDouble(Columns.DIST_BORNEREF_FIN.toString()).floatValue());
        }

        largeur.setCommentaire(row.getString(Columns.COMMENTAIRE.toString()));

        if (row.getInt(Columns.ID_TYPE_LARGEUR_FB.toString()) != null) {
            largeur.setTypeLargeurFrancBord(typesLargeur.get(row.getInt(Columns.ID_TYPE_LARGEUR_FB.toString())).getId());
        }

        largeur.setDesignation(String.valueOf(row.getInt(Columns.ID_ELEMENT_GEOMETRIE.toString())));
        largeur.setValid(true);
        
        return largeur;
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
