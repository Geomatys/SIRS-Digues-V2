package fr.sirs.importer.objet.reseau;

import fr.sirs.core.SirsCore;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import static fr.sirs.core.LinearReferencingUtilities.buildGeometry;
import fr.sirs.core.model.BorneDigue;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.core.model.RefCote;
import fr.sirs.core.model.RefMateriau;
import fr.sirs.core.model.RefNature;
import fr.sirs.core.model.RefPosition;
import fr.sirs.core.model.RefSource;
import fr.sirs.core.model.RefUsageVoie;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.core.model.VoieAcces;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.cleanNullString;
import fr.sirs.importer.TypeCoteImporter;
import fr.sirs.importer.objet.TypeNatureImporter;
import fr.sirs.importer.objet.TypePositionImporter;
import fr.sirs.importer.objet.SourceInfoImporter;
import fr.sirs.importer.objet.TypeMateriauImporter;
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
class SysEvtCheminAccesImporter extends GenericReseauImporter<VoieAcces> {

    private final TypeUsageVoieImporter typeUsageVoieImporter;

    SysEvtCheminAccesImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final SystemeReperageImporter systemeReperageImporter,
            final BorneDigueImporter borneDigueImporter,
            final SourceInfoImporter typeSourceImporter,
            final TypeCoteImporter typeCoteImporter,
            final TypePositionImporter typePositionImporter,
            final TypeMateriauImporter typeMateriauImporter,
            final TypeNatureImporter typeNatureImporter,
            final TypeUsageVoieImporter typeUsageVoieImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter,
                typeSourceImporter, typeCoteImporter,
                typePositionImporter, typeMateriauImporter, typeNatureImporter);
        this.typeUsageVoieImporter = typeUsageVoieImporter;
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
        N_SECTEUR,
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
        ID_TYPE_USAGE_VOIE,
        ID_TYPE_POSITION,
        LARGEUR,
        //        ID_TYPE_OUVRAGE_VOIRIE,
        //        HAUTEUR,
        //        DIAMETRE,
        //        ID_TYPE_RESEAU_EAU,
        ID_TYPE_NATURE,
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
        return SYS_EVT_CHEMIN_ACCES.toString();
    }

    @Override
    public VoieAcces importRow(Row row) throws IOException, AccessDbImporterException {

        final TronconDigue troncon = tronconGestionDigueImporter.getTronconsDigues().get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();

        final Map<Integer, RefSource> typesSource = sourceInfoImporter.getTypeReferences();
        final Map<Integer, RefCote> typesCote = typeCoteImporter.getTypeReferences();
        final Map<Integer, RefPosition> typesPosition = typePositionImporter.getTypeReferences();
        final Map<Integer, RefMateriau> typesMateriau = typeMateriauImporter.getTypeReferences();

        final Map<Integer, RefUsageVoie> typesUsages = typeUsageVoieImporter.getTypeReferences();

        final VoieAcces voie = createAnonymValidElement(VoieAcces.class);
        
        voie.setLinearId(troncon.getId());

        voie.setLibelle(cleanNullString(row.getString(Columns.NOM.toString())));

        if (row.getInt(Columns.ID_TYPE_COTE.toString()) != null) {
            voie.setCoteId(typesCote.get(row.getInt(Columns.ID_TYPE_COTE.toString())).getId());
        }

        if (row.getInt(Columns.ID_SOURCE.toString()) != null) {
            voie.setSourceId(typesSource.get(row.getInt(Columns.ID_SOURCE.toString())).getId());
        }

        if (row.getDate(Columns.DATE_DEBUT_VAL.toString()) != null) {
            voie.setDate_debut(DbImporter.parseLocalDate(row.getDate(Columns.DATE_DEBUT_VAL.toString()), dateTimeFormatter));
        }

        if (row.getDate(Columns.DATE_FIN_VAL.toString()) != null) {
            voie.setDate_fin(DbImporter.parseLocalDate(row.getDate(Columns.DATE_FIN_VAL.toString()), dateTimeFormatter));
        }

        if (row.getDouble(Columns.PR_DEBUT_CALCULE.toString()) != null) {
            voie.setPrDebut(row.getDouble(Columns.PR_DEBUT_CALCULE.toString()).floatValue());
        }

        if (row.getDouble(Columns.PR_FIN_CALCULE.toString()) != null) {
            voie.setPrFin(row.getDouble(Columns.PR_FIN_CALCULE.toString()).floatValue());
        }

        GeometryFactory geometryFactory = new GeometryFactory();
        final MathTransform lambertToRGF;
        try {
            lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), getOutputCrs(), true);

            try {

                if (row.getDouble(Columns.X_DEBUT.toString()) != null && row.getDouble(Columns.Y_DEBUT.toString()) != null) {
                    voie.setPositionDebut((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                            row.getDouble(Columns.X_DEBUT.toString()),
                            row.getDouble(Columns.Y_DEBUT.toString()))), lambertToRGF));
                }
            } catch (MismatchedDimensionException | TransformException ex) {
                Logger.getLogger(SysEvtCheminAccesImporter.class.getName()).log(Level.WARNING, null, ex);
            }

            try {

                if (row.getDouble(Columns.X_FIN.toString()) != null && row.getDouble(Columns.Y_FIN.toString()) != null) {
                    voie.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                            row.getDouble(Columns.X_FIN.toString()),
                            row.getDouble(Columns.Y_FIN.toString()))), lambertToRGF));
                }
            } catch (MismatchedDimensionException | TransformException ex) {
                Logger.getLogger(SysEvtCheminAccesImporter.class.getName()).log(Level.WARNING, null, ex);
            }
        } catch (FactoryException ex) {
            Logger.getLogger(SysEvtCheminAccesImporter.class.getName()).log(Level.WARNING, null, ex);
        }

        if (row.getInt(Columns.ID_SYSTEME_REP.toString()) != null) {
            voie.setSystemeRepId(systemesReperage.get(row.getInt(Columns.ID_SYSTEME_REP.toString())).getId());
        }

        if (row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()) != null) {
            voie.setBorneDebutId(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()).doubleValue()).getId());
        }

        voie.setBorne_debut_aval(row.getBoolean(Columns.AMONT_AVAL_DEBUT.toString()));

        if (row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()) != null) {
            voie.setBorne_debut_distance(row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()).floatValue());
        }

        if (row.getDouble(Columns.ID_BORNEREF_FIN.toString()) != null) {
            if (bornes.get((int) row.getDouble(Columns.ID_BORNEREF_FIN.toString()).doubleValue()) != null) {
                voie.setBorneFinId(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_FIN.toString()).doubleValue()).getId());
            }
        }

        voie.setBorne_fin_aval(row.getBoolean(Columns.AMONT_AVAL_FIN.toString()));

        if (row.getDouble(Columns.DIST_BORNEREF_FIN.toString()) != null) {
            voie.setBorne_fin_distance(row.getDouble(Columns.DIST_BORNEREF_FIN.toString()).floatValue());
        }

        voie.setCommentaire(cleanNullString(row.getString(Columns.COMMENTAIRE.toString())));

        if (row.getString(Columns.N_SECTEUR.toString()) != null) {
            try {
                voie.setNumeroSecteur(Integer.parseInt(row.getString(Columns.N_SECTEUR.toString())));
            } catch (NumberFormatException e) {
                SirsCore.LOGGER.log(Level.FINE, e.getMessage());
            }
        }

        if (row.getInt(Columns.ID_TYPE_USAGE_VOIE.toString()) != null) {
            if (typesUsages.get(row.getInt(Columns.ID_TYPE_USAGE_VOIE.toString())) != null) {
                voie.setUsageId(typesUsages.get(row.getInt(Columns.ID_TYPE_USAGE_VOIE.toString())).getId());
            }
        }

        if (row.getInt(Columns.ID_TYPE_POSITION.toString()) != null) {
            voie.setPositionId(typesPosition.get(row.getInt(Columns.ID_TYPE_POSITION.toString())).getId());
        }

        if (row.getDouble(Columns.LARGEUR.toString()) != null) {
            voie.setLargeur(row.getDouble(Columns.LARGEUR.toString()).floatValue());
        }

        if (row.getInt(Columns.ID_TYPE_NATURE.toString()) != null) {
            voie.setMateriauId(typesMateriau.get(row.getInt(Columns.ID_TYPE_NATURE.toString())).getId());
        }

        voie.setDesignation(String.valueOf(row.getInt(Columns.ID_ELEMENT_RESEAU.toString())));
        voie.setGeometry(buildGeometry(troncon.getGeometry(), voie, tronconGestionDigueImporter.getBorneDigueRepository()));
        
        return voie;
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
