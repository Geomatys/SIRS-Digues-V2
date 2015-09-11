package fr.sirs.importer.link;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.ArticleJournal;
import fr.sirs.core.model.MonteeEaux;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.objet.monteeDesEaux.MonteeDesEauxImporter;
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
public class MonteeDesEauxJournalImporter extends GenericEntityLinker {

    private final MonteeDesEauxImporter monteeDesEauxImporter;
    private final JournalArticleImporter journalArticleImporter;
    
    public MonteeDesEauxJournalImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final MonteeDesEauxImporter monteeDesEauxImporter,
            final JournalArticleImporter journalArticleImporter) {
        super(accessDatabase, couchDbConnector);
        this.monteeDesEauxImporter = monteeDesEauxImporter;
        this.journalArticleImporter = journalArticleImporter;
    }

    private enum Columns {
        ID_ARTICLE_JOURNAL,
        ID_MONTEE_DES_EAUX,
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
        return MONTEE_DES_EAUX_JOURNAL.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Map<Integer, MonteeEaux> montees = monteeDesEauxImporter.getById();
        final Map<Integer, ArticleJournal> articles = journalArticleImporter.getRelated();
        
        final Iterator<Row> it = context.inputDb.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            
            final MonteeEaux montee = montees.get(row.getInt(Columns.ID_MONTEE_DES_EAUX.toString()));
            final ArticleJournal article = articles.get(row.getInt(Columns.ID_ARTICLE_JOURNAL.toString()));
            
            if(montee!=null && article!=null){
                montee.getArticleIds().add(article.getId());
            }
        }
        
        context.outputDb.executeBulk(montees.values());
    }
}
