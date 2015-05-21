package fr.sirs.importer.documentTroncon.document.documentAGrandeEchelle;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.DocumentGrandeEchelle;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.RefDocumentGrandeEchelle;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import static fr.sirs.importer.DbImporter.cleanNullString;
import fr.sirs.importer.documentTroncon.TypeDocumentImporter;
import fr.sirs.importer.documentTroncon.document.GenericDocumentRelatedImporter;
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
public class DocumentAGrandeEchelleImporter extends GenericDocumentRelatedImporter<DocumentGrandeEchelle> {

    private final TypeDocumentAGrandeEchelleImporter typeDocumentAGrandeEchelleImporter;
    
    private final TypeDocumentImporter typeDocumentImporter;
    
    public DocumentAGrandeEchelleImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final TypeDocumentImporter typeDocumentImporter) {
        super(accessDatabase, couchDbConnector);
        this.typeDocumentAGrandeEchelleImporter = new TypeDocumentAGrandeEchelleImporter(
                accessDatabase, couchDbConnector);
        this.typeDocumentImporter = typeDocumentImporter;
    }
    
    private enum Columns {
        ID_DOC,
//        ID_TRONCON_GESTION,
        ID_TYPE_DOCUMENT,
//        ID_DOSSIER, // Pas dans le nouveau modèle
        REFERENCE_PAPIER,
        REFERENCE_NUMERIQUE,
        REFERENCE_CALQUE,
//        DATE_DOCUMENT,
//        DATE_DEBUT_VAL, // Pas dans le nouveau modèle
//        DATE_FIN_VAL, // Pas dans le nouveau modèle
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
        COMMENTAIRE, 
        NOM,
//        ID_MARCHE,
//        ID_INTERV_CREATEUR,
//        ID_ORG_CREATEUR,
//        ID_ARTICLE_JOURNAL,
//        ID_PROFIL_EN_TRAVERS,
//        ID_PROFIL_EN_LONG, // Utilisation interdite ! C'est ID_DOC qui est utilisé par les profils en long !
        ID_TYPE_DOCUMENT_A_GRANDE_ECHELLE,
//        ID_CONVENTION,
        DATE_DERNIERE_MAJ,
//        AUTEUR_RAPPORT,
//        ID_RAPPORT_ETUDE
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
        // En l'absence de table spécifique, on considère que l'ensemble des 
        // informations relatives aux documents à grande échelle est contenue 
        // dans la table des documents.
        return DOCUMENT.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        related = new HashMap<>();
        
        final Map<Integer, RefDocumentGrandeEchelle> types = typeDocumentAGrandeEchelleImporter.getTypeReferences();
        final Map<Integer, Class> typesDocument = typeDocumentImporter.getClasseDocument();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()){
            final Row row = it.next();
            final DocumentGrandeEchelle documentGrandeEchelle = createAnonymValidElement(DocumentGrandeEchelle.class);
            
            // On ne s'intéresse qu'aux lignes qui sont de type "document à grande échelle"
            if(DocumentGrandeEchelle.class.equals(typesDocument.get(row.getInt(Columns.ID_TYPE_DOCUMENT.toString())))){

                documentGrandeEchelle.setCommentaire(cleanNullString(row.getString(Columns.COMMENTAIRE.toString())));

                documentGrandeEchelle.setLibelle(cleanNullString(row.getString(Columns.NOM.toString())));

                documentGrandeEchelle.setReferencePapier(cleanNullString(row.getString(Columns.REFERENCE_PAPIER.toString())));

                documentGrandeEchelle.setChemin(cleanNullString(row.getString(Columns.REFERENCE_NUMERIQUE.toString())));

                documentGrandeEchelle.setReference_calque(cleanNullString(row.getString(Columns.REFERENCE_CALQUE.toString())));

                documentGrandeEchelle.setTypeDocumentGrandeEchelleId(types.get(row.getInt(Columns.ID_TYPE_DOCUMENT_A_GRANDE_ECHELLE.toString())).getId());
    
                if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                    documentGrandeEchelle.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
                }
                
                // Faute de mieux, on référence le document à grande échelle avec l'id du document.
                documentGrandeEchelle.setDesignation(String.valueOf(row.getInt(Columns.ID_DOC.toString())));
                
                related.put(row.getInt(Columns.ID_DOC.toString()), documentGrandeEchelle);
            }
        }
        couchDbConnector.executeBulk(related.values());
    }
}
