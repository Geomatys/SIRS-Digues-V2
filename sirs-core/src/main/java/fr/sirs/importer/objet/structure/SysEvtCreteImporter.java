package fr.sirs.importer.objet.structure;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import static fr.sirs.core.LinearReferencingUtilities.buildGeometry;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.core.model.Crete;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.RefFonction;
import fr.sirs.core.model.RefMateriau;
import fr.sirs.core.model.RefNature;
import fr.sirs.core.model.RefSource;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.TypeCoteImporter;
import fr.sirs.importer.objet.TypeFonctionImporter;
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
class SysEvtCreteImporter extends GenericStructureImporter<Crete> {

    SysEvtCreteImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final SystemeReperageImporter systemeReperageImporter,
            final BorneDigueImporter borneDigueImporter,
            final SourceInfoImporter typeSourceImporter,
            final TypeCoteImporter typeCoteImporter,
            final TypePositionImporter typePositionImporter,
            final TypeMateriauImporter typeMateriauImporter,
            final TypeNatureImporter typeNatureImporter,
            final TypeFonctionImporter typeFonctionImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter,
                typeSourceImporter, typeCoteImporter,
                typePositionImporter, typeMateriauImporter, typeNatureImporter,
                typeFonctionImporter);
    }

    private enum Columns {

        ID_ELEMENT_STRUCTURE,
        //        id_nom_element,//Inutile
        //        ID_SOUS_GROUPE_DONNEES,//Redondant
        //        LIBELLE_TYPE_ELEMENT_STRUCTURE,// Redondant
        //        DECALAGE_DEFAUT,//Affichage
        //        DECALAGE,//Affichage
        //        LIBELLE_SOURCE, // Dans le TypeSourceImporter
        //        LIBELLE_SYSTEME_REP,// Dans le SystemeRepImporter
        //        NOM_BORNE_DEBUT, // Dans le BorneImporter
        //        NOM_BORNE_FIN, // Dans le BorneImporter
        //        LIBELLE_TYPE_MATERIAU, // Dans l'importateur de matériaux
        //        LIBELLE_TYPE_NATURE, // Dans l'importation des natures
        //        LIBELLE_TYPE_FONCTION, // Redondant avec l'importation des fonctions
        //        ID_TYPE_ELEMENT_STRUCTURE,// Dans le TypeElementStructureImporter
        ID_SOURCE,
        ID_TRONCON_GESTION,
        DATE_DEBUT_VAL,
        DATE_FIN_VAL,
        PR_DEBUT_CALCULE,
        PR_FIN_CALCULE,
        ID_SYSTEME_REP,
        ID_BORNEREF_DEBUT,
        AMONT_AVAL_DEBUT,
        DIST_BORNEREF_DEBUT,
        ID_BORNEREF_FIN,
        AMONT_AVAL_FIN,
        DIST_BORNEREF_FIN,
        COMMENTAIRE,
        N_COUCHE,
        ID_TYPE_MATERIAU,
        ID_TYPE_NATURE,
        ID_TYPE_FONCTION,
        EPAISSEUR,
        //        TALUS_INTERCEPTE_CRETE,
        //        ID_AUTO

        // Empty fields
        //     LIBELLE_TYPE_COTE, // Dans le typeCoteimporter
        //     LIBELLE_TYPE_NATURE_HAUT, Dans le NatureImporter
        //     LIBELLE_TYPE_MATERIAU_HAUT, // Dans l'importateur de matériaux
        //     LIBELLE_TYPE_NATURE_BAS, // Dans l'importation des natures
        //     LIBELLE_TYPE_MATERIAU_BAS, // Dans l'importateur de matériaux
        //     LIBELLE_TYPE_OUVRAGE_PARTICULIER,
        //     LIBELLE_TYPE_POSITION, // Dans le TypePositionImporter
        //     RAISON_SOCIALE_ORG_PROPRIO,
        //     RAISON_SOCIALE_ORG_GESTION,
        //     INTERV_PROPRIO,
        //     INTERV_GARDIEN,
        //     LIBELLE_TYPE_COMPOSITION,
        //     LIBELLE_TYPE_VEGETATION, 
        ID_TYPE_COTE,
        X_DEBUT,
        Y_DEBUT,
        X_FIN,
        Y_FIN,
        //     ID_TYPE_NATURE_HAUT, // Pas dans le nouveau modèle
        //     ID_TYPE_MATERIAU_HAUT, // Pas dans le nouveau modèle
        //     ID_TYPE_MATERIAU_BAS, // Pas dans le nouveau modèle
        //     ID_TYPE_NATURE_BAS, // Pas dans le nouveau modèle
        //     LONG_RAMP_HAUT,
        //     LONG_RAMP_BAS,
        //     PENTE_INTERIEURE,
        //     ID_TYPE_OUVRAGE_PARTICULIER,
        ID_TYPE_POSITION,
//     ID_ORG_PROPRIO,
//     ID_ORG_GESTION,
//     ID_INTERV_PROPRIO,
//     ID_INTERV_GARDIEN,
//     DATE_DEBUT_ORGPROPRIO,
//     DATE_FIN_ORGPROPRIO,
//     DATE_DEBUT_GESTION,
//     DATE_FIN_GESTION,
//     DATE_DEBUT_INTERVPROPRIO,
//     DATE_FIN_INTERVPROPRIO,
//     ID_TYPE_COMPOSITION,
//     DISTANCE_TRONCON,
//     LONGUEUR,
//     DATE_DEBUT_GARDIEN,
//     DATE_FIN_GARDIEN,
//     LONGUEUR_PERPENDICULAIRE,
//     LONGUEUR_PARALLELE,
//     COTE_AXE,
//     ID_TYPE_VEGETATION,
//     HAUTEUR,
//     DIAMETRE,
//     DENSITE,
//     EPAISSEUR_Y11,
//     EPAISSEUR_Y12,
//     EPAISSEUR_Y21,
//     EPAISSEUR_Y22,
    };

    @Override
    public String getTableName() {
        return SYS_EVT_CRETE.toString();
    }

    @Override
    public Crete importRow(Row row) throws IOException, AccessDbImporterException {

        final TronconDigue troncon = tronconGestionDigueImporter.getTronconsDigues().get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();

        final Map<Integer, RefSource> typesSource = sourceInfoImporter.getTypeReferences();
        final Map<Integer, RefMateriau> typesMateriau = typeMateriauImporter.getTypeReferences();
        final Map<Integer, RefNature> typesNature = typeNatureImporter.getTypeReferences();
        final Map<Integer, RefFonction> typesFonction = typeFonctionImporter.getTypeReferences();

        final Crete crete = createAnonymValidElement(Crete.class);
        
        crete.setLinearId(troncon.getId());

        if (row.getInt(Columns.ID_SOURCE.toString()) != null) {
            crete.setSourceId(typesSource.get(row.getInt(Columns.ID_SOURCE.toString())).getId());
        }

        if (row.getDate(Columns.DATE_DEBUT_VAL.toString()) != null) {
            crete.setDate_debut(DbImporter.parseLocalDate(row.getDate(Columns.DATE_DEBUT_VAL.toString()), dateTimeFormatter));
        }

        if (row.getDate(Columns.DATE_FIN_VAL.toString()) != null) {
            crete.setDate_fin(DbImporter.parseLocalDate(row.getDate(Columns.DATE_FIN_VAL.toString()), dateTimeFormatter));
        }

        if (row.getDouble(Columns.PR_DEBUT_CALCULE.toString()) != null) {
            crete.setPrDebut(row.getDouble(Columns.PR_DEBUT_CALCULE.toString()).floatValue());
        }

        if (row.getDouble(Columns.PR_FIN_CALCULE.toString()) != null) {
            crete.setPrFin(row.getDouble(Columns.PR_FIN_CALCULE.toString()).floatValue());
        }

        if (row.getInt(Columns.ID_SYSTEME_REP.toString()) != null) {
            crete.setSystemeRepId(systemesReperage.get(row.getInt(Columns.ID_SYSTEME_REP.toString())).getId());
        }

        if (row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()) != null) {
            crete.setBorneDebutId(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()).doubleValue()).getId());
        }

        crete.setBorne_debut_aval(row.getBoolean(Columns.AMONT_AVAL_DEBUT.toString()));

        if (row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()) != null) {
            crete.setBorne_debut_distance(row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()).floatValue());
        }

        if (row.getDouble(Columns.ID_BORNEREF_FIN.toString()) != null) {
            crete.setBorneFinId(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_FIN.toString()).doubleValue()).getId());
        }

        crete.setBorne_fin_aval(row.getBoolean(Columns.AMONT_AVAL_FIN.toString()));

        if (row.getDouble(Columns.DIST_BORNEREF_FIN.toString()) != null) {
            crete.setBorne_fin_distance(row.getDouble(Columns.DIST_BORNEREF_FIN.toString()).floatValue());
        }

        crete.setCommentaire(row.getString(Columns.COMMENTAIRE.toString()));

        crete.setNumCouche(row.getInt(Columns.N_COUCHE.toString()));

        if (row.getInt(Columns.ID_TYPE_MATERIAU.toString()) != null) {
            crete.setMateriauId(typesMateriau.get(row.getInt(Columns.ID_TYPE_MATERIAU.toString())).getId());
        }

        if (row.getInt(Columns.ID_TYPE_NATURE.toString()) != null) {
            crete.setNatureId(typesNature.get(row.getInt(Columns.ID_TYPE_NATURE.toString())).getId());
        }

        if (row.getInt(Columns.ID_TYPE_FONCTION.toString()) != null) {
            crete.setFonctionId(typesFonction.get(row.getInt(Columns.ID_TYPE_FONCTION.toString())).getId());
        }

        if (row.getDouble(Columns.EPAISSEUR.toString()) != null) {
            crete.setEpaisseur(row.getDouble(Columns.EPAISSEUR.toString()).floatValue());
        }

        GeometryFactory geometryFactory = new GeometryFactory();
        final MathTransform lambertToRGF;
        try {
            lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), getOutputCrs(), true);

            try {

                if (row.getDouble(Columns.X_DEBUT.toString()) != null && row.getDouble(Columns.Y_DEBUT.toString()) != null) {
                    crete.setPositionDebut((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                            row.getDouble(Columns.X_DEBUT.toString()),
                            row.getDouble(Columns.Y_DEBUT.toString()))), lambertToRGF));
                }
            } catch (MismatchedDimensionException | TransformException ex) {
                Logger.getLogger(SysEvtCreteImporter.class.getName()).log(Level.WARNING, null, ex);
            }

            try {

                if (row.getDouble(Columns.X_FIN.toString()) != null && row.getDouble(Columns.Y_FIN.toString()) != null) {
                    crete.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                            row.getDouble(Columns.X_FIN.toString()),
                            row.getDouble(Columns.Y_FIN.toString()))), lambertToRGF));
                }
            } catch (MismatchedDimensionException | TransformException ex) {
                Logger.getLogger(SysEvtCreteImporter.class.getName()).log(Level.WARNING, null, ex);
            }
        } catch (FactoryException ex) {
            Logger.getLogger(SysEvtCreteImporter.class.getName()).log(Level.WARNING, null, ex);
        }

        crete.setDesignation(String.valueOf(row.getInt(Columns.ID_ELEMENT_STRUCTURE.toString())));
        crete.setGeometry(buildGeometry(troncon.getGeometry(), crete, tronconGestionDigueImporter.getBorneDigueRepository()));
        
        return crete;
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
