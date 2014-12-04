package fr.sirs.importer.link;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.ArticleJournal;
import fr.sirs.core.model.Desordre;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.objet.desordre.DesordreImporter;
import fr.sirs.importer.theme.document.related.journal.JournalArticleImporter;
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
public class DesordreJournalImporter extends GenericEntityLinker {

    private final DesordreImporter desordreImporter;
    private final JournalArticleImporter journalArticleImporter;
    
    public DesordreJournalImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final DesordreImporter desordreImporter,
            final JournalArticleImporter journalArticleImporter) {
        super(accessDatabase, couchDbConnector);
        this.desordreImporter = desordreImporter;
        this.journalArticleImporter = journalArticleImporter;
    }

    private enum Columns {
        ID_ARTICLE_JOURNAL,
        ID_DESORDRE,
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
        return DbImporter.TableName.DESORDRE_JOURNAL.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Map<Integer, Desordre> desordres = desordreImporter.getById();
        final Map<Integer, ArticleJournal> articles = journalArticleImporter.getRelated();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            
            final Desordre desordre = desordres.get(row.getInt(Columns.ID_DESORDRE.toString()));
            final ArticleJournal article = articles.get(row.getInt(Columns.ID_ARTICLE_JOURNAL.toString()));
            
            if(desordre!=null && article!=null){
                desordre.getArticleJournal().add(article.getId());
                article.getDesordre().add(desordre.getId());
            }
        }
    }
}
