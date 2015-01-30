package fr.sirs.importer.link;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.ArticleJournal;
import fr.sirs.core.model.LaisseCrue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.objet.laisseCrue.LaisseCrueImporter;
import fr.sirs.importer.documentTroncon.document.journal.JournalArticleImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class LaisseCrueJournalImporter extends GenericEntityLinker {

    private final LaisseCrueImporter laisseCrueImporter;
    private final JournalArticleImporter journalArticleImporter;
    
    public LaisseCrueJournalImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final LaisseCrueImporter laisseCrueImporter,
            final JournalArticleImporter journalArticleImporter) {
        super(accessDatabase, couchDbConnector);
        this.laisseCrueImporter = laisseCrueImporter;
        this.journalArticleImporter = journalArticleImporter;
    }

    private enum Columns {
        ID_ARTICLE_JOURNAL,
        ID_LAISSE_CRUE,
//        DATE_DERNIERE_MAJ
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
        return DbImporter.TableName.LAISSE_CRUE_JOURNAL.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Map<Integer, LaisseCrue> laisseCrues = laisseCrueImporter.getById();
        final Map<Integer, ArticleJournal> articles = journalArticleImporter.getRelated();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            
            final LaisseCrue laisseCrue = laisseCrues.get(row.getInt(Columns.ID_LAISSE_CRUE.toString()));
            final ArticleJournal article = articles.get(row.getInt(Columns.ID_ARTICLE_JOURNAL.toString()));
            
            if(laisseCrue!=null && article!=null){
                laisseCrue.getArticleJournal().add(article.getId());
            }
        }
    }
}
