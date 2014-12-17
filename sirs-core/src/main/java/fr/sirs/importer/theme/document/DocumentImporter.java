package fr.sirs.importer.theme.document;

import fr.sirs.importer.theme.document.related.convention.ConventionImporter;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
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
import fr.sirs.importer.DocumentsUpdater;
import fr.sirs.importer.IntervenantImporter;
import fr.sirs.importer.OrganismeImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.troncon.TronconGestionDigueImporter;
import fr.sirs.importer.evenementHydraulique.EvenementHydrauliqueImporter;
import fr.sirs.importer.theme.document.related.GenericDocumentRelatedImporter;
import fr.sirs.importer.theme.document.related.TypeSystemeReleveProfilImporter;
import fr.sirs.importer.theme.document.related.documentAGrandeEchelle.DocumentAGrandeEchelleImporter;
import fr.sirs.importer.theme.document.related.journal.JournalArticleImporter;
import fr.sirs.importer.theme.document.related.marche.MarcheImporter;
import fr.sirs.importer.theme.document.related.profilLong.ProfilEnLongImporter;
import fr.sirs.importer.theme.document.related.profilTravers.ProfilEnTraversDescriptionImporter;
import fr.sirs.importer.theme.document.related.profilTravers.ProfilEnTraversImporter;
import fr.sirs.importer.theme.document.related.rapportEtude.RapportEtudeImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class DocumentImporter extends GenericDocumentImporter  implements DocumentsUpdater {
    
    private final TypeSystemeReleveProfilImporter typeSystemeReleveProfilImporter;
    
    private final ConventionImporter conventionImporter;
    private final ProfilEnLongImporter profilLongImporter;
    private final ProfilEnTraversImporter profilTraversImporter;
    private final ProfilEnTraversDescriptionImporter profilTraversDescriptionImporter;
    private final RapportEtudeImporter rapportEtudeImporter;
    private final JournalArticleImporter journalArticleImporter;
    private final MarcheImporter marcheImporter;
    private final DocumentAGrandeEchelleImporter documentAGrandeEchelleImporter;
    
    private final List<GenericDocumentRelatedImporter> documentRelated = new ArrayList<GenericDocumentRelatedImporter>();
    
    private final TypeDocumentImporter typeDocumentImporter;
    
    private final SysEvtConventionImporter documentConventionImporter;
    private final SysEvtProfilEnLongImporter documentProfilLongImporter;
    private final SysEvtProfilEnTraversImporter documentProfilTraversImporter;
    private final SysEvtRapportEtudesImporter documentRapportEtudeImporter;
    private final SysEvtJournalImporter documentJournalImporter;
    private final SysEvtMarcheImporter sysEvtMarcheImporter;
    private final SysEvtDocumentAGrandeEchelleImporter sysEvtDocumentAGrandeEchelleImporter;
    
    private final List<GenericDocumentImporter> documentImporters = new ArrayList<>();
    
    public DocumentImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector, 
            final BorneDigueImporter borneDigueImporter,
            final IntervenantImporter intervenantImporter,
            final OrganismeImporter organismeImporter,
            final SystemeReperageImporter systemeReperageImporter,
            final EvenementHydrauliqueImporter evenementHydrauliqueImporter,
            final MarcheImporter marcheImporter,
            final TronconGestionDigueImporter tronconGestionDigueImporter){
        super(accessDatabase, couchDbConnector, 
                borneDigueImporter, systemeReperageImporter, tronconGestionDigueImporter);
        this.typeDocumentImporter = new TypeDocumentImporter(accessDatabase, 
                couchDbConnector);
        
        typeSystemeReleveProfilImporter = new TypeSystemeReleveProfilImporter(
                accessDatabase, couchDbConnector);
        
        conventionImporter = new ConventionImporter(accessDatabase, 
                couchDbConnector, intervenantImporter, organismeImporter);
        documentRelated.add(conventionImporter);
        
        profilLongImporter = new ProfilEnLongImporter(accessDatabase, 
                couchDbConnector, organismeImporter, 
                evenementHydrauliqueImporter, typeSystemeReleveProfilImporter);
        documentRelated.add(profilLongImporter);
        
        profilTraversDescriptionImporter = new ProfilEnTraversDescriptionImporter(
                accessDatabase, couchDbConnector, 
                typeSystemeReleveProfilImporter, organismeImporter, 
                evenementHydrauliqueImporter, this);
        profilTraversImporter = new ProfilEnTraversImporter(accessDatabase, 
                couchDbConnector, profilTraversDescriptionImporter);
        documentRelated.add(profilTraversImporter);
        
        rapportEtudeImporter = new RapportEtudeImporter(accessDatabase, 
                couchDbConnector);
        documentRelated.add(rapportEtudeImporter);
        
        journalArticleImporter = new JournalArticleImporter(accessDatabase, 
                couchDbConnector);
        documentRelated.add(journalArticleImporter);
        
        this.marcheImporter = marcheImporter;
//        marcheImporter = new MarcheImporter(accessDatabase, couchDbConnector, 
//                organismeImporter);
        documentRelated.add(marcheImporter);
        
        documentAGrandeEchelleImporter = new DocumentAGrandeEchelleImporter(
                accessDatabase, couchDbConnector, typeDocumentImporter);
        documentRelated.add(documentAGrandeEchelleImporter);
        
        documentConventionImporter = new SysEvtConventionImporter(
                accessDatabase, couchDbConnector, 
                borneDigueImporter, systemeReperageImporter, 
                tronconGestionDigueImporter, conventionImporter);
        documentImporters.add(documentConventionImporter);
        documentProfilTraversImporter = new SysEvtProfilEnTraversImporter(
                accessDatabase, couchDbConnector, 
                borneDigueImporter, systemeReperageImporter, 
                tronconGestionDigueImporter, profilTraversImporter);
        documentImporters.add(documentProfilTraversImporter);
        documentProfilLongImporter = new SysEvtProfilEnLongImporter(
                accessDatabase, couchDbConnector, 
                borneDigueImporter, systemeReperageImporter, 
                tronconGestionDigueImporter, profilLongImporter);
        documentImporters.add(documentProfilLongImporter);
        documentRapportEtudeImporter = new SysEvtRapportEtudesImporter(
                accessDatabase, couchDbConnector, 
                borneDigueImporter, systemeReperageImporter, 
                tronconGestionDigueImporter, rapportEtudeImporter);
        documentImporters.add(documentRapportEtudeImporter);
        documentJournalImporter = new SysEvtJournalImporter(accessDatabase, 
                couchDbConnector, borneDigueImporter, 
                systemeReperageImporter, tronconGestionDigueImporter, 
                journalArticleImporter);
        documentImporters.add(documentJournalImporter);
        sysEvtMarcheImporter = new SysEvtMarcheImporter(accessDatabase, 
                couchDbConnector, borneDigueImporter, 
                systemeReperageImporter, tronconGestionDigueImporter, 
                marcheImporter);
        documentImporters.add(sysEvtMarcheImporter);
        sysEvtDocumentAGrandeEchelleImporter = new SysEvtDocumentAGrandeEchelleImporter(
                accessDatabase, couchDbConnector, 
                borneDigueImporter, systemeReperageImporter, 
                tronconGestionDigueImporter, documentAGrandeEchelleImporter);
        documentImporters.add(sysEvtDocumentAGrandeEchelleImporter);
    }
    
    public JournalArticleImporter getJournalArticleImporter() {return this.journalArticleImporter;}
    public ConventionImporter getConventionImporter() {return this.conventionImporter;}
    public RapportEtudeImporter getRapportEtudeImporter() {return this.rapportEtudeImporter;}
    public DocumentAGrandeEchelleImporter getDocumentAGrandeEchelleImporter() {return this.documentAGrandeEchelleImporter;}
    public MarcheImporter getMarcheImporter(){return this.marcheImporter;}

    @Override
    public void update() throws IOException, AccessDbImporterException {
        for(final GenericDocumentRelatedImporter related : documentRelated){
            related.update();
        }
        if(documents==null) compute();
        couchDbConnector.executeBulk(documents.values());
    }
    
    private enum Columns {
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
        ID_ARTICLE_JOURNAL,
        ID_PROFIL_EN_TRAVERS,
//        ID_PROFIL_EN_LONG, // Utilisation interdite ! C'est ID_DOC qui est utilisé par les profils en long !
//        ID_TYPE_DOCUMENT_A_GRANDE_ECHELLE,
        ID_CONVENTION,
        DATE_DERNIERE_MAJ,
//        AUTEUR_RAPPORT,
        ID_RAPPORT_ETUDE
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
        return DbImporter.TableName.DOCUMENT.toString();
    }

    @Override
    protected void preCompute() throws IOException, AccessDbImporterException {
        documents = new HashMap<>();
        
        for (final GenericDocumentImporter gdi : documentImporters){
            final Map<Integer, Document> objets = gdi.getPrecomputedDocuments();
            if(objets!=null){
                for (final Integer key : objets.keySet()){
                    if(documents.get(key)!=null){
                        throw new AccessDbImporterException(objets.get(key).getClass().getCanonicalName()+" : This structure ID is ever used ("+key+") by "+documents.get(key).getClass().getCanonicalName());
                    }
                    else {
                        documents.put(key, objets.get(key));
                    }
                }
            }
        }
        
//        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
//        while (it.hasNext()){
//            final Row row = it.next();
//            Document document = documents.get(row.getInt(DocumentColumns.ID_DOC.toString()));
//            if (document==null) {
//                document=new Document();
//                documents.put(row.getInt(DocumentColumns.ID_DOC.toString()), document);
//            }
//        }
        couchDbConnector.executeBulk(documents.values());
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Map<Integer, Class> classesDocument = typeDocumentImporter.getClasseDocument();
        final Map<Integer, RefTypeDocument> typesDocument = typeDocumentImporter.getTypeDocument();
        
        
        
        for (final GenericDocumentImporter gdi : documentImporters){
            final Map<Integer, Document> objets = gdi.getDocuments();
//            if(objets!=null){
//                for (final Integer key : objets.keySet()){
//                    if(documents.get(key)!=null){
//                        throw new AccessDbImporterException(objets.get(key).getClass().getCanonicalName()+" : This structure ID is ever used ("+key+") by "+documents.get(key).getClass().getCanonicalName());
//                    }
//                    else {
//                        documents.put(key, objets.get(key));
//                    }
//                }
//            }
        }
        
        
        
        
//        final Map<Integer, Document> documentConventions = documentConventionImporter.getDocuments();
//        if(documentConventions!=null) for(final Integer key : documentConventions.keySet()){
//            if(documents.get(key)!=null) throw new AccessDbImporterException(documentConventions.get(key).getClass().getCanonicalName()+" : This structure ID is ever used ("+key+") by "+documents.get(key).getClass().getCanonicalName());
//            else documents.put(key, documentConventions.get(key));
//        }
//        
//        final Map<Integer, Document> documentProfilTravers = documentProfilTraversImporter.getDocuments();
//        if(documentProfilTravers!=null) for(final Integer key : documentProfilTravers.keySet()){
//            if(documents.get(key)!=null) throw new AccessDbImporterException(documentConventions.get(key).getClass().getCanonicalName()+" : This structure ID is ever used ("+key+") by "+documents.get(key).getClass().getCanonicalName());
//            else documents.put(key, documentProfilTravers.get(key));
//        }
//        
//        final Map<Integer, Document> documentRapportEtude = documentRapportEtudeImporter.getDocuments();
//        if(documentRapportEtude!=null) for(final Integer key : documentRapportEtude.keySet()){
//            if(documents.get(key)!=null) throw new AccessDbImporterException(documentConventions.get(key).getClass().getCanonicalName()+" : This structure ID is ever used ("+key+") by "+documents.get(key).getClass().getCanonicalName());
//            else documents.put(key, documentRapportEtude.get(key));
//        }
        
        
        
        final Map<Integer, TronconDigue> troncons = tronconGestionDigueImporter.getTronconsDigues();
        final Map<Integer, Convention> conventions = conventionImporter.getRelated();
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while(it.hasNext()){
            final Row row = it.next();
            final Document document;
            final boolean nouveauDocument;
            if(documents.get(row.getInt(Columns.ID_DOC.toString()))!=null){
                document = documents.get(row.getInt(Columns.ID_DOC.toString()));
                nouveauDocument=false;
            }
            else{
//                System.out.println("Nouveau document !!");
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
                lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), getOutputCrs(), true);

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
            
            
            
            
            
            
            final Class classeDocument = classesDocument.get(row.getInt(Columns.ID_TYPE_DOCUMENT.toString()));

            if (classeDocument != null) {
                document.setTypeDocumentId(classeDocument.getCanonicalName());
                if (classeDocument.equals(Convention.class)) {
                    // Pour les conventions !
                    if (row.getInt(Columns.ID_CONVENTION.toString()) != null) {
                        if (conventions.get(row.getInt(Columns.ID_CONVENTION.toString())) != null) {
                            document.setConvention(conventions.get(row.getInt(Columns.ID_CONVENTION.toString())).getId());
                        }
                    }
                } 
                else if(classeDocument.equals(Marche.class)){
                    
                }
                else if(classeDocument.equals(ArticleJournal.class)){
                    
                }
                else {
//                    System.out.println("Type de document non pris en charge : ID = " + row.getInt(DocumentColumns.ID_TYPE_DOCUMENT.toString()));
                }
            } else {
//                System.out.println("Type de document inconnu !");
            }
                
            document.setTypeDocumentId(typesDocument.get(row.getInt(Columns.ID_TYPE_DOCUMENT.toString())).getId());

            if(nouveauDocument){
               documents.put(row.getInt(Columns.ID_DOC.toString()), document);
            }
        }
        couchDbConnector.executeBulk(documents.values());
    }
}
