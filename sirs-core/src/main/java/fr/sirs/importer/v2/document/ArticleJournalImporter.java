package fr.sirs.importer.v2.document;

import fr.sirs.core.model.ArticleJournal;
import static fr.sirs.importer.DbImporter.TableName.JOURNAL_ARTICLE;
import fr.sirs.importer.v2.AbstractImporter;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class ArticleJournalImporter extends AbstractImporter<ArticleJournal> {

    @Override
    public Class<ArticleJournal> getElementClass() {
        return ArticleJournal.class;
    }

    @Override
    public String getRowIdFieldName() {
        return "ID_ARTICLE_JOURNAL";
    }

    @Override
    public String getTableName() {
        return JOURNAL_ARTICLE.toString();
    }
}
