package fr.sirs.importer.theme.document;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.component.DocumentRepository;
import fr.sirs.core.model.Convention;
import fr.sirs.core.model.Document;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericImporter;
import fr.sirs.importer.TronconGestionDigueImporter;
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
public class DocumentImporter extends GenericDocumentImporter {
    
    private DocumentRepository documentRepository;
    private ConventionImporter conventionImporter;
    private TronconGestionDigueImporter tronconGestionDigueImporter;
    private TypeDocumentImporter typeDocumentImporter;

    private DocumentImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    public DocumentImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector, 
            final DocumentRepository documentRepository, 
            final ConventionImporter conventionImporter,
            final TronconGestionDigueImporter tronconGestionDigueImporter){
        this(accessDatabase, couchDbConnector);
        this.documentRepository = documentRepository;
        this.conventionImporter = conventionImporter;
        this.tronconGestionDigueImporter = tronconGestionDigueImporter;
        this.typeDocumentImporter = new TypeDocumentImporter(accessDatabase, couchDbConnector);
    }
    
    private enum DocumentColumns {
        ID_DOC,
        ID_TRONCON_GESTION,
        ID_TYPE_DOCUMENT,
//        ID_DOSSIER,
//        REFERENCE_PAPIER,
//        REFERENCE_NUMERIQUE,
//        REFERENCE_CALQUE,
//        DATE_DOCUMENT,
//        DATE_DEBUT_VAL,
//        DATE_FIN_VAL,
//        PR_DEBUT_CALCULE,
//        PR_FIN_CALCULE,
//        X_DEBUT,
//        Y_DEBUT,
//        X_FIN,
//        Y_FIN,
//        ID_SYSTEME_REP,
//        ID_BORNEREF_DEBUT,
//        AMONT_AVAL_DEBUT,
//        DIST_BORNEREF_DEBUT,
//        ID_BORNEREF_FIN,
//        AMONT_AVAL_FIN,
//        DIST_BORNEREF_FIN,
//        COMMENTAIRE,
//        NOM,
//        ID_MARCHE,
//        ID_INTERV_CREATEUR,
//        ID_ORG_CREATEUR,
//        ID_ARTICLE_JOURNAL,
//        ID_PROFIL_EN_TRAVERS,
//        ID_PROFIL_EN_LONG,
//        ID_TYPE_DOCUMENT_A_GRANDE_ECHELLE,
        ID_CONVENTION,
//        DATE_DERNIERE_MAJ,
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
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        
        final Map<Integer, TronconDigue> troncons = tronconGestionDigueImporter.getTronconsDigues();
        final Map<Integer, Convention> conventions = conventionImporter.getConventions();
        
        while(it.hasNext()){
            final Row row = it.next();
            final Document document = new Document();
            
            document.setTronconId(troncons.get(row.getInt(DocumentColumns.ID_TRONCON_GESTION.toString())).getId());
            
            
            final Class typeDocument = this.typeDocumentImporter.getTypeDocument().get(row.getInt(DocumentColumns.ID_TYPE_DOCUMENT.toString()));
            
            if(typeDocument.equals(Convention.class)){
                // Pour les conventions !
                if(row.getInt(DocumentColumns.ID_CONVENTION.toString())!=null){
                    if(conventions.get(row.getInt(DocumentColumns.ID_CONVENTION.toString()))!=null){
                        document.setConvention(conventions.get(row.getInt(DocumentColumns.ID_CONVENTION.toString())).getId());
                    }
                }
            }
            
            documents.put(row.getInt(DocumentColumns.ID_DOC.toString()), document);
            
        }
        couchDbConnector.executeBulk(documents.values());
    }
}
