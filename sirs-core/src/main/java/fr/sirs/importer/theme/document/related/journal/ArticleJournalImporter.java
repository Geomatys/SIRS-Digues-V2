package fr.sirs.importer.theme.document.related.journal;

import fr.sirs.importer.*;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.component.ArticleJournalRepository;
import fr.sirs.core.model.ArticleJournal;
import static fr.sirs.importer.DbImporter.cleanNullString;
import java.io.IOException;
import java.time.LocalDateTime;
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
public class ArticleJournalImporter extends GenericImporter {

    private Map<Integer, ArticleJournal> articles = null;
    private ArticleJournalRepository articleJournalRepository;
    private JournalImporter journalImporter;
    
    private ArticleJournalImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    public ArticleJournalImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final ArticleJournalRepository articleJournalRepository) {
        this(accessDatabase, couchDbConnector);
        this.articleJournalRepository = articleJournalRepository;
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
        return DbImporter.TableName.JOURNAL_ARTICLE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        articles = new HashMap<>();
        
        final Map<Integer, String> journaux = journalImporter.getJournalNames();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final ArticleJournal articleJournal = new ArticleJournal();
            
            if(row.getInt(Columns.ID_JOURNAL.toString())!=null){
                articleJournal.setNomJournal(cleanNullString(journaux.get(row.getInt(Columns.ID_JOURNAL.toString()))));
            }
            
            articleJournal.setLibelle(cleanNullString(row.getString(Columns.INTITULE_ARTICLE.toString())));
            
            if (row.getDate(Columns.DATE_ARTICLE.toString()) != null) {
                articleJournal.setDateArticle(LocalDateTime.parse(row.getDate(Columns.DATE_ARTICLE.toString()).toString(), dateTimeFormatter));
            }
            
            articleJournal.setReference_papier(cleanNullString(row.getString(Columns.REFERENCE_PAPIER.toString())));
            
            articleJournal.setReference_numerique(cleanNullString(row.getString(Columns.REFERENCE_NUMERIQUE.toString())));
            
            articleJournal.setCommentaire(cleanNullString(row.getString(Columns.COMMENTAIRE.toString())));
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                articleJournal.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            articles.put(row.getInt(Columns.ID_ARTICLE_JOURNAL.toString()), articleJournal);
        }
        couchDbConnector.executeBulk(articles.values());
    }
    
    /**
     *
     * @return A map containing all RapportEtude instances accessibles from the
     * internal database identifier.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    public Map<Integer, ArticleJournal> getArticleJournal() throws IOException, AccessDbImporterException {
        if (articles == null)  compute();
        return articles;
    }
}
