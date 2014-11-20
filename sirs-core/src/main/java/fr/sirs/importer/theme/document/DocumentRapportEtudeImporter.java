package fr.sirs.importer.theme.document;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.core.component.DocumentRepository;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Document;
import fr.sirs.core.model.RapportEtude;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.TronconGestionDigueImporter;
import fr.sirs.importer.theme.document.related.rapportEtude.RapportEtudeImporter;
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
class DocumentRapportEtudeImporter extends GenericDocumentImporter {

    private final RapportEtudeImporter rapportEtudeImporter;
    
    DocumentRapportEtudeImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector, 
            final DocumentRepository documentRepository, 
            final BorneDigueImporter borneDigueImporter, 
            final SystemeReperageImporter systemeReperageImporter,
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final RapportEtudeImporter rapportEtudeImporter) {
        super(accessDatabase, couchDbConnector, documentRepository, 
                borneDigueImporter, systemeReperageImporter, 
                tronconGestionDigueImporter);
        this.rapportEtudeImporter = rapportEtudeImporter;
    }
    
    private enum DocumentRapportEtudeColumns {
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
        DATE_DOCUMENT,
        NOM,
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
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (DocumentRapportEtudeColumns c : DocumentRapportEtudeColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.SYS_EVT_RAPPORT_ETUDES.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        documents = new HashMap<>();
        
        final Map<Integer, TronconDigue> troncons = tronconGestionDigueImporter.getTronconsDigues();
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        final Map<Integer, RapportEtude> rapports = rapportEtudeImporter.getRapportsEtude();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()){
            final Row row = it.next();
            final Document document = new Document();
            
            document.setTronconId(troncons.get(row.getInt(DocumentRapportEtudeColumns.ID_TRONCON_GESTION.toString())).getId());
            
            document.setCommentaire(row.getString(DocumentRapportEtudeColumns.COMMENTAIRE.toString()));
            
            if (row.getDouble(DocumentRapportEtudeColumns.PR_DEBUT_CALCULE.toString()) != null) {
                document.setPR_debut(row.getDouble(DocumentRapportEtudeColumns.PR_DEBUT_CALCULE.toString()).floatValue());
            }

            if (row.getDouble(DocumentRapportEtudeColumns.PR_FIN_CALCULE.toString()) != null) {
                document.setPR_fin(row.getDouble(DocumentRapportEtudeColumns.PR_FIN_CALCULE.toString()).floatValue());
            }
            
            GeometryFactory geometryFactory = new GeometryFactory();
            final MathTransform lambertToRGF;
            try {
                lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), CRS.decode("EPSG:2154"), true);

                try {

                    if (row.getDouble(DocumentRapportEtudeColumns.X_DEBUT.toString()) != null && row.getDouble(DocumentRapportEtudeColumns.Y_DEBUT.toString()) != null) {
                        document.setPositionDebut((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(DocumentRapportEtudeColumns.X_DEBUT.toString()),
                                row.getDouble(DocumentRapportEtudeColumns.Y_DEBUT.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(DocumentRapportEtudeImporter.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {

                    if (row.getDouble(DocumentRapportEtudeColumns.X_FIN.toString()) != null && row.getDouble(DocumentRapportEtudeColumns.Y_FIN.toString()) != null) {
                        document.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(DocumentRapportEtudeColumns.X_FIN.toString()),
                                row.getDouble(DocumentRapportEtudeColumns.Y_FIN.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(DocumentRapportEtudeImporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (FactoryException ex) {
                Logger.getLogger(DocumentRapportEtudeImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if (row.getDate(DocumentRapportEtudeColumns.DATE_DOCUMENT.toString()) != null) {
                document.setDate_document(LocalDateTime.parse(row.getDate(DocumentRapportEtudeColumns.DATE_DOCUMENT.toString()).toString(), dateTimeFormatter));
            }
            
            if(row.getInt(DocumentRapportEtudeColumns.ID_SYSTEME_REP.toString())!=null){
                document.setSystemeRepId(systemesReperage.get(row.getInt(DocumentRapportEtudeColumns.ID_SYSTEME_REP.toString())).getId());
            }
            
            if(row.getDouble(DocumentRapportEtudeColumns.ID_BORNEREF_DEBUT.toString())!=null){
                document.setBorneDebutId(bornes.get((int) row.getDouble(DocumentRapportEtudeColumns.ID_BORNEREF_DEBUT.toString()).doubleValue()).getId());
            }
            
            document.setBorne_debut_aval(row.getBoolean(DocumentRapportEtudeColumns.AMONT_AVAL_DEBUT.toString()));
            
            if (row.getDouble(DocumentRapportEtudeColumns.DIST_BORNEREF_DEBUT.toString()) != null) {
                document.setBorne_debut_distance(row.getDouble(DocumentRapportEtudeColumns.DIST_BORNEREF_DEBUT.toString()).floatValue());
            }
            
            if(row.getDouble(DocumentRapportEtudeColumns.ID_BORNEREF_FIN.toString())!=null){
                document.setBorneFinId(bornes.get((int) row.getDouble(DocumentRapportEtudeColumns.ID_BORNEREF_FIN.toString()).doubleValue()).getId());
            }
            
            document.setBorne_fin_aval(row.getBoolean(DocumentRapportEtudeColumns.AMONT_AVAL_FIN.toString()));
            
            if (row.getDouble(DocumentRapportEtudeColumns.DIST_BORNEREF_FIN.toString()) != null) {
                document.setBorne_fin_distance(row.getDouble(DocumentRapportEtudeColumns.DIST_BORNEREF_FIN.toString()).floatValue());
            }
            
            document.setLibelle(row.getString(DocumentRapportEtudeColumns.NOM.toString()));
            
            
            
            
            
            
            
            if (row.getInt(DocumentRapportEtudeColumns.ID_RAPPORT_ETUDE.toString()) != null) {
                if (rapports.get(row.getInt(DocumentRapportEtudeColumns.ID_RAPPORT_ETUDE.toString())) != null) {
                    document.setConvention(rapports.get(row.getInt(DocumentRapportEtudeColumns.ID_RAPPORT_ETUDE.toString())).getId());
                }
            }
            
            
            
            
            

            
            
            
            
            
            
            
            
            documents.put(row.getInt(DocumentRapportEtudeColumns.ID_DOC.toString()), document);
            
        }
    }
    
    public Map<Integer, Document> getDocumentRapportEtude() throws IOException, AccessDbImporterException{
        if(documents==null) compute();
        return documents;
    }
}
