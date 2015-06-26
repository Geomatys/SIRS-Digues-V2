package fr.sirs.importer.documentTroncon.document.journal;

import fr.sirs.importer.*;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.ArticleJournal;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import static fr.sirs.importer.DbImporter.TableName.JOURNAL_ARTICLE;
import static fr.sirs.importer.DbImporter.cleanNullString;
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
public class JournalArticleImporter extends GenericDocumentRelatedImporter<ArticleJournal> {
    
    private final JournalImporter journalImporter;
    
    public JournalArticleImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
        this.journalImporter = new JournalImporter(accessDatabase, 
                couchDbConnector);
    }

    private enum Columns {
        ID_ARTICLE_JOURNAL,
        ID_JOURNAL,
        INTITULE_ARTICLE,
        DATE_ARTICLE,
        REFERENCE_PAPIER,
        REFERENCE_NUMERIQUE,
        COMMENTAIRE,
        DATE_DERNIERE_MAJ
    };
    
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
        return JOURNAL_ARTICLE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        related = new HashMap<>();
        
        final Map<Integer, String> journaux = journalImporter.getJournalNames();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final ArticleJournal articleJournal = createAnonymValidElement(ArticleJournal.class);
            
            if(row.getInt(Columns.ID_JOURNAL.toString())!=null){
                articleJournal.setNomJournal(cleanNullString(journaux.get(row.getInt(Columns.ID_JOURNAL.toString()))));
            }
            
            articleJournal.setLibelle(cleanNullString(row.getString(Columns.INTITULE_ARTICLE.toString())));
            
            if (row.getDate(Columns.DATE_ARTICLE.toString()) != null) {
                articleJournal.setDateArticle(DbImporter.parseLocalDate(row.getDate(Columns.DATE_ARTICLE.toString()), dateTimeFormatter));
            }
            
            articleJournal.setReferencePapier(cleanNullString(row.getString(Columns.REFERENCE_PAPIER.toString())));
            
            articleJournal.setChemin(cleanNullString(row.getString(Columns.REFERENCE_NUMERIQUE.toString())));
            
            articleJournal.setCommentaire(cleanNullString(row.getString(Columns.COMMENTAIRE.toString())));
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                articleJournal.setDateMaj(DbImporter.parseLocalDateTime(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            
            articleJournal.setDesignation(String.valueOf(row.getInt(Columns.ID_ARTICLE_JOURNAL.toString())));
            
            related.put(row.getInt(Columns.ID_ARTICLE_JOURNAL.toString()), articleJournal);
        }
        couchDbConnector.executeBulk(related.values());
    }
}
