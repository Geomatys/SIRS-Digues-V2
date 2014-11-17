package fr.sirs.importer.theme.document;

import fr.sirs.importer.theme.document.related.ConventionImporter;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.core.component.DocumentRepository;
import fr.sirs.core.model.ArticleJournal;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Convention;
import fr.sirs.core.model.Document;
import fr.sirs.core.model.Marche;
import fr.sirs.core.model.RefTypeDocument;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.TronconGestionDigueImporter;
import fr.sirs.importer.theme.document.related.ProfilTraversImporter;
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
public class DocumentImporter extends GenericDocumentImporter {
    
    private ConventionImporter conventionImporter;
    private DocumentConventionImporter documentConventionImporter;
    private ProfilTraversImporter profilTraversImporter;
    private DocumentProfilTraversImporter documentProfilTraversImporter;
    private TypeDocumentImporter typeDocumentImporter;
    
    public DocumentImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector, 
            final DocumentRepository documentRepository, 
            final BorneDigueImporter borneDigueImporter,
            final SystemeReperageImporter systemeReperageImporter,
            final ConventionImporter conventionImporter,
            final ProfilTraversImporter profilTraversImporter,
            final TronconGestionDigueImporter tronconGestionDigueImporter){
        super(accessDatabase, couchDbConnector, documentRepository, 
                borneDigueImporter, systemeReperageImporter, tronconGestionDigueImporter);
        this.typeDocumentImporter = new TypeDocumentImporter(accessDatabase, couchDbConnector, new TypeDocumentGrandeEchelleImporter(accessDatabase, couchDbConnector));
        
        this.conventionImporter = conventionImporter;
        this.documentConventionImporter = new DocumentConventionImporter(
                accessDatabase, couchDbConnector, documentRepository, 
                borneDigueImporter, systemeReperageImporter, 
                tronconGestionDigueImporter, conventionImporter);
        this.profilTraversImporter = profilTraversImporter;
        this.documentProfilTraversImporter = new DocumentProfilTraversImporter(
                accessDatabase, couchDbConnector, documentRepository, 
                borneDigueImporter, systemeReperageImporter, 
                tronconGestionDigueImporter, profilTraversImporter);
    }
    
    private enum DocumentColumns {
        ID_DOC,
        ID_TRONCON_GESTION,
        ID_TYPE_DOCUMENT,
//        ID_DOSSIER, // Pas dans le nouveau modèle
//        REFERENCE_PAPIER, // Pas dans le nouveau modèle
//        REFERENCE_NUMERIQUE, // Pas dans le nouveau modèle
//        REFERENCE_CALQUE, // Pas dans le nouveau modèle
        DATE_DOCUMENT,
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
        NOM,
//        ID_MARCHE,
//        ID_INTERV_CREATEUR,
//        ID_ORG_CREATEUR,
//        ID_ARTICLE_JOURNAL,
        ID_PROFIL_EN_TRAVERS,
//        ID_PROFIL_EN_LONG,
//        ID_TYPE_DOCUMENT_A_GRANDE_ECHELLE,
        ID_CONVENTION,
        DATE_DERNIERE_MAJ,
//        AUTEUR_RAPPORT,
//        ID_RAPPORT_ETUDE
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (DocumentColumns c : DocumentColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.DOCUMENT.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        documents = new HashMap<>();
        final Map<Integer, Class> classesDocument = typeDocumentImporter.getClasseDocument();
        final Map<Integer, RefTypeDocument> typesDocument = typeDocumentImporter.getTypeDocument();
        
        final Map<Integer, Document> documentConventions = documentConventionImporter.getDocumentConvention();
        if(documentConventions!=null) for(final Integer key : documentConventions.keySet()){
            if(documents.get(key)!=null) throw new AccessDbImporterException(documentConventions.get(key).getClass().getCanonicalName()+" : This structure ID is ever used ("+key+") by "+documents.get(key).getClass().getCanonicalName());
            else documents.put(key, documentConventions.get(key));
        }
        
        final Map<Integer, Document> documentProfilTravers = documentProfilTraversImporter.getDocumentProfilTravers();
        if(documentProfilTravers!=null) for(final Integer key : documentProfilTravers.keySet()){
            if(documents.get(key)!=null) throw new AccessDbImporterException(documentConventions.get(key).getClass().getCanonicalName()+" : This structure ID is ever used ("+key+") by "+documents.get(key).getClass().getCanonicalName());
            else documents.put(key, documentProfilTravers.get(key));
        }
        
        final Map<Integer, TronconDigue> troncons = tronconGestionDigueImporter.getTronconsDigues();
        final Map<Integer, Convention> conventions = conventionImporter.getConventions();
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while(it.hasNext()){
            final Row row = it.next();
            final Document document;
            final boolean nouveauDocument;
            if(documents.get(row.getInt(DocumentColumns.ID_DOC.toString()))!=null){
                document = documents.get(row.getInt(DocumentColumns.ID_DOC.toString()));
                nouveauDocument=false;
            }
            else{
                System.out.println("Nouveau document !!");
                document = new Document();
                nouveauDocument=true;
            }
            
            /*
            
            document.setTronconId(troncons.get(row.getInt(DocumentColumns.ID_TRONCON_GESTION.toString())).getId());
            
            if (row.getDate(DocumentColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                document.setDateMaj(LocalDateTime.parse(row.getDate(DocumentColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            if (row.getDate(DocumentColumns.DATE_DOCUMENT.toString()) != null) {
                document.setDate_document(LocalDateTime.parse(row.getDate(DocumentColumns.DATE_DOCUMENT.toString()).toString(), dateTimeFormatter));
            }
            document.setLibelle(row.getString(DocumentColumns.NOM.toString()));
            
            GeometryFactory geometryFactory = new GeometryFactory();
            final MathTransform lambertToRGF;
            try {
                lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), CRS.decode("EPSG:2154"), true);

                try {

                    if (row.getDouble(DocumentColumns.X_DEBUT.toString()) != null && row.getDouble(DocumentColumns.Y_DEBUT.toString()) != null) {
                        document.setPositionDebut((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(DocumentColumns.X_DEBUT.toString()),
                                row.getDouble(DocumentColumns.Y_DEBUT.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(DocumentImporter.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {

                    if (row.getDouble(DocumentColumns.X_FIN.toString()) != null && row.getDouble(DocumentColumns.Y_FIN.toString()) != null) {
                        document.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(DocumentColumns.X_FIN.toString()),
                                row.getDouble(DocumentColumns.Y_FIN.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(DocumentImporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (FactoryException ex) {
                Logger.getLogger(DocumentImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
            
            if(row.getDouble(DocumentColumns.ID_BORNEREF_DEBUT.toString())!=null){
                document.setBorneDebutId(bornes.get((int) row.getDouble(DocumentColumns.ID_BORNEREF_DEBUT.toString()).doubleValue()).getId());
            }
            
            if(row.getDouble(DocumentColumns.ID_BORNEREF_FIN.toString())!=null){
                document.setBorneFinId(bornes.get((int) row.getDouble(DocumentColumns.ID_BORNEREF_FIN.toString()).doubleValue()).getId());
            }
            document.setBorne_debut_aval(row.getBoolean(DocumentColumns.AMONT_AVAL_DEBUT.toString())); 
            document.setBorne_fin_aval(row.getBoolean(DocumentColumns.AMONT_AVAL_FIN.toString()));
            if (row.getDouble(DocumentColumns.DIST_BORNEREF_DEBUT.toString()) != null) {
                document.setBorne_debut_distance(row.getDouble(DocumentColumns.DIST_BORNEREF_DEBUT.toString()).floatValue());
            }
            if (row.getDouble(DocumentColumns.DIST_BORNEREF_FIN.toString()) != null) {
                document.setBorne_fin_distance(row.getDouble(DocumentColumns.DIST_BORNEREF_FIN.toString()).floatValue());
            }
            
            if(row.getInt(DocumentColumns.ID_SYSTEME_REP.toString())!=null){
                document.setSystemeRepId(systemesReperage.get(row.getInt(DocumentColumns.ID_SYSTEME_REP.toString())).getId());
            }

            if (row.getDouble(DocumentColumns.PR_DEBUT_CALCULE.toString()) != null) {
                document.setPR_debut(row.getDouble(DocumentColumns.PR_DEBUT_CALCULE.toString()).floatValue());
            }

            if (row.getDouble(DocumentColumns.PR_FIN_CALCULE.toString()) != null) {
                document.setPR_fin(row.getDouble(DocumentColumns.PR_FIN_CALCULE.toString()).floatValue());
            }

            */
            
            
            
            
            
            
            final Class classeDocument = classesDocument.get(row.getInt(DocumentColumns.ID_TYPE_DOCUMENT.toString()));

            if (classeDocument != null) {
                document.setTypeDocumentId(classeDocument.getCanonicalName());
                if (classeDocument.equals(Convention.class)) {
                    // Pour les conventions !
                    if (row.getInt(DocumentColumns.ID_CONVENTION.toString()) != null) {
                        if (conventions.get(row.getInt(DocumentColumns.ID_CONVENTION.toString())) != null) {
                            document.setConvention(conventions.get(row.getInt(DocumentColumns.ID_CONVENTION.toString())).getId());
                        }
                    }
                } 
                else if(classeDocument.equals(Marche.class)){
                    
                }
                else if(classeDocument.equals(ArticleJournal.class)){
                    
                }
                else {
                    System.out.println("Type de document non pris en charge : ID = " + row.getInt(DocumentColumns.ID_TYPE_DOCUMENT.toString()));
                }
            } else {
                System.out.println("Type de document inconnu !");
            }
                
            document.setTypeDocumentId(typesDocument.get(row.getInt(DocumentColumns.ID_TYPE_DOCUMENT.toString())).getId());

            if(nouveauDocument){
               documents.put(row.getInt(DocumentColumns.ID_DOC.toString()), document);
            }
        }
        couchDbConnector.executeBulk(documents.values());
    }
}
