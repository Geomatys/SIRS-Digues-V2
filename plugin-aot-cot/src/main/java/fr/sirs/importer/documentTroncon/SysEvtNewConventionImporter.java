package fr.sirs.importer.documentTroncon;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import static fr.sirs.core.LinearReferencingUtilities.buildGeometry;
import fr.sirs.core.model.BorneDigue;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.NewConvention;
import fr.sirs.core.model.PositionDocument;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.documentTroncon.document.convention.NewConventionImporter;
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
class SysEvtNewConventionImporter extends GenericPositionDocumentImporter<PositionDocument> {

    private final NewConventionImporter conventionImporter;
    
    SysEvtNewConventionImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector, 
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final BorneDigueImporter borneDigueImporter, 
            final SystemeReperageImporter systemeReperageImporter,
            final NewConventionImporter conventionImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                borneDigueImporter, systemeReperageImporter);
        this.conventionImporter = conventionImporter;
    }
    
    private enum Columns {
        ID_DOC,
//        id_nom_element, // Redondant avec ID_DOC
//        ID_SOUS_GROUPE_DONNEES, // Redondant avec le type de données
//        LIBELLE_TYPE_DOCUMENT, // Redondant avec le type d'importateur
//        DECALAGE_DEFAUT, // Relatif à l'affichage
//        DECALAGE, // Relatif à l'affichage
//        LIBELLE_SYSTEME_REP, // Redondant avec l'importation des SR
//        NOM_BORNE_DEBUT, // Redondant avec l'importation des bornes
//        NOM_BORNE_FIN, // Redondant avec l'importation des bornes
//        NOM_PROFIL_EN_TRAVERS, 
//        LIBELLE_MARCHE,
//        INTITULE_ARTICLE,
//        TITRE_RAPPORT_ETUDE,
//        ID_TYPE_RAPPORT_ETUDE,
//        TE16_AUTEUR_RAPPORT,
//        DATE_RAPPORT,
        ID_TRONCON_GESTION,
//        ID_TYPE_DOCUMENT,
//        ID_DOSSIER, // Pas dans le nouveau modèle
//        DATE_DEBUT_VAL, // Pas dans le nouveau modèle
//        DATE_FIN_VAL, // Pas dans le nouveau modèle
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
//        REFERENCE_PAPIER, // Pas dans le nouveau modèle
//        REFERENCE_NUMERIQUE, // Pas dans le nouveau modèle
//        REFERENCE_CALQUE, // Pas dans le nouveau modèle
//        DATE_DOCUMENT, // Pas dans le nouveau modèle
//        NOM, // Pas dans le nouveau modèle
//        TM_AUTEUR_RAPPORT,
//        ID_MARCHE,
//        ID_INTERV_CREATEUR,
//        ID_ORG_CREATEUR,
//        ID_ARTICLE_JOURNAL,
//        ID_PROFIL_EN_TRAVERS,
//        ID_TYPE_DOCUMENT_A_GRANDE_ECHELLE,
        ID_CONVENTION,
//        ID_RAPPORT_ETUDE,
//        ID_AUTO
    }

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
        return SYS_EVT_CONVENTION.toString();
    }

//    @Override
//    protected void preCompute() throws IOException {
//        
//        positions = new HashMap<>();
//        positionsByTronconId = new HashMap<>();
//        
//        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
//        while (it.hasNext()){
//            final Row row = it.next();
//            final PositionDocument documentTroncon = createAnonymValidElement(PositionDocument.class);
//            positions.put(row.getInt(Columns.ID_DOC.toString()), documentTroncon);
//            
//            final Integer tronconId = row.getInt(Columns.ID_TRONCON_GESTION.toString());
//            if(positionsByTronconId.get(tronconId)==null)
//                positionsByTronconId.put(tronconId, new ArrayList<>());
//            positionsByTronconId.get(tronconId).add(documentTroncon);
//        }
//    }
//
//    @Override
//    protected void compute() throws IOException, AccessDbImporterException {
//        if(computed) return;
//        
//        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
//        while (it.hasNext()){
//            final Row row = it.next();
//            final PositionDocument docTroncon = importRow(row);
//
//            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
//            positions.put(row.getInt(Columns.ID_DOC.toString()), docTroncon);
//
//            // Set the list ByTronconId
//            List<PositionDocument> listByTronconId = positionsByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
//            if (listByTronconId == null) {
//                listByTronconId = new ArrayList<>();
//                positionsByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
//            }
//            listByTronconId.add(docTroncon);
//            
//            
//        }
//        computed=true;
//    }

    @Override
    PositionDocument importRow(Row row) throws IOException, AccessDbImporterException {

        final TronconDigue troncon = tronconGestionDigueImporter.getTronconsDigues().get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        final Map<Integer, NewConvention> conventions = conventionImporter.getRelated();

        final PositionDocument position = createAnonymValidElement(PositionDocument.class);
        position.setLinearId(troncon.getId());
        
        final GeometryFactory geometryFactory = new GeometryFactory();
        final MathTransform lambertToRGF;
        try {
            lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), getOutputCrs(), true);

            try {

                if (row.getDouble(Columns.X_DEBUT.toString()) != null && row.getDouble(Columns.Y_DEBUT.toString()) != null) {
                    position.setPositionDebut((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                            row.getDouble(Columns.X_DEBUT.toString()),
                            row.getDouble(Columns.Y_DEBUT.toString()))), lambertToRGF));
                }
            } catch (MismatchedDimensionException | TransformException ex) {
                Logger.getLogger(SysEvtNewConventionImporter.class.getName()).log(Level.WARNING, null, ex);
            }

            try {

                if (row.getDouble(Columns.X_FIN.toString()) != null && row.getDouble(Columns.Y_FIN.toString()) != null) {
                    position.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                            row.getDouble(Columns.X_FIN.toString()),
                            row.getDouble(Columns.Y_FIN.toString()))), lambertToRGF));
                }
            } catch (MismatchedDimensionException | TransformException ex) {
                Logger.getLogger(SysEvtNewConventionImporter.class.getName()).log(Level.WARNING, null, ex);
            }
        } catch (FactoryException ex) {
            Logger.getLogger(SysEvtNewConventionImporter.class.getName()).log(Level.WARNING, null, ex);
        }

        position.setCommentaire(row.getString(Columns.COMMENTAIRE.toString()));

        if (row.getInt(Columns.ID_CONVENTION.toString()) != null) {
            if (conventions.get(row.getInt(Columns.ID_CONVENTION.toString())) != null) {
                position.setSirsdocument(conventions.get(row.getInt(Columns.ID_CONVENTION.toString())).getId());
            }
        }

        if (row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()) != null) {
            position.setBorneDebutId(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()).doubleValue()).getId());
        }

        if (row.getDouble(Columns.ID_BORNEREF_FIN.toString()) != null) {
            position.setBorneFinId(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_FIN.toString()).doubleValue()).getId());
        }
        position.setBorne_debut_aval(row.getBoolean(Columns.AMONT_AVAL_DEBUT.toString()));
        position.setBorne_fin_aval(row.getBoolean(Columns.AMONT_AVAL_FIN.toString()));
        if (row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()) != null) {
            position.setBorne_debut_distance(row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()).floatValue());
        }
        if (row.getDouble(Columns.DIST_BORNEREF_FIN.toString()) != null) {
            position.setBorne_fin_distance(row.getDouble(Columns.DIST_BORNEREF_FIN.toString()).floatValue());
        }

        if (row.getInt(Columns.ID_SYSTEME_REP.toString()) != null) {
            position.setSystemeRepId(systemesReperage.get(row.getInt(Columns.ID_SYSTEME_REP.toString())).getId());
        }

        if (row.getDouble(Columns.PR_DEBUT_CALCULE.toString()) != null) {
            position.setPrDebut(row.getDouble(Columns.PR_DEBUT_CALCULE.toString()).floatValue());
        }

        if (row.getDouble(Columns.PR_FIN_CALCULE.toString()) != null) {
            position.setPrFin(row.getDouble(Columns.PR_FIN_CALCULE.toString()).floatValue());
        }
        position.setDesignation(String.valueOf(row.getInt(Columns.ID_DOC.toString())));
        position.setGeometry(buildGeometry(troncon.getGeometry(), position, tronconGestionDigueImporter.getBorneDigueRepository()));
        
        return position;
    }
}