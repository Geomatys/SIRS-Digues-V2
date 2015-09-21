package fr.sirs.importer.link;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.ArticleJournal;
import fr.sirs.core.model.LigneEau;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.objet.ligneEau.LigneEauImporter;
import fr.sirs.importer.documentTroncon.document.journal.ArticleJournalImporter;
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
public class LigneEauJournalImporter extends GenericEntityLinker {

    private final LigneEauImporter ligneEauImporter;
    private final ArticleJournalImporter journalArticleImporter;
    
    public LigneEauJournalImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final LigneEauImporter ligneEauImporter,
            final ArticleJournalImporter journalArticleImporter) {
        super(accessDatabase, couchDbConnector);
        this.ligneEauImporter = ligneEauImporter;
        this.journalArticleImporter = journalArticleImporter;
    }

    private enum Columns {
        ID_ARTICLE_JOURNAL,
        ID_LIGNE_EAU,
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
        return LIGNE_EAU_JOURNAL.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Map<Integer, LigneEau> lignesEau = ligneEauImporter.getById();
        final Map<Integer, ArticleJournal> articles = journalArticleImporter.getRelated();
        
        final Iterator<Row> it = context.inputDb.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            
            final LigneEau ligneEau = lignesEau.get(row.getInt(Columns.ID_LIGNE_EAU.toString()));
            final ArticleJournal article = articles.get(row.getInt(Columns.ID_ARTICLE_JOURNAL.toString()));
            
            if(ligneEau!=null && article!=null){
                ligneEau.getArticleIds().add(article.getId());
            }
        }
        
        context.outputDb.executeBulk(lignesEau.values());
    }
}
