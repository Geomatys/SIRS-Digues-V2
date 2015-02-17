package fr.sirs.importer.documentTroncon;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.DocumentTroncon;
import fr.sirs.core.model.DocumentGrandeEchelle;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.documentTroncon.document.documentAGrandeEchelle.DocumentAGrandeEchelleImporter;
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
class SysEvtDocumentAGrandeEchelleImporter extends GenericDocumentImporter {

    private final DocumentAGrandeEchelleImporter documentAGrandeEchelleImporter;
    
    SysEvtDocumentAGrandeEchelleImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector, 
            final BorneDigueImporter borneDigueImporter, 
            final SystemeReperageImporter systemeReperageImporter,
            final DocumentAGrandeEchelleImporter documentAGrandeEchelleImporter) {
        super(accessDatabase, couchDbConnector, 
                borneDigueImporter, systemeReperageImporter);
        this.documentAGrandeEchelleImporter = documentAGrandeEchelleImporter;
    }
    
    private enum Columns {
        ID_DOC,
//        id_nom_element,
//        ID_SOUS_GROUPE_DONNEES,
//        LIBELLE_TYPE_DOCUMENT,
//        DECALAGE_DEFAUT,
//        DECALAGE,
//        LIBELLE_SYSTEME_REP,
//        NOM_BORNE_DEBUT,
//        NOM_BORNE_FIN,
//        NOM_PROFIL_EN_TRAVERS,
//        LIBELLE_MARCHE,
//        INTITULE_ARTICLE,
//        TITRE_RAPPORT_ETUDE,
//        ID_TYPE_RAPPORT_ETUDE,
//        TE16_AUTEUR_RAPPORT,
//        DATE_RAPPORT,
        ID_TRONCON_GESTION,
//        ID_TYPE_DOCUMENT,
//        ID_DOSSIER,
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
//        REFERENCE_PAPIER,
//        REFERENCE_NUMERIQUE,
//        REFERENCE_CALQUE,
//        DATE_DOCUMENT,
//        NOM,
//        TM_AUTEUR_RAPPORT,
//        ID_MARCHE,
//        ID_INTERV_CREATEUR,
//        ID_ORG_CREATEUR,
//        ID_ARTICLE_JOURNAL,
//        ID_PROFIL_EN_TRAVERS,
//        ID_TYPE_DOCUMENT_A_GRANDE_ECHELLE,
//        ID_CONVENTION,
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
        return DbImporter.TableName.SYS_EVT_DOCUMENT_A_GRANDE_ECHELLE.toString();
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
        if(computed) return;
        
        
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
        final Map<Integer, DocumentGrandeEchelle> documentsGrandeEchelle = documentAGrandeEchelleImporter.getRelated();

        if (row.getDouble(Columns.PR_DEBUT_CALCULE.toString()) != null) {
            docTroncon.setPR_debut(row.getDouble(Columns.PR_DEBUT_CALCULE.toString()).floatValue());
        }

        if (row.getDouble(Columns.PR_FIN_CALCULE.toString()) != null) {
            docTroncon.setPR_fin(row.getDouble(Columns.PR_FIN_CALCULE.toString()).floatValue());
        }

        final GeometryFactory geometryFactory = new GeometryFactory();
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
                Logger.getLogger(SysEvtDocumentAGrandeEchelleImporter.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {

                if (row.getDouble(Columns.X_FIN.toString()) != null && row.getDouble(Columns.Y_FIN.toString()) != null) {
                    docTroncon.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                            row.getDouble(Columns.X_FIN.toString()),
                            row.getDouble(Columns.Y_FIN.toString()))), lambertToRGF));
                }
            } catch (MismatchedDimensionException | TransformException ex) {
                Logger.getLogger(SysEvtDocumentAGrandeEchelleImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FactoryException ex) {
            Logger.getLogger(SysEvtDocumentAGrandeEchelleImporter.class.getName()).log(Level.SEVERE, null, ex);
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

        docTroncon.setCommentaire(row.getString(Columns.COMMENTAIRE.toString()));

            // Pas d'identifiant vers des documents à grande échelle ???
        // Comme pour les profils en long on se base sur l'identifiant du document relatif
        if (row.getInt(Columns.ID_DOC.toString()) != null) {
            if (documentsGrandeEchelle.get(row.getInt(Columns.ID_DOC.toString())) != null) {
                docTroncon.setSirsdocument(documentsGrandeEchelle.get(row.getInt(Columns.ID_DOC.toString())).getId());
            }
        }
        docTroncon.setPseudoId(row.getInt(Columns.ID_DOC.toString()));
    }
}
