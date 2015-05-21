package fr.sirs.importer.objet.structure;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import static fr.sirs.core.LinearReferencingUtilities.buildGeometry;
import fr.sirs.core.model.BorneDigue;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.PiedFrontFrancBord;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.core.model.RefCote;
import fr.sirs.core.model.RefMateriau;
import fr.sirs.core.model.RefNature;
import fr.sirs.core.model.RefPosition;
import fr.sirs.core.model.RefSource;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.TypeCoteImporter;
import fr.sirs.importer.objet.TypeMateriauImporter;
import fr.sirs.importer.objet.TypeNatureImporter;
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
class SysEvtPiedFrontFrancBordImporter extends GenericStructureImporter<PiedFrontFrancBord> {

    SysEvtPiedFrontFrancBordImporter(final Database accessDatabase,
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
        //        id_nom_element,
        //        ID_SOUS_GROUPE_DONNEES,
        //        LIBELLE_TYPE_ELEMENT_STRUCTURE,
        //        DECALAGE_DEFAUT,
        //        DECALAGE,
        //        LIBELLE_SOURCE,
        //        LIBELLE_TYPE_COTE,
        //        LIBELLE_SYSTEME_REP,
        //        NOM_BORNE_DEBUT,
        //        NOM_BORNE_FIN,
        //        LIBELLE_TYPE_MATERIAU,
        //        LIBELLE_TYPE_NATURE,
        //        LIBELLE_TYPE_FONCTION,
        //        LIBELLE_TYPE_NATURE_HAUT,
        //        LIBELLE_TYPE_MATERIAU_HAUT,
        //        LIBELLE_TYPE_NATURE_BAS,
        //        LIBELLE_TYPE_MATERIAU_BAS,
        //        LIBELLE_TYPE_OUVRAGE_PARTICULIER,
        //        LIBELLE_TYPE_POSITION,
        //        RAISON_SOCIALE_ORG_PROPRIO,
        //        RAISON_SOCIALE_ORG_GESTION,
        //        INTERV_PROPRIO,
        //        INTERV_GARDIEN,
        //        LIBELLE_TYPE_COMPOSITION,
        //        LIBELLE_TYPE_VEGETATION,
        //        ID_TYPE_ELEMENT_STRUCTURE,
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
        //        N_COUCHE,
        ID_TYPE_MATERIAU,
        ID_TYPE_NATURE,
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
        return SYS_EVT_PIED_FRONT_FRANC_BORD.toString();
    }
    
    @Override
    public PiedFrontFrancBord importRow(Row row) throws IOException, AccessDbImporterException {

        final TronconDigue troncon = tronconGestionDigueImporter.getTronconsDigues().get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();

        final Map<Integer, RefSource> typesSource = sourceInfoImporter.getTypeReferences();
        final Map<Integer, RefCote> typesCote = typeCoteImporter.getTypeReferences();
        final Map<Integer, RefPosition> typesPosition = typePositionImporter.getTypeReferences();
        final Map<Integer, RefMateriau> typesMateriau = typeMateriauImporter.getTypeReferences();
        final Map<Integer, RefNature> typesNature = typeNatureImporter.getTypeReferences();

        final PiedFrontFrancBord pied = createAnonymValidElement(PiedFrontFrancBord.class);
        
        pied.setLinearId(troncon.getId());

        if (row.getInt(Columns.ID_TYPE_COTE.toString()) != null) {
            pied.setCoteId(typesCote.get(row.getInt(Columns.ID_TYPE_COTE.toString())).getId());
        }

        if (row.getInt(Columns.ID_SOURCE.toString()) != null) {
            pied.setSourceId(typesSource.get(row.getInt(Columns.ID_SOURCE.toString())).getId());
        }

        if (row.getDate(Columns.DATE_DEBUT_VAL.toString()) != null) {
            pied.setDate_debut(DbImporter.parse(row.getDate(Columns.DATE_DEBUT_VAL.toString()), dateTimeFormatter));
        }

        if (row.getDate(Columns.DATE_FIN_VAL.toString()) != null) {
            pied.setDate_fin(DbImporter.parse(row.getDate(Columns.DATE_FIN_VAL.toString()), dateTimeFormatter));
        }

        if (row.getDouble(Columns.PR_DEBUT_CALCULE.toString()) != null) {
            pied.setPR_debut(row.getDouble(Columns.PR_DEBUT_CALCULE.toString()).floatValue());
        }

        if (row.getDouble(Columns.PR_FIN_CALCULE.toString()) != null) {
            pied.setPR_fin(row.getDouble(Columns.PR_FIN_CALCULE.toString()).floatValue());
        }

        GeometryFactory geometryFactory = new GeometryFactory();
        final MathTransform lambertToRGF;
        try {
            lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), getOutputCrs(), true);

            try {

                if (row.getDouble(Columns.X_DEBUT.toString()) != null && row.getDouble(Columns.Y_DEBUT.toString()) != null) {
                    pied.setPositionDebut((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                            row.getDouble(Columns.X_DEBUT.toString()),
                            row.getDouble(Columns.Y_DEBUT.toString()))), lambertToRGF));
                }
            } catch (MismatchedDimensionException | TransformException ex) {
                Logger.getLogger(SysEvtPiedFrontFrancBordImporter.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {

                if (row.getDouble(Columns.X_FIN.toString()) != null && row.getDouble(Columns.Y_FIN.toString()) != null) {
                    pied.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                            row.getDouble(Columns.X_FIN.toString()),
                            row.getDouble(Columns.Y_FIN.toString()))), lambertToRGF));
                }
            } catch (MismatchedDimensionException | TransformException ex) {
                Logger.getLogger(SysEvtPiedFrontFrancBordImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FactoryException ex) {
            Logger.getLogger(SysEvtPiedFrontFrancBordImporter.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (row.getInt(Columns.ID_SYSTEME_REP.toString()) != null) {
            pied.setSystemeRepId(systemesReperage.get(row.getInt(Columns.ID_SYSTEME_REP.toString())).getId());
        }

        if (row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()) != null) {
            pied.setBorneDebutId(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()).doubleValue()).getId());
        }

        pied.setBorne_debut_aval(row.getBoolean(Columns.AMONT_AVAL_DEBUT.toString()));

        if (row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()) != null) {
            pied.setBorne_debut_distance(row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()).floatValue());
        }

        if (row.getDouble(Columns.ID_BORNEREF_FIN.toString()) != null) {
            if (bornes.get((int) row.getDouble(Columns.ID_BORNEREF_FIN.toString()).doubleValue()) != null) {
                pied.setBorneFinId(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_FIN.toString()).doubleValue()).getId());
            }
        }

        pied.setBorne_fin_aval(row.getBoolean(Columns.AMONT_AVAL_FIN.toString()));

        if (row.getDouble(Columns.DIST_BORNEREF_FIN.toString()) != null) {
            pied.setBorne_fin_distance(row.getDouble(Columns.DIST_BORNEREF_FIN.toString()).floatValue());
        }

        pied.setCommentaire(row.getString(Columns.COMMENTAIRE.toString()));

        if (row.getInt(Columns.ID_TYPE_MATERIAU.toString()) != null) {
            pied.setMateriauId(typesMateriau.get(row.getInt(Columns.ID_TYPE_MATERIAU.toString())).getId());
        }

        if (row.getInt(Columns.ID_TYPE_NATURE.toString()) != null) {
            pied.setNatureId(typesNature.get(row.getInt(Columns.ID_TYPE_NATURE.toString())).getId());
        }

        if (row.getInt(Columns.ID_TYPE_POSITION.toString()) != null) {
            pied.setPositionId(typesPosition.get(row.getInt(Columns.ID_TYPE_POSITION.toString())).getId());
        }

        pied.setDesignation(String.valueOf(row.getInt(Columns.ID_ELEMENT_STRUCTURE.toString())));
        pied.setGeometry(buildGeometry(troncon.getGeometry(), pied, tronconGestionDigueImporter.getBorneDigueRepository()));
        
        return pied;
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
