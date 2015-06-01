package fr.sirs.importer.objet.reseau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import static fr.sirs.core.LinearReferencingUtilities.buildGeometry;
import fr.sirs.core.model.BorneDigue;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.OuvrageVoirie;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.core.model.RefOuvrageVoirie;
import fr.sirs.core.model.RefPosition;
import fr.sirs.core.model.RefSource;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.cleanNullString;
import fr.sirs.importer.TypeCoteImporter;
import fr.sirs.importer.objet.TypePositionImporter;
import fr.sirs.importer.objet.SourceInfoImporter;
import fr.sirs.importer.troncon.TronconGestionDigueImporter;
import java.io.IOException;
import java.util.ArrayList;
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
class SysEvtOuvrageVoirieImporter extends GenericReseauImporter<OuvrageVoirie> {

    private final TypeOuvrageVoirieImporter typeOuvrageVoirieImporter;

    SysEvtOuvrageVoirieImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final SystemeReperageImporter systemeReperageImporter,
            final BorneDigueImporter borneDigueImporter,
            final SourceInfoImporter typeSourceImporter,
            final TypeCoteImporter typeCoteImporter,
            final TypePositionImporter typePositionImporter,
            final TypeOuvrageVoirieImporter typeOuvrageVoirieImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter,
                typeSourceImporter, typeCoteImporter,
                typePositionImporter, null);
        this.typeOuvrageVoirieImporter = typeOuvrageVoirieImporter;
    }

    private enum Columns {

        ID_ELEMENT_RESEAU,
        //        id_nom_element,
        //        ID_SOUS_GROUPE_DONNEES,
        //        LIBELLE_TYPE_ELEMENT_RESEAU,
        //        DECALAGE_DEFAUT,
        //        DECALAGE,
        //        LIBELLE_SOURCE,
        //        LIBELLE_TYPE_COTE,
        //        LIBELLE_SYSTEME_REP,
        //        NOM_BORNE_DEBUT,
        //        NOM_BORNE_FIN,
        //        LIBELLE_ECOULEMENT,
        //        LIBELLE_IMPLANTATION,
        //        LIBELLE_UTILISATION_CONDUITE,
        //        LIBELLE_TYPE_CONDUITE_FERMEE,
        //        LIBELLE_TYPE_OUVR_HYDRAU_ASSOCIE,
        //        LIBELLE_TYPE_RESEAU_COMMUNICATION,
        //        LIBELLE_TYPE_VOIE_SUR_DIGUE,
        //        NOM_OUVRAGE_VOIRIE,
        //        LIBELLE_TYPE_POSITION,
        //        LIBELLE_TYPE_OUVRAGE_VOIRIE,
        //        LIBELLE_TYPE_RESEAU_EAU,
        //        LIBELLE_TYPE_REVETEMENT,
        //        LIBELLE_TYPE_USAGE_VOIE,
        NOM,
        //        ID_TYPE_ELEMENT_RESEAU,
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
        //        N_SECTEUR,
        //        ID_ECOULEMENT,
        //        ID_IMPLANTATION,
        //        ID_UTILISATION_CONDUITE,
        //        ID_TYPE_CONDUITE_FERMEE,
        //        AUTORISE,
        //        ID_TYPE_OUVR_HYDRAU_ASSOCIE,
        //        ID_TYPE_RESEAU_COMMUNICATION,
        //        ID_OUVRAGE_COMM_NRJ,
        //        ID_TYPE_VOIE_SUR_DIGUE,
        //        ID_OUVRAGE_VOIRIE,
        //        ID_TYPE_REVETEMENT,
        //        ID_TYPE_USAGE_VOIE,
        ID_TYPE_POSITION,
        //        LARGEUR,
        ID_TYPE_OUVRAGE_VOIRIE,
//        HAUTEUR,
//        DIAMETRE,
//        ID_TYPE_RESEAU_EAU,
//        ID_TYPE_NATURE,
//        LIBELLE_TYPE_NATURE,
//        ID_TYPE_NATURE_HAUT,
//        LIBELLE_TYPE_NATURE_HAUT,
//        ID_TYPE_NATURE_BAS,
//        LIBELLE_TYPE_NATURE_BAS,
//        ID_TYPE_REVETEMENT_HAUT,
//        LIBELLE_TYPE_REVETEMENT_HAUT,
//        ID_TYPE_REVETEMENT_BAS,
//        LIBELLE_TYPE_REVETEMENT_BAS,
//        ID_AUTO
    };

    @Override
    public String getTableName() {
        return SYS_EVT_OUVRAGE_VOIRIE.toString();
    }

    @Override
    public OuvrageVoirie importRow(Row row) throws IOException, AccessDbImporterException {

        final TronconDigue troncon = tronconGestionDigueImporter.getTronconsDigues().get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();

        final Map<Integer, RefSource> typesSource = sourceInfoImporter.getTypeReferences();
        final Map<Integer, RefPosition> typesPosition = typePositionImporter.getTypeReferences();

        final Map<Integer, RefOuvrageVoirie> typesOuvrageVoirie = typeOuvrageVoirieImporter.getTypeReferences();

        final OuvrageVoirie ouvrage = createAnonymValidElement(OuvrageVoirie.class);
        
        ouvrage.setLinearId(troncon.getId());

        ouvrage.setLibelle(cleanNullString(row.getString(Columns.NOM.toString())));

        if (row.getInt(Columns.ID_SOURCE.toString()) != null) {
            ouvrage.setSourceId(typesSource.get(row.getInt(Columns.ID_SOURCE.toString())).getId());
        }

        if (row.getDate(Columns.DATE_DEBUT_VAL.toString()) != null) {
            ouvrage.setDate_debut(DbImporter.parse(row.getDate(Columns.DATE_DEBUT_VAL.toString()), dateTimeFormatter));
        }

        if (row.getDate(Columns.DATE_FIN_VAL.toString()) != null) {
            ouvrage.setDate_fin(DbImporter.parse(row.getDate(Columns.DATE_FIN_VAL.toString()), dateTimeFormatter));
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
                Logger.getLogger(SysEvtOuvrageVoirieImporter.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {

                if (row.getDouble(Columns.X_FIN.toString()) != null && row.getDouble(Columns.Y_FIN.toString()) != null) {
                    ouvrage.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                            row.getDouble(Columns.X_FIN.toString()),
                            row.getDouble(Columns.Y_FIN.toString()))), lambertToRGF));
                }
            } catch (MismatchedDimensionException | TransformException ex) {
                Logger.getLogger(SysEvtOuvrageVoirieImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FactoryException ex) {
            Logger.getLogger(SysEvtOuvrageVoirieImporter.class.getName()).log(Level.SEVERE, null, ex);
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
            if (bornes.get((int) row.getDouble(Columns.ID_BORNEREF_FIN.toString()).doubleValue()) != null) {
                ouvrage.setBorneFinId(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_FIN.toString()).doubleValue()).getId());
            }
        }

        ouvrage.setBorne_fin_aval(row.getBoolean(Columns.AMONT_AVAL_FIN.toString()));

        if (row.getDouble(Columns.DIST_BORNEREF_FIN.toString()) != null) {
            ouvrage.setBorne_fin_distance(row.getDouble(Columns.DIST_BORNEREF_FIN.toString()).floatValue());
        }

        ouvrage.setCommentaire(row.getString(Columns.COMMENTAIRE.toString()));

        if (row.getInt(Columns.ID_TYPE_POSITION.toString()) != null) {
            ouvrage.setPositionId(typesPosition.get(row.getInt(Columns.ID_TYPE_POSITION.toString())).getId());
        }

        if (row.getInt(Columns.ID_TYPE_OUVRAGE_VOIRIE.toString()) != null) {
            ouvrage.setTypeOuvrageVoirieId(typesOuvrageVoirie.get(row.getInt(Columns.ID_TYPE_OUVRAGE_VOIRIE.toString())).getId());
        }

        ouvrage.setDesignation(String.valueOf(row.getInt(Columns.ID_ELEMENT_RESEAU.toString())));
        ouvrage.setGeometry(buildGeometry(troncon.getGeometry(), ouvrage, tronconGestionDigueImporter.getBorneDigueRepository()));
        
        return ouvrage;
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
