package fr.sirs.importer.objet.ligneEau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import static fr.sirs.core.LinearReferencingUtilities.buildGeometry;
import fr.sirs.core.model.BorneDigue;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.core.model.LigneEau;
import fr.sirs.core.model.MesureLigneEauXYZ;
import fr.sirs.core.model.MesureLigneEauPrZ;
import fr.sirs.core.model.RefReferenceHauteur;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import static fr.sirs.importer.DbImporter.cleanNullString;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.evenementHydraulique.EvenementHydrauliqueImporter;
import fr.sirs.importer.objet.TypeRefHeauImporter;
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
class SysEvtLigneEauImporter extends GenericLigneEauImporter {

    private final LigneEauMesuresPrzImporter ligneEauMesuresPrzImporter;
    private final LigneEauMesuresXyzImporter ligneEauMesuresXyzImporter;

    SysEvtLigneEauImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final SystemeReperageImporter systemeReperageImporter,
            final BorneDigueImporter borneDigueImporter,
            final EvenementHydrauliqueImporter evenementHydrauliqueImporter,
            final LigneEauMesuresPrzImporter ligneEauMesuresPrzImporter,
            final LigneEauMesuresXyzImporter ligneEauMesuresXyzImporter,
            final TypeRefHeauImporter typeRefHeauImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter,
                evenementHydrauliqueImporter, typeRefHeauImporter);
        this.ligneEauMesuresPrzImporter = ligneEauMesuresPrzImporter;
        this.ligneEauMesuresXyzImporter = ligneEauMesuresXyzImporter;
    }

    private enum Columns {

        ID_LIGNE_EAU,
        //        id_nom_element, // Redondant avec ID_LIGNE_EAU
        //        ID_SOUS_GROUPE_DONNEES, // Redondant avec le type de données
        //        LIBELLE_SOUS_GROUPE_DONNEES, // Redondant avec le type de données
        //        DECALAGE_DEFAUT, // Affichage
        //        DECALAGE, // Affichage
        //        LIBELLE_SYSTEME_REP, // Redondant avec les SR
        //        NOM_BORNE_DEBUT, // Redondant avec les bornes
        //        NOM_BORNE_FIN, // Redondant avec les bornes
        //        NOM_EVENEMENT_HYDRAU, // Redondant avec les événements hydrauliques
        //        TypeRefHEau, // Redondant avec les références de hauteur d'eau
        ID_TRONCON_GESTION,
        ID_EVENEMENT_HYDRAU,
        DATE,
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
        ID_TYPE_REF_HEAU,
//        ID_AUTO
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
        return SYS_EVT_LIGNE_EAU.toString();
    }

    @Override
    public LigneEau importRow(Row row) throws IOException, AccessDbImporterException {

        final TronconDigue troncon = tronconGestionDigueImporter.getTronconsDigues().get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();

        final Map<Integer, EvenementHydraulique> evenementsHydrau = evenementHydrauliqueImporter.getEvenementHydraulique();

        final Map<Integer, RefReferenceHauteur> referenceHauteur = typeRefHeauImporter.getTypeReferences();

        final Map<Integer, List<MesureLigneEauPrZ>> mesuresPrz = ligneEauMesuresPrzImporter.getMesuresByLigneEau();
        final Map<Integer, List<MesureLigneEauXYZ>> mesuresXyz = ligneEauMesuresXyzImporter.getMesuresByLigneEau();

        final LigneEau ligneEau = createAnonymValidElement(LigneEau.class);

        ligneEau.setLinearId(troncon.getId());

        if (row.getInt(Columns.ID_EVENEMENT_HYDRAU.toString()) != null) {
            ligneEau.setEvenementHydrauliqueId(evenementsHydrau.get(row.getInt(Columns.ID_EVENEMENT_HYDRAU.toString())).getId());
        }

        if (row.getDate(Columns.DATE.toString()) != null) {
            ligneEau.setDate(DbImporter.parseLocalDateTime(row.getDate(Columns.DATE.toString()), dateTimeFormatter));
        }

        if (row.getDouble(Columns.PR_DEBUT_CALCULE.toString()) != null) {
            ligneEau.setPrDebut(row.getDouble(Columns.PR_DEBUT_CALCULE.toString()).floatValue());
        }

        if (row.getDouble(Columns.PR_FIN_CALCULE.toString()) != null) {
            ligneEau.setPrFin(row.getDouble(Columns.PR_FIN_CALCULE.toString()).floatValue());
        }

        GeometryFactory geometryFactory = new GeometryFactory();
        final MathTransform lambertToRGF;
        try {
            lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), getOutputCrs(), true);

            try {

                if (row.getDouble(Columns.X_DEBUT.toString()) != null && row.getDouble(Columns.Y_DEBUT.toString()) != null) {
                    ligneEau.setPositionDebut((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                            row.getDouble(Columns.X_DEBUT.toString()),
                            row.getDouble(Columns.Y_DEBUT.toString()))), lambertToRGF));
                }
            } catch (MismatchedDimensionException | TransformException ex) {
                Logger.getLogger(SysEvtLigneEauImporter.class.getName()).log(Level.WARNING, null, ex);
            }

            try {

                if (row.getDouble(Columns.X_FIN.toString()) != null && row.getDouble(Columns.Y_FIN.toString()) != null) {
                    ligneEau.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                            row.getDouble(Columns.X_FIN.toString()),
                            row.getDouble(Columns.Y_FIN.toString()))), lambertToRGF));
                }
            } catch (MismatchedDimensionException | TransformException ex) {
                Logger.getLogger(SysEvtLigneEauImporter.class.getName()).log(Level.WARNING, null, ex);
            }
        } catch (FactoryException ex) {
            Logger.getLogger(SysEvtLigneEauImporter.class.getName()).log(Level.WARNING, null, ex);
        }

        if (row.getInt(Columns.ID_SYSTEME_REP.toString()) != null) {
            ligneEau.setSystemeRepId(systemesReperage.get(row.getInt(Columns.ID_SYSTEME_REP.toString())).getId());
        }

        if (row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()) != null) {
            final BorneDigue b = bornes.get((int) row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()).doubleValue());
            if (b != null) {
                ligneEau.setBorneDebutId(b.getId());
            }
        }

        ligneEau.setBorne_debut_aval(row.getBoolean(Columns.AMONT_AVAL_DEBUT.toString()));

        if (row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()) != null) {
            ligneEau.setBorne_debut_distance(row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()).floatValue());
        }

        if (row.getDouble(Columns.ID_BORNEREF_FIN.toString()) != null) {
            final BorneDigue b = bornes.get((int) row.getDouble(Columns.ID_BORNEREF_FIN.toString()).doubleValue());
            if (b != null) {
                ligneEau.setBorneFinId(b.getId());
            }
        }

        ligneEau.setBorne_fin_aval(row.getBoolean(Columns.AMONT_AVAL_FIN.toString()));

        if (row.getDouble(Columns.DIST_BORNEREF_FIN.toString()) != null) {
            ligneEau.setBorne_fin_distance(row.getDouble(Columns.DIST_BORNEREF_FIN.toString()).floatValue());
        }

        ligneEau.setCommentaire(cleanNullString(row.getString(Columns.COMMENTAIRE.toString())));

        if (row.getInt(Columns.ID_TYPE_REF_HEAU.toString()) != null) {
            ligneEau.setReferenceHauteurId(referenceHauteur.get(row.getInt(Columns.ID_TYPE_REF_HEAU.toString())).getId());
        }

        if (mesuresPrz.get(row.getInt(Columns.ID_LIGNE_EAU.toString())) != null) {
            ligneEau.getMesuresDZ().addAll(mesuresPrz.get(row.getInt(Columns.ID_LIGNE_EAU.toString())));
        }

        if (mesuresXyz.get(row.getInt(Columns.ID_LIGNE_EAU.toString())) != null) {
            ligneEau.getMesuresXYZ().addAll(mesuresXyz.get(row.getInt(Columns.ID_LIGNE_EAU.toString())));
        }

        ligneEau.setDesignation(String.valueOf(row.getInt(Columns.ID_LIGNE_EAU.toString())));
        ligneEau.setGeometry(buildGeometry(troncon.getGeometry(), ligneEau, tronconGestionDigueImporter.getBorneDigueRepository()));

        return ligneEau;
    }
}
