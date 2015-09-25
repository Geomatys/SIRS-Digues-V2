/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2.mapper.document;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.ArticleJournal;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.document.JournalRegistry;
import fr.sirs.importer.v2.mapper.AbstractMapper;
import fr.sirs.importer.v2.mapper.Mapper;
import fr.sirs.importer.v2.mapper.MapperSpi;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class ArticleJournalMapper extends AbstractMapper<ArticleJournal> {

    @Autowired
    private JournalRegistry registry;

    private enum Columns {
        ID_JOURNAL,
        INTITULE_ARTICLE,
        DATE_ARTICLE
    }

    public ArticleJournalMapper(Table table) {
        super(table);
    }

    @Override
    public void map(Row input, ArticleJournal output) throws IllegalStateException, IOException, AccessDbImporterException {
        final Integer jId = input.getInt(Columns.ID_JOURNAL.toString());
        if (jId != null) {
            String title = registry.getTitle(jId);
            if (title != null) {
                output.setNomJournal(title);
            }
        }

        final String libelle = input.getString(Columns.INTITULE_ARTICLE.toString());
        if (libelle != null) {
            output.setLibelle(libelle);
        }

        final Date date = input.getDate(Columns.DATE_ARTICLE.toString());
        if (date != null) {
            output.setDateArticle(context.convertData(date, LocalDate.class));
        }
    }

    @Component
    public static class Spi implements MapperSpi<ArticleJournal> {

        @Override
        public Optional<Mapper<ArticleJournal>> configureInput(Table inputType) throws IllegalStateException {
            if (MapperSpi.checkColumns(inputType, Columns.values())) {
                return Optional.of(new ArticleJournalMapper(inputType));
            }
            return Optional.empty();
        }

        @Override
        public Class<ArticleJournal> getOutputClass() {
            return ArticleJournal.class;
        }

    }
}
