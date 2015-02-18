package fr.sirs.importer.documentTroncon;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.DocumentTroncon;
import fr.sirs.core.model.RapportEtude;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.documentTroncon.document.rapportEtude.RapportEtudeImporter;
import java.io.IOException;
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
class SysEvtRapportEtudesImporter extends GenericDocumentImporter {

    private final RapportEtudeImporter rapportEtudeImporter;
    
    SysEvtRapportEtudesImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector, 
            final BorneDigueImporter borneDigueImporter, 
            final SystemeReperageImporter systemeReperageImporter,
            final RapportEtudeImporter rapportEtudeImporter) {
        super(accessDatabase, couchDbConnector, 
                borneDigueImporter, systemeReperageImporter);
        this.rapportEtudeImporter = rapportEtudeImporter;
    }
    
    private enum Columns {
        ID_DOC,
//        id_nom_element, // Redondand avec ID_DOC
//        ID_SOUS_GROUPE_DONNEES, // Redondant avec le type de données
//        LIBELLE_TYPE_DOCUMENT, // Redondant avec le type de données
//        DECALAGE_DEFAUT, // Relatif à l'affichage
//        DECALAGE, // Relatif à l'affichage
//        LIBELLE_SYSTEME_REP, // Redondant avec l'importation des SR
//        NOM_BORNE_DEBUT, // Redondant avec l'importation des bornes
//        NOM_BORNE_FIN, // Redondant avec l'importation des bornes
//        NOM_PROFIL_EN_TRAVERS, // Non pertinent pour le rapport d'études
//        LIBELLE_MARCHE, // Non pertinent pour le rapport d'études
//        INTITULE_ARTICLE, // Non pertinent pour le rapport d'études
//        TITRE_RAPPORT_ETUDE, // Pas dans le nouveau modèle
//        ID_TYPE_RAPPORT_ETUDE, // Redondant avec l'importation des rapports d'étude
//        TE16_AUTEUR_RAPPORT,
//        DATE_RAPPORT,
        ID_TRONCON_GESTION,
//        ID_TYPE_DOCUMENT, // Redondant avec le type de données
//        ID_DOSSIER,
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
//        DATE_DOCUMENT,
//        NOM,
//        TM_AUTEUR_RAPPORT,
//        ID_MARCHE, // Non pertinent pour le rapport d'études
//        ID_INTERV_CREATEUR,
//        ID_ORG_CREATEUR,
//        ID_ARTICLE_JOURNAL, // Non pertinent pour le rapport d'études
//        ID_PROFIL_EN_TRAVERS, // Non pertinent pour le rapport d'études
//        ID_TYPE_DOCUMENT_A_GRANDE_ECHELLE,
//        ID_CONVENTION, // Non pertinent pour le rapport d'études
        ID_RAPPORT_ETUDE,
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
        return DbImporter.TableName.SYS_EVT_RAPPORT_ETUDES.toString();
    }

    @Override
    protected void preCompute() throws IOException {
        
        documentTroncons = new HashMap<>();
        documentTronconByTronconId = new HashMap<>();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()){
            final Row row = it.next();
            final DocumentTroncon documentTroncon = new DocumentTroncon();
            documentTroncons.put(row.getInt(Columns.ID_DOC.toString()), documentTroncon);
            
            final Integer tronconId = row.getInt(Columns.ID_TRONCON_GESTION.toString());
            if(documentTronconByTronconId.get(tronconId)==null)
                documentTronconByTronconId.put(tronconId, new ArrayList<>());
            documentTronconByTronconId.get(tronconId).add(documentTroncon);
        }
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()){
            final Row row = it.next();
            final DocumentTroncon docTroncon = documentTroncons.get(row.getInt(Columns.ID_DOC.toString()));
            
            importRow(row, docTroncon);
        }
        computed=true;
    }

    @Override
    void importRow(Row row, DocumentTroncon docTroncon) throws IOException, AccessDbImporterException {

        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        final Map<Integer, RapportEtude> rapports = rapportEtudeImporter.getRelated();

        docTroncon.setCommentaire(row.getString(Columns.COMMENTAIRE.toString()));

        if (row.getDouble(Columns.PR_DEBUT_CALCULE.toString()) != null) {
            docTroncon.setPR_debut(row.getDouble(Columns.PR_DEBUT_CALCULE.toString()).floatValue());
        }

        if (row.getDouble(Columns.PR_FIN_CALCULE.toString()) != null) {
            docTroncon.setPR_fin(row.getDouble(Columns.PR_FIN_CALCULE.toString()).floatValue());
        }

        GeometryFactory geometryFactory = new GeometryFactory();
        final MathTransform lambertToRGF;
        try {
            lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), getOutputCrs(), true);

            try {

                if (row.getDouble(Columns.X_DEBUT.toString()) != null && row.getDouble(Columns.Y_DEBUT.toString()) != null) {
                    docTroncon.setPositionDebut((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                            row.getDouble(Columns.X_DEBUT.toString()),
                            row.getDouble(Columns.Y_DEBUT.toString()))), lambertToRGF));
                }
            } catch (MismatchedDimensionException | TransformException ex) {
                Logger.getLogger(SysEvtRapportEtudesImporter.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {

                if (row.getDouble(Columns.X_FIN.toString()) != null && row.getDouble(Columns.Y_FIN.toString()) != null) {
                    docTroncon.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                            row.getDouble(Columns.X_FIN.toString()),
                            row.getDouble(Columns.Y_FIN.toString()))), lambertToRGF));
                }
            } catch (MismatchedDimensionException | TransformException ex) {
                Logger.getLogger(SysEvtRapportEtudesImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FactoryException ex) {
            Logger.getLogger(SysEvtRapportEtudesImporter.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (row.getInt(Columns.ID_SYSTEME_REP.toString()) != null) {
            docTroncon.setSystemeRepId(systemesReperage.get(row.getInt(Columns.ID_SYSTEME_REP.toString())).getId());
        }

        if (row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()) != null) {
            docTroncon.setBorneDebutId(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()).doubleValue()).getId());
        }

        docTroncon.setBorne_debut_aval(row.getBoolean(Columns.AMONT_AVAL_DEBUT.toString()));

        if (row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()) != null) {
            docTroncon.setBorne_debut_distance(row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()).floatValue());
        }

        if (row.getDouble(Columns.ID_BORNEREF_FIN.toString()) != null) {
            docTroncon.setBorneFinId(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_FIN.toString()).doubleValue()).getId());
        }

        docTroncon.setBorne_fin_aval(row.getBoolean(Columns.AMONT_AVAL_FIN.toString()));

        if (row.getDouble(Columns.DIST_BORNEREF_FIN.toString()) != null) {
            docTroncon.setBorne_fin_distance(row.getDouble(Columns.DIST_BORNEREF_FIN.toString()).floatValue());
        }

        if (row.getInt(Columns.ID_RAPPORT_ETUDE.toString()) != null) {
            if (rapports.get(row.getInt(Columns.ID_RAPPORT_ETUDE.toString())) != null) {
                docTroncon.setSirsdocument(rapports.get(row.getInt(Columns.ID_RAPPORT_ETUDE.toString())).getId());
            }
        }
        docTroncon.setPseudoId(String.valueOf(row.getInt(Columns.ID_DOC.toString())));
    }
}
